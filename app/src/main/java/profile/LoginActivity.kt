// LoginActivity.kt
package profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.tutoringsystem.R
import network.LoginRequest
import network.LoginResponse
import network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import chat.ChatListActivity

class LoginActivity : Activity() {
    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView

    companion object {
        private const val SHARED_PREFS_NAME = "ProfilePreferences"
        private const val USER_ID_KEY = "UserID"
        private const val USERNAME_KEY = "Username"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameField = findViewById(R.id.login_username)
        passwordField = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.login_button)
        registerLink = findViewById(R.id.register_link)

        usernameField.setText("")

        loginButton.setOnClickListener {
            val username = usernameField.text.toString()
            val password = passwordField.text.toString()
            val request = LoginRequest(username, password)

            RetrofitInstance.authService.login(request).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val userId = response.body()?.id ?: return
                        saveUserCredentials(userId, username)
                        val intent = Intent(this@LoginActivity, ChatListActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid username or password", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
        }

        registerLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveUserCredentials(userId: String, username: String) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(USER_ID_KEY, userId)
        editor.putString(USERNAME_KEY, username)
        editor.apply()
    }

    private fun loadLastUsername() {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val lastUsername = sharedPreferences.getString(USERNAME_KEY, "")
        usernameField.setText(lastUsername)
    }
}
