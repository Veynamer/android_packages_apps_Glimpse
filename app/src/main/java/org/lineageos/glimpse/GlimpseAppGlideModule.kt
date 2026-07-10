/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glimpse

import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

/**
 * Glimpse's [AppGlideModule].
 *
 * This class doesn't need to do anything on its own, its only purpose is to give Glide's
 * annotation processor an entry point so that it generates [com.bumptech.glide.Glide]'s
 * configuration and merges in every [com.bumptech.glide.module.LibraryGlideModule] found on the
 * classpath, such as the one bundled with jxl-coder-glide, which adds support for decoding
 * JPEG XL (.jxl) images.
 */
@GlideModule
class GlimpseAppGlideModule : AppGlideModule()
