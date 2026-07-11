/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glimpse.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.lineageos.glimpse.R
import java.util.Locale

/**
 * File actions merged into [VideoPlayerControls] (favorite/share/edit/delete
 * - previously a separate row of labeled buttons in `activity_view.xml`'s
 * `bottomSheetLinearLayout`, now icon-only and part of the same panel for
 * video playback). `null` fields' visibility mirrors what `ViewActivity`
 * already computes (read-only media, secure mode, etc.)
 */
data class VideoPlayerActions(
    val isFavorite: Boolean,
    val isTrashed: Boolean,
    val showFavorite: Boolean,
    val showEdit: Boolean,
    val showDelete: Boolean,
    val onToggleFavorite: () -> Unit,
    val onShare: () -> Unit,
    val onEdit: () -> Unit,
    val onDelete: () -> Unit,
    val onDeleteLongClick: () -> Unit,
)

/**
 * Compose replacement for media3's built-in [androidx.media3.ui.PlayerControlView].
 *
 * Renders play/pause, an optional pair of ±10s seek buttons, and a scrubber
 * with elapsed/total time, faded in/out with [visible] (used for fullscreen
 * mode - see `MediaViewerAdapter`). When [actions] is non-null, also renders
 * the file actions (favorite/share/edit/delete) as an icon-only row - see
 * [VideoPlayerActions].
 *
 * @param player The player to control, or null while this page isn't the
 *   currently displayed video (mirrors the single-shared-ExoPlayer setup in
 *   `LocalPlayerViewModel`)
 * @param visible Whether the controls should be shown
 * @param showSeekButtons Whether to show the ±10s seek buttons (tied to the
 *   "hide native seek buttons" preference)
 * @param onTogglePlayPause Called when the play/pause button is tapped
 * @param actions File actions to merge into this panel, or null to omit them
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoPlayerControls(
    player: Player?,
    visible: Boolean,
    showSeekButtons: Boolean,
    onTogglePlayPause: () -> Unit,
    modifier: Modifier = Modifier,
    actions: VideoPlayerActions? = null,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        val isPlaying = rememberIsPlaying(player)
        val (position, duration) = rememberPlaybackProgress(player, isPlaying)

        var isDragging by remember { mutableStateOf(false) }
        var dragPosition by remember { mutableFloatStateOf(0f) }

        val sliderValue = when (isDragging) {
            true -> dragPosition
            false -> position.toFloat()
        }
        val sliderRange = 0f..duration.toFloat().coerceAtLeast(1f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = formatPosition(sliderValue.toLong()),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                    )

                    Slider(
                        value = sliderValue,
                        onValueChange = {
                            isDragging = true
                            dragPosition = it
                        },
                        onValueChangeFinished = {
                            player?.seekTo(dragPosition.toLong())
                            isDragging = false
                        },
                        valueRange = sliderRange,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                    )

                    Text(
                        text = formatPosition(duration),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (showSeekButtons) {
                        IconButton(onClick = { player?.seekBack() }) {
                            Icon(
                                imageVector = Icons.Filled.FastRewind,
                                contentDescription = stringResource(R.string.seek_back),
                                tint = Color.White,
                            )
                        }
                    }

                    FilledIconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    ) {
                        Icon(
                            imageVector = when (isPlaying) {
                                true -> Icons.Filled.Pause
                                false -> Icons.Filled.PlayArrow
                            },
                            contentDescription = stringResource(
                                when (isPlaying) {
                                    true -> R.string.pause
                                    false -> R.string.play
                                }
                            ),
                        )
                    }

                    if (showSeekButtons) {
                        IconButton(onClick = { player?.seekForward() }) {
                            Icon(
                                imageVector = Icons.Filled.FastForward,
                                contentDescription = stringResource(R.string.seek_forward),
                                tint = Color.White,
                            )
                        }
                    }
                }

                actions?.let {
                    FileActionsRow(it)
                }
            }
        }
    }
}

/**
 * Icon-only row of file actions, merged into the video controls panel.
 * Deliberately no text labels here (unlike the photo-viewing bottom sheet)
 * to keep this panel compact alongside the scrubber/transport controls.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileActionsRow(actions: VideoPlayerActions) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (actions.showFavorite) {
            ActionIconButton(
                iconRes = when (actions.isFavorite) {
                    true -> R.drawable.ic_star
                    false -> R.drawable.ic_star_border
                },
                contentDescription = stringResource(
                    when (actions.isFavorite) {
                        true -> R.string.file_action_remove_from_favorites
                        false -> R.string.file_action_add_to_favorites
                    }
                ),
                onClick = actions.onToggleFavorite,
            )
        }

        ActionIconButton(
            iconRes = R.drawable.ic_share,
            contentDescription = stringResource(R.string.file_action_share),
            onClick = actions.onShare,
        )

        if (actions.showEdit) {
            ActionIconButton(
                iconRes = R.drawable.ic_edit,
                contentDescription = stringResource(R.string.file_action_edit),
                onClick = actions.onEdit,
            )
        }

        if (actions.showDelete) {
            ActionIconButton(
                iconRes = when (actions.isTrashed) {
                    true -> R.drawable.ic_restore_from_trash
                    false -> R.drawable.ic_delete
                },
                contentDescription = stringResource(
                    when (actions.isTrashed) {
                        true -> R.string.file_action_restore_from_trash
                        false -> R.string.file_action_move_to_trash
                    }
                ),
                onClick = actions.onDelete,
                onLongClick = actions.onDeleteLongClick,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ActionIconButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = Color.White,
        )
    }
}

@Composable
private fun rememberIsPlaying(player: Player?): Boolean {
    var isPlaying by remember(player) { mutableStateOf(player?.isPlaying == true) }

    DisposableEffect(player) {
        if (player == null) {
            isPlaying = false
            return@DisposableEffect onDispose {}
        }

        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }

        isPlaying = player.isPlaying
        player.addListener(listener)

        onDispose { player.removeListener(listener) }
    }

    return isPlaying
}

@Composable
private fun rememberPlaybackProgress(player: Player?, isPlaying: Boolean): Pair<Long, Long> {
    var position by remember(player) { mutableLongStateOf(player?.currentPosition ?: 0L) }
    var duration by remember(player) {
        mutableLongStateOf(player?.duration?.takeIf { it > 0 } ?: 0L)
    }

    LaunchedEffect(player, isPlaying) {
        val currentPlayer = player ?: return@LaunchedEffect

        // Always refresh once immediately (covers the paused/seeked case),
        // then keep polling only while actually playing - ExoPlayer has no
        // continuous position Flow to collect instead.
        while (isActive) {
            position = currentPlayer.currentPosition
            currentPlayer.duration.takeIf { it > 0 }?.let { duration = it }

            if (!isPlaying) break

            delay(200)
        }
    }

    return position to duration
}

private fun formatPosition(positionMs: Long): String {
    val totalSeconds = (positionMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
