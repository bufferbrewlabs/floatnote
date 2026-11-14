package com.bufferbrew.perch.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.bufferbrew.perch.R

/**
 * Back on the app's root screen asks "Exit Perch?" before [onExit] closes it. Pass
 * [enabled] = false while another screen is showing.
 */
@Composable
fun ExitConfirmBackHandler(onExit: () -> Unit, enabled: Boolean = true) {
    var asking by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = enabled && !asking) { asking = true }
    if (asking) {
        AlertDialog(
            onDismissRequest = { asking = false },
            title = { Text(stringResource(R.string.exit_title)) },
            text = { Text(stringResource(R.string.exit_message)) },
            confirmButton = {
                TextButton(onClick = {
                    asking = false
                    onExit()
                }) { Text(stringResource(R.string.exit)) }
            },
            dismissButton = {
                TextButton(onClick = { asking = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}
