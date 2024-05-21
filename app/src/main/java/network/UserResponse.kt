package network

data class UserResponse(
    val success: Boolean,
    val message: String,
    val username: String? = null,
    val profile_photo: String? = null
)