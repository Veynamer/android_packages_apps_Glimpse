/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glimpse.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.lineageos.glimpse.R

/**
 * A single destination in [GlimpseFloatingNavigationBar].
 */
data class NavigationBarDestination(
    val iconRes: Int,
    val labelRes: Int,
)

/**
 * Glimpse's main bottom navigation, as a floating pill.
 *
 * Fully Compose, no [com.google.android.material.bottomnavigation.BottomNavigationView]
 * involved - drawn as an explicitly clipped pill [Row] so there's no
 * leftover opaque rectangle around the shape and no reliance on a View
 * widget's own (finicky, height-sensitive) internal centering of the
 * selection indicator/icons.
 *
 * @param destinations The tabs to show, in order
 * @param selectedIndex Index into [destinations] of the current tab
 * @param onDestinationSelected Called with the tapped tab's index
 */
@Composable
fun GlimpseFloatingNavigationBar(
    destinations: List<NavigationBarDestination>,
    selectedIndex: Int,
    onDestinationSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(50)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .shadow(elevation = 6.dp, shape = shape, clip = false)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            destinations.forEachIndexed { index, destination ->
                val label = stringResource(destination.labelRes)

                NavigationBarItem(
                    selected = index == selectedIndex,
                    onClick = { onDestinationSelected(index) },
                    icon = {
                        Icon(
                            painter = painterResource(destination.iconRes),
                            contentDescription = label,
                        )
                    },
                    label = { Text(label) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                )
            }
        }
    }
}
