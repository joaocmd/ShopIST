package pt.ulisboa.tecnico.cmov.shopist.ui.pantries

import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.TopBarController
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.utils.API

class PantriesListUI : Fragment() {
    // TODO: Don't show cart quantities

    private lateinit var recyclerAdapter: PantriesListAdapter
    private lateinit var globalData: ShopIST

    companion object {
        const val GET_BARCODE_PRODUCT = 2
    }

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
        val root = inflater.inflate(R.layout.fragment_pantries_list, container, false)
        val recyclerView: RecyclerView = root.findViewById(R.id.pantriesList)

        globalData = requireActivity().applicationContext as ShopIST
        recyclerAdapter = PantriesListAdapter(globalData.pantries.toList(), requireActivity())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = recyclerAdapter

        root.findViewById<FloatingActionButton>(R.id.newPantryButton).setOnClickListener{ onNewPantry() }

        root.findViewById<SwipeRefreshLayout>(R.id.swiperRefresh).setOnRefreshListener { onRefresh(swiperRefresh) }
        return root
    }

    override fun onResume() {
        super.onResume()
        updateData()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        TopBarController.noOptionsMenu(menu, requireActivity(),
            getString(R.string.pantries_list))
    }

    private fun updateData(callback: (() -> Unit)? = null) {
        recyclerAdapter.list = globalData.pantries.toList()
        recyclerAdapter.notifyDataSetChanged()

        globalData.callbackDataSetChanged = {
            recyclerAdapter.list = globalData.pantries.toList()
            recyclerAdapter.notifyDataSetChanged()
        }
        activity?.let { globalData.getCurrentDeviceLocation(it) {

            // TODO: Update currentLocation when getting the route
            globalData.pantries.forEach {
                if (it.location != null && globalData.currentLocation != null) {
                    API.getInstance(requireContext()).getRouteTime(
                        globalData.currentLocation!!,
                        it.location!!,
                        { time ->
                            it.drivingTime = time
                            globalData.callbackDataSetChanged?.invoke()
                        },
                        {
                            // Ignore
                        }
                    )
                }
            }

            callback?.invoke()
        } }

    }

    fun onRefresh( refresh : SwipeRefreshLayout) {
        Log.i("tessi", "tessi done")
        updateData {
            refresh.isRefreshing = false
        }
    }

    private fun onNewPantry() {
        findNavController().navigate(R.id.action_nav_pantries_list_to_nav_create_pantry)
    }

    inner class PantriesListAdapter(
        var list: List<PantryList>,
        private val activity: FragmentActivity
    ) :
        RecyclerView.Adapter<PantriesListAdapter.ViewHolder>() {

        inner class ViewHolder(val view: View, val activity: FragmentActivity) : RecyclerView.ViewHolder(view) {
            private val textView: TextView = view.findViewById(R.id.rowText)
            //private val drivingTimeImage: ImageView = view.findViewById(R.id.drivingTimeImage)
            private val drivingTimeText: TextView = view.findViewById(R.id.drivingTime)
            private val itemsAtHouseText: TextView = view.findViewById(R.id.itemQuantityDisplay)
            private val missingItemsText: TextView = view.findViewById(R.id.itemsMissingDisplay)

            private fun calculateItemsAtHouse(pantryList: PantryList): Number {
                var total = 0
                for(item in pantryList.items) {
                    total += item.pantryQuantity
                }
                return total
            }

            private fun calculateMissingItems(pantryList: PantryList): Number {
                var total = 0
                for(item in pantryList.items) {
                    total += item.needingQuantity
                }
                return total
            }

            fun bind(pantryList: PantryList) {
                textView.text = pantryList.name
                if (pantryList.drivingTime != null) {
                    drivingTimeText.text = DateUtils.formatElapsedTime(pantryList.drivingTime!!)
                }
                else {
                    drivingTimeText.text = "---"
                }

                itemsAtHouseText.text = calculateItemsAtHouse(pantryList).toString()
                missingItemsText.text = calculateMissingItems(pantryList).toString()


                val cardView: View = view.findViewById(R.id.rowCard)
                cardView.setOnClickListener {
                    view.findNavController().navigate(
                        R.id.action_nav_list_pantries_to_nav_pantry,
                        bundleOf(
                            PantryUI.ARG_PANTRY_ID to pantryList.uuid.toString()
                        )
                    )
                }

                cardView.setOnLongClickListener {
                    view.findNavController().navigate(
                        R.id.action_nav_list_pantries_to_edit_pantry,
                        bundleOf(
                            CreatePantryUI.ARG_PANTRY_ID to pantryList.uuid.toString()
                        )
                    )
                    true
                }
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.pantry_list_row, viewGroup, false)

            return ViewHolder(view, activity)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.bind(list[position])
        }

        override fun getItemCount() = list.size
    }
}