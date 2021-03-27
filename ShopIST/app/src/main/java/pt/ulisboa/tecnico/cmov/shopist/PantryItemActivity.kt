package pt.ulisboa.tecnico.cmov.shopist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import pt.ulisboa.tecnico.cmov.shopist.domain.Item
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import java.util.*

class PantryItemActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "shopist.PantryActivity"
        const val PANTRY_ID = "$TAG.PANTRY_ID"
        const val ITEM_ID = "$TAG.ITEM_ID"
    }

    private lateinit var pantryList: PantryList
    private lateinit var item: Item

    private var pantry = 0
    private lateinit var pantryView: TextView

    private var needing = 0
    private lateinit var needingView: TextView

    private var cart = 0
    private lateinit var cartView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantry_item)

        val globalData = applicationContext as ShopIST

        val pantryUUID = UUID.fromString(intent.getStringExtra(PANTRY_ID))
        pantryList = globalData.getPantryList(pantryUUID)
        val itemUUID = UUID.fromString(intent.getStringExtra(ITEM_ID))
        item = pantryList.getItem(itemUUID)

        pantryView = findViewById(R.id.pantryView)
        pantry = item.pantryQuantity
        changePantry(0)

        needingView = findViewById(R.id.needingView)
        needing = item.needingQuantity
        changeNeeding(0)

        cartView = findViewById(R.id.cartView)
        cart = item.cartQuantity
        changeCart(0)

        findViewById<View>(R.id.incrementPantry).setOnClickListener { changePantry(1) }
        findViewById<View>(R.id.decrementPantry).setOnClickListener { changePantry(-1) }
        findViewById<View>(R.id.incrementNeeding).setOnClickListener { changeNeeding(1) }
        findViewById<View>(R.id.decrementNeeding).setOnClickListener { changeNeeding(-1) }
        findViewById<View>(R.id.incrementCart).setOnClickListener { changeCart(1) }
        findViewById<View>(R.id.decrementCart).setOnClickListener { changeCart(-1) }
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

    fun onCancelButton(view: View) {
        finish()
    }

    fun onOkButton(view: View) {
        item.pantryQuantity = pantry
        item.needingQuantity = needing
        item.cartQuantity = cart
        (applicationContext as ShopIST).savePersistent()
        finish()
    }
}