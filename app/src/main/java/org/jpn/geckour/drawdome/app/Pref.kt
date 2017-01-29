package org.jpn.geckour.drawdome.app

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.EditTextPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment

class Pref : PreferenceActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // version3.0以前
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            //設定画面を追加
            addPreferencesFromResource(R.xml.preferences)
            //preferences.xml内のmin_vertexが変更されたかをListen
            val MinVertexChangeListener = Preference.OnPreferenceChangeListener { preference, newValue -> isMinVertexChanged(preference, newValue) }
            //上記リスナーを登録
            val editTextMinVertex = findPreference("min_vertices") as EditTextPreference
            editTextMinVertex.onPreferenceChangeListener = MinVertexChangeListener
            editTextMinVertex.summary = editTextMinVertex.text

            //preferences.xml内のmax_vertexが変更されたかをListen
            val MaxVertexChangeListener = Preference.OnPreferenceChangeListener { preference, newValue -> isMaxVertexChanged(preference, newValue) }
            //上記リスナーを登録
            val editTextMaxVertex = findPreference("max_vertices") as EditTextPreference
            editTextMaxVertex.onPreferenceChangeListener = MaxVertexChangeListener
            editTextMaxVertex.summary = editTextMaxVertex.text
            // version3.0以降
        } else {
            fragmentManager.beginTransaction().replace(android.R.id.content, PrefFragment()).commit()
        }
    }

    fun isMinVertexChanged(preference: Preference, newValue: Any): Boolean {
        preference.summary = newValue as String
        val intent = Intent()
        intent.putExtra("min_vertices", "" + newValue)
        setResult(Activity.RESULT_OK, intent)

        return true
    }

    fun isMaxVertexChanged(preference: Preference, newValue: Any): Boolean {
        preference.summary = newValue as String
        val intent = Intent()
        intent.putExtra("max_vertices", "" + newValue)
        setResult(Activity.RESULT_OK, intent)

        return true
    }

    class PrefFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            //設定画面を追加
            addPreferencesFromResource(R.xml.preferences)
            //preferences.xml内のmin_vertexが変更されたかをListen
            val MinVertexChangeListener = Preference.OnPreferenceChangeListener { preference, newValue -> (activity as Pref).isMinVertexChanged(preference, newValue) }
            //上記リスナーを登録
            val editTextMinVertex = findPreference("min_vertices") as EditTextPreference
            editTextMinVertex.onPreferenceChangeListener = MinVertexChangeListener
            editTextMinVertex.summary = editTextMinVertex.text

            //preferences.xml内のmax_vertexが変更されたかをListen
            val MaxVertexChangeListener = Preference.OnPreferenceChangeListener { preference, newValue -> (activity as Pref).isMaxVertexChanged(preference, newValue) }
            //上記リスナーを登録
            val editTextMaxVertex = findPreference("max_vertices") as EditTextPreference
            editTextMaxVertex.onPreferenceChangeListener = MaxVertexChangeListener
            editTextMaxVertex.summary = editTextMaxVertex.text
        }
    }
}
