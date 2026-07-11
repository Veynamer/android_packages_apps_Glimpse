/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glimpse.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared between [org.lineageos.glimpse.fragments.MainFragment] and the
 * fragments it hosts in its [androidx.viewpager2.widget.ViewPager2]
 * (Reels/Albums/Library).
 *
 * The floating navigation bar overlays the content instead of pushing it
 * up, so each hosted fragment needs to know its real height (which varies
 * by device: gesture nav vs 3-button nav insets differ) to pad its list's
 * bottom content so the last row/item can still scroll fully into view.
 */
class MainViewModel : ViewModel() {
    private val _navigationBarHeight = MutableStateFlow(0)
    val navigationBarHeight = _navigationBarHeight.asStateFlow()

    fun setNavigationBarHeight(height: Int) {
        _navigationBarHeight.value = height
    }
}
