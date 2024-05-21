package profile

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.tutoringsystem.R
import network.LoginResponse
import network.RegisterRequest
import network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class RegisterActivity : Activity() {
    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText
    private lateinit var registerButton: Button

    companion object {
        private const val SHARED_PREFS_NAME = "ProfilePreferences"
        private const val USERNAME_KEY = "Username"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        usernameField = findViewById(R.id.register_username)
        passwordField = findViewById(R.id.register_password)
        registerButton = findViewById(R.id.register_button)

        registerButton.setOnClickListener {
            val username = usernameField.text.toString()
            val password = passwordField.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = RegisterRequest(username, password)
            RetrofitInstance.authService.register(request).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                        saveUsername(username)
                        finish()  // Закрыть экран регистрации
                    } else {
                        Toast.makeText(this@RegisterActivity, "Registration failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    val message = when (t) {
                        is IOException -> "Network error: Check your connection"
                        is HttpException -> "HTTP error: ${t.response()?.errorBody()?.string()}"
                        else -> "Unknown error"
                    }

                    Log.e("RegisterActivity", "Registration failed: ${t.message}")
                    Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun saveUsername(username: String) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(USERNAME_KEY, username)
        editor.apply()
    }
}
