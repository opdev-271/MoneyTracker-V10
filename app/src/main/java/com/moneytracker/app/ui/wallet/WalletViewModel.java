package com.moneytracker.app.ui.wallet;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.moneytracker.app.MoneyTrackerApp;
import com.moneytracker.app.data.entity.Account;
import com.moneytracker.app.data.relation.AccountWithBalance;
import com.moneytracker.app.data.repository.AccountRepository;
import com.moneytracker.app.util.SettingsPrefs;

import java.util.List;

public class WalletViewModel extends AndroidViewModel {

    private final AccountRepository accountRepository;
    private final long activeProfileId;
    private final LiveData<List<AccountWithBalance>> accountsWithBalance;

    public WalletViewModel(@NonNull Application application) {
        super(application);
        accountRepository = MoneyTrackerApp.from(application).getAccountRepository();
        activeProfileId = SettingsPrefs.getActiveProfileId(application);
        accountsWithBalance = accountRepository.getAccountsWithBalanceForProfile(activeProfileId);
    }

    public LiveData<List<AccountWithBalance>> getAccountsWithBalance() {
        return accountsWithBalance;
    }

    public void saveAccount(Account account) {
        if (account.getId() == 0) {
            accountRepository.insert(account, null);
        } else {
            accountRepository.update(account);
        }
    }

    public void deleteAccount(long accountId) {
        Account account = new Account();
        account.setId(accountId);
        accountRepository.delete(account);
    }
}
