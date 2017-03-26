package com.example.blockwatch;

/**
 * Created by Michael on 3/22/2017.
 */

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.thebluealliance.spectrum.SpectrumPreferenceCompat;

import static com.example.blockwatch.R.string.hour_one_color;

/**
 * An activity that presents a set of application settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class Preferences extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartScreenCallback {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout); // Set the main activity

        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar); // Get the settings toolbar ID
        setSupportActionBar(toolbar); // Set the toolbar
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (savedInstanceState == null) {
            // Create the fragment only when the activity is created for the first time.
            // ie. not after orientation changes
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(Prefs1Fragment.PREF_FRAGMENT_TAG);
            if (fragment == null) {
                fragment = new Prefs1Fragment();
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.settings_content, fragment, Prefs1Fragment.PREF_FRAGMENT_TAG).commit();
        }

    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat preferenceFragmentCompat,
                                           PreferenceScreen preferenceScreen) {
        Prefs1Fragment fragment = new Prefs1Fragment();
        Bundle args = new Bundle();
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.getKey());
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.settings_content, fragment, preferenceScreen.getKey())
         .addToBackStack(preferenceScreen.getKey())
         .commit();

        return true;
    }

    /**
     * This fragment shows the preferences for the first header.
     */
    public static class Prefs1Fragment extends PreferenceFragmentCompat {
        static final String PREF_FRAGMENT_TAG = "preference_fragment";
        private final Preference.OnPreferenceClickListener KeepColorsSameListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                CheckBoxPreference keepColorsSame = (CheckBoxPreference) getPreferenceManager().findPreference(
                        getString(R.string.keep_colors_same_key));
                int hourOneColor = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(getResources().getString(hour_one_color), ContextCompat.getColor(getContext(),R.color.red));
                FlipVisibilityOfColors();
                if(keepColorsSame.isChecked()) {
                    MakeColorsTheSame(hourOneColor);
                }
                Toast.makeText(getContext(), "Some text", Toast.LENGTH_SHORT).show();
                return true;
            }
        };

        private final Preference.OnPreferenceChangeListener ColorChangeListener = new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                CheckBoxPreference keepColorsSame = (CheckBoxPreference) getPreferenceManager().findPreference(
                        getString(R.string.keep_colors_same_key));
                if (keepColorsSame.isChecked()) {
                    MakeColorsTheSame((int) newValue);
                }
                return true;
            }
        };

        private void MakeColorsTheSame(int newValue) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(getString(R.string.hour_two_color), newValue);
            editor.putInt(getString(R.string.minute_one_color), newValue);
            editor.putInt(getString(R.string.minute_two_color), newValue);
            editor.apply();
        }

        private void FlipVisibilityOfColors() {
            final Preference SecondHourColor = getPreferenceManager().findPreference(
                    getString(R.string.hour_two_color));
            final SpectrumPreferenceCompat FirstMinuteColor = (SpectrumPreferenceCompat) getPreferenceManager().findPreference(
                    getString(R.string.minute_one_color));
            final SpectrumPreferenceCompat SecondMinuteColor = (SpectrumPreferenceCompat) getPreferenceManager().findPreference(
                    getString(R.string.minute_two_color));
            SecondHourColor.setVisible(!SecondHourColor.isVisible()); // Flip the visibility of the view
            FirstMinuteColor.setVisible(!FirstMinuteColor.isVisible()); // Flip the visibility of the view
            SecondMinuteColor.setVisible(!SecondMinuteColor.isVisible()); // Flip the visibility of the view
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // Make sure default values are applied.
            PreferenceManager.setDefaultValues(getActivity(),
                    R.xml.advanced_preferences, false);
            setPreferencesFromResource(R.xml.preference_layout, rootKey);
            CheckBoxPreference keepColorsSame = (CheckBoxPreference) getPreferenceManager().findPreference(
                    getString(R.string.keep_colors_same_key));
            if(keepColorsSame != null){
                keepColorsSame.setOnPreferenceClickListener(KeepColorsSameListener);
                if(keepColorsSame.isChecked()){ // Make sure to make the other colors invisible in case you are coming back to the preference screen after it was checked already
                    final Preference SecondHourColor = getPreferenceManager().findPreference(
                            getString(R.string.hour_two_color));
                    final SpectrumPreferenceCompat FirstMinuteColor = (SpectrumPreferenceCompat) getPreferenceManager().findPreference(
                            getString(R.string.minute_one_color));
                    final SpectrumPreferenceCompat SecondMinuteColor = (SpectrumPreferenceCompat) getPreferenceManager().findPreference(
                            getString(R.string.minute_two_color));
                    SecondHourColor.setVisible(false); // Make invisible
                    FirstMinuteColor.setVisible(false); // Make invisible
                    SecondMinuteColor.setVisible(false); // Make invisible
                }
            }
            Preference firstHourColor = getPreferenceManager().findPreference(
                    getString(R.string.hour_one_color));
            if(firstHourColor != null){
                firstHourColor.setOnPreferenceChangeListener(ColorChangeListener);
            }
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            if (!SpectrumPreferenceCompat.onDisplayPreferenceDialog(preference, this)) {
                super.onDisplayPreferenceDialog(preference);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.getSupportActionBar().setTitle("Preferences");
    }
}

