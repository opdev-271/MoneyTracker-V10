package com.moneytracker.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Small app-wide flags that live outside Room (language is handled by LocaleHelper). */
public class SettingsPrefs {

    private static final String PREFS_NAME = "settings";
    private static final String KEY_HIDE_BALANCE = "hide_balance";
    private static final String KEY_ACTIVE_ACCOUNT_ID = "active_account_id";
    private static final String KEY_ACTIVE_PROFILE_ID = "active_profile_id";
    private static final String KEY_COLOR_CODE_TRANSACTIONS = "color_code_transactions";
    private static final String KEY_BALANCE_ACCOUNTS_PREFIX = "balance_accounts_";
    private static final String BALANCE_ACCOUNTS_ALL = "ALL";

    /** The id of the very first profile ever created (see OnboardingActivity). */
    public static final long DEFAULT_PROFILE_ID = 1L;

    private SettingsPrefs() {
    }

    public static boolean isBalanceHidden(Context context) {
        return prefs(context).getBoolean(KEY_HIDE_BALANCE, false);
    }

    public static void setBalanceHidden(Context context, boolean hidden) {
        prefs(context).edit().putBoolean(KEY_HIDE_BALANCE, hidden).apply();
    }

    public static long getActiveAccountId(Context context) {
        return prefs(context).getLong(KEY_ACTIVE_ACCOUNT_ID, -1L);
    }

    public static void setActiveAccountId(Context context, long accountId) {
        prefs(context).edit().putLong(KEY_ACTIVE_ACCOUNT_ID, accountId).apply();
    }

    public static long getActiveProfileId(Context context) {
        return prefs(context).getLong(KEY_ACTIVE_PROFILE_ID, DEFAULT_PROFILE_ID);
    }

    public static void setActiveProfileId(Context context, long profileId) {
        prefs(context).edit().putLong(KEY_ACTIVE_PROFILE_ID, profileId).apply();
    }

    public static boolean isColorCodeTransactions(Context context) {
        return prefs(context).getBoolean(KEY_COLOR_CODE_TRANSACTIONS, true);
    }

    public static void setColorCodeTransactions(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_COLOR_CODE_TRANSACTIONS, enabled).apply();
    }

    /**
     * Which of a profile's accounts should be summed into the Home balance card.
     * {@code null} means "all accounts" (also the default before the user picks anything).
     */
    public static Set<Long> getBalanceAccountSelection(Context context, long profileId) {
        String stored = prefs(context).getString(KEY_BALANCE_ACCOUNTS_PREFIX + profileId, BALANCE_ACCOUNTS_ALL);
        if (BALANCE_ACCOUNTS_ALL.equals(stored) || stored.isEmpty()) {
            return null;
        }
        Set<Long> ids = new HashSet<>();
        for (String part : stored.split(",")) {
            try {
                ids.add(Long.parseLong(part));
            } catch (NumberFormatException ignored) {
                // skip malformed entry
            }
        }
        return ids;
    }

    /** Pass {@code null} (or a set covering every account) to mean "all accounts". */
    public static void setBalanceAccountSelection(Context context, long profileId, Set<Long> accountIds) {
        String value;
        if (accountIds == null) {
            value = BALANCE_ACCOUNTS_ALL;
        } else {
            List<String> parts = new ArrayList<>();
            for (Long id : accountIds) {
                parts.add(String.valueOf(id));
            }
            value = TextUtils.join(",", parts);
        }
        prefs(context).edit().putString(KEY_BALANCE_ACCOUNTS_PREFIX + profileId, value).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
