package org.openedx.dashboard.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.openedx.core.data.model.room.discovery.EnrolledCourseEntity
import org.openedx.core.data.model.room.WishlistEntity

@Dao
interface DashboardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnrolledCourseEntity(vararg courseEntity: EnrolledCourseEntity)

    @Query("DELETE FROM course_enrolled_table")
    suspend fun clearCachedData()

    @Query("SELECT * FROM course_enrolled_table")
    suspend fun readAllData(): List<EnrolledCourseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistItems(vararg items: WishlistEntity)

    @Query("DELETE FROM wishlist_table")
    suspend fun clearWishlist()

    @Query("SELECT * FROM wishlist_table")
    suspend fun readWishlist(): List<WishlistEntity>
}
