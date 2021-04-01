package pt.ulisboa.tecnico.cmov.shopist.ui.pantries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.domain.Product
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.domain.Store

class CreateProductUI: Fragment() {

    private val selectedStores: MutableSet<Store> = mutableSetOf()

    private lateinit var recyclerView: RecyclerView
    private lateinit var productNameView: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_pantry_create_product, container, false)

        recyclerView = root.findViewById(R.id.storesList)
        productNameView = root.findViewById(R.id.productName)


        val globalData = activity?.applicationContext as ShopIST
        val adapter = StoresListAdapter(globalData.stores)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        root.findViewById<View>(R.id.okButton).setOnClickListener { onCreateProduct() }

        return root
    }

    private fun onCreateProduct() {
        val title = productNameView.text.toString()

        // Check if has a name
        if (title.isEmpty()) {
            Toast.makeText(context, "First type a title.", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if has a store
        if (selectedStores.isEmpty()) {
            Toast.makeText(context, "Select at least one store.", Toast.LENGTH_SHORT).show()
            return
        }

        val product = Product(productNameView.text.toString())
        product.stores = selectedStores
        val globalData = activity?.applicationContext as ShopIST
        globalData.addProduct(product)

        // Save data in file
        globalData.savePersistent()
        findNavController().popBackStack()
    }

    inner class StoresListAdapter(
        var list: Array<Store>
    ) :
        RecyclerView.Adapter<StoresListAdapter.ViewHolder>() {

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            private val textView: TextView = view.findViewById(R.id.rowText)

            fun bind(store: Store) {
                textView.text = store.title

                val checkBox: CheckBox = view.findViewById(R.id.checkBox)

                val globalData = activity?.applicationContext as ShopIST
                val defaultStore = globalData.getDefaultStore()
                if (defaultStore != null && defaultStore == store) {
                    checkBox.isChecked = true
                    selectedStores.add(store)
                }


                checkBox.setOnCheckedChangeListener { _, _ ->
                    if (selectedStores.contains(store)) {
                        selectedStores.remove(store)
                    } else {
                        selectedStores.add(store)
                    }
                }
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.store_selected_row, viewGroup, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.bind(list[position])
        }

        override fun getItemCount() = list.size
    }
}