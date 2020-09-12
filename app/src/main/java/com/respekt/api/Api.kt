package com.respekt.api

import com.respekt.models.PlayListResponse
import retrofit2.Call
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.atomic.AtomicInteger

interface Api {

    @GET("/music/playlist")
    fun getPlayList(@Query("id") id: String
    ): Call<PlayListResponse>
}