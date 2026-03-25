package org.openedx.course.data.storage

import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import org.openedx.core.data.model.room.BlockDb
import org.openedx.core.data.model.room.GradingPolicyDb
import org.openedx.core.data.model.room.SectionScoreDb
import org.openedx.core.data.model.room.discovery.CourseDateBlockDb
import java.util.Date

class CourseConverter {

    @TypeConverter
    fun fromDate(value: Date?): Long? {
        return value?.time
    }

    @TypeConverter
    fun toDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun fromListOfString(value: List<String>): String {
        val json = Gson().toJson(value)
        return json.toString()
    }

    @TypeConverter
    fun toListOfString(value: String?): List<String> {
        if (value.isNullOrBlank() || value == "null") return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson<List<String>>(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromListOfBlockDbEntity(value: List<BlockDb>): String {
        val json = Gson().toJson(value)
        return json.toString()
    }

    @TypeConverter
    fun toListOfBlockDbEntity(value: String?): List<BlockDb> {
        if (value.isNullOrBlank() || value == "null") return emptyList()
        val type = object : TypeToken<List<BlockDb>>() {}.type
        return Gson().fromJson<List<BlockDb>>(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromListOfCourseDateBlockDb(value: List<CourseDateBlockDb>): String {
        val json = Gson().toJson(value)
        return json.toString()
    }

    @TypeConverter
    fun toListOfCourseDateBlockDb(value: String?): List<CourseDateBlockDb> {
        if (value.isNullOrBlank() || value == "null") return emptyList()
        val type = object : TypeToken<List<CourseDateBlockDb>>() {}.type
        return Gson().fromJson<List<CourseDateBlockDb>>(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromSectionScoreDbList(value: List<SectionScoreDb>?): String =
        Gson().toJson(value)

    @TypeConverter
    fun toSectionScoreDbList(value: String?): List<SectionScoreDb> =
        if (value.isNullOrBlank() || value == "null") emptyList() else Gson().fromJson<List<SectionScoreDb>>(value, object : TypeToken<List<SectionScoreDb>>() {}.type) ?: emptyList()

    @TypeConverter
    fun fromAssignmentPolicyDbList(value: List<GradingPolicyDb.AssignmentPolicyDb>?): String =
        Gson().toJson(value)

    @TypeConverter
    fun toAssignmentPolicyDbList(value: String?): List<GradingPolicyDb.AssignmentPolicyDb> =
        if (value.isNullOrBlank() || value == "null") emptyList() else Gson().fromJson<List<GradingPolicyDb.AssignmentPolicyDb>>(value, object : TypeToken<List<GradingPolicyDb.AssignmentPolicyDb>>() {}.type) ?: emptyList()

    @TypeConverter
    fun fromGradeRangeMap(value: Map<String, Float>?): String =
        Gson().toJson(value)

    @TypeConverter
    fun toGradeRangeMap(value: String?): Map<String, Float> =
        if (value.isNullOrBlank() || value == "null") emptyMap() else Gson().fromJson<Map<String, Float>>(value, object : TypeToken<Map<String, Float>>() {}.type) ?: emptyMap()
}
