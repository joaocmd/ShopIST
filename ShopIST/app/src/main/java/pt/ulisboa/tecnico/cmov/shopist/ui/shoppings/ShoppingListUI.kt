package pt.ulisboa.tecnico.cmov.shopist.ui.shoppings

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.TopBarController
import pt.ulisboa.tecnico.cmov.shopist.TopBarItems
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.domain.Store
import pt.ulisboa.tecnico.cmov.shopist.domain.shoppingList.ShoppingList
import pt.ulisboa.tecnico.cmov.shopist.domain.shoppingList.ShoppingListItem
import pt.ulisboa.tecnico.cmov.shopist.ui.pantries.CreateProductUI
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [ShoppingListUI.newInstance] factory method to
 * create an instance of this fragment.
 */
class ShoppingListUI : Fragment() {

    private lateinit var shoppingList: ShoppingList
    private lateinit var store: Store
    private lateinit var storeId: UUID
    private lateinit var recyclerAdapter: ShoppingListAdapter

    companion object {
        const val ARG_STORE_ID = "pantryId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            storeId = UUID.fromString(it.getString(ARG_STORE_ID))
            val globalData = requireActivity().applicationContext as ShopIST
            shoppingList = globalData.getShoppingList(storeId)
            store = globalData.getStore(storeId)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_store_shopping_list, container, false)
        val listView: RecyclerView = root.findViewById(R.id.productsList)

        // TODO: Change title bar to have store name

        recyclerAdapter = ShoppingListAdapter(shoppingList)

        listView.layoutManager = LinearLayoutManager(context)
        listView.adapter = recyclerAdapter

        // Navigation Buttons
        root.findViewById<View>(R.id.cancelButton).setOnClickListener { cancel() }
        root.findViewById<View>(R.id.okButton).setOnClickListener { saveAndReturn() }

        // TODO: Improve the location of this button
        root.findViewById<View>(R.id.editStoreButton).setOnClickListener {
            editStore()
        }

        return root
    }

    fun editStore() {
        findNavController().navigate(
            R.id.action_nav_store_shopping_list_to_nav_create_shopping_list,
            bundleOf(
                CreateShoppingListUI.ARG_STORE_ID to storeId.toString()
            )
        )
    }

    override fun onResume() {
        recyclerAdapter.notifyDataSetChanged();
        super.onResume()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        TopBarController.optionsMenu(menu, requireActivity(),
            store.name, listOf(TopBarItems.Edit))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit -> editStore()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun cancel() {
        // Shouldn't be needed but can't hurt
        (activity?.applicationContext as ShopIST).currentShoppingListItem = null
        findNavController().popBackStack()
    }

    private fun saveAndReturn() {
        shoppingList.saveChanges()
        (requireActivity().applicationContext as ShopIST).savePersistent()
        cancel()
    }


    inner class ShoppingListAdapter(private val shoppingList: ShoppingList) :
        RecyclerView.Adapter<ShoppingListAdapter.ViewHolder>() {


        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */
        inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
            private val textView: TextView = view.findViewById(R.id.rowText)
            private val pantryQuantityView : TextView = view.findViewById(R.id.pantryQuantityDisplay)
            private val needingQuantityView : TextView = view.findViewById(R.id.needingQuantityDisplay)
            private val cartQuantityView : TextView = view.findViewById(R.id.cartQuantityDisplay)

            fun bind(item: ShoppingListItem) {
                textView.text = item.product.name
                val quantities = item.getAllQuantities()
                pantryQuantityView.text = quantities.pantry.toString()
                needingQuantityView.text = quantities.needing.toString()
                cartQuantityView.text = quantities.cart.toString()

                view.setOnClickListener {
                    // set current item because we can't pass object references in bundles
                    (activity?.applicationContext as ShopIST).currentShoppingListItem = item
                    findNavController().navigate(R.id.action_nav_store_shopping_list_to_nav_store_shopping_list_item)
                }
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.product_row, viewGroup, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.bind(shoppingList.items[position])
        }

        override fun getItemCount() = shoppingList.items.size
    }
}