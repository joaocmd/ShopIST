package pt.ulisboa.tecnico.cmov.shopist.ui.pantries

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.TopBarController
import pt.ulisboa.tecnico.cmov.shopist.TopBarItems
import pt.ulisboa.tecnico.cmov.shopist.domain.Item
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.utils.API
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
        setHasOptionsMenu(true)
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

        root.findViewById<FloatingActionButton>(R.id.newItemButton).setOnClickListener { onNewItem() }
        return root
    }

    override fun onResume() {
        super.onResume()
        val globalData = (requireActivity().applicationContext as ShopIST)
        pantryList = globalData.getPantryList(pantryList.uuid)
        recyclerAdapter.notifyDataSetChanged()
        globalData.callbackDataSetChanged = {
            pantryList = globalData.getPantryList(pantryList.uuid)
            recyclerAdapter.pantryList = pantryList
            recyclerAdapter.notifyDataSetChanged()
        }

        if (pantryList.isShared) {
            API.getInstance(requireContext()).getPantry(pantryList.uuid, { result ->
                globalData.populateFromServer(result)
                pantryList = globalData.getPantryList(pantryList.uuid)
                if (globalData.callbackDataSetChanged !== null) {
                    globalData.callbackDataSetChanged!!()
                }
            }, {
                // FIXME: Handle gracefully
                Log.e(ShopIST.TAG, it.toString())
            })
        }
    }

    override fun onPause() {
        super.onPause()
        val globalData = (requireActivity().applicationContext as ShopIST)
        globalData.callbackDataSetChanged = null
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val items = mutableListOf(TopBarItems.Share, TopBarItems.Edit)
        if (pantryList.location != null) {
            items.add(TopBarItems.Directions)
        }
        TopBarController.optionsMenu(menu, requireActivity(), pantryList.name, items)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> sharePantryList()
            R.id.action_edit -> editPantryList()
            R.id.action_get_directions -> {
                val location = pantryList.location!!
                val gmmIntentUri = Uri.parse("google.navigation:q=${location.latitude},${location.longitude}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun editPantryList() {
        findNavController().navigate(
            R.id.action_nav_pantry_to_nav_create_pantry,
            bundleOf(
                CreatePantryUI.ARG_PANTRY_ID to pantryList.uuid.toString()
            )
        )
    }

    private fun sharePantryList() {
        // Send pantry to server
        val context = requireActivity().applicationContext
        val globalData = context as ShopIST

        API.getInstance(context).postNewPantry(pantryList, {
            pantryList.share()
            globalData.addPantryList(pantryList)
            globalData.savePersistent()
            // Share code to user
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(
                    Intent.EXTRA_TEXT, getString(R.string.share_pantry_message).format(
                        pantryList.name, ShopIST.createUri(pantryList)
                    )
                )
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }, {
            // TODO: Resource the string
            Toast.makeText(context, "Cannot get link.", Toast.LENGTH_SHORT).show()
        })
    }


    private fun onNewItem() {
        findNavController().navigate(
            R.id.action_nav_pantry_to_add_item,
            bundleOf(
                AddItemUI.ARG_PANTRY_ID to pantryList.uuid.toString()
            )
        )
    }

    inner class PantryAdapter(var pantryList: PantryList) :
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