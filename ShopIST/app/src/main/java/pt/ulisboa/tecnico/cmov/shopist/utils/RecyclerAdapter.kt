package pt.ulisboa.tecnico.cmov.shopist.utils

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.ulisboa.tecnico.cmov.shopist.PantryActivity
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList

class RecyclerAdapter(private val list: Array<PantryList>) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.rowText)

        fun bind(pantryList: PantryList) {
            textView.text = pantryList.title

            val cardView: View = view.findViewById(R.id.rowCard)
            cardView.setOnClickListener {
                val intent = Intent(view.context, PantryActivity::class.java)
                    .putExtra(PantryActivity.PANTRY_ID, pantryList.uuid.toString())
                view.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recycler_view_row, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(list[position])
    }

    override fun getItemCount() = list.size
}