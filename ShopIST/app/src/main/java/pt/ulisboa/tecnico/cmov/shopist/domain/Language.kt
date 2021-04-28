package pt.ulisboa.tecnico.cmov.shopist.domain

import android.content.Context
import android.util.Log
import pt.ulisboa.tecnico.cmov.shopist.utils.API

class Language() {
    var currentLanguage: Languages? = null

    companion object {
        var languages: Map<String, Languages> = mapOf(
            "pt" to Languages.PT,
            "en" to Languages.EN,
            "PT" to Languages.PT,
            "EN" to Languages.EN,
        )
    }
}

enum class Languages(val language: String) {
    PT("pt"),
    EN("en")
}

open class Translatable(var originText: String, var originLang: Languages?) {
    var translatedText: String = ""
    var hasTranslated = false

    fun getText(targetLang: Languages, context: Context, onSuccessListener: (String) -> Unit) {
        // Return translated if already translated once

        Log.i("lang", "test1")
        if (hasTranslated) {
            onSuccessListener(translatedText)
            return
        }
        Log.i("lang", "test")
        // Get current language if not given
        if (originLang === null) {
            val globalData = context.applicationContext as ShopIST
            originLang = globalData.getLang()
        }
        return when (targetLang) {
            (originLang) -> {
                this.translatedText = originText
                onSuccessListener(translatedText)
            }
            else -> {
                API.getInstance(context).translate(originText, originLang!!.language, targetLang.language, {
                    hasTranslated = true
                    this.translatedText = it
                    onSuccessListener(it)
                }, {
                    this.translatedText = originText
                })
            }
        }
    }
}