package pt.ulisboa.tecnico.cmov.shopist.ui.pantries

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.TopBarController
import pt.ulisboa.tecnico.cmov.shopist.domain.Item
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import java.util.*

class PantryItemUI : Fragment() {

    private lateinit var item: Item

    private var pantry = 0
    private lateinit var pantryView: TextView

    private var needing = 0
    private lateinit var needingView: TextView

    private var cart = 0
    private lateinit var cartView: TextView

    companion object {
        const val ARG_PANTRY_ID = "pantryId"
        const val ARG_PRODUCT_ID = "productId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val pantryId = UUID.fromString(it.getString(ARG_PANTRY_ID))
            val productId = UUID.fromString(it.getString(ARG_PRODUCT_ID))
            val globalData = requireActivity().applicationContext as ShopIST

            val pantryList = globalData.getPantryList(pantryId)
            item = pantryList.getItem(productId)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_pantry_item, container, false)

        // Set product and pantry titles
        root.findViewById<TextView>(R.id.productTitleView).text = item.product.name
        root.findViewById<TextView>(R.id.pantryTitleView).text = item.pantryList.title

        pantryView = root.findViewById(R.id.pantryView)
        pantry = item.pantryQuantity
        changePantry(0)

        needingView = root.findViewById(R.id.needingView)
        needing = item.needingQuantity
        changeNeeding(0)

        cartView = root.findViewById(R.id.cartView)
        cart = item.cartQuantity
        changeCart(0)

        // Quantity Buttons
        root.findViewById<View>(R.id.incrementPantry).setOnClickListener { changePantry(1) }
        root.findViewById<View>(R.id.decrementPantry).setOnClickListener { changePantry(-1) }
        root.findViewById<View>(R.id.incrementNeeding).setOnClickListener { changeNeeding(1) }
        root.findViewById<View>(R.id.decrementNeeding).setOnClickListener { changeNeeding(-1) }
        root.findViewById<View>(R.id.incrementCart).setOnClickListener { changeCart(1) }
        root.findViewById<View>(R.id.decrementCart).setOnClickListener { changeCart(-1) }

        // Navigation Buttons
        root.findViewById<View>(R.id.cancelButton).setOnClickListener { cancel() }
        root.findViewById<View>(R.id.okButton).setOnClickListener { saveAndReturn() }

        return root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        TopBarController.noOptionsMenu(menu)
        menu.findItem(R.id.action_delete).isVisible = true
        menu.findItem(R.id.action_see_more).isVisible = true
        menu.findItem(R.id.action_edit).isVisible = true
        (requireActivity() as AppCompatActivity).supportActionBar!!.title =
            item.product.name
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit -> {
                findNavController().navigate(
                    R.id.action_nav_pantry_item_to_nav_create_product,
                    bundleOf(
                        CreateProductUI.ARG_PRODUCT_ID to this.item.product.uuid.toString()
                    )
                )
            }
            R.id.action_see_more -> {} // TODO
            R.id.action_delete -> {} // TODO
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun changePantry(v: Int) {
        if (pantry + v >= 0) {
            pantry += v
        }
        pantryView.text = pantry.toString()
    }

    private fun changeNeeding(v: Int) {
        if (needing + v >= 0) {
            needing += v
        }
        needingView.text = needing.toString()
    }

    private fun changeCart(v: Int) {
        if (cart + v >= 0) {
            cart += v
        }
        cartView.text = cart.toString()
    }

    private fun cancel() {
        findNavController().popBackStack()
    }

    private fun saveAndReturn() {
        item.pantryQuantity = pantry
        item.needingQuantity = needing
        item.cartQuantity = cart
        (requireActivity().applicationContext as ShopIST).savePersistent()
        cancel()
    }
}