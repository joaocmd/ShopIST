package pt.ulisboa.tecnico.cmov.shopist

import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity

class TopBarController {
    companion object {
        val items: Map<TopBarItems, Int> = mapOf(
            TopBarItems.ScanBarcode to R.id.action_scan_barcode,
            TopBarItems.SeeMore     to R.id.action_see_more,
            TopBarItems.Delete      to R.id.action_delete,
            TopBarItems.Edit        to R.id.action_edit,
            TopBarItems.Directions  to R.id.action_get_directions,
            TopBarItems.Share       to R.id.action_share,
        )

        fun optionsMenu(menu: Menu, app: FragmentActivity, title: String, topBarItems: List<TopBarItems>) {
            menu.findItem(R.id.action_scan_barcode).isVisible = topBarItems.contains(TopBarItems.ScanBarcode)
            menu.findItem(R.id.action_see_more).isVisible = topBarItems.contains(TopBarItems.SeeMore)
            menu.findItem(R.id.action_delete).isVisible = topBarItems.contains(TopBarItems.Delete)
            menu.findItem(R.id.action_edit).isVisible = topBarItems.contains(TopBarItems.Edit)
            menu.findItem(R.id.action_get_directions).isVisible = topBarItems.contains(TopBarItems.Directions)
            menu.findItem(R.id.action_share).isVisible = topBarItems.contains(TopBarItems.Share)
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
            (app as AppCompatActivity).supportActionBar!!.title = title
        }

        fun setOnlineOptions(menu: Menu, enabled: Boolean) {
            menu.findItem(R.id.action_scan_barcode).isEnabled = enabled
            menu.findItem(R.id.action_share).isEnabled = enabled
        }

        fun setSharedOptions(menu: Menu, enabled: Boolean) {
            menu.findItem(R.id.action_edit).isEnabled = enabled
            menu.findItem(R.id.action_delete).isEnabled = enabled
        }

        fun setEnable(menu: Menu, item: TopBarItems, enabled: Boolean) {
            if (items.containsKey(item)) {
                menu.findItem(items[item]!!).isEnabled = enabled
            }
        }

        private fun setOptionsMenu(menu: Menu, value: Boolean) {
            menu.findItem(R.id.action_scan_barcode).isVisible = value
            menu.findItem(R.id.action_see_more).isVisible = value
            menu.findItem(R.id.action_delete).isVisible = value
            menu.findItem(R.id.action_edit).isVisible = value
            menu.findItem(R.id.action_share).isVisible = value
            menu.findItem(R.id.action_get_directions).isVisible = value
        }
    }
}

enum class TopBarItems {
    ScanBarcode,
    SeeMore,
    Delete,
    Edit,
    Directions,
    Share
}
