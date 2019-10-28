package io.digibyte.presenter.activities.utils

import android.content.Context
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.*
import java.io.File

fun createPlayer(context: Context?): SimpleExoPlayer {
    val loadController = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                    2000,
                    10000,
                    2000,
                    2000
            ).createDefaultLoadControl()
    return ExoPlayerFactory.newSimpleInstance(
            context,
            DefaultRenderersFactory(context),
            DefaultTrackSelector(),
            loadController
    )
}

object AssetVideos {
    lateinit var cache: LocalCacheDataSourceFactory

    fun init(context: Context) {
        cache = LocalCacheDataSourceFactory(context)
    }
}

class LocalCacheDataSourceFactory(context: Context) : DataSource.Factory {

    private val cache: Cache = SimpleCache(
            File(context.externalCacheDir, "assetVideoCache"),
            LeastRecentlyUsedCacheEvictor(10 * 1024 * 1024)
    )

    private val defaultDataSourceFactory: DefaultDataSourceFactory
    private val cacheDataSink: CacheDataSink = CacheDataSink(cache, 100)
    private val fileDataSource: FileDataSource = FileDataSource()

    init {
        val userAgent = "movie-player"
        val bandwidthMeter = DefaultBandwidthMeter()
        defaultDataSourceFactory = DefaultDataSourceFactory(
                context,
                bandwidthMeter,
                DefaultHttpDataSourceFactory(userAgent)
        )
    }

    override fun createDataSource(): DataSource {
        return CacheDataSource(
                cache, defaultDataSourceFactory.createDataSource(),
                fileDataSource, cacheDataSink,
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null
        )
    }
}