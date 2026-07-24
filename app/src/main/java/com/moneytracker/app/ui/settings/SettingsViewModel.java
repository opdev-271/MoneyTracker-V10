package com.moneytracker.app.ui.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.moneytracker.app.util.LocaleHelper;
import com.moneytracker.app.util.SettingsPrefs;
import com.moneytracker.app.util.ThemePrefs;

public class SettingsViewModel extends AndroidViewModel {

    private final MutableLiveData<String> language;
    private final MutableLiveData<Boolean> hideBalance;
    private final MutableLiveData<Boolean> colorCodeTransactions;
    private final MutableLiveData<Boolean> animationsEnabled;
    private final MutableLiveData<String> theme;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        language = new MutableLiveData<>(LocaleHelper.getPersistedLanguage(application));
        hideBalance = new MutableLiveData<>(SettingsPrefs.isBalanceHidden(application));
        colorCodeTransactions = new MutableLiveData<>(SettingsPrefs.isColorCodeTransactions(application));
        animationsEnabled = new MutableLiveData<>(SettingsPrefs.isAnimationsEnabled(application));
        theme = new MutableLiveData<>(ThemePrefs.getThemeMode(application));
    }

    public LiveData<String> getLanguage() {
        return language;
    }

    public LiveData<Boolean> getHideBalance() {
        return hideBalance;
    }

    public LiveData<String> getTheme() {
        return theme;
    }

    public void setTheme(String mode) {
        if (mode.equals(theme.getValue())) {
            return;
        }
        ThemePrefs.setThemeMode(getApplication(), mode);
        theme.setValue(mode);
    }

    /** Returns true if the language actually changed (caller should recreate the Activity). */
    public boolean setLanguage(String newLanguage) {
        if (newLanguage.equals(language.getValue())) {
            return false;
        }
        LocaleHelper.persistLanguage(getApplication(), newLanguage);
        language.setValue(newLanguage);
        return true;
    }

    public void setHideBalance(boolean hidden) {
        SettingsPrefs.setBalanceHidden(getApplication(), hidden);
        hideBalance.setValue(hidden);
    }

    public LiveData<Boolean> getColorCodeTransactions() {
        return colorCodeTransactions;
    }

    public void setColorCodeTransactions(boolean enabled) {
        SettingsPrefs.setColorCodeTransactions(getApplication(), enabled);
        colorCodeTransactions.setValue(enabled);
    }

    public LiveData<Boolean> getAnimationsEnabled() {
        return animationsEnabled;
    }

    public void setAnimationsEnabled(boolean enabled) {
        SettingsPrefs.setAnimationsEnabled(getApplication(), enabled);
        animationsEnabled.setValue(enabled);
    }
}
