package com.example.blockwatch;

/**
 * Created by Michael on 3/22/2017.
 */

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * An activity that presents a set of application settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class Preferences extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout); // Set the main activity

        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar); // Get the settings toolbar ID
        setSupportActionBar(toolbar); // Set the toolbar

        getFragmentManager().beginTransaction()
                .replace(R.id.settings_content, new Prefs1Fragment()).commit();
    }

    /**
     * This fragment shows the preferences for the first header.
     */
    public static class Prefs1Fragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Make sure default values are applied.  In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            PreferenceManager.setDefaultValues(getActivity(),
                    R.xml.advanced_preferences, false);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference_layout);
        }
    }
}

