package pt.ulisboa.tecnico.cmov.shopist

import android.view.Menu

class TopBarController {
    companion object {

        fun noOptionsMenu(menu: Menu) {
            setOptionsMenu(menu, false)
        }

        fun allOptionsMenu(menu: Menu) {
            setOptionsMenu(menu, true)
        }

        private fun setOptionsMenu(menu: Menu, value: Boolean) {
            menu.findItem(R.id.action_see_more).isVisible = value
            menu.findItem(R.id.action_delete).isVisible = value
            menu.findItem(R.id.action_edit).isVisible = value
            menu.findItem(R.id.action_share).isVisible = value
        }
    }
}