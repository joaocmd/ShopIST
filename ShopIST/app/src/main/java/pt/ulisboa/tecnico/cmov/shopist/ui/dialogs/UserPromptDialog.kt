package pt.ulisboa.tecnico.cmov.shopist.ui.dialogs

import android.app.AlertDialog
import android.content.DialogInterface
import androidx.fragment.app.Fragment
import pt.ulisboa.tecnico.cmov.shopist.R

class UserPromptDialog(
    fragment: Fragment,
    message: PromptMessage,
    onAcceptListener: () -> Unit,
    onStopListener: (PromptMessage) -> Unit
) {
    private var builder: AlertDialog.Builder = AlertDialog.Builder(fragment.requireContext())

    init {
        builder.setTitle(fragment.getString(R.string.user_prompt_title))
            .setMessage(fragment.getString(message.stringId))
            .setNeutralButton(fragment.getString(R.string.user_prompt_cancel)) {
                    dialogInterface, _ ->
                // Cancel
                dialogInterface.dismiss()
            }
            .setPositiveButton(fragment.getString(R.string.user_prompt_accept)) {
                    dialogInterface, _ ->
                // Accept
                dialogInterface.dismiss()
                onAcceptListener()
            }
            .setNegativeButton(fragment.getString(R.string.user_prompt_stop_remembering)) {
                    dialogInterface, _ ->
                // Stop remembering this type of dialog
                dialogInterface.dismiss()
                onStopListener(message)
            }
            .show()
    }
}

enum class PromptMessage(val stringId: Int) {
    ADD_BARCODE(R.string.user_prompt_on_cart),
    ADD_PRICE_IMAGE(R.string.user_prompt_on_barcode)
}