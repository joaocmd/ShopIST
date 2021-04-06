package pt.ulisboa.tecnico.cmov.shopist.ui.shoppings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.TopBarController
import pt.ulisboa.tecnico.cmov.shopist.TopBarItems
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.domain.Store
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [StoresListUI.newInstance] factory method to
 * create an instance of this fragment.
 */
class StoresListUI : Fragment() {

    private lateinit var recyclerAdapter: StoreListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_stores_list, container, false)
        val recyclerView: RecyclerView = root.findViewById(R.id.storesList)

        val globalData = activity?.applicationContext as ShopIST
        recyclerAdapter = StoreListAdapter(globalData.stores, requireActivity())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = recyclerAdapter

        root.findViewById<Button>(R.id.newShoppingListButton).setOnClickListener { onNewShoppingList() }

        return root
    }

    override fun onResume() {
        updateData()
        super.onResume()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        TopBarController.noOptionsMenu(menu, requireActivity(), getString(R.string.shopping_lists_title))
    }


    private fun updateData() {
        val globalData = activity?.applicationContext as ShopIST
        recyclerAdapter.list = globalData.stores
        recyclerAdapter.notifyDataSetChanged()
    }

    private fun onNewShoppingList() {
        findNavController().navigate(R.id.action_nav_shoppings_list_to_nav_create_shopping_list)
    }

    inner class StoreListAdapter(
        var list: Array<Store>,
        private val activity: FragmentActivity
    ) :
        RecyclerView.Adapter<StoreListAdapter.ViewHolder>() {

        inner class ViewHolder(val view: View, val activity: FragmentActivity) : RecyclerView.ViewHolder(view) {
            private val textView: TextView = view.findViewById(R.id.rowText)

            fun bind(store: Store) {
                textView.text = store.title

                val cardView: View = view.findViewById(R.id.rowCard)
                cardView.setOnClickListener {
                    view.findNavController().navigate(
                        R.id.action_nav_stores_list_to_nav_store_shopping_list,
                        bundleOf(ShoppingListUI.ARG_STORE_ID to store.uuid.toString())
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
}