package pt.ulisboa.tecnico.cmov.shopist.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.ulisboa.tecnico.cmov.shopist.PantriesListActivity
import pt.ulisboa.tecnico.cmov.shopist.PantryActivity
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST

class RecyclerAdapter(private val context: Context, private val list: Array<PantryList>) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.rowText)

        init {
            // val buttonView: View = view.findViewById(R.id.rowButton)
            val cardView: View = view.findViewById(R.id.rowCard)
            cardView.setOnClickListener {
                val position = adapterPosition
                val intent = Intent(view.context, PantryActivity::class.java)
                    .putExtra(PantriesListActivity.GET_PANTRY_INDEX_INT, position)
                Log.d(ShopIST.TAG, "Got pantry nº$position")
                view.context.startActivity(intent)
            }
        }

        fun bind(pantryList: PantryList) {
            textView.text = pantryList.title
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recycler_view_row, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.bind(list[position])
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = list.size
}