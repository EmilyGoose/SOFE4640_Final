package com.emilygoose.sofe4640final.util

import okhttp3.Interceptor
import okhttp3.Response

class RateLimitInterceptor: Interceptor {

    // Time of last request
    private var lastRequest: Long = 0

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Allow requests to go through only 5 times per second
        if (System.currentTimeMillis() - lastRequest <= 1000/5) {
            // Give it a bit extra just in case
            Thread.sleep(1000/5 + 50)
        }
        lastRequest = System.currentTimeMillis()

        return chain.proceed(request)
    }
}