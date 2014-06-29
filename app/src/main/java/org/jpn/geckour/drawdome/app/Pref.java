package org.jpn.geckour.drawdome.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class Pref extends PreferenceActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // version3.0以前
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            //設定画面を追加
            addPreferencesFromResource(R.xml.preferences);
            //preferences.xml内のmin_vertexが変更されたかをListen
            final Preference.OnPreferenceChangeListener MinVertexChangeListener = new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    return isMinVertexChanged(preference, newValue);
                }
            };
            //上記リスナーを登録
            EditTextPreference editTextMinVertex = (EditTextPreference) findPreference("min_vertices");
            editTextMinVertex.setOnPreferenceChangeListener(MinVertexChangeListener);
            editTextMinVertex.setSummary(editTextMinVertex.getText());

            //preferences.xml内のmax_vertexが変更されたかをListen
            final Preference.OnPreferenceChangeListener MaxVertexChangeListener = new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    return isMaxVertexChanged(preference, newValue);
                }
            };
            //上記リスナーを登録
            EditTextPreference editTextMaxVertex = (EditTextPreference) findPreference("max_vertices");
            editTextMaxVertex.setOnPreferenceChangeListener(MaxVertexChangeListener);
            editTextMaxVertex.setSummary(editTextMaxVertex.getText());
        // version3.0以降
        } else {
            getFragmentManager().beginTransaction().replace(android.R.id.content, new prefFragment()).commit();
        }
    }

    public boolean isMinVertexChanged (Preference preference, Object newValue) {
        preference.setSummary((String) newValue);
        Intent intent = new Intent();
        intent.putExtra("min_vertices", "" + newValue);
        setResult(Activity.RESULT_OK, intent);

        return true;
    }

    public boolean isMaxVertexChanged (Preference preference, Object newValue) {
        preference.setSummary((String) newValue);
        Intent intent = new Intent();
        intent.putExtra("max_vertices", "" + newValue);
        setResult(Activity.RESULT_OK, intent);

        return true;
    }

    public class prefFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //設定画面を追加
            addPreferencesFromResource(R.xml.preferences);
            //preferences.xml内のmin_vertexが変更されたかをListen
            final Preference.OnPreferenceChangeListener MinVertexChangeListener = new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    return isMinVertexChanged(preference, newValue);
                }
            };
            //上記リスナーを登録
            EditTextPreference editTextMinVertex = (EditTextPreference) findPreference("min_vertices");
            editTextMinVertex.setOnPreferenceChangeListener(MinVertexChangeListener);
            editTextMinVertex.setSummary(editTextMinVertex.getText());

            //preferences.xml内のmax_vertexが変更されたかをListen
            final Preference.OnPreferenceChangeListener MaxVertexChangeListener = new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    return isMaxVertexChanged(preference, newValue);
                }
            };
            //上記リスナーを登録
            EditTextPreference editTextMaxVertex = (EditTextPreference) findPreference("max_vertices");
            editTextMaxVertex.setOnPreferenceChangeListener(MaxVertexChangeListener);
            editTextMaxVertex.setSummary(editTextMaxVertex.getText());
        }
    }
}
