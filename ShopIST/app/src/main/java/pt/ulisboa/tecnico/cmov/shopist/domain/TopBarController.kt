package pt.ulisboa.tecnico.cmov.shopist.domain

import android.view.Menu
import pt.ulisboa.tecnico.cmov.shopist.R

class TopBarController {
    companion object {
        fun noOptionsMenu(menu: Menu) {
            menu.findItem(R.id.action_see_more).isVisible = false
            menu.findItem(R.id.action_delete_current).isVisible = false
            menu.findItem(R.id.action_edit_current).isVisible = false
            menu.findItem(R.id.action_delete_selected).isVisible = false
            menu.findItem(R.id.action_edit_selected).isVisible = false
        }

        fun onPrepareOptionsMenu(menu: Menu) {
            menu.findItem(R.id.action_see_more).isVisible = false
            menu.findItem(R.id.action_delete_current).isVisible = false
            menu.findItem(R.id.action_edit_current).isVisible = false
            menu.findItem(R.id.action_delete_selected).isVisible = false
            menu.findItem(R.id.action_edit_selected).isVisible = false
        }

        fun onPrepareOptionsMenu(menu: Menu, currentlySelectedItem : PantryList?) {
            menu.findItem(R.id.action_see_more).isVisible = false
            menu.findItem(R.id.action_delete_current).isVisible = false
            menu.findItem(R.id.action_edit_current).isVisible = false
            menu.findItem(R.id.action_delete_selected).isVisible = currentlySelectedItem != null
            menu.findItem(R.id.action_edit_selected).isVisible = currentlySelectedItem != null
        }
    }
}