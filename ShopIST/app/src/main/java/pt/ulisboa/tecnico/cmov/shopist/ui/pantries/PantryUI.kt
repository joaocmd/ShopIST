package pt.ulisboa.tecnico.cmov.shopist.ui.pantries

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_pantries_list.*
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.TopBarController
import pt.ulisboa.tecnico.cmov.shopist.TopBarItems
import pt.ulisboa.tecnico.cmov.shopist.domain.Item
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.ui.dialogs.*
import pt.ulisboa.tecnico.cmov.shopist.utils.API
import java.util.*

class PantryUI : Fragment() {

    private lateinit var root: View
    private lateinit var menuRoot: Menu
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
    ): View {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_pantry, container, false)
        val listView: RecyclerView = root.findViewById(R.id.productsList)

        // Hide cart quantities
        root.findViewById<ImageView>(R.id.cartQuantityDisplay).visibility = View.GONE

        recyclerAdapter = PantryAdapter(pantryList)

        listView.layoutManager = LinearLayoutManager(context)
        listView.adapter = recyclerAdapter

        root.findViewById<FloatingActionButton>(R.id.newItemButton).setOnClickListener { onNewItem() }
        root.findViewById<SwipeRefreshLayout>(R.id.swiperRefresh).setOnRefreshListener { onRefresh(swiperRefresh) }

        return root
    }

    override fun onResume() {
        super.onResume()
        updateData()
    }

    private fun updateData(callback: (() -> Unit)? = null) {

        val globalData = (requireActivity().applicationContext as ShopIST)
        pantryList = globalData.getPantryList(pantryList.uuid)
        recyclerAdapter.notifyDataSetChanged()
        globalData.callbackDataSetChanged = {
            pantryList = globalData.getPantryList(pantryList.uuid)
            recyclerAdapter.pantryList = pantryList
            recyclerAdapter.notifyDataSetChanged()

            // Received data from server, enable the buttons
            setEnableButtons(true)
        }
        setEnableButtons(globalData.isAPIConnected)

        if (pantryList.isShared) {
            API.getInstance(requireContext()).getPantry(pantryList.uuid, { result ->
                globalData.populateFromServer(result)
                pantryList = globalData.getPantryList(pantryList.uuid)

                // Get one image for each product
                pantryList.getProducts().forEach {
                    if (it.images.size > 0) {
                        globalData.imageCache.getAsImage(UUID.fromString(it.getLastImageId()), {
                            globalData.callbackDataSetChanged?.invoke()
                        }, {})
                    }
                }

                // FIXME: When loading the pantry from the server, when it translates to the current language, the text will flick from the original one to the translated one
                globalData.callbackDataSetChanged?.invoke()
            }, {
                // Cannot edit this pantry then :(
                if (context !== null) {
                    setEnableButtons(globalData.isAPIConnected)
                }
            })
        } else {
            pantryList.getProducts().forEach {
                if (it.images.size > 0) {
                    val uuid = UUID.fromString(it.getLastImageId())
                    globalData.imageCache.getAsImage(uuid, {
                        globalData.callbackDataSetChanged?.invoke()
                    }, {})
                }
            }
        }

        callback?.invoke()
    }

    fun onRefresh( refresh : SwipeRefreshLayout) {
        Log.i("tessi", "tessi done")
        updateData {
            refresh.isRefreshing = false
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val items = mutableListOf(TopBarItems.Share, TopBarItems.Edit, TopBarItems.Delete)
        if (pantryList.location != null) {
            items.add(TopBarItems.Directions)
        }
        TopBarController.optionsMenu(menu, requireActivity(), pantryList.name, items)
        menuRoot = menu

        val globalData = (requireActivity().applicationContext as ShopIST)
        // If couldn't connect until now disable everything
        setEnableButtons(globalData.isAPIConnected)
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
            R.id.action_delete -> confirmDeletePantryList()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun setEnableButtons(enabled: Boolean) {
        if (pantryList.isShared) {
            root.findViewById<FloatingActionButton>(R.id.newItemButton).isVisible = enabled
            if (this::menuRoot.isInitialized) {
                TopBarController.setSharedOptions(menuRoot, enabled)
            }
        } else {
            root.findViewById<FloatingActionButton>(R.id.newItemButton).isVisible = true
            if (this::menuRoot.isInitialized) {
                TopBarController.setSharedOptions(menuRoot, true)
            }
        }
        if (this::menuRoot.isInitialized) {
            TopBarController.setOnlineOptions(menuRoot, enabled)
            TopBarController.setEnable(menuRoot, TopBarItems.Delete, true)
        }
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
        API.getInstance(requireContext()).postNewPantry(pantryList, {
            pantryList.share()
            val globalData = (requireActivity().applicationContext as ShopIST)
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
            Toast.makeText(context, getString(R.string.error_getting_link), Toast.LENGTH_SHORT).show()
        })

        // TODO: Send images, prices and ratings on share
    }

    private fun deletePantryList() {
        // Remove pantry from ShopIST
        val globalData = (requireActivity().applicationContext as ShopIST)
        globalData.deletePantryList(pantryList)
        globalData.savePersistent()

        // Go to previous page
        findNavController().popBackStack()
    }

    private fun confirmDeletePantryList() {
        // Set confirmation dialog
        ConfirmationDialog(
            requireContext(),
            getString(R.string.confirm_pantry_delete),
            {
                deletePantryList()
            }, {
            }
        )
    }

    private fun onNewItem() {
        findNavController().navigate(
            R.id.action_nav_pantry_to_add_item,
            bundleOf(
                AddItemUI.ARG_PANTRY_ID to pantryList.uuid.toString()
            )
        )
    }

    fun displayFullScreenImage(imageView: ImageView) {
        val dialog = ImageFullScreenDialog(this, imageView)
        dialog.show()
    }

    inner class PantryAdapter(var pantryList: PantryList) :
        RecyclerView.Adapter<PantryAdapter.ViewHolder>() {

        inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
            private val imageView: ImageView = view.findViewById(R.id.productImageView)
            private val textView: TextView = view.findViewById(R.id.rowText)
            private val pantryQuantityView : TextView = view.findViewById(R.id.pantryQuantityDisplay)
            private val needingQuantityView : TextView = view.findViewById(R.id.needingQuantityDisplay)
            private val cartQuantityView : TextView = view.findViewById(R.id.cartQuantityDisplay)

            fun bind(item: Item) {

                textView.text = item.product.getTranslatedName()
                pantryQuantityView.text = item.pantryQuantity.toString()
                needingQuantityView.text = item.needingQuantity.toString()
                cartQuantityView.text = item.cartQuantity.toString()

                view.setOnLongClickListener {
                    view.findNavController().navigate(
                        R.id.action_nav_pantry_to_nav_view_product,
                        bundleOf(
                            PantryItemUI.ARG_PRODUCT_ID to item.product.uuid.toString()
                        )
                    )
                    true
                }

                view.setOnClickListener {
                    view.findNavController().navigate(
                        R.id.action_nav_pantry_to_pantryItem,
                        bundleOf(
                            PantryItemUI.ARG_PANTRY_ID to pantryList.uuid.toString(),
                            PantryItemUI.ARG_PRODUCT_ID to item.product.uuid.toString()
                        )
                    )
                }

                imageView.setOnClickListener {
                    displayFullScreenImage(it as ImageView)
                }

                // view.setOnLongClickListener {
                //     // set long click change top menu so that we're able to delete items
                //     // it can also simply appear on the menu, though
                //     true
                // }

                // Set last image
                if (item.product.images.size > 0) {
                    val uuid = UUID.fromString(item.product.getLastImageId())
                    val globalData = (requireContext().applicationContext as ShopIST)
                    globalData.imageCache.getAsImage(uuid, {
                        view.findViewById<ImageView>(R.id.productImageView).setImageBitmap(it)
                    }, {})
                }
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.product_row, viewGroup, false)
            // Hide cart quantities
            view.findViewById<TextView>(R.id.cartQuantityDisplay).visibility = View.GONE

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