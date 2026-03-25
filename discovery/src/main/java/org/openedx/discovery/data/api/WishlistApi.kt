package org.openedx.discovery.data.api

import retrofit2.http.Body
import retrofit2.http.POST

data class WishlistRequest(val course_id: String)
data class WishlistResponse(val success: Boolean, val message: String?)

interface WishlistApi {
    @POST("/api/v1/wishlist/add/")
    suspend fun add(@Body body: WishlistRequest): WishlistResponse

    @POST("/api/v1/wishlist/remove/")
    suspend fun remove(@Body body: WishlistRequest): WishlistResponse
}
