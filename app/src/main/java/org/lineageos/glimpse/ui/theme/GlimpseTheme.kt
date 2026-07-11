/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glimpse.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Glimpse's Material You / Expressive shape scale.
 *
 * Mirrors the corner radii set on the View side in `values/shapes.xml` /
 * `values/themes.xml`, so screens built with Compose (bottom sheets,
 * dialogs, cards, buttons) look consistent with the still-View-based ones
 * during the incremental migration.
 */
val GlimpseShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

/**
 * Glimpse's Compose theme.
 *
 * Wraps [MaterialExpressiveTheme] (Material 3 Expressive: adds the
 * "spirited", physics-based expressive [MotionScheme] on top of regular
 * [androidx.compose.material3.MaterialTheme]) with the platform's dynamic
 * (Material You) color scheme where available - this is a system Gallery
 * app on LineageOS, which already ships Monet/dynamic color system-wide, so
 * this matches what the rest of the (View-based) UI resolves via
 * `Theme.Material3.DayNight`.
 *
 * On API < 31 (no dynamic color support), falls back to the Material 3
 * baseline color scheme.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GlimpseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> when (darkTheme) {
            true -> dynamicDarkColorScheme(context)
            false -> dynamicLightColorScheme(context)
        }

        darkTheme -> androidx.compose.material3.darkColorScheme()
        else -> androidx.compose.material3.lightColorScheme()
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        shapes = GlimpseShapes,
        content = content,
    )
}
