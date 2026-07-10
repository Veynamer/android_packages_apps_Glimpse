/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glimpse

import android.app.WallpaperManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Consumer
import org.lineageos.glimpse.ui.screens.SetWallpaperScreen
import org.lineageos.glimpse.ui.theme.GlimpseTheme

class SetWallpaperActivity : AppCompatActivity() {
    // System services
    private val wallpaperManager by lazy { getSystemService(WallpaperManager::class.java) }

    // Intents
    private val intentListener = Consumer<Intent> { handleIntent(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge
        enableEdgeToEdge()

        intentListener.accept(intent)
        addOnNewIntentListener(intentListener)
    }

    override fun onDestroy() {
        removeOnNewIntentListener(intentListener)

        super.onDestroy()
    }

    private fun handleIntent(intent: Intent) {
        // Load wallpaper from intent
        val wallpaperUri = intent.data ?: run {
            Toast.makeText(this, R.string.intent_media_not_found, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Try to offload this task to styles and wallpaper
        runCatching {
            startActivity(wallpaperManager.getCropAndSetWallpaperIntent(wallpaperUri))
            finish()
            return
        }

        // If we reached this point, we have to do stuff on our own

        // Check if the wallpaper can be changed
        if (!wallpaperManager.isWallpaperSupported || !wallpaperManager.isSetWallpaperAllowed) {
            Toast.makeText(
                this, R.string.intent_wallpaper_cannot_be_changed, Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        setContent {
            GlimpseTheme {
                SetWallpaperScreen(
                    wallpaperUri = wallpaperUri,
                    onSetWallpaper = { targetIndex ->
                        setWallpaper(wallpaperUri, positionToFlag[targetIndex])
                        finish()
                    },
                )
            }
        }
    }

    private fun setWallpaper(uri: Uri, flags: Int) {
        contentResolver.openInputStream(uri)?.use {
            wallpaperManager.setStream(it, null, true, flags)
        }
    }

    companion object {
        private val positionToFlag = arrayOf(
            WallpaperManager.FLAG_SYSTEM,
            WallpaperManager.FLAG_LOCK,
            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK,
        )
    }
}
