package pt.ulisboa.tecnico.cmov.shopist.ui.pantries

import android.graphics.Color
import android.os.Bundle
import android.util.Log
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

/**
 * A simple [Fragment] subclass.
 * Use the [PantriesListUI.newInstance] factory method to
 * create an instance of this fragment.
 */
class PantriesListUI : Fragment() {
    // TODO: Don't show cart quantities

    private lateinit var recyclerAdapter: PantriesListAdapter

    private var currentlySelectedItem: PantryList? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentlySelectedItem != null) {
                    currentlySelectedItem = null
                    recyclerAdapter.list.forEach { it.isSelected = false }
                    recyclerAdapter.notifyDataSetChanged()
                    requireActivity().invalidateOptionsMenu()
                } else {
                    requireActivity().finish()
                }
            }
        })
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
        recyclerAdapter = PantriesListAdapter(globalData.pantries.map { ListItem(it) }, requireActivity())
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
        menu.findItem(R.id.action_see_more).isVisible = false
        menu.findItem(R.id.action_delete_current).isVisible = false
        menu.findItem(R.id.action_edit_current).isVisible = false
        menu.findItem(R.id.action_delete_selected).isVisible = currentlySelectedItem != null
        menu.findItem(R.id.action_edit_selected).isVisible = currentlySelectedItem != null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit_selected ->
                findNavController().navigate(
                    R.id.action_nav_pantries_list_to_nav_create_pantry,
                    bundleOf(
                        CreatePantryUI.ARG_PANTRY_ID to currentlySelectedItem!!.uuid.toString())
                )
            R.id.action_delete_selected ->
                // TODO
                false
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateData() {
        val globalData = activity?.applicationContext as ShopIST
        recyclerAdapter.list = globalData.pantries.map { ListItem(it) }
        recyclerAdapter.notifyDataSetChanged()
    }

    private fun onNewPantry() {
        findNavController().navigate(R.id.action_nav_pantries_list_to_nav_create_pantry)
    }

    inner class ListItem(val pantryList: PantryList) {
        var isSelected = false
    }

    inner class PantriesListAdapter(
        var list: List<ListItem>,
        private val activity: FragmentActivity
    ) :
        RecyclerView.Adapter<PantriesListAdapter.ViewHolder>() {

        inner class ViewHolder(val view: View, val activity: FragmentActivity) : RecyclerView.ViewHolder(view) {
            private val textView: TextView = view.findViewById(R.id.rowText)

            fun bind(listItem: ListItem) {
                val pantryList = listItem.pantryList
                textView.text = pantryList.title

                if (listItem.isSelected) {
                    view.setBackgroundColor(Color.RED)
                } else {
                    view.setBackgroundColor(Color.TRANSPARENT)
                }

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
                    currentlySelectedItem = pantryList
                    val result = !listItem.isSelected

                    list.forEach { it.isSelected = false }
                    listItem.isSelected = result
                    notifyDataSetChanged()
                    requireActivity().invalidateOptionsMenu()
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