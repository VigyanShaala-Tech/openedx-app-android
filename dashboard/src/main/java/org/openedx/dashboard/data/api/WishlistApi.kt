package org.openedx.dashboard.data.api

import org.openedx.dashboard.data.model.WishlistRequest
import org.openedx.dashboard.data.model.WishlistResponse
import retrofit2.http.Body
import retrofit2.http.POST
interface WishlistApi {
    @POST("/api/v1/wishlist/add/")
    suspend fun add(@Body body: WishlistRequest): WishlistResponse

    @POST("/api/v1/wishlist/remove/")
    suspend fun remove(@Body body: WishlistRequest): WishlistResponse
}
