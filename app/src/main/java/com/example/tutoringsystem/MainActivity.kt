import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import profile.BulletinBoardActivity
import com.example.tutoringsystem.R
import com.google.android.material.navigation.NavigationView
import profile.ProfileActivity

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_with_drawer)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_bulletin_board -> {
                    val intent = Intent(this, BulletinBoardActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }.also {
                drawerLayout.closeDrawers()
            }
        }
    }
}
