package pt.ulisboa.tecnico.cmov.shopist.domain

import android.content.Context
import android.util.Log
import pt.ulisboa.tecnico.cmov.shopist.utils.API

class Language {
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
    var hasTranslatedToLanguage: Languages? = null

    fun getText(targetLang: Languages, context: Context, onSuccessListener: (String) -> Unit) {
        // Return translated if already translated to target language
        if (hasTranslatedToLanguage == targetLang) {
            onSuccessListener(translatedText)
            return
        }
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
                    hasTranslatedToLanguage = targetLang
                    this.translatedText = it
                    onSuccessListener(it)
                }, {
                    this.translatedText = originText
                })
            }
        }
    }
}