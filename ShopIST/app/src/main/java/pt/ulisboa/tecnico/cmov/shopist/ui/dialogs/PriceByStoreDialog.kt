package pt.ulisboa.tecnico.cmov.shopist.ui.dialogs

import android.app.AlertDialog.Builder
import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.domain.Product
import pt.ulisboa.tecnico.cmov.shopist.domain.Store


class PriceByStoreDialog(fragment: Fragment, product: Product) {
    private var recyclerAdapter: PriceByStoreDialog.PriceListAdapter
    private var builder: Builder = Builder(fragment.requireContext())
    private var dialog: Dialog? = null

    init {
        builder.setTitle(fragment.getString(R.string.prices_by_stores))

        val root = fragment.layoutInflater.inflate(R.layout.dialog_price_by_store, null)
        builder.setView(root)

        val listView: RecyclerView = root.findViewById(R.id.pricesList)
        recyclerAdapter = PriceListAdapter(product.prices.toList())
        listView.layoutManager = LinearLayoutManager(fragment.requireContext())
        listView.adapter = recyclerAdapter
    }

    fun show() {
        dialog = builder.show()
    }

    fun dismiss() {
        dialog?.dismiss()
    }

    private inner class PriceListAdapter(var priceList: List<Pair<Store, Number>>) :
        RecyclerView.Adapter<PriceListAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val storeNameView: TextView = view.findViewById(R.id.storeName)
            private val priceView : TextView = view.findViewById(R.id.priceValue)

            fun bind(item: Pair<Store, Number>) {
                storeNameView.text = item.first.name
                priceView.text = "${item.second} €"
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.row_store_price, viewGroup, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.bind(priceList[position])
        }

        override fun getItemCount() = priceList.size
    }
}