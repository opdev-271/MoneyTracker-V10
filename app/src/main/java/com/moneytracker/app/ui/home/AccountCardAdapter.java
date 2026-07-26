package com.moneytracker.app.ui.home;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
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

/**
 * Horizontal "My Cards" carousel on Home: each account rendered as a bank-card
 * shaped tile filled with the account's color, with black/white content picked
 * for contrast. Honors the "hide balances" toggle the same way the main
 * balance figure does.
 */
public class AccountCardAdapter extends ListAdapter<AccountWithBalance, AccountCardAdapter.ViewHolder> {

    private static final String HIDDEN_BALANCE_TEXT = "••••••";

    public interface OnAccountCardClickListener {
        void onAccountCardClick(AccountWithBalance item);
    }

    private final OnAccountCardClickListener listener;
    private final int[] highestAnimatedPosition = {-1};
    private boolean balancesHidden;

    public AccountCardAdapter(OnAccountCardClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public void setBalancesHidden(boolean hidden) {
        if (balancesHidden != hidden) {
            balancesHidden = hidden;
            notifyItemRangeChanged(0, getItemCount());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener, balancesHidden);
        ViewAnimations.animateItemEntranceOnce(holder.itemView, position, highestAnimatedPosition);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageAccountType;
        private final TextView textCurrency;
        private final TextView textName;
        private final TextView textBalance;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageAccountType = itemView.findViewById(R.id.image_account_type);
            textCurrency = itemView.findViewById(R.id.text_currency);
            textName = itemView.findViewById(R.id.text_name);
            textBalance = itemView.findViewById(R.id.text_balance);
            ViewAnimations.addPressFeedback(itemView);
        }

        void bind(AccountWithBalance item, OnAccountCardClickListener listener, boolean balancesHidden) {
            int color = item.account.getColor() != 0 ? item.account.getColor() : ColorSwatchRow.DEFAULT_COLORS[1];
            GradientDrawable card = (GradientDrawable) itemView.getBackground().mutate();
            card.setColor(color);

            int onCard = ContrastColor.forBackground(color);
            int onCardMuted = ColorUtils.setAlphaComponent(onCard, 170);

            imageAccountType.setImageResource(AccountIcons.resolve(item.account.getAccountType()));
            imageAccountType.setColorFilter(onCard);
            textCurrency.setText(item.account.getCurrencyCode());
            textCurrency.setTextColor(onCardMuted);
            textName.setText(DisplayNameResolver.resolve(itemView.getContext(), item.account.getName()));
            textName.setTextColor(onCardMuted);
            textBalance.setText(balancesHidden ? HIDDEN_BALANCE_TEXT
                    : CurrencyFormatter.format(item.balance, item.account.getCurrencyCode()));
            textBalance.setTextColor(onCard);

            itemView.setOnClickListener(v -> listener.onAccountCardClick(item));
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
