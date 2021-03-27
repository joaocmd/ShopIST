package pt.ulisboa.tecnico.cmov.shopist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ulisboa.tecnico.cmov.shopist.domain.Item
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST

class PantryActivity : AppCompatActivity() {

    private lateinit var pantryList : PantryList
    private lateinit var adapter : PantryAdapter
    private var idx : Int = 0

    companion object {
        private const val TAG = "shopist.PantryActivity"
        const val SHOW_ITEM = "$TAG.SHOW_ITEM"
    }

    inner class PantryAdapter(private val dataSet: MutableList<Item>) :
            RecyclerView.Adapter<PantryAdapter.ViewHolder>() {

        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */
        inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
            private val textView: TextView = view.findViewById(R.id.rowText)
            private val pantryQuantityView : TextView = view.findViewById(R.id.pantryQuantityDisplay)
            private val needingQuantityView : TextView = view.findViewById(R.id.needingQuantityDisplay)

            fun bind(item: Item) {
                textView.text = item.product.name
                pantryQuantityView.text = item.pantryQuantity.toString()
                needingQuantityView.text = item.needingQuantity.toString()

                view.setOnClickListener {
                    val int = Intent(applicationContext, AddItemActivity::class.java)
                    int.putExtra(SHOW_ITEM, item.product.uuid)
                    startActivity(int)
                }

                view.setOnLongClickListener {
                    // set long click change top menu so that we're able to delete items
                    // it can also simply appear on the menu, though
                    true
                }
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.product_row, viewGroup, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.bind(dataSet[position])
        }

        override fun getItemCount() = dataSet.size
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantry)

        // Get pantry list
        idx = intent.getIntExtra(PantriesListActivity.GET_PANTRY_INDEX_INT, 0) // FIXME: Default value must not be 0
        val globalData = applicationContext as ShopIST
        pantryList = globalData.getPantryList(idx)

        // Set products
        val listView = findViewById<RecyclerView>(R.id.recyclerView)
        adapter = PantryAdapter(pantryList.items)
        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged();
    }

    fun onNewItem(view: View) {
        val int = Intent(applicationContext, AddItemActivity::class.java)
        int.putExtra( PantriesListActivity.GET_PANTRY_INDEX_INT, idx)
        startActivity(int)
    }
}