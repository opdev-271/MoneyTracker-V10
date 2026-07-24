package com.moneytracker.app.ui.alltransactions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.moneytracker.app.R;
import com.moneytracker.app.databinding.ActivityAllTransactionsBinding;
import com.moneytracker.app.util.LocaleHelper;

import java.util.ArrayList;
import java.util.List;

public class AllTransactionsActivity extends AppCompatActivity {

    private static final String EXTRA_ACCOUNT_IDS = "extra_account_ids";

    public static Intent newIntent(Context context, List<Long> accountIds) {
        long[] ids = new long[accountIds.size()];
        for (int i = 0; i < accountIds.size(); i++) {
            ids[i] = accountIds.get(i);
        }
        Intent intent = new Intent(context, AllTransactionsActivity.class);
        intent.putExtra(EXTRA_ACCOUNT_IDS, ids);
        return intent;
    }

    private ActivityAllTransactionsBinding binding;
    private AllTransactionsAdapter adapter;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyStoredLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_up_in, R.anim.stay);
        binding = ActivityAllTransactionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new AllTransactionsAdapter();
        binding.recyclerTransactions.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTransactions.setAdapter(adapter);

        binding.btnClose.setOnClickListener(v -> closeWithSlideDown());

        long[] ids = getIntent().getLongArrayExtra(EXTRA_ACCOUNT_IDS);
        List<Long> accountIds = new ArrayList<>();
        if (ids != null) {
            for (long id : ids) {
                accountIds.add(id);
            }
        }

        AllTransactionsViewModel viewModel = new ViewModelProvider(this).get(AllTransactionsViewModel.class);
        viewModel.setAccountIds(accountIds);
        viewModel.getTransactions().observe(this, transactions -> {
            adapter.submitList(AllTransactionsAdapter.buildItems(transactions));
            boolean empty = transactions == null || transactions.isEmpty();
            binding.textEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.recyclerTransactions.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    private void closeWithSlideDown() {
        finish();
        overridePendingTransition(R.anim.stay, R.anim.slide_down_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stay, R.anim.slide_down_out);
    }
}
