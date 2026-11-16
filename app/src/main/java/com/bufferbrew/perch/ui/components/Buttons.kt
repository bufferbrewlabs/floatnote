package com.bufferbrew.perch.ui.components

import com.bufferbrew.perch.ui.theme.LocalDenseTouchTarget
import com.bufferbrew.perch.R
import androidx.compose.material3.TextButton
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.annotation.DrawableRes
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.bufferbrew.perch.ui.theme.Dimens

/**
 * Soft rounded chip, the app's main control. [selected] fills it with the accent.
 * Keyboard focus draws a ring at the edge (never a layout shift), in a colour that stays
 * visible on the filled chip.
 */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    dense: Boolean = false,
    enabled: Boolean = true,
    /** A leading icon (a drawable from res/drawable), tinted like the text. */
    @DrawableRes icon: Int? = null,
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (selected) colors.primary else colors.surfaceVariant,
            contentColor = if (selected) colors.onPrimary else colors.onSurface,
        ),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(
            horizontal = if (dense) Dimens.SpaceS + Dimens.SpaceXs else Dimens.SpaceM,
            vertical = if (dense) Dimens.SpaceXs else Dimens.SpaceS,
        ),
        modifier = modifier
            .defaultMinSize(minHeight = if (dense) Dimens.CompactChipHeight else Dimens.TouchTarget)
            .then(
                if (focused && enabled) Modifier.border(
                    Dimens.FocusRing,
                    if (selected) colors.onPrimary else colors.primary,
                    CircleShape,
                ) else Modifier,
            ),
    ) {
        if (icon != null) {
            Icon(
                painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(if (dense) Dimens.IconSmall else Dimens.IconInline),
            )
            Spacer(Modifier.width(if (dense) Dimens.SpaceXs else Dimens.SpaceS))
        }
        Text(text, style = if (dense) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge)
    }
}

/**
 * Small round icon button with a full-size touch target around it. [icon] is a drawable from
 * res/drawable; [label] is what TalkBack reads. Disabled, it dims and drops out of focus order.
 */
@Composable
fun CircleButton(
    @DrawableRes icon: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    dense: Boolean = false,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    IconButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .size(if (dense) LocalDenseTouchTarget.current else Dimens.TouchTarget)
            .semantics { contentDescription = label },
    ) {
        Box(
            modifier = Modifier
                .size(if (dense) Dimens.CompactButton else Dimens.HeaderButton)
                .background(colors.surfaceVariant, CircleShape)
                .then(if (focused && enabled) Modifier.border(Dimens.FocusRing, colors.primary, CircleShape) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(icon),
                contentDescription = null,
                tint = if (enabled) colors.onSurface else colors.onSurface.copy(alpha = DisabledAlpha),
                modifier = Modifier.size(if (dense) Dimens.IconSmall else Dimens.IconInline),
            )
        }
    }
}

/** "‹ Notes"-style way back: a back arrow and where it goes, in secondary ink. */
@Composable
fun BackButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
    ) {
        Icon(painterResource(R.drawable.ic_back), contentDescription = null, modifier = Modifier.size(Dimens.IconInline))
        Spacer(Modifier.width(Dimens.SpaceS))
        Text(text)
    }
}

/** Material's disabled content alpha. */
private const val DisabledAlpha = 0.38f
