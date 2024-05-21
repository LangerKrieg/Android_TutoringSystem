package profile

import BulletinAdapter
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tutoringsystem.R
import models.Bulletin
import network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BulletinBoardActivity : Activity() {
    private lateinit var bulletinAdapter: BulletinAdapter
    private val bulletins = mutableListOf<Bulletin>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bulletin_board)

        val recyclerView: RecyclerView = findViewById(R.id.bulletinRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this) // Установка LayoutManager
        bulletinAdapter = BulletinAdapter(bulletins)
        recyclerView.adapter = bulletinAdapter

        val createButton: Button = findViewById(R.id.create_bulletin_button)
        createButton.setOnClickListener {
            showCreateBulletinDialog()
        }
    }

    private fun showCreateBulletinDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_create_bulletin, null)
        val subjectInput: EditText = dialogLayout.findViewById(R.id.subject_input)
        val teacherInput: EditText = dialogLayout.findViewById(R.id.teacher_input)
        val descriptionInput: EditText = dialogLayout.findViewById(R.id.description_input)

        with(builder) {
            setTitle("Create Bulletin")
            setView(dialogLayout)
            setPositiveButton("Save") { dialog, _ ->
                val subject = subjectInput.text.toString().trim()
                val teacher = teacherInput.text.toString().trim()
                val description = descriptionInput.text.toString().trim()
                if (subject.isNotEmpty() && teacher.isNotEmpty() && description.isNotEmpty()) {
                    saveBulletin(subject, teacher, description)
                } else {
                    Toast.makeText(context, "All fields are required.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            setNegativeButton("Cancel", null)
            show()
        }
    }

    private fun saveBulletin(subject: String, teacher: String, description: String) {
        val newBulletin = Bulletin(subject, teacher, description)

        // Предполагаем, что RetrofitInstance.bulletinApiService.createBulletin(newBulletin) это ваш вызов API
        RetrofitInstance.bulletinService.createBulletin(newBulletin).enqueue(object :
            Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    bulletins.add(0, newBulletin)
                    bulletinAdapter.updateData(bulletins)
                    Toast.makeText(this@BulletinBoardActivity, "Bulletin saved and uploaded!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("BulletinUpload", "Failed to upload bulletin: " + response.errorBody()?.string())
                    Toast.makeText(this@BulletinBoardActivity, "Failed to upload bulletin", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("BulletinUpload", "Error uploading bulletin", t)
                Toast.makeText(this@BulletinBoardActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }
}
