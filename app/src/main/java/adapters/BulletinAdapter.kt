import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tutoringsystem.R
import models.Bulletin

class BulletinAdapter(private var bulletins: List<Bulletin>) : RecyclerView.Adapter<BulletinAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val subjectTextView: TextView = view.findViewById(R.id.subjectTextView)
        val teacherTextView: TextView = view.findViewById(R.id.teacherTextView)
        val descriptionTextView: TextView = view.findViewById(R.id.descriptionTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bulletin_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bulletin = bulletins[position]
        holder.subjectTextView.text = bulletin.subject
        holder.teacherTextView.text = bulletin.teacher
        holder.descriptionTextView.text = bulletin.description
    }

    override fun getItemCount() = bulletins.size

    fun updateData(newBulletins: List<Bulletin>) {
        bulletins = newBulletins
        notifyDataSetChanged()
    }
}
