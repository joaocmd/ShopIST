package pt.ulisboa.tecnico.cmov.shopist

import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity

class TopBarController {
    companion object {
        fun optionsMenu(menu: Menu, app: FragmentActivity, title: String, topBarItems: List<TopBarItems>) {
            menu.findItem(R.id.action_see_more).isVisible = topBarItems.contains(TopBarItems.SeeMore)
            menu.findItem(R.id.action_delete).isVisible = topBarItems.contains(TopBarItems.Delete)
            menu.findItem(R.id.action_edit).isVisible = topBarItems.contains(TopBarItems.Edit)
            menu.findItem(R.id.action_share).isVisible = topBarItems.contains(TopBarItems.Share)
            menu.findItem(R.id.action_get_directions).isVisible = topBarItems.contains(TopBarItems.Directions)
            setTitle(app, title)
        }

        fun noOptionsMenu(menu: Menu, app: FragmentActivity, title: String) {
            setOptionsMenu(menu, false)
            setTitle(app, title)
        }

        fun allOptionsMenu(menu: Menu, app: FragmentActivity, title: String) {
            setOptionsMenu(menu, true)
            setTitle(app, title)
        }

        private fun setTitle(app: FragmentActivity, title: String) {
            (app as AppCompatActivity).supportActionBar!!.title = title;
        }

        private fun setOptionsMenu(menu: Menu, value: Boolean) {
            menu.findItem(R.id.action_see_more).isVisible = value
            menu.findItem(R.id.action_delete).isVisible = value
            menu.findItem(R.id.action_edit).isVisible = value
            menu.findItem(R.id.action_share).isVisible = value
            menu.findItem(R.id.action_get_directions).isVisible = value
        }
    }
}

enum class TopBarItems {
    SeeMore,
    Delete,
    Edit,
    Directions,
    Share
}