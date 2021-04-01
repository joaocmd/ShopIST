package pt.ulisboa.tecnico.cmov.shopist.ui.pantries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST

class CreatePantryUI : Fragment() {

    private lateinit var editTitle: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_pantries_new, container, false)
        editTitle = root.findViewById(R.id.pantryName)
        root.findViewById<View>(R.id.okButton) .setOnClickListener { createPantry() }

        return root
    }

    private fun createPantry() {
        val globalData = activity?.applicationContext as ShopIST

        val newPantry = PantryList(editTitle.text.toString())
        globalData.addPantryList(newPantry)

        // Save data in file
        globalData.savePersistent()
        findNavController().popBackStack()
    }
}