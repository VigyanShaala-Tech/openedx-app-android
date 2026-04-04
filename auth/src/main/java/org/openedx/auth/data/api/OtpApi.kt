package org.openedx.auth.data.api

import org.openedx.auth.data.model.AuthResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class OtpSendRequest(val contact_identifier: String)
data class OtpVerifyRequest(
    val contact_identifier: String,
    val otp_code: String,
    val verification_key: String
)

data class OtpLoginRequest(
    val phone_number: String,
    val otp_code: String,
    val verification_key: String
)

data class OtpSendResponse(
    val success: Boolean,
    val message: String,
    val verification_key: String?,
    val resend_after_seconds: Int?,
    val expires_in_seconds: Int?
)

data class OtpVerifyResponse(
    val success: Boolean,
    val message: String,
    val verification_key: String?
)

interface OtpApi {
    // 4.1 Sign up Page, send OTP
    @POST("/otp/send/")
    suspend fun sendSignUpOtp(@Body body: OtpSendRequest): OtpSendResponse

    // 4.2 Sign up, verify OTP & 4.6 Verify OTP
    @POST("/otp/verify/")
    suspend fun verifyOtp(@Body body: OtpVerifyRequest): OtpVerifyResponse

    // 4.3 Sign up Page, resend OTP
    @POST("/otp/resend/")
    suspend fun resendSignUpOtp(@Body body: OtpSendRequest): OtpSendResponse

    // 4.4 Login page, Send OTP
    @POST("/otp/login/send/")
    suspend fun sendLoginOtp(@Body body: OtpSendRequest): OtpSendResponse

    // 4.5 Login page, resend OTP
    @POST("/otp/login/resend/")
    suspend fun resendLoginOtp(@Body body: OtpSendRequest): OtpSendResponse

    // 4.7 Login with OTP
    @Headers("content-type: application/json")
    @POST("/otp/login/")
    suspend fun loginWithOtp(@Body body: OtpLoginRequest): AuthResponse
}
