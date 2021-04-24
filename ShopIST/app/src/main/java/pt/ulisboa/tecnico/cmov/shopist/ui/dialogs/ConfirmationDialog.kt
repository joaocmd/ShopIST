package pt.ulisboa.tecnico.cmov.shopist.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import pt.ulisboa.tecnico.cmov.shopist.R

class ConfirmationDialog(
    context: Context,
    title: String,
    onPositiveListener: () -> Unit,
    onNegativeListener: () -> Unit
) {
    private var builder: AlertDialog.Builder = AlertDialog.Builder(context)

    init {
        builder.setTitle(title)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(context.resources.getString(R.string.ok)) { dialog, _ ->
                onPositiveListener()
                dialog.dismiss()
            }
            .setNegativeButton(context.resources.getString(R.string.cancel)) { dialog, _ ->
                onNegativeListener()
                dialog.dismiss()
            }
            .show()
    }
}