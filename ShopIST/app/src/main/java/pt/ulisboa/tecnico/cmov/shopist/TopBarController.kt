package pt.ulisboa.tecnico.cmov.shopist

import android.view.Menu

class TopBarController {
    companion object {
        fun noOptionsMenu(menu: Menu) {
            menu.findItem(R.id.action_see_more).isVisible = false
            menu.findItem(R.id.action_delete).isVisible = false
            menu.findItem(R.id.action_edit).isVisible = false
            menu.findItem(R.id.action_share).isVisible = false
        }
    }
}