package com.moneytracker.app.ui.alltransactions;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
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
import com.moneytracker.app.data.entity.Category;
import com.moneytracker.app.data.relation.TransactionWithDetails;
import com.moneytracker.app.util.CategoryIcons;
import com.moneytracker.app.util.CurrencyFormatter;
import com.moneytracker.app.util.DisplayNameResolver;
import com.moneytracker.app.util.TransactionColors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** Flat list of date headers (day balance) + transaction rows, grouped from a date-DESC list. */
public class AllTransactionsAdapter extends ListAdapter<AllTransactionsAdapter.ListItem, RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TRANSACTION = 1;

    public AllTransactionsAdapter() {
        super(DIFF_CALLBACK);
    }

    public static List<ListItem> buildItems(List<TransactionWithDetails> transactions) {
        List<ListItem> items = new ArrayList<>();
        if (transactions == null || transactions.isEmpty()) {
            return items;
        }
        Calendar calendar = Calendar.getInstance();
        long currentDayKey = Long.MIN_VALUE;
        List<TransactionWithDetails> dayBucket = new ArrayList<>();

        for (int i = 0; i <= transactions.size(); i++) {
            long dayKey = Long.MIN_VALUE;
            TransactionWithDetails transaction = null;
            if (i < transactions.size()) {
                transaction = transactions.get(i);
                calendar.setTime(transaction.date);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                dayKey = calendar.getTimeInMillis();
            }

            boolean dayChanged = dayKey != currentDayKey;
            if (dayChanged && !dayBucket.isEmpty()) {
                flushDay(items, dayBucket);
                dayBucket = new ArrayList<>();
            }
            if (transaction == null) {
                break;
            }
            currentDayKey = dayKey;
            dayBucket.add(transaction);
        }
        return items;
    }

    private static void flushDay(List<ListItem> items, List<TransactionWithDetails> dayBucket) {
        double dayBalance = 0;
        for (TransactionWithDetails t : dayBucket) {
            dayBalance += t.getSignedAmount();
        }
        items.add(new HeaderItem(dayBucket.get(0).date, dayBalance, dayBucket.get(0).accountCurrency));
        for (TransactionWithDetails t : dayBucket) {
            items.add(new TransactionItem(t));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position) instanceof HeaderItem ? TYPE_HEADER : TYPE_TRANSACTION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_header, parent, false);
            return new HeaderViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ListItem item = getItem(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((HeaderItem) item);
        } else {
            ((TransactionViewHolder) holder).bind((TransactionItem) item);
        }
    }

    public abstract static class ListItem {
    }

    public static class HeaderItem extends ListItem {
        final java.util.Date date;
        final double dayBalance;
        final String currencyCode;

        HeaderItem(java.util.Date date, double dayBalance, String currencyCode) {
            this.date = date;
            this.dayBalance = dayBalance;
            this.currencyCode = currencyCode;
        }
    }

    public static class TransactionItem extends ListItem {
        final TransactionWithDetails transaction;

        TransactionItem(TransactionWithDetails transaction) {
            this.transaction = transaction;
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView textDate;
        private final TextView textDayBalance;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.text_date);
            textDayBalance = itemView.findViewById(R.id.text_day_balance);
        }

        void bind(HeaderItem item) {
            Context context = itemView.getContext();
            textDate.setText(new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(item.date));
            textDayBalance.setText(CurrencyFormatter.formatSigned(item.dayBalance, item.currencyCode));
            textDayBalance.setTextColor(context.getColor(
                    item.dayBalance < 0 ? R.color.expense_red : R.color.income_green));
        }
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final FrameLayout badgeIcon;
        private final ImageView imageDirection;
        private final TextView textTitle;
        private final TextView textSubtitle;
        private final TextView textAmount;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            badgeIcon = itemView.findViewById(R.id.badge_icon);
            imageDirection = itemView.findViewById(R.id.image_direction);
            textTitle = itemView.findViewById(R.id.text_title);
            textSubtitle = itemView.findViewById(R.id.text_subtitle);
            textAmount = itemView.findViewById(R.id.text_amount);
        }

        void bind(TransactionItem item) {
            TransactionWithDetails t = item.transaction;
            Context context = itemView.getContext();
            boolean isExpense = Category.TYPE_EXPENSE.equals(t.type);

            if (t.categoryIcon != null) {
                imageDirection.setImageResource(CategoryIcons.resolve(t.categoryIcon));
                GradientDrawable badge = (GradientDrawable) badgeIcon.getBackground().mutate();
                badge.setColor(t.categoryColor != 0 ? t.categoryColor : context.getColor(R.color.text_tertiary));
            } else {
                imageDirection.setImageResource(isExpense ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down);
                GradientDrawable badge = (GradientDrawable) badgeIcon.getBackground().mutate();
                badge.setColor(context.getColor(R.color.surface_variant));
            }

            String title = !TextUtils.isEmpty(t.counterparty)
                    ? t.counterparty
                    : DisplayNameResolver.resolve(context, t.categoryName);
            textTitle.setText(title);

            String relation = context.getString(isExpense ? R.string.sent_by_you : R.string.received_by_you);
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(t.date);
            textSubtitle.setText(context.getString(R.string.subtitle_format, relation, time));

            double signed = isExpense ? -t.amount : t.amount;
            textAmount.setText(CurrencyFormatter.formatSigned(signed, t.accountCurrency));
            textAmount.setTextColor(TransactionColors.amountColor(context, t.type));
        }
    }

    private static final DiffUtil.ItemCallback<ListItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<ListItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
            if (oldItem instanceof HeaderItem && newItem instanceof HeaderItem) {
                return ((HeaderItem) oldItem).date.equals(((HeaderItem) newItem).date);
            }
            if (oldItem instanceof TransactionItem && newItem instanceof TransactionItem) {
                return ((TransactionItem) oldItem).transaction.id == ((TransactionItem) newItem).transaction.id;
            }
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
            if (oldItem instanceof HeaderItem && newItem instanceof HeaderItem) {
                return ((HeaderItem) oldItem).dayBalance == ((HeaderItem) newItem).dayBalance;
            }
            if (oldItem instanceof TransactionItem && newItem instanceof TransactionItem) {
                TransactionWithDetails a = ((TransactionItem) oldItem).transaction;
                TransactionWithDetails b = ((TransactionItem) newItem).transaction;
                return a.amount == b.amount && a.type.equals(b.type)
                        && TextUtils.equals(a.counterparty, b.counterparty)
                        && TextUtils.equals(a.categoryName, b.categoryName)
                        && a.date.equals(b.date);
            }
            return false;
        }
    };
}
