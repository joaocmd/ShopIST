package pt.ulisboa.tecnico.cmov.shopist.ui.pantries

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.TopBarController

/**
 * A simple [Fragment] subclass.
 * Use the [PantriesListUI.newInstance] factory method to
 * create an instance of this fragment.
 */
class PantriesListUI : Fragment() {
    // TODO: Don't show cart quantities

    private lateinit var recyclerAdapter: PantriesListAdapter
    private var currentlySelectedItem: PantryList? = null

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

        val globalData = activity?.applicationContext as ShopIST
        recyclerAdapter = PantriesListAdapter(globalData.pantries.toList(), requireActivity())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = recyclerAdapter

        root.findViewById<Button>(R.id.newPantryButton).setOnClickListener{ onNewPantry() }

        return root
    }

    override fun onResume() {
        updateData()
        currentlySelectedItem = null
        super.onResume()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        TopBarController.noOptionsMenu(menu)
    }

    private fun updateData() {
        val globalData = activity?.applicationContext as ShopIST
        recyclerAdapter.list = globalData.pantries.toList()
        recyclerAdapter.notifyDataSetChanged()
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

            fun bind(pantryList: PantryList) {
                textView.text = pantryList.title

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
                .inflate(R.layout.recycler_view_row, viewGroup, false)

            return ViewHolder(view, activity)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.bind(list[position])
        }

        override fun getItemCount() = list.size
    }
}