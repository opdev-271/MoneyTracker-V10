package com.moneytracker.app;

import android.app.Application;
import android.content.Context;

import com.moneytracker.app.data.AppDatabase;
import com.moneytracker.app.data.repository.AccountRepository;
import com.moneytracker.app.data.repository.CategoryRepository;
import com.moneytracker.app.data.repository.ProfileRepository;
import com.moneytracker.app.data.repository.TransactionRepository;
import com.moneytracker.app.util.AppExecutors;
import com.moneytracker.app.util.LocaleHelper;
import com.moneytracker.app.util.ThemePrefs;

/** Holds app-wide singletons (DB + repositories) so every ViewModel can reach them cheaply. */
public class MoneyTrackerApp extends Application {

    private AccountRepository accountRepository;
    private CategoryRepository categoryRepository;
    private TransactionRepository transactionRepository;
    private ProfileRepository profileRepository;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.applyStoredLocale(base));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ThemePrefs.applyStoredThemeMode(this);
        AppDatabase database = AppDatabase.getInstance(this);
        AppExecutors executors = AppExecutors.getInstance();
        accountRepository = new AccountRepository(database, executors);
        categoryRepository = new CategoryRepository(database, executors);
        transactionRepository = new TransactionRepository(database, executors);
        profileRepository = new ProfileRepository(database, executors);
    }

    public AccountRepository getAccountRepository() {
        return accountRepository;
    }

    public CategoryRepository getCategoryRepository() {
        return categoryRepository;
    }

    public TransactionRepository getTransactionRepository() {
        return transactionRepository;
    }

    public ProfileRepository getProfileRepository() {
        return profileRepository;
    }

    public static MoneyTrackerApp from(Context context) {
        return (MoneyTrackerApp) context.getApplicationContext();
    }
}
