/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.kata.szakdoga

import android.app.Application
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.Util

/**
 * Placeholder application to facilitate overriding Application methods for debugging and testing.
 */
class DemoApplication : Application() {

    private lateinit var userAgent: String

    override fun onCreate() {
        super.onCreate()
        userAgent = Util.getUserAgent(this, "ExoPlayerDemo")
    }

    fun buildDataSourceFactory(bandwidthMeter: DefaultBandwidthMeter?): DataSource.Factory {
        return DefaultDataSourceFactory(this, bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter))
    }

    fun buildHttpDataSourceFactory(bandwidthMeter: DefaultBandwidthMeter?): HttpDataSource.Factory {
        return DefaultHttpDataSourceFactory(userAgent, bandwidthMeter)
    }

    fun useExtensionRenderers(): Boolean {
        return BuildConfig.FLAVOR == "withExtensions"
    }

}
