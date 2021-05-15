package pt.ulisboa.tecnico.cmov.shopist.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import pt.ulisboa.tecnico.cmov.shopist.R
import java.util.*

class LocaleHelper {

    companion object {
        fun setLocale(c: Context) {
            setNewLocale(c, getLanguage(c))
        }

        fun setNewLocale(c: Context, language: String) {
            persistLanguage(c, language)
            updateResources(c, language)
        }

        fun getLanguage(c: Context): String {
            val settings = c.getSharedPreferences(c.getString(R.string.my_preference), MODE_PRIVATE)
            val langString = settings.getString(c.getString(R.string.my_lang), null)

            if (langString != null) {
                return langString
            }

            return Locale.getDefault().language
        }

        private fun persistLanguage(c: Context, language: String) {
            val settings = c.getSharedPreferences(
                c.getString(R.string.my_preference),
                AppCompatActivity.MODE_PRIVATE
            )

            val editor: SharedPreferences.Editor = settings.edit()
            editor.putString(c.getString(R.string.my_lang), language)
            editor.apply()
        }

        private fun updateResources(c: Context, language: String) {

            val locale = Locale(language)
            Locale.setDefault(locale)

            val newConfig = c.resources.configuration
            newConfig.setLocale(locale)
            //val newContext = c.createConfigurationContext(newConfig)
            c.resources.updateConfiguration(newConfig, c.resources.displayMetrics)

        }
    }
}