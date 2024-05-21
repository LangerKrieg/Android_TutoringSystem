package chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.tutoringsystem.R
import com.google.android.material.navigation.NavigationView
import profile.BulletinBoardActivity
import profile.LoginActivity
import profile.ProfileActivity

class ChatListActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView // Сделаем переменную доступной на уровне класса

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        val menuButton: ImageButton = findViewById(R.id.menu_button)

        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_chats -> {
                    // Ничего не делаем, если мы на странице чатов
                    false
                }
                R.id.nav_bulletin_board -> {
                    val intent = Intent(this, BulletinBoardActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
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

    private fun updateMenuState(menu: Menu) {
        val chatItem = menu.findItem(R.id.nav_chats)
        chatItem.isEnabled = false // Отключаем, так как мы уже на странице чатов
        chatItem.icon?.alpha = 130 // Делаем иконку полупрозрачной
    }
    override fun onResume() {
        super.onResume()
        val profileItem = navigationView.menu.findItem(R.id.nav_profile)
        if (true) {
            profileItem.icon?.alpha = 255  // Восстанавливаем непрозрачность на странице профиля
        }
    }

    private fun logout() {
        // Очищаем данные пользователя
        val sharedPreferences = getSharedPreferences(ProfileActivity.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Переход на экран входа LoginActivity
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()  // Закрыть текущую активность
    }

}
