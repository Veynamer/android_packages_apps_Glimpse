/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glimpse.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import org.lineageos.glimpse.R
import org.lineageos.glimpse.SettingsActivity
import org.lineageos.glimpse.ext.getViewProperty
import org.lineageos.glimpse.models.AlbumType
import org.lineageos.glimpse.ui.navigation.GlimpseFloatingNavigationBar
import org.lineageos.glimpse.ui.navigation.NavigationBarDestination
import org.lineageos.glimpse.ui.theme.GlimpseTheme
import org.lineageos.glimpse.viewmodels.MainViewModel

class MainFragment : Fragment(R.layout.fragment_main) {
    // View models
    private val mainViewModel by activityViewModels<MainViewModel>()

    // Views
    private val navigationBarComposeView by getViewProperty<ComposeView>(
        R.id.navigationBarComposeView
    )
    private val settingsMaterialButton by getViewProperty<MaterialButton>(R.id.settingsMaterialButton)
    private val toolbar by getViewProperty<MaterialToolbar>(R.id.toolbar)
    private val viewPager2 by getViewProperty<ViewPager2>(R.id.viewPager2)

    // Compose navigation bar selection state, updated either by tapping a
    // tab (below) or by swiping/programmatically changing pages (via
    // onPageChangeCallback) - either way it's a single source of truth read
    // by the composable set on navigationBarComposeView.
    private var selectedIndex by mutableIntStateOf(0)

    private val onPageChangeCallback by lazy {
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                selectedIndex = position
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Toolbar
        toolbar.setupWithNavController(findNavController())

        // Floating navigation bar
        navigationBarComposeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        navigationBarComposeView.setContent {
            GlimpseTheme {
                GlimpseFloatingNavigationBar(
                    destinations = destinations,
                    selectedIndex = selectedIndex,
                    onDestinationSelected = { index -> viewPager2.currentItem = index },
                )
            }
        }

        // Content in the hosted fragments (Reels/Albums/Library) scrolls
        // behind the floating nav bar rather than stopping above it, so
        // they need to know its real height to pad their lists' bottom
        // content - see MainViewModel.
        navigationBarComposeView.addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
            mainViewModel.setNavigationBarHeight(view.height)
        }

        settingsMaterialButton.setOnClickListener {
            val intent = Intent(context, SettingsActivity::class.java)
            startActivity(intent)
        }

        // ViewPager2
        viewPager2.isUserInputEnabled = false
        viewPager2.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]()
        }
        viewPager2.offscreenPageLimit = fragments.size
        viewPager2.registerOnPageChangeCallback(onPageChangeCallback)
    }

    override fun onDestroyView() {
        // ViewPager2
        viewPager2.unregisterOnPageChangeCallback(onPageChangeCallback)
        viewPager2.adapter = null

        super.onDestroyView()
    }

    companion object {
        // Keep in sync with the `destinations` list below
        private val fragments = arrayOf(
            {
                AlbumFragment().apply {
                    arguments = AlbumFragment.createBundle(
                        albumType = AlbumType.REELS,
                        hideToolbar = true,
                    )
                }
            },
            { AlbumsFragment() },
            { LibraryFragment() },
        )

        // Keep in sync with the `fragments` array above
        private val destinations = listOf(
            NavigationBarDestination(
                iconRes = R.drawable.ic_photo_size_select_actual,
                labelRes = R.string.reels_title,
            ),
            NavigationBarDestination(
                iconRes = R.drawable.ic_albums,
                labelRes = R.string.albums_title,
            ),
            NavigationBarDestination(
                iconRes = R.drawable.ic_library,
                labelRes = R.string.library_title,
            ),
        )
    }
}
