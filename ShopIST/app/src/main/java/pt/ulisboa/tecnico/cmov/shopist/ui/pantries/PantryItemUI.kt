package pt.ulisboa.tecnico.cmov.shopist.ui.pantries

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.TopBarController
import pt.ulisboa.tecnico.cmov.shopist.TopBarItems
import pt.ulisboa.tecnico.cmov.shopist.domain.Item
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.ui.dialogs.ConfirmationDialog
import pt.ulisboa.tecnico.cmov.shopist.ui.products.CreateProductUI
import pt.ulisboa.tecnico.cmov.shopist.utils.API
import java.util.*

class PantryItemUI : Fragment() {

    private lateinit var root: View
    private lateinit var pantryList: PantryList
    private lateinit var item: Item

    private var pantry = 0
    private lateinit var pantryView: TextView

    private var needing = 0
    private lateinit var needingView: TextView

    private var cart = 0
    private lateinit var cartView: TextView

    private lateinit var menuRoot: Menu

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

            pantryList = globalData.getPantryList(pantryId)
            item = pantryList.getItem(productId)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_pantry_item, container, false)

        // Set product and pantry titles
        root.findViewById<TextView>(R.id.productTitleView).text = item.product.name
        root.findViewById<TextView>(R.id.pantryTitleView).text = item.pantryList.name

        pantryView = root.findViewById(R.id.pantryView)
        pantry = item.pantryQuantity
        changePantry(0)

        needingView = root.findViewById(R.id.needingView)
        needing = item.needingQuantity
        changeNeeding(0)

        // cartView = root.findViewById(R.id.cartView)
        // cart = item.cartQuantity
        // changeCart(0)

        // Quantity Buttons
        root.findViewById<View>(R.id.incrementPantry).setOnClickListener { changePantry(1) }
        root.findViewById<View>(R.id.decrementPantry).setOnClickListener { changePantry(-1) }
        root.findViewById<View>(R.id.incrementNeeding).setOnClickListener { changeNeeding(1) }
        root.findViewById<View>(R.id.decrementNeeding).setOnClickListener { changeNeeding(-1) }
        // root.findViewById<View>(R.id.incrementCart).setOnClickListener { changeCart(1) }
        // root.findViewById<View>(R.id.decrementCart).setOnClickListener { changeCart(-1) }

        // Navigation Buttons
        root.findViewById<View>(R.id.cancelButton).setOnClickListener { cancel() }
        root.findViewById<View>(R.id.okButton).setOnClickListener { saveAndReturn() }

        return root
    }

    override fun onResume() {
        super.onResume()

        val globalData = requireActivity().applicationContext as ShopIST
        setEnableButtons(globalData.isAPIConnected)
    }

    private fun setEnableButtons(enabled: Boolean) {
        if (pantryList.isShared) {
            if (this::menuRoot.isInitialized) {
                TopBarController.setSharedOptions(menuRoot, enabled)
            }
        } else {
            if (this::menuRoot.isInitialized) {
                TopBarController.setSharedOptions(menuRoot, true)
            }
        }
        if (this::menuRoot.isInitialized) {
            TopBarController.setOnlineOptions(menuRoot, enabled)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        menuRoot = menu

        TopBarController.optionsMenu(menu, requireActivity(), item.product.name,
            listOf(TopBarItems.Edit, TopBarItems.Delete))

        val globalData = (requireActivity().applicationContext as ShopIST)
        // If couldn't connect until now disable everything
        setEnableButtons(globalData.isAPIConnected)
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
            R.id.action_delete -> deleteItem()
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

    private fun deleteItem() {
        ConfirmationDialog(
            requireContext(),
            getString(R.string.confirm_pantry_item_delete),
            {
                // Remove item from pantry
                pantryList.removeItem(item.product.uuid)

                saveAndReturn()
            },
            {}
        )
    }

    private fun cancel() {
        findNavController().popBackStack()
    }

    private fun saveAndReturn() {
        item.pantryQuantity = pantry
        item.needingQuantity = needing
        item.cartQuantity = cart

        if (pantryList.isShared) {
            API.getInstance(requireContext()).updatePantry(pantryList)
        }

        (requireActivity().applicationContext as ShopIST).savePersistent()
        cancel()
    }
}