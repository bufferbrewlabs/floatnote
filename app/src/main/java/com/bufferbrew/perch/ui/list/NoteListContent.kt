package com.bufferbrew.perch.ui.list

import androidx.compose.runtime.derivedStateOf
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.bufferbrew.perch.R
import com.bufferbrew.perch.ai.labelRes
import com.bufferbrew.perch.domain.model.NoteCategory
import com.bufferbrew.perch.domain.model.NoteSummary
import com.bufferbrew.perch.ui.components.CategoryChips
import com.bufferbrew.perch.ui.components.CircleButton
import com.bufferbrew.perch.ui.components.PillButton
import com.bufferbrew.perch.ui.theme.Dimens
import com.bufferbrew.perch.ui.theme.Motion
import kotlinx.coroutines.delay

private const val UNDO_MS = 8_000L

/** The notes library, shared by the Activity and the floating panel. */
@Composable
fun NoteListContent(
    notes: List<NoteSummary>?,
    onOpen: (String) -> Unit,
    onCreate: () -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
    onHistory: (() -> Unit)? = null,
    onDictionary: (() -> Unit)? = null,
    compact: Boolean = false,
    deletedTitle: String? = null,
    onUndoDelete: () -> Unit = {},
    onDeletedDismissed: () -> Unit = {},
    query: String = "",
    onQueryChange: ((String) -> Unit)? = null,
    onPin: ((id: String, pinned: Boolean) -> Unit)? = null,
    categoryFilter: NoteCategory? = null,
    onCategoryFilter: ((NoteCategory?) -> Unit)? = null,
    onSettings: (() -> Unit)? = null,
    onFloat: (() -> Unit)? = null,
) {
    var actionsFor by rememberSaveable { mutableStateOf<String?>(null) }
    val gap = if (compact) Dimens.SpaceXs else Dimens.SpaceM
    val gridState = rememberLazyGridState()
    // The New note button shows its label at the top of the list and shrinks to its icon once
    // you scroll, so it covers less of the notes.
    val fabExpanded by remember { derivedStateOf { gridState.firstVisibleItemIndex == 0 } }
    Column(modifier) {
        if (!compact) {
            NotesTitleBar(onHistory = onHistory, onDictionary = onDictionary, onSettings = onSettings, onFloat = onFloat)
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            LazyVerticalGrid(
                columns = if (compact) GridCells.Fixed(1) else GridCells.Adaptive(Dimens.NoteCardWidth),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                // Full app: room under the last note for the New note button.
                contentPadding = PaddingValues(top = gap, bottom = if (compact) gap else gap + Dimens.FabClearance),
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalArrangement = Arrangement.spacedBy(gap),
            ) {
                item(key = "header", span = { GridItemSpan(maxLineSpan) }, contentType = "header") {
                    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                        if (compact) {
                            Row(
                                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
                            ) {
                                PillButton(stringResource(R.string.new_note), onCreate, selected = true, dense = true, icon = R.drawable.ic_add)
                                onHistory?.let { PillButton(stringResource(R.string.history_title), it, dense = true, icon = R.drawable.ic_history) }
                                onDictionary?.let { PillButton(stringResource(R.string.vocab_title), it, dense = true, icon = R.drawable.ic_dictionary) }
                            }
                        }
                        if (onQueryChange != null) SearchField(query, onQueryChange, compact)
                        if (onCategoryFilter != null) {
                            CategoryChips(
                                selected = categoryFilter,
                                onSelect = onCategoryFilter,
                                noneLabel = stringResource(R.string.category_all),
                                dense = compact,
                            )
                        }
                    }
                }
                if (notes != null && notes.isEmpty()) {
                    item(key = "empty", span = { GridItemSpan(maxLineSpan) }, contentType = "empty") {
                        val colors = MaterialTheme.colorScheme
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.extraLarge)
                                .background(colors.surfaceContainerLow)
                                .padding(if (compact) Dimens.SpaceM else Dimens.SpaceXl),
                            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
                        ) {
                            Text(
                                when {
                                    query.isNotBlank() -> stringResource(R.string.search_none)
                                    categoryFilter != null ->
                                        stringResource(R.string.no_notes_in_category, stringResource(categoryFilter.labelRes))
                                    else -> stringResource(R.string.no_notes)
                                },
                                style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                            )
                            if (!compact && query.isBlank() && categoryFilter == null) {
                                Text(
                                    stringResource(R.string.no_notes_help),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
                items(notes.orEmpty(), key = { it.id }, contentType = { "note" }) { summary ->
                    val showActions = actionsFor == summary.id
                    NoteCard(
                        summary = summary,
                        compact = compact,
                        showCategory = summary.category != categoryFilter,
                        showActions = showActions,
                        onOpen = { onOpen(summary.id) },
                        onActions = { actionsFor = if (showActions) null else summary.id },
                        onPin = onPin?.let { pin ->
                            {
                                pin(summary.id, !summary.pinned)
                                actionsFor = null
                            }
                        },
                        onDelete = {
                            onDelete(summary.id)
                            actionsFor = null
                        },
                        modifier = Modifier.animateItem(
                            fadeInSpec = Motion.fade(),
                            fadeOutSpec = Motion.fade(),
                        ),
                    )
                }
            }
            if (!compact) {
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.new_note)) },
                    icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null) },
                    onClick = onCreate,
                    expanded = fabExpanded,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = Dimens.SpaceM),
                )
            }
        }
        if (deletedTitle != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.SpaceXs)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(start = Dimens.SpaceM)
                    .semantics { liveRegion = LiveRegionMode.Polite },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.note_deleted, deletedTitle.ifBlank { stringResource(R.string.untitled) }),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onUndoDelete) { Text(stringResource(R.string.undo)) }
            }
            LaunchedEffect(deletedTitle) {
                delay(UNDO_MS)
                onDeletedDismissed()
            }
        }
    }
}

/**
 * The full app's notes bar, pinned above the list: the title, History, Dictionary and Settings
 * as icon buttons, and Float, the app's own action, as a labelled button.
 */
@Composable
private fun NotesTitleBar(
    onHistory: (() -> Unit)?,
    onDictionary: (() -> Unit)?,
    onSettings: (() -> Unit)?,
    onFloat: (() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = Dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.notes_title),
            style = MaterialTheme.typography.headlineLarge,
            maxLines = 1,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        onHistory?.let { CircleButton(R.drawable.ic_history, stringResource(R.string.history_title), it) }
        onDictionary?.let { CircleButton(R.drawable.ic_dictionary, stringResource(R.string.vocab_title), it) }
        onSettings?.let { CircleButton(R.drawable.ic_settings, stringResource(R.string.settings), it) }
        onFloat?.let { PillButton(stringResource(R.string.float_button), it, selected = true, icon = R.drawable.ic_float_window) }
    }
}

@Composable
private fun NoteCard(
    summary: NoteSummary,
    compact: Boolean,
    showCategory: Boolean,
    showActions: Boolean,
    onOpen: () -> Unit,
    onActions: () -> Unit,
    onPin: (() -> Unit)?,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shape = if (compact) MaterialTheme.shapes.medium else MaterialTheme.shapes.extraLarge
    val actionsLabel = stringResource(R.string.note_actions)
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val configuration = LocalConfiguration.current
    val updated = remember(summary.updatedAt, configuration) {
        DateUtils.getRelativeTimeSpanString(summary.updatedAt).toString()
    }
    val metadata = listOfNotNull(
        stringResource(R.string.pinned).takeIf { summary.pinned },
        summary.category?.takeIf { showCategory }?.let { stringResource(it.labelRes) },
        updated,
    )
    Column(
        modifier.fillMaxWidth()
            .clip(shape)
            .background(if (summary.pinned) colors.primaryContainer else colors.surfaceContainerLow)
            .border(
                if (focused) Dimens.FocusRing else Dimens.Hairline,
                if (focused) colors.primary else colors.outlineVariant,
                shape,
            ),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(
                Modifier.weight(1f)
                    .heightIn(min = if (compact) Dimens.CompactTouchTarget else Dimens.TouchTarget)
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        role = Role.Button,
                        onLongClickLabel = actionsLabel,
                        onLongClick = onActions,
                        onClick = onOpen,
                    )
                    .padding(if (compact) Dimens.SpaceS else Dimens.SpaceM),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
            ) {
                Text(
                    summary.title.ifBlank { stringResource(R.string.untitled) },
                    style = if (compact) typography.titleMedium else typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (summary.preview.isNotBlank()) {
                    Text(
                        summary.preview,
                        style = typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        maxLines = if (compact) 2 else 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    metadata.joinToString(" · "),
                    style = typography.labelMedium,
                    color = if (summary.pinned) colors.onPrimaryContainer else colors.onSurfaceVariant,
                )
            }
            CircleButton(
                R.drawable.ic_more, actionsLabel, onActions,
                modifier = Modifier.padding(top = Dimens.SpaceXs, end = Dimens.SpaceXs),
                dense = compact,
            )
        }
        AnimatedVisibility(
            visible = showActions,
            enter = fadeIn(Motion.fade()),
            exit = fadeOut(Motion.fade()),
        ) {
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(Dimens.SpaceS),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
            ) {
                onPin?.let { PillButton(stringResource(if (summary.pinned) R.string.unpin else R.string.pin), it, dense = compact) }
                PillButton(stringResource(R.string.delete), onDelete, dense = compact)
                PillButton(stringResource(R.string.cancel), onActions, dense = compact)
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onChange: (String) -> Unit, compact: Boolean) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    OutlinedTextField(
        value = query,
        onValueChange = onChange,
        label = { Text(stringResource(R.string.search_hint)) },
        singleLine = true,
        shape = MaterialTheme.shapes.extraLarge,
        textStyle = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
        leadingIcon = {
            Icon(painterResource(R.drawable.ic_search), contentDescription = null, tint = color)
        },
        trailingIcon = if (query.isEmpty()) null else {
            { CircleButton(R.drawable.ic_close, stringResource(R.string.search_clear), { onChange("") }, dense = compact) }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}
