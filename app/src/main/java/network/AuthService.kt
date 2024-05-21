package network

import models.Bulletin
import models.Chat
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Интерфейс для доступа к методам API
interface AuthService {
    // Метод для отправки POST-запроса на `/login`
    @POST("/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    // Метод для отправки POST-запроса на `/register`
    @POST("/register")
    fun register(@Body request: RegisterRequest): Call<LoginResponse>

    @GET("/api/chats")  // Замените на актуальный эндпоинт для вашего сервера
    fun getChatList(): Call<List<Chat>>

    @POST("/api/add_bulletin")
    fun addBulletin(@Body request: BulletinRequest): Call<BulletinResponse>

    @POST("/update_username")
    fun updateUsername(@Body request: UpdateUsernameRequest): Call<LoginResponse>

    @POST("/update_profile_photo")
    fun updateProfilePhoto(@Body request: UpdateProfilePhotoRequest): Call<LoginResponse>

    @GET("/get_user/{userId}")
    fun getUser(@Path("userId") userId: String): Call<UserResponse>

    @POST("/bulletins")
    fun createBulletin(@Body bulletin: Bulletin): Call<Void>
}
