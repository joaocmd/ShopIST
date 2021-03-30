package pt.ulisboa.tecnico.cmov.shopist.ui.shoppings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.domain.Store
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [ShoppingsList.newInstance] factory method to
 * create an instance of this fragment.
 */
class ShoppingsList : Fragment() {

    private lateinit var recyclerAdapter: ShoppingsListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_shoppings_list, container, false)
        val recyclerView: RecyclerView = root.findViewById(R.id.recyclerView)

        val globalData = activity?.applicationContext as ShopIST
        recyclerAdapter = ShoppingsListAdapter(globalData.shoppingLists, requireActivity())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = recyclerAdapter

        root.findViewById<Button>(R.id.newShoppingListButton).setOnClickListener { onNewShoppingList() }

        return root
    }

    override fun onResume() {
        updateData()
        super.onResume()
    }

    private fun updateData() {
        val globalData = activity?.applicationContext as ShopIST
        recyclerAdapter.list = globalData.shoppingLists
        recyclerAdapter.notifyDataSetChanged()
    }

    private fun onNewShoppingList() {
        findNavController().navigate(R.id.action_nav_shoppings_list_to_nav_create_shopping_list)
    }

    inner class ShoppingsListAdapter(
        var list: Array<Store>,
        private val activity: FragmentActivity
    ) :
        RecyclerView.Adapter<ShoppingsListAdapter.ViewHolder>() {

        inner class ViewHolder(val view: View, val activity: FragmentActivity) : RecyclerView.ViewHolder(view) {
            private val textView: TextView = view.findViewById(R.id.rowText)

            fun bind(shoppingList: Store) {
                textView.text = shoppingList.title

                // val cardView: View = view.findViewById(R.id.rowCard)
                // cardView.setOnClickListener {
                //     view.findNavController().navigate(
                //         R.id.action_nav_list_pantries_to_nav_pantry,
                //         bundleOf(Pantry.ARG_PANTRY_ID to pantryList.uuid.toString())
                //     )
                // }
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
}