package pt.ulisboa.tecnico.cmov.shopist.ui.shoppings

import android.graphics.Color
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_pantries_list.*
import kotlinx.android.synthetic.main.fragment_stores_list.*
import kotlinx.android.synthetic.main.stores_list_row.view.*
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.TopBarController
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.domain.Store
import pt.ulisboa.tecnico.cmov.shopist.ui.pantries.CreatePantryUI
import pt.ulisboa.tecnico.cmov.shopist.utils.API
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

        root.findViewById<FloatingActionButton>(R.id.newShoppingListButton).setOnClickListener { onNewShoppingList() }
        root.findViewById<SwipeRefreshLayout>(R.id.swiperRefresh2).setOnRefreshListener { onRefresh(swiperRefresh2) }

        return root
    }

    override fun onResume() {
        updateData()
        super.onResume()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        TopBarController.noOptionsMenu(
            menu,
            requireActivity(),
            getString(R.string.shopping_lists_title)
        )
    }

    private fun updateData(callback: (() -> Unit)? = null) {
        val globalData = activity?.applicationContext as ShopIST
        recyclerAdapter.list = globalData.stores
        recyclerAdapter.notifyDataSetChanged()

        globalData.callbackDataSetChanged = {
            recyclerAdapter.list = globalData.stores
            recyclerAdapter.notifyDataSetChanged()
        }

        val context = requireContext()
        activity?.let { globalData.getCurrentDeviceLocation(it) {
            globalData.pantries.forEach { pantry ->
                if (pantry.isShared) {
                    // Check for updates
                    API.getInstance(context).getPantry(pantry.uuid, { result ->
                        globalData.populateFromServer(result)
                        globalData.callbackDataSetChanged?.invoke()
                    }, {
                    })
                }
            }
        } }

        API.getInstance(requireContext()).beaconEstimates(globalData.stores.toList(), {
            globalData.stores.forEach { store -> store.queueTime = null }
            it.forEach { s ->
                val store = globalData.getStore(UUID.fromString(s.key))
                store.queueTime = (s.value / 1000).toLong() // comes in milliseconds
            }
            globalData.callbackDataSetChanged?.invoke()
        }, {
            // Ignore, can't get beacon estimates data
        })

        callback?.invoke()
    }

    private fun onRefresh(refresh : SwipeRefreshLayout) {
        updateData {
            refresh.isRefreshing = false
        }
    }

    private fun onNewShoppingList() {
        findNavController().navigate(R.id.action_nav_shoppings_list_to_nav_create_shopping_list)
    }

    inner class StoreListAdapter(
        var list: Array<Store>,
        private val activity: FragmentActivity
    ) :
        RecyclerView.Adapter<StoreListAdapter.ViewHolder>() {

        inner class ViewHolder(val view: View, val activity: FragmentActivity) : RecyclerView.ViewHolder(
            view
        ) {
            private val textView: TextView = view.findViewById(R.id.rowText)
            private val drivingTimeText: TextView = view.findViewById(R.id.drivingTime)
            private val checkoutTime: TextView = view.findViewById(R.id.checkoutTime)
            private val itemPercentage: TextView = view.findViewById(R.id.itemPercentage)

            fun bind(store: Store) {
                textView.text = store.name

                if (store.drivingTime != null) {
                    drivingTimeText.text = DateUtils.formatElapsedTime(store.drivingTime!!)
                } else {
                    drivingTimeText.text = "---"
                }

                if ( store.queueTime != null) {
                    checkoutTime.text = DateUtils.formatElapsedTime(store.queueTime!!)
                } else {
                    checkoutTime.text = "---"
                }

                val globalData = activity.applicationContext as ShopIST
                val itemQuantityTotal = store.itemQuantityTotal(globalData.pantries.toList())
                itemPercentage.text = itemQuantityTotal.toString()

                val cardView: View = view.findViewById(R.id.rowCard)
                cardView.setOnClickListener {
                    view.findNavController().navigate(
                        R.id.action_nav_stores_list_to_nav_store_shopping_list,
                        bundleOf(ShoppingListUI.ARG_STORE_ID to store.uuid.toString())
                    )
                }

                cardView.setOnLongClickListener {
                    view.findNavController().navigate(
                        R.id.action_nav_shoppings_list_to_nav_create_shopping_list,
                        bundleOf(CreateShoppingListUI.ARG_STORE_ID to store.uuid.toString())
                    )
                    true
                }
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.stores_list_row, viewGroup, false)

            return ViewHolder(view, activity)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.bind(list[position])
        }

        override fun getItemCount() = list.size
    }
}