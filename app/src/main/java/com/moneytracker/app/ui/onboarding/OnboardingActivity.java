package com.moneytracker.app.ui.onboarding;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.moneytracker.app.MoneyTrackerApp;
import com.moneytracker.app.R;
import com.moneytracker.app.data.entity.Account;
import com.moneytracker.app.data.entity.Profile;
import com.moneytracker.app.databinding.ActivityOnboardingBinding;
import com.moneytracker.app.ui.MainActivity;
import com.moneytracker.app.ui.widget.IconGridAdapter;
import com.moneytracker.app.ui.widget.SegmentedToggle;
import com.moneytracker.app.util.LocaleHelper;
import com.moneytracker.app.util.OnboardingPrefs;
import com.moneytracker.app.util.ProfileIcons;
import com.moneytracker.app.util.SettingsPrefs;
import com.moneytracker.app.util.ThemePrefs;

/**
 * Shown once, on first launch. Answers are only committed (locale/theme applied,
 * onboarding marked done) when the user taps "Get started" — applying the theme
 * live on each tap would call AppCompatDelegate.setDefaultNightMode(), which
 * recreates every running AppCompatActivity including this one, wiping the form.
 */
public class OnboardingActivity extends AppCompatActivity {

    private static final String[] CURRENCIES = {"CZK", "EUR", "USD", "GBP"};

    private ActivityOnboardingBinding binding;
    private String selectedLanguage;
    private String selectedTheme = ThemePrefs.THEME_SYSTEM;
    private String selectedCurrency = "CZK";
    private String selectedPictogram = "pfp_cat";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyStoredLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        selectedLanguage = LocaleHelper.getPersistedLanguage(this);

        int selectedColor = getColor(R.color.on_accent_text);
        int unselectedColor = getColor(R.color.text_secondary);

        IconGridAdapter avatarAdapter = new IconGridAdapter(ProfileIcons.allOptions(), selectedPictogram,
                getColor(R.color.icon_primary), R.drawable.bg_circle_light, R.drawable.bg_circle_light_selected);
        avatarAdapter.setOnIconSelectedListener(key -> selectedPictogram = key);
        binding.recyclerAvatar.setLayoutManager(new GridLayoutManager(this, 4));
        binding.recyclerAvatar.setAdapter(avatarAdapter);

        SegmentedToggle languageToggle = new SegmentedToggle(binding.languageContainer, binding.languageIndicator,
                new TextView[]{binding.optionEnglish, binding.optionCzech, binding.optionGerman},
                selectedColor, unselectedColor);
        languageToggle.setOnSegmentSelectedListener(index -> {
            selectedLanguage = languageForIndex(index);
            languageToggle.setSelectedIndex(index);
        });
        languageToggle.setSelectedIndex(indexForLanguage(selectedLanguage));

        SegmentedToggle themeToggle = new SegmentedToggle(binding.themeContainer, binding.themeIndicator,
                new TextView[]{binding.optionThemeLight, binding.optionThemeDark, binding.optionThemeSystem},
                selectedColor, unselectedColor);
        themeToggle.setOnSegmentSelectedListener(index -> {
            selectedTheme = themeForIndex(index);
            themeToggle.setSelectedIndex(index);
        });
        themeToggle.setSelectedIndex(indexForTheme(selectedTheme));

        SegmentedToggle currencyToggle = new SegmentedToggle(binding.currencyContainer, binding.currencyIndicator,
                new TextView[]{binding.optionCurrencyCzk, binding.optionCurrencyEur,
                        binding.optionCurrencyUsd, binding.optionCurrencyGbp},
                selectedColor, unselectedColor);
        currencyToggle.setOnSegmentSelectedListener(index -> {
            selectedCurrency = CURRENCIES[index];
            currencyToggle.setSelectedIndex(index);
        });
        currencyToggle.setSelectedIndex(0);

        binding.btnGetStarted.setOnClickListener(v -> finishOnboarding());
    }

    private void finishOnboarding() {
        String name = binding.editName.getText().toString().trim();
        OnboardingPrefs.setPreferredCurrency(this, selectedCurrency);
        LocaleHelper.persistLanguage(this, selectedLanguage);
        OnboardingPrefs.setDone(this, true);
        ThemePrefs.setThemeMode(this, selectedTheme);

        MoneyTrackerApp app = MoneyTrackerApp.from(this);
        Profile profile = new Profile();
        profile.setName(name);
        profile.setPictogram(selectedPictogram);
        profile.setSortOrder(0);
        app.getProfileRepository().insert(profile, profileId -> {
            SettingsPrefs.setActiveProfileId(this, profileId);

            Account cash = new Account();
            cash.setProfileId(profileId);
            cash.setName("account_cash");
            cash.setAccountType(Account.TYPE_CASH);
            cash.setCurrencyCode(selectedCurrency);
            cash.setInitialBalance(0);
            cash.setSortOrder(0);
            app.getAccountRepository().insert(cash, accountId -> {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            });
        });
    }

    private static String languageForIndex(int index) {
        switch (index) {
            case 1:
                return LocaleHelper.LANGUAGE_CZECH;
            case 2:
                return LocaleHelper.LANGUAGE_GERMAN;
            default:
                return LocaleHelper.LANGUAGE_ENGLISH;
        }
    }

    private static int indexForLanguage(String language) {
        if (LocaleHelper.LANGUAGE_CZECH.equals(language)) {
            return 1;
        }
        if (LocaleHelper.LANGUAGE_GERMAN.equals(language)) {
            return 2;
        }
        return 0;
    }

    private static String themeForIndex(int index) {
        switch (index) {
            case 1:
                return ThemePrefs.THEME_DARK;
            case 2:
                return ThemePrefs.THEME_SYSTEM;
            default:
                return ThemePrefs.THEME_LIGHT;
        }
    }

    private static int indexForTheme(String theme) {
        if (ThemePrefs.THEME_DARK.equals(theme)) {
            return 1;
        }
        if (ThemePrefs.THEME_SYSTEM.equals(theme)) {
            return 2;
        }
        return 0;
    }
}
