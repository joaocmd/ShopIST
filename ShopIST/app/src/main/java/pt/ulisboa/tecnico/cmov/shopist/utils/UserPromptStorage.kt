package pt.ulisboa.tecnico.cmov.shopist.utils

import androidx.fragment.app.Fragment
import pt.ulisboa.tecnico.cmov.shopist.ui.dialogs.PromptMessage
import pt.ulisboa.tecnico.cmov.shopist.ui.dialogs.UserPromptDialog
import kotlin.random.Random

class UserPromptStorage() {
    var savedPrompts: MutableMap<PromptMessage, Boolean> = mutableMapOf()

    constructor(map: Map<PromptMessage, Boolean>): this() {
        savedPrompts = map.toMutableMap()
    }

    private fun setPrompt(type: PromptMessage, enabled: Boolean) {
        savedPrompts[type] = enabled
    }

    fun getPrompt(type: PromptMessage, fragment: Fragment, onAcceptListener: () -> Unit) {
        // Verify if enabled with probability of 30%
        if (savedPrompts[type] != false && Random.nextInt(1, 100) < 30) {
            UserPromptDialog(fragment, type, {
                 onAcceptListener()
            }, {
                setPrompt(it, false)
            })
        }
    }
}
