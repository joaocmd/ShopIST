package pt.ulisboa.tecnico.cmov.shopist.ui.pantries

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

    private var touchingDown: Boolean = false

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

    @SuppressLint("ClickableViewAccessibility")
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

        root.findViewById<View>(R.id.incrementPantry).setOnTouchListener {
                view, motionEvent -> onTouchButton(view, motionEvent) {
            lifecycleScope.launch {
                changePantryWhileHeld(1)
            }
        }
        }
        root.findViewById<View>(R.id.decrementPantry).setOnTouchListener {
                view, motionEvent -> onTouchButton(view, motionEvent) {
            lifecycleScope.launch {
                changePantryWhileHeld(-1)
            }
        }
        }
        root.findViewById<View>(R.id.incrementNeeding).setOnTouchListener {
                view, motionEvent -> onTouchButton(view, motionEvent) {
            lifecycleScope.launch {
                changeNeedingWhileHeld(1)
            }
        }
        }
        root.findViewById<View>(R.id.decrementNeeding).setOnTouchListener { view, motionEvent ->
            onTouchButton(view, motionEvent) {
                lifecycleScope.launch {
                    changeNeedingWhileHeld(-1)
                }
            }
        }

        /*
        root.findViewById<View>(R.id.decrementPantry).setOnClickListener { changePantry(-1) }
        root.findViewById<View>(R.id.incrementNeeding).setOnClickListener { changeNeeding(1) }
        root.findViewById<View>(R.id.decrementNeeding).setOnClickListener { changeNeeding(-1) }

         */
        // root.findViewById<View>(R.id.incrementCart).setOnClickListener { changeCart(1) }
        // root.findViewById<View>(R.id.decrementCart).setOnClickListener { changeCart(-1) }

        // Deactivate buttons if shared and not connect
        val globalData = requireActivity().applicationContext as ShopIST
        if (!globalData.isAPIConnected && pantryList.isShared) {
            root.findViewById<View>(R.id.incrementPantry).isEnabled = false
            root.findViewById<View>(R.id.decrementPantry).isEnabled = false
            root.findViewById<View>(R.id.incrementNeeding).isEnabled = false
            root.findViewById<View>(R.id.decrementNeeding).isEnabled = false
            root.findViewById<View>(R.id.okButton).isEnabled = false
        }

        // Navigation Buttons
        root.findViewById<View>(R.id.cancelButton).setOnClickListener { cancel() }
        root.findViewById<View>(R.id.okButton).setOnClickListener { runBlocking {
            saveAndReturn()
            }
        }

        return root
    }

    private fun onTouchButton(view: View, motionEvent: MotionEvent, callback: (() -> Unit) ): Boolean {
        when(motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                touchingDown = true
                callback.invoke()
            }
            MotionEvent.ACTION_UP -> {
                touchingDown = false
            }
        }
        return false
    }

    private suspend fun changePantryWhileHeld(changeAmount : Int) {
        var counterUntilWorking = 5;
        var currentCounter = 0;
        while(touchingDown) {
            currentCounter++
            if(currentCounter > counterUntilWorking) {
                changePantry(changeAmount)
            }
            delay(100L)
        }
    }

    private suspend fun changeNeedingWhileHeld(changeAmount : Int) {
        var counterUntilWorking = 5;
        var currentCounter = 0;
        while(touchingDown) {
            currentCounter++
            if(currentCounter > counterUntilWorking) {
                changeNeeding(changeAmount)
            }
            delay(100L)
        }
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

    private fun changeHoldPantry(v: Int, onLongClickListener: View.OnLongClickListener) {
        //onLongClickListener.
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
                runBlocking {
                    saveAndReturn()
                }
            },
            {}
        )
    }

    private fun cancel() {
        findNavController().popBackStack()
    }

    private suspend fun saveAndReturn() {

        touchingDown = false
        delay(200L)

        item.pantryQuantity = pantry
        item.needingQuantity = needing
        item.cartQuantity = cart

        if (pantryList.isShared) {
            API.getInstance(requireContext()).updatePantry(pantryList) {
                (requireActivity().applicationContext as ShopIST).savePersistent()
                cancel()
            }
        }
        else {

            (requireActivity().applicationContext as ShopIST).savePersistent()
            cancel()
        }
    }
}