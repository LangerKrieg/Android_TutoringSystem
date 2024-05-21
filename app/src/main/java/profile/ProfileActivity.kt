package profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import chat.ChatListActivity
import com.example.tutoringsystem.R
import com.google.android.material.navigation.NavigationView
import network.LoginResponse
import network.UpdateProfilePhotoRequest
import network.UpdateUsernameRequest
import network.UserResponse
import network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ProfileActivity : Activity() {
    private lateinit var profileAvatar: ImageView
    private lateinit var updatePhotoButton: Button
    private lateinit var updateNameButton: Button
    private lateinit var usernameTextView: TextView
    private lateinit var newNameField: EditText

    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
        const val SHARED_PREFS_NAME = "ProfilePreferences"
        private const val USER_ID_KEY = "UserID"
        private const val USERNAME_KEY = "Username"
        private const val PROFILE_PHOTO_PATH_KEY = "ProfilePhotoPath"
    }

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setContentView(R.layout.activity_profile)

        profileAvatar = findViewById(R.id.profile_avatar)
        updatePhotoButton = findViewById(R.id.update_photo_button)
        updateNameButton = findViewById(R.id.update_name_button)
        usernameTextView = findViewById(R.id.username_text_view)
        newNameField = findViewById(R.id.new_name_field)

        loadUserData()

        updatePhotoButton.setOnClickListener {
            openGalleryForImage()
        }

        updateNameButton.setOnClickListener {
            newNameField.visibility = if (newNameField.visibility == View.GONE) View.VISIBLE else View.GONE
            val newName = newNameField.text.toString().trim()
            if (newName.isNotEmpty()) {
                checkAndUpdateUsername(newName)
            } else {
                Toast.makeText(this, "Please enter a valid name", Toast.LENGTH_SHORT).show()
            }
        }

        drawerLayout = findViewById(R.id.drawer_layout)
        val menuButton: ImageButton = findViewById(R.id.menu_button)
        menuButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        navigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_chats -> {
                    // Переход к активности чатов
                    Intent(this, ChatListActivity::class.java).also { intent ->
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish() // Закрывает текущую активность (ProfileActivity)
                    }
                    startActivity(Intent(this, ChatListActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.END)
                    true
                }
                R.id.nav_profile -> {
                    drawerLayout.closeDrawer(GravityCompat.END)
                    true
                }
                R.id.nav_exit -> {
                    logout()
                    true
                }
                else -> false
            }
        }
        updateMenuState(navigationView.menu)
    }


    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE_PICK) {
            val selectedImageUri: Uri? = data?.data
            selectedImageUri?.let {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                val savedPath = saveProfilePhoto(bitmap)
                profileAvatar.setImageBitmap(bitmap)
                saveProfilePhotoPath(savedPath)
                updateProfilePhotoOnServer(savedPath)
            }
        }
    }

    private fun saveProfilePhoto(bitmap: Bitmap): String {
        val profilePhotoFile = File(filesDir, "profile_photo_${getCurrentUserId()}.png")
        try {
            val outputStream = FileOutputStream(profilePhotoFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return profilePhotoFile.absolutePath
    }

    private fun saveProfilePhotoPath(path: String) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(PROFILE_PHOTO_PATH_KEY, path)
        editor.apply()
    }

    private fun loadProfilePhoto(profilePhotoPath: String?) {
        if (profilePhotoPath != null) {
            val profilePhotoFile = File(profilePhotoPath)
            if (profilePhotoFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(profilePhotoFile.absolutePath)
                profileAvatar.setImageBitmap(bitmap)
            }
        }
    }

    private fun loadUserData() {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString(USER_ID_KEY, null) ?: return
        RetrofitInstance.authService.getUser(userId).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    usernameTextView.text = user?.username ?: "Username"
                    loadProfilePhoto(user?.profile_photo)
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkAndUpdateUsername(newName: String) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString(USER_ID_KEY, null)

        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        val request = UpdateUsernameRequest(userId, newName)
        RetrofitInstance.authService.updateUsername(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    saveUsername(newName)
                    usernameTextView.text = newName
                    newNameField.visibility = View.GONE // Скрываем поле ввода после успешного обновления
                    Toast.makeText(this@ProfileActivity, "Profile name updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProfileActivity, response.body()?.message ?: "Username already taken", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                val message = when (t) {
                    is IOException -> "Network error: Check your connection"
                    is HttpException -> "HTTP error: ${t.response()?.errorBody()?.string()}"
                    else -> "Unknown error"
                }

                Toast.makeText(this@ProfileActivity, message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateProfilePhotoOnServer(photoPath: String) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString(USER_ID_KEY, null)

        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        val request = UpdateProfilePhotoRequest(userId, photoPath)
        RetrofitInstance.authService.updateProfilePhoto(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Profile photo updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProfileActivity, response.body()?.message ?: "Failed to update profile photo", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                val message = when (t) {
                    is IOException -> "Network error: Check your connection"
                    is HttpException -> "HTTP error: ${t.response()?.errorBody()?.string()}"
                    else -> "Unknown error"
                }

                Toast.makeText(this@ProfileActivity, message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getCurrentUserId(): String? {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(USER_ID_KEY, null)
    }

    private fun saveUsername(username: String) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(USERNAME_KEY, username)
        editor.apply()
    }

    private fun updateMenuState(menu: Menu) {
        val chatItem = menu.findItem(R.id.nav_profile)
        chatItem.isEnabled = false // Отключаем, так как мы уже на странице чатов
        chatItem.icon?.setAlpha(130) // Делаем иконку полупрозрачной
    }

    override fun onResume() {
        super.onResume()
        val chatItem = navigationView.menu.findItem(R.id.nav_chats)
        if (true) {
            chatItem.icon?.alpha = 255  // Восстанавливаем непрозрачность на странице профиля
        }
    }

    private fun logout() {
        // Очищаем данные пользователя
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Переход на экран входа LoginActivity
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()  // Закрыть текущую активность
    }
}