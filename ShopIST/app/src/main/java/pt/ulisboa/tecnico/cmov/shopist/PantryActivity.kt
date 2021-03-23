package pt.ulisboa.tecnico.cmov.shopist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ulisboa.tecnico.cmov.shopist.domain.Product
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST

class PantryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantry)

        // Get pantry list
         val idx = intent.getIntExtra(ListPantryActivity.GET_PANTRY_INDEX_INT, 0) // FIXME: Default value must not be 0
        val globalData = applicationContext as ShopIST
        val pantryList = globalData.getPantryList(idx)

        // Set products
        val listView = findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = PantryAdapter(pantryList.products)
        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = adapter
    }

    fun onNewProduct(view: View) {
        // TODO:
    }
}

class PantryAdapter(private val dataSet: MutableList<Product>) :
        RecyclerView.Adapter<PantryAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.rowText)

        init {
            // Define click listener for the ViewHolder's View.
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.recycler_view_row, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = dataSet[position].name
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}