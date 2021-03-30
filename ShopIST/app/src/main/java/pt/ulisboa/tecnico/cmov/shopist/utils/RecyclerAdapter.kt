package pt.ulisboa.tecnico.cmov.shopist.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.ui.Pantry

class RecyclerAdapter(
    var list: Array<PantryList>,
    private val activity: FragmentActivity
) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    class ViewHolder(val view: View, val activity: FragmentActivity) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.rowText)

        fun bind(pantryList: PantryList) {
            textView.text = pantryList.title

            val cardView: View = view.findViewById(R.id.rowCard)
            cardView.setOnClickListener {
                view.findNavController().navigate(
                    R.id.action_nav_list_pantries_to_nav_pantry,
                    bundleOf(Pantry.ARG_PANTRY_ID to pantryList.uuid.toString())
                )
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recycler_view_row, viewGroup, false)

        return ViewHolder(view, activity)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(list[position])
    }

    override fun getItemCount() = list.size
}