package pt.ulisboa.tecnico.cmov.shopist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ulisboa.tecnico.cmov.shopist.domain.Product
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.domain.Store

class CreateProductActivity : AppCompatActivity() {

    private val selectedStores: MutableSet<Store> = mutableSetOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_product)

        initListStores()
    }

    private fun initListStores() {
        val recyclerView = findViewById<RecyclerView>(R.id.storesList)
        val globalData = applicationContext as ShopIST
        val adapter = StoresListAdapter(globalData.shoppingLists)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    fun onCreateProduct(view: View) {

        val productName = findViewById<EditText>(R.id.productName).text.toString()
        val product = Product(productName)
        product.stores = selectedStores

        val globalData = applicationContext as ShopIST
        globalData.addProduct(product)

        // Save data in file
        globalData.savePersistent()

        finish()
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
                checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
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