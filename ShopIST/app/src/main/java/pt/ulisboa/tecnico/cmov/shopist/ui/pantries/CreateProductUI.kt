package pt.ulisboa.tecnico.cmov.shopist.ui.pantries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.TopBarController
import pt.ulisboa.tecnico.cmov.shopist.domain.Product
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.domain.Store
import java.util.*

class CreateProductUI: Fragment() {

    private var selectedStores: MutableSet<Store> = mutableSetOf()

    private lateinit var recyclerView: RecyclerView
    private lateinit var productNameView: EditText
    private var product: Product? = null

    companion object {
        const val ARG_PRODUCT_ID = "productId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val productId = UUID.fromString(it.getString(ARG_PRODUCT_ID))
            val globalData = requireActivity().applicationContext as ShopIST

            product = globalData.getProduct(productId)
        }
        setHasOptionsMenu(true)
    }

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

        if (product != null) {
            productNameView.setText(product!!.name)
            selectedStores = product!!.stores.toMutableSet()

            root.findViewById<TextView>(R.id.textView).text = getString(R.string.edit_product)
            root.findViewById<Button>(R.id.okButton).text = getString(R.string.edit_product_save)
        }

        return root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        var name = R.string.create_product_title;
        if(product != null) name = R.string.edit_product_title;
        TopBarController.noOptionsMenu(menu, requireActivity(), getString(name))
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

        val globalData = activity?.applicationContext as ShopIST
        if (product == null) {
            val product = Product(productNameView.text.toString())
            product.stores = selectedStores
            globalData.addProduct(product)
        } else {
            product!!.name = title
            product!!.stores = selectedStores
        }

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
                textView.text = store.name

                val checkBox: CheckBox = view.findViewById(R.id.checkBox)

                val globalData = activity?.applicationContext as ShopIST
                val defaultStore = globalData.getDefaultStore()
                if (product == null && defaultStore != null && defaultStore == store) {
                    checkBox.isChecked = true
                    selectedStores.add(store)
                } else if (product !== null && store in product!!.stores) {
                    checkBox.isChecked = true
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