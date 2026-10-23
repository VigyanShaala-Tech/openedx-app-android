package org.openedx.course.presentation.unit.video

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.cast.CastPlayer
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.Clock
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.DefaultAnalyticsCollector
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.extractor.DefaultExtractorsFactory
import com.google.android.gms.cast.framework.CastContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.domain.model.VideoQuality
import org.openedx.core.module.TranscriptManager
import org.openedx.core.system.connection.NetworkConnection
import org.openedx.core.system.notifier.CourseNotifier
import org.openedx.course.data.repository.CourseRepository
import org.openedx.course.presentation.CourseAnalytics
import org.openedx.course.presentation.CourseAnalyticsKey
import java.util.concurrent.Executors

@SuppressLint("StaticFieldLeak")
class EncodedVideoUnitViewModel(
    courseId: String,
    videoUrl: String,
    blockId: String,
    private val context: Context,
    private val preferencesManager: CorePreferences,
    courseRepository: CourseRepository,
    notifier: CourseNotifier,
    networkConnection: NetworkConnection,
    transcriptManager: TranscriptManager,
    courseAnalytics: CourseAnalytics,
    config: org.openedx.core.config.Config,
) : VideoUnitViewModel(
    courseId,
    videoUrl,
    blockId,
    courseRepository,
    notifier,
    networkConnection,
    transcriptManager,
    courseAnalytics,
    config
) {

    private val _isVideoEnded = MutableLiveData(false)
    val isVideoEnded: LiveData<Boolean>
        get() = _isVideoEnded

    var exoPlayer: ExoPlayer? = null
        private set

    @SuppressLint("UnsafeOptInUsageError")
    var castPlayer: CastPlayer? = null
        private set

    var isCastActive = false

    var isPlayerSetUp = false

    private val exoPlayerListener = object : Player.Listener {
        override fun onRenderedFirstFrame() {
            super.onRenderedFirstFrame()
            viewModelScope.launch {
                while (exoPlayer?.duration == null || exoPlayer?.duration!! < 0f) {
                    delay(500)
                }
                duration = exoPlayer?.duration ?: 0L
            }
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            super.onPlayWhenReadyChanged(playWhenReady, reason)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            if (playbackState == Player.STATE_ENDED) {
                _isVideoEnded.value = true
                markBlockCompleted(blockId, CourseAnalyticsKey.NATIVE.key)
                trackVideoEvent(
                    eventType = "stop_video",
                    blockId = blockId,
                    duration = duration,
                    currentTime = duration.toDouble() / 1000.0
                )
            }
        }

        override fun onIsPlayingChanged(newIsPlaying: Boolean) {
            super.onIsPlayingChanged(newIsPlaying)
            if (this@EncodedVideoUnitViewModel.isTrackedPlaying != newIsPlaying) {
                this@EncodedVideoUnitViewModel.isTrackedPlaying = newIsPlaying
                this@EncodedVideoUnitViewModel.isPlaying = newIsPlaying
                logPlayPauseEvent(videoUrl, newIsPlaying, getCurrentVideoTime(), getPlayingMedium())
                trackVideoEvent(
                    eventType = if (newIsPlaying) "play_video" else "pause_video",
                    blockId = blockId,
                    duration = duration,
                    currentTime = getCurrentVideoTime().toDouble() / 1000.0
                )
            }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            super.onPositionDiscontinuity(oldPosition, newPosition, reason)
            if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                if (!this@EncodedVideoUnitViewModel.isTrackedPlaying && this@EncodedVideoUnitViewModel.isPlaying) {
                    this@EncodedVideoUnitViewModel.isTrackedPlaying = true
                    logPlayPauseEvent(videoUrl, true, oldPosition.positionMs, getPlayingMedium())
                    trackVideoEvent(
                        eventType = "play_video",
                        blockId = blockId,
                        duration = duration,
                        currentTime = oldPosition.positionMs.toDouble() / 1000.0
                    )
                }

                trackVideoEvent(
                    eventType = "seek_video",
                    blockId = blockId,
                    duration = duration,
                    currentTime = newPosition.positionMs.toDouble() / 1000.0,
                    oldTime = oldPosition.positionMs.toDouble() / 1000.0,
                    newTime = newPosition.positionMs.toDouble() / 1000.0
                )
            }
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            super.onPlaybackParametersChanged(playbackParameters)
            logVideoSpeedEvent(
                videoUrl,
                playbackParameters.speed,
                getCurrentVideoTime(),
                getPlayingMedium()
            )
        }
    }

    @androidx.media3.common.util.UnstableApi
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        if (exoPlayer != null) {
            return
        }
        initPlayer()

        runCatching {
            val executor = Executors.newSingleThreadExecutor()
            CastContext.getSharedInstance(context, executor).addOnCompleteListener {
                it.result?.let { castContext ->
                    castPlayer = CastPlayer(castContext)
                    isUpdatedMutable.value = true
                }
            }
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        exoPlayer?.addListener(exoPlayerListener)
        getActivePlayer()?.playWhenReady = isPlaying
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        if (isTrackedPlaying) {
            isTrackedPlaying = false
            isPlaying = false
            logPlayPauseEvent(videoUrl, false, getCurrentVideoTime(), getPlayingMedium())
            trackVideoEvent(
                eventType = "pause_video",
                blockId = blockId,
                duration = duration,
                currentTime = getCurrentVideoTime().toDouble() / 1000.0
            )
        }
        exoPlayer?.pause()
        exoPlayer?.removeListener(exoPlayerListener)
    }

    fun getActivePlayer(): Player? {
        return if (isCastActive) {
            castPlayer
        } else {
            exoPlayer
        }
    }

    @androidx.media3.common.util.UnstableApi
    fun releasePlayers() {
        exoPlayer?.release()
        castPlayer?.release()
        exoPlayer = null
        castPlayer = null
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun initPlayer() {
        val videoQuality = getVideoQuality()
        val params = DefaultTrackSelector.Parameters.Builder(context)
            .apply {
                if (videoQuality != VideoQuality.AUTO) {
                    setMaxVideoSize(videoQuality.width, videoQuality.height)
                    setViewportSize(videoQuality.width, videoQuality.height, false)
                }
            }
            .build()

        val factory = AdaptiveTrackSelection.Factory()
        val selector = DefaultTrackSelector(context, factory)
        selector.parameters = params

        exoPlayer = ExoPlayer.Builder(
            context,
            DefaultRenderersFactory(context),
            DefaultMediaSourceFactory(context, DefaultExtractorsFactory()),
            selector,
            DefaultLoadControl(),
            DefaultBandwidthMeter.getSingletonInstance(context),
            DefaultAnalyticsCollector(Clock.DEFAULT)
        ).setSeekForwardIncrementMs(10000L)
        .setSeekBackIncrementMs(10000L)
        .build()
        logLoadedCompletedEvent(videoUrl, true, getCurrentVideoTime(), getPlayingMedium())
    }

    private fun getVideoQuality() = preferencesManager.videoSettings.videoStreamingQuality

    override fun markBlockCompleted(blockId: String, medium: String) {
        super.markBlockCompleted(
            blockId,
            getPlayingMedium()
        )
    }

    private fun getPlayingMedium(): String {
        return if (getActivePlayer() == castPlayer) {
            CourseAnalyticsKey.GOOGLE_CAST.key
        } else {
            CourseAnalyticsKey.NATIVE.key
        }
    }
}
