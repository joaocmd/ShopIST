package pt.ulisboa.tecnico.cmov.shopist.ui.dialogs

import android.app.AlertDialog.Builder
import android.media.Image
import android.widget.ImageView
import android.widget.RatingBar
import androidx.fragment.app.Fragment
import pt.ulisboa.tecnico.cmov.shopist.R


class ImageFullScreenDialog(fragment: Fragment, img: ImageView) {

    private var image: ImageView
    private var builder: Builder = Builder(fragment.requireContext())

    init {
        val root = fragment.layoutInflater.inflate(R.layout.dialog_image_full_screen, null)

        image = root.findViewById<ImageView>(R.id.productImageView)
        image.setImageDrawable(img.drawable)
        //ratingBar = root.findViewById(R.id.ratingBar)
        //ratingBar.rating = rating?.toFloat() ?: 0f

        val context = fragment.requireContext()
        builder//.setTitle(context.getString(R.string.image_full_screen))
                /*
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(context.resources.getString(R.string.ok)) { dialog, _ ->
                //onPositiveListener(ratingBar.rating.toInt())
                dialog.dismiss()
            }
            .setNegativeButton(context.resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            */
            .setView(root)

    }

    fun show() {
        builder.show()
    }
}