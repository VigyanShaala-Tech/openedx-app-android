package org.openedx.core.data.model

import com.google.gson.annotations.SerializedName
import org.openedx.core.BlockType
import org.openedx.core.utils.TimeUtils
import org.openedx.core.domain.model.Block as DomainBlock
import org.openedx.core.domain.model.BlockCounts as DomainBlockCounts
import org.openedx.core.domain.model.EncodedVideos as DomainEncodedVideos
import org.openedx.core.domain.model.MeetingInfo as DomainMeetingInfo
import org.openedx.core.domain.model.StudentViewData as DomainStudentViewData
import org.openedx.core.domain.model.VideoInfo as DomainVideoInfo

data class Block(
    @SerializedName("id")
    val id: String?,
    @SerializedName("block_id")
    val blockId: String?,
    @SerializedName("lms_web_url")
    val lmsWebUrl: String?,
    @SerializedName("legacy_web_url")
    val legacyWebUrl: String?,
    @SerializedName("student_view_url")
    val studentViewUrl: String?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("display_name")
    val displayName: String?,
    @SerializedName("graded")
    val graded: Boolean?,
    @SerializedName("descendants")
    val descendants: List<String>?,
    @SerializedName("student_view_data")
    val studentViewData: StudentViewData?,
    @SerializedName("student_view_multi_device")
    val studentViewMultiDevice: Boolean?,
    @SerializedName("block_counts")
    val blockCounts: BlockCounts?,
    @SerializedName("completion")
    val completion: Double?,
    @SerializedName("contains_gated_content")
    val containsGatedContent: Boolean?,
    @SerializedName("assignment_progress")
    val assignmentProgress: AssignmentProgress?,
    @SerializedName("due")
    val due: String?,
    @SerializedName("offline_download")
    val offlineDownload: OfflineDownload?,
    @SerializedName("vimeo_url")
    val vimeoUrl: String?,
    @SerializedName("pdf_web_url")
    val pdfWebUrl: String?,
    @SerializedName("meeting_info")
    val meetingInfo: MeetingInfo?
) {
    fun mapToDomain(blockData: Map<String, Block>): DomainBlock {
        val blockType = BlockType.getBlockType(type.orEmpty())
        val descendantsType = determineDescendantsType(blockType, blockData)

        return DomainBlock(
            id = id.orEmpty(),
            blockId = blockId.orEmpty(),
            lmsWebUrl = lmsWebUrl.orEmpty(),
            legacyWebUrl = legacyWebUrl.orEmpty(),
            studentViewUrl = studentViewUrl.orEmpty(),
            type = blockType,
            displayName = displayName.orEmpty(),
            descendants = descendants.orEmpty(),
            descendantsType = descendantsType,
            graded = graded ?: false,
            studentViewData = studentViewData?.mapToDomain(),
            studentViewMultiDevice = studentViewMultiDevice ?: false,
            blockCounts = blockCounts?.mapToDomain()!!,
            completion = completion ?: 0.0,
            containsGatedContent = containsGatedContent ?: false,
            assignmentProgress = assignmentProgress?.mapToDomain(displayName.orEmpty()),
            due = TimeUtils.iso8601ToDate(due.orEmpty()),
            offlineDownload = offlineDownload?.mapToDomain(),
            vimeoUrl = vimeoUrl,
            pdfWebUrl = pdfWebUrl,
            meetingInfo = meetingInfo?.mapToDomain()
        )
    }

    private fun determineDescendantsType(blockType: BlockType, blockData: Map<String, Block>): BlockType {
        if (blockType != BlockType.VERTICAL) return blockType

        val types = descendants?.map { descendant ->
            BlockType.getBlockType(blockData[descendant]?.type.orEmpty())
        }.orEmpty()

        return BlockType.sortByPriority(types).firstOrNull() ?: blockType
    }
}

data class StudentViewData(
    @SerializedName("only_on_web")
    var onlyOnWeb: Boolean?,
    @SerializedName("duration")
    var duration: Any?,
    @SerializedName("transcripts")
    var transcripts: HashMap<String, String>?,
    @SerializedName("encoded_videos")
    var encodedVideos: EncodedVideos?,
    @SerializedName("all_sources")
    var allSources: List<Any?>?,
    @SerializedName("topic_id")
    val topicId: String?
) {
    fun mapToDomain() = DomainStudentViewData(
        onlyOnWeb = onlyOnWeb ?: false,
        duration = duration ?: "",
        transcripts = transcripts,
        encodedVideos = encodedVideos?.mapToDomain(),
        topicId = topicId.orEmpty()
    )
}

data class MeetingInfo(
    @SerializedName("meeting_id")
    val meetingId: String?,
    @SerializedName("topic")
    val topic: String?,
    @SerializedName("passcode")
    val passcode: String?,
    @SerializedName("start_time")
    val startTime: String?,
    @SerializedName("duration")
    val duration: String?,
    @SerializedName("isMeetingEnded")
    val isMeetingEnded: Boolean?,
    @SerializedName("isSessionOngoing")
    val isSessionOngoing: Boolean?
) {
    fun mapToDomain() = DomainMeetingInfo(
        meetingId = meetingId.orEmpty(),
        topic = topic.orEmpty(),
        passcode = passcode.orEmpty(),
        startTime = startTime.orEmpty(),
        duration = duration.orEmpty(),
        isMeetingEnded = isMeetingEnded ?: false,
        isSessionOngoing = isSessionOngoing ?: false
    )
}

data class EncodedVideos(
    @SerializedName("youtube")
    var videoInfo: VideoInfo?,
    @SerializedName("hls")
    var hls: VideoInfo?,
    @SerializedName("fallback")
    var fallback: VideoInfo?,
    @SerializedName("desktop_mp4")
    var desktopMp4: VideoInfo?,
    @SerializedName("mobile_high")
    var mobileHigh: VideoInfo?,
    @SerializedName("mobile_low")
    var mobileLow: VideoInfo?,
    @SerializedName("vimeo_video")
    var vimeoVideo: VideoInfo?
) {
    fun mapToDomain() = DomainEncodedVideos(
        youtube = videoInfo?.mapToDomain(),
        hls = hls?.mapToDomain(),
        fallback = fallback?.mapToDomain(),
        desktopMp4 = desktopMp4?.mapToDomain(),
        mobileHigh = mobileHigh?.mapToDomain(),
        mobileLow = mobileLow?.mapToDomain(),
        vimeoVideo = vimeoVideo?.mapToDomain()
    )
}

data class VideoInfo(
    @SerializedName("url")
    var url: String?,
    @SerializedName("file_size")
    var fileSize: Long?
) {
    fun mapToDomain() = DomainVideoInfo(
        url = url
            .orEmpty()
            .trim(),
        fileSize = fileSize ?: 0
    )
}

data class BlockCounts(
    @SerializedName("video")
    var video: Int?
) {
    fun mapToDomain() = DomainBlockCounts(
        video = video ?: 0
    )
}
