/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glimpse.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import org.lineageos.glimpse.R

/**
 * Full-screen wallpaper preview with a "Set wallpaper" action that opens a
 * dialog to choose the target (home / lock / both).
 *
 * @param wallpaperUri The wallpaper image to preview
 * @param onSetWallpaper Called with the chosen target's index into
 *   R.array.set_wallpaper_items (home screen / lock screen / both)
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SetWallpaperScreen(
    wallpaperUri: Uri,
    onSetWallpaper: (targetIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showTargetDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        GlideImage(
            model = wallpaperUri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Button(
            onClick = { showTargetDialog = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
                .padding(bottom = 24.dp),
        ) {
            Text(stringResource(R.string.set_wallpaper))
        }
    }

    if (showTargetDialog) {
        val targets = stringArrayResource(R.array.set_wallpaper_items)

        AlertDialog(
            onDismissRequest = { showTargetDialog = false },
            title = { Text(stringResource(R.string.set_wallpaper_dialog_title)) },
            text = {
                Column {
                    targets.forEachIndexed { index, target ->
                        TextButton(
                            onClick = {
                                showTargetDialog = false
                                onSetWallpaper(index)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = target,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            },
            confirmButton = {},
        )
    }
}
