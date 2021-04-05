package pt.ulisboa.tecnico.cmov.shopist.ui.pantries

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.domain.Item
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [PantryUI.newInstance] factory method to
 * create an instance of this fragment.
 */
class PantryUI : Fragment() {

    private lateinit var pantryList: PantryList
    private lateinit var recyclerAdapter: PantryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val pantryId = UUID.fromString(it.getString(ARG_PANTRY_ID))
            val globalData = requireActivity().applicationContext as ShopIST
            pantryList = globalData.getPantryList(pantryId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_pantry, container, false)
        val listView: RecyclerView = root.findViewById(R.id.productsList)

        recyclerAdapter = PantryAdapter(pantryList)

        listView.layoutManager = LinearLayoutManager(context)
        listView.adapter = recyclerAdapter

        root.findViewById<Button>(R.id.newItemButton).setOnClickListener { onNewItem() }
        // TODO: Improve location of this button
        root.findViewById<Button>(R.id.editPantryButton).setOnClickListener {
            it.findNavController().navigate(
                R.id.action_nav_pantry_to_nav_create_pantry,
                bundleOf(
                    CreatePantryUI.ARG_PANTRY_ID to pantryList.uuid.toString()
                )
            )
        }
        root.findViewById<Button>(R.id.sharePantryButton).setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_pantry_message).format(
                    pantryList.title, ShopIST.createUri(pantryList)
                ))
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        return root
    }

    override fun onResume() {
        recyclerAdapter.notifyDataSetChanged();
        super.onResume()
    }

    private fun onNewItem() {
        findNavController().navigate(
            R.id.action_nav_pantry_to_add_item,
            bundleOf(
                AddItemUI.ARG_PANTRY_ID to pantryList.uuid.toString()
            )
        )
    }

    inner class PantryAdapter(private val pantryList: PantryList) :
        RecyclerView.Adapter<PantryAdapter.ViewHolder>() {


        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */
        inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
            private val textView: TextView = view.findViewById(R.id.rowText)
            private val pantryQuantityView : TextView = view.findViewById(R.id.pantryQuantityDisplay)
            private val needingQuantityView : TextView = view.findViewById(R.id.needingQuantityDisplay)
            private val cartQuantityView : TextView = view.findViewById(R.id.cartQuantityDisplay)

            fun bind(item: Item) {
                textView.text = item.product.name
                pantryQuantityView.text = item.pantryQuantity.toString()
                needingQuantityView.text = item.needingQuantity.toString()
                cartQuantityView.text = item.cartQuantity.toString()

                view.setOnClickListener {
                    view.findNavController().navigate(
                        R.id.action_nav_pantry_to_pantryItem,
                        bundleOf(
                            PantryItemUI.ARG_PANTRY_ID to pantryList.uuid.toString(),
                            PantryItemUI.ARG_PRODUCT_ID to item.product.uuid.toString()
                        )
                    )
                }

                // view.setOnLongClickListener {
                //     // set long click change top menu so that we're able to delete items
                //     // it can also simply appear on the menu, though
                //     true
                // }
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.product_row, viewGroup, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.bind(pantryList.items[position])
        }

        override fun getItemCount() = pantryList.items.size
    }


    companion object {
        const val ARG_PANTRY_ID = "pantryId"

        @JvmStatic
        fun newInstance(pantryId: String) =
            PantryUI().apply {
                arguments = Bundle().apply {
                    putString(ARG_PANTRY_ID, pantryId)
                }
            }
    }
}