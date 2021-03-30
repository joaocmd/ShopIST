package pt.ulisboa.tecnico.cmov.shopist.ui.shoppings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.domain.ShoppingList
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [CreateShoppingList.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateShoppingList : Fragment() {

    private lateinit var root: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_create_shopping_list, container, false)
        root.findViewById<View>(R.id.okButton).setOnClickListener { saveAndReturn() }

        return root
    }

    private fun saveAndReturn() {
        val globalData = requireActivity().applicationContext as ShopIST

        val newShoppingList = ShoppingList(
            root.findViewById<EditText>(R.id.editTitle).text.toString()
        )
        globalData.addShoppingList(newShoppingList)
        globalData.savePersistent()
        findNavController().popBackStack()
    }
}