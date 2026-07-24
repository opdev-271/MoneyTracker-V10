package com.moneytracker.app.ui.wallet;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.moneytracker.app.R;
import com.moneytracker.app.data.relation.AccountWithBalance;
import com.moneytracker.app.ui.widget.ColorSwatchRow;
import com.moneytracker.app.util.AccountIcons;
import com.moneytracker.app.util.ContrastColor;
import com.moneytracker.app.util.CurrencyFormatter;
import com.moneytracker.app.util.DisplayNameResolver;
import com.moneytracker.app.util.ViewAnimations;

public class AccountAdapter extends ListAdapter<AccountWithBalance, AccountAdapter.ViewHolder> {

    public interface OnAccountClickListener {
        void onAccountClick(AccountWithBalance item);
    }

    private final OnAccountClickListener listener;
    private final int[] highestAnimatedPosition = {-1};

    public AccountAdapter(OnAccountClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
        ViewAnimations.animateItemEntranceOnce(holder.itemView, position, highestAnimatedPosition);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final FrameLayout badgeColor;
        private final ImageView imageAccountType;
        private final TextView textName;
        private final TextView textCurrency;
        private final TextView textBalance;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            badgeColor = itemView.findViewById(R.id.badge_color);
            imageAccountType = itemView.findViewById(R.id.image_account_type);
            textName = itemView.findViewById(R.id.text_name);
            textCurrency = itemView.findViewById(R.id.text_currency);
            textBalance = itemView.findViewById(R.id.text_balance);
            ViewAnimations.addPressFeedback(itemView);
        }

        void bind(AccountWithBalance item, OnAccountClickListener listener) {
            textName.setText(DisplayNameResolver.resolve(itemView.getContext(), item.account.getName()));
            textCurrency.setText(item.account.getCurrencyCode());
            textBalance.setText(CurrencyFormatter.format(item.balance, item.account.getCurrencyCode()));
            imageAccountType.setImageResource(AccountIcons.resolve(item.account.getAccountType()));

            int color = item.account.getColor() != 0 ? item.account.getColor() : ColorSwatchRow.DEFAULT_COLORS[0];
            GradientDrawable badge = (GradientDrawable) badgeColor.getBackground().mutate();
            badge.setColor(color);
            imageAccountType.setColorFilter(ContrastColor.forBackground(color));

            itemView.setOnClickListener(v -> listener.onAccountClick(item));
        }
    }

    private static final DiffUtil.ItemCallback<AccountWithBalance> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<AccountWithBalance>() {
                @Override
                public boolean areItemsTheSame(@NonNull AccountWithBalance oldItem, @NonNull AccountWithBalance newItem) {
                    return oldItem.account.getId() == newItem.account.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull AccountWithBalance oldItem, @NonNull AccountWithBalance newItem) {
                    return oldItem.account.getName().equals(newItem.account.getName())
                            && oldItem.account.getCurrencyCode().equals(newItem.account.getCurrencyCode())
                            && oldItem.account.getAccountType().equals(newItem.account.getAccountType())
                            && oldItem.account.getColor() == newItem.account.getColor()
                            && oldItem.balance == newItem.balance;
                }
            };
}
