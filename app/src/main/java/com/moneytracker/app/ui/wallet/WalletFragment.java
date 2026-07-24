package com.moneytracker.app.ui.wallet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.moneytracker.app.databinding.FragmentWalletBinding;
import com.moneytracker.app.util.DisplayNameResolver;
import com.moneytracker.app.util.ViewAnimations;

public class WalletFragment extends Fragment {

    private FragmentWalletBinding binding;
    private WalletViewModel viewModel;
    private AccountAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentWalletBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(WalletViewModel.class);
        ViewAnimations.fadeInContent(view);

        adapter = new AccountAdapter(item -> {
            String resolvedName = DisplayNameResolver.resolve(requireContext(), item.account.getName());
            AddEditAccountDialogFragment.newInstanceForEdit(item.account, resolvedName)
                    .show(getChildFragmentManager(), "edit_account");
        });
        binding.recyclerAccounts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerAccounts.setAdapter(adapter);

        binding.btnAddAccount.setOnClickListener(v ->
                AddEditAccountDialogFragment.newInstanceForAdd().show(getChildFragmentManager(), "add_account"));
        ViewAnimations.addPressFeedback(binding.btnAddAccount);

        viewModel.getAccountsWithBalance().observe(getViewLifecycleOwner(), accounts -> {
            adapter.submitList(accounts);
            boolean empty = accounts == null || accounts.isEmpty();
            binding.textEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.recyclerAccounts.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
