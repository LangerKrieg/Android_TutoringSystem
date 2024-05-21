package network

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val id: String? = null,
    val profile_photo: String? = null
)