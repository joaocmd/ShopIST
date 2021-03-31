package pt.ulisboa.tecnico.cmov.shopist.ui.shoppings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.domain.Item
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.domain.shoppingList.ShoppingListItem

class ShoppingListItemUI : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var shoppingListItem: ShoppingListItem

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_store_shopping_list_item, container, false)
        shoppingListItem = (activity?.applicationContext as ShopIST).currentShoppingListItem!!


        recyclerView = root.findViewById(R.id.shoppingListItemList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ShoppingListItemListAdapter(shoppingListItem)

        return root
    }


    inner class ShoppingListItemListAdapter(
        var shoppingListItem: ShoppingListItem
    ) :
        RecyclerView.Adapter<ShoppingListItemListAdapter.ViewHolder>() {

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            private val textView: TextView = view.findViewById(R.id.rowText)
            private val pantryView: TextView = view.findViewById(R.id.pantryViewLocal)
            private val needingView: TextView = view.findViewById(R.id.needingViewLocal)
            private val cartView: TextView = view.findViewById(R.id.cartViewLocal)
            private val currentQuantity: TextView = view.findViewById(R.id.currentQuantity)

            fun bind(item: Item) {
                val pantryList = item.pantryList
                textView.text = pantryList.title

                val quantities = shoppingListItem.quantities[pantryList]!!
                pantryView.text = quantities.pantry.toString()
                needingView.text = quantities.needing.toString()
                cartView.text = quantities.cart.toString()
                currentQuantity.text = quantities.cart.toString()

                view.findViewById<View>(R.id.moreButton).setOnClickListener {
                    // Add and update view
                    shoppingListItem.add(item.pantryList)
                    cartView.text = quantities.cart.toString()
                    currentQuantity.text = quantities.cart.toString()
                }
                view.findViewById<View>(R.id.lessButton).setOnClickListener {
                    // Subtract and update view
                    shoppingListItem.remove(item.pantryList)
                    cartView.text = quantities.cart.toString()
                    currentQuantity.text = quantities.cart.toString()
                }
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.shopping_list_cart_quantity_row, viewGroup, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.bind(shoppingListItem.items[position])
        }

        override fun getItemCount() = shoppingListItem.items.size
    }
}