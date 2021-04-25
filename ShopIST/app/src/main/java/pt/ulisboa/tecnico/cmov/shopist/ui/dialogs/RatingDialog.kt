package pt.ulisboa.tecnico.cmov.shopist.ui.dialogs

import android.app.AlertDialog.Builder
import android.widget.RatingBar
import androidx.fragment.app.Fragment
import pt.ulisboa.tecnico.cmov.shopist.R


class RatingDialog(fragment: Fragment, rating: Int?, onPositiveListener: (Int) -> Unit) {
    private var ratingBar: RatingBar
    private var builder: Builder = Builder(fragment.requireContext())

    init {
        val root = fragment.layoutInflater.inflate(R.layout.dialog_product_rating, null)

        ratingBar = root.findViewById(R.id.ratingBar)
        ratingBar.rating = rating?.toFloat() ?: 0f

        val context = fragment.requireContext()
        builder.setTitle(context.getString(R.string.add_rating_dialog))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(context.resources.getString(R.string.ok)) { dialog, _ ->
                onPositiveListener(ratingBar.rating.toInt())
                dialog.dismiss()
            }
            .setNegativeButton(context.resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setView(root)
    }

    fun show() {
        builder.show()
    }
}