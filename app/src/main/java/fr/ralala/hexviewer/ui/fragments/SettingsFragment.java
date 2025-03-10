package fr.ralala.hexviewer.ui.fragments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.CheckBox;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import fr.ralala.hexviewer.BuildConfig;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.SettingsKeys;
import fr.ralala.hexviewer.ui.activities.settings.SettingsActivity;
import fr.ralala.hexviewer.ui.activities.settings.SettingsListsLandscapeActivity;
import fr.ralala.hexviewer.ui.activities.settings.SettingsListsPortraitActivity;
import fr.ralala.hexviewer.ui.utils.UIHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Settings fragments
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class SettingsFragment extends AbstractSettingsFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
  private static final String GITHUB_URL = "https://github.com/Keidan/HexViewer";
  private static final String GITHUB_LIC_URL = GITHUB_URL + "/blob/master/license.txt";
  protected Preference mSettingsListsPortrait;
  protected Preference mSettingsListsLandscape;
  protected Preference mLicense;
  protected Preference mVersion;
  private Preference mRestoreDefault;
  private ListPreference mLanguage;
  private ListPreference mScreenOrientation;
  private ListPreference mNbBytesPerLine;

  public SettingsFragment(AppCompatActivity owner) {
    super(owner);
  }

  /**
   * Called during {@link #onCreate(Bundle)} to supply the preferences for this fragment.
   * Subclasses are expected to call {@link #setPreferenceScreen(PreferenceScreen)} either
   * directly or via helper methods such as {@link #addPreferencesFromResource(int)}.
   *
   * @param savedInstanceState If the fragment is being re-created from a previous saved state,
   *                           this is the state.
   * @param rootKey            If non-null, this preference fragment should be rooted at the
   *                           {@link PreferenceScreen} with this key.
   */
  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.preferences, rootKey);

    mSettingsListsPortrait = findPreference(SettingsKeys.CFG_LISTS_PORTRAIT);
    mSettingsListsLandscape = findPreference(SettingsKeys.CFG_LISTS_LANDSCAPE);
    mLicense = findPreference(SettingsKeys.CFG_LICENSE);
    mVersion = findPreference(SettingsKeys.CFG_VERSION);
    mLanguage = findPreference(SettingsKeys.CFG_LANGUAGE);
    mScreenOrientation = findPreference(SettingsKeys.CFG_SCREEN_ORIENTATION);
    mNbBytesPerLine = findPreference(SettingsKeys.CFG_NB_BYTES_PER_LINE);
    mRestoreDefault = findPreference(SettingsKeys.CFG_RESTORE_DEFAULT);

    mSettingsListsPortrait.setOnPreferenceClickListener(this);
    mSettingsListsLandscape.setOnPreferenceClickListener(this);
    mLicense.setOnPreferenceClickListener(this);
    mVersion.setOnPreferenceClickListener(this);
    mLanguage.setOnPreferenceChangeListener(this);
    mScreenOrientation.setOnPreferenceChangeListener(this);
    mNbBytesPerLine.setOnPreferenceChangeListener(this);
    mRestoreDefault.setOnPreferenceClickListener(this);

    mVersion.setSummary(BuildConfig.VERSION_NAME);

    mLanguage.setDefaultValue(mApp.getApplicationLanguage(getContext()));
    mScreenOrientation.setDefaultValue(mApp.getScreenOrientationStr());
    mScreenOrientation.setValue(mApp.getScreenOrientationStr());
    mNbBytesPerLine.setDefaultValue("" + mApp.getNbBytesPerLine());

    refreshUiAccordingToOrientation(null);

  }

  /**
   * Refreshes the user interface according to the screen orientation.
   *
   * @param ref The reference value, if it is null, the value stored in the parameters will be used.
   */
  private void refreshUiAccordingToOrientation(String ref) {
    int orientation = mApp.getScreenOrientation(ref);
    boolean landscapeEnable;
    boolean portraitEnable;
    if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
      landscapeEnable = true;
      portraitEnable = false;
    } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
      landscapeEnable = false;
      portraitEnable = true;
    } else {
      landscapeEnable = true;
      portraitEnable = true;
    }
    mSettingsListsLandscape.setEnabled(landscapeEnable);
    mSettingsListsPortrait.setEnabled(portraitEnable);
  }


  /**
   * Called when a preference has been clicked.
   *
   * @param preference The preference that was clicked
   * @return {@code true} if the click was handled
   */
  @Override
  public boolean onPreferenceClick(Preference preference) {
    if (preference.equals(mLicense)) {
      Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_LIC_URL));
      startActivity(browserIntent);
    } else if (preference.equals(mVersion)) {
      Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL));
      startActivity(browserIntent);
    } else if (preference.equals(mSettingsListsPortrait)) {
      SettingsListsPortraitActivity.startActivity(mActivity);
    } else if (preference.equals(mSettingsListsLandscape)) {
      SettingsListsLandscapeActivity.startActivity(mActivity);
    } else if (preference.equals(mRestoreDefault)) {
      if (!((SettingsActivity) mActivity).isChanged()) {
        restoreDefaultDialog();
      } else {
        UIHelper.showErrorDialog(mActivity, preference.getTitle(), mActivity.getString(R.string.control_language_change));
      }
    }
    return false;
  }

  /**
   * Called when a preference has been changed.
   *
   * @param preference The preference that was clicked
   * @param newValue   The new value.
   * @return {@code true} if the click was handled
   */
  @Override
  public boolean onPreferenceChange(Preference preference, Object newValue) {
    if (preference.equals(mLanguage)) {
      if (!((SettingsActivity) mActivity).isChanged()) {
        mApp.setApplicationLanguage("" + newValue);
        mActivity.finish();
        return true;
      } else {
        UIHelper.showErrorDialog(mActivity, preference.getTitle(), mActivity.getString(R.string.control_language_change));
      }
    } else if (preference.equals(mScreenOrientation)) {
      refreshUiAccordingToOrientation("" + newValue);
      return true;
    } else if (preference.equals(mNbBytesPerLine)) {
      if (!((SettingsActivity) mActivity).isOpen()) {
        mApp.setNbBytesPerLine("" + newValue);
        return true;
      } else {
        UIHelper.showErrorDialog(mActivity, preference.getTitle(), mActivity.getString(R.string.error_file_open));
      }
    }
    return false;
  }

  @SuppressLint("InflateParams")
  private void restoreDefaultDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
    builder.setCancelable(false)
        .setTitle(R.string.dialog_restore_title)
        .setPositiveButton(android.R.string.ok, null)
        .setNegativeButton(android.R.string.cancel, (dialog, whichButton) -> {
        });
    LayoutInflater factory = LayoutInflater.from(mActivity);
    builder.setView(factory.inflate(R.layout.content_dialog_restore, null));
    final AlertDialog dialog = builder.create();
    dialog.show();
    final CheckBox cb = dialog.findViewById(R.id.deleteRecent);
    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
      mApp.loadDefaultValues(cb != null && cb.isChecked());
      mActivity.finish();
      dialog.dismiss();
    });
  }
}