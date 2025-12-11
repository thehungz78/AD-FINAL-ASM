package com.example.se07203_b5;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final Context context;
    private final ArrayList<Transaction> transactionList;
    private final OnItemClickListener listener;

    // Interface để xử lý sự kiện click vào item (RecyclerView không có sẵn setOnItemClickListener như ListView)
    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    public TransactionAdapter(Context context, ArrayList<Transaction> transactionList, OnItemClickListener listener) {
        this.context = context;
        this.transactionList = transactionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout cho từng item
        // Lưu ý: Đảm bảo tên file layout XML của bạn là activity_transaction_adapter.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        // Gán dữ liệu vào View
        holder.tvCategory.setText(transaction.getCategory());
        holder.tvDescription.setText(transaction.getDescription());
        holder.tvDate.setText(transaction.getDate());

        String amountText;
        int color;

        // Kiểm tra loại giao dịch để đổi màu và dấu +/-
        if ("EXPENSE".equals(transaction.getType())) {
            amountText = String.format(Locale.getDefault(), "- %,.0f đ", transaction.getAmount());
            color = Color.parseColor("#F44336"); // Màu đỏ cho Chi tiêu
            holder.ivIcon.setColorFilter(color);
        } else {
            amountText = String.format(Locale.getDefault(), "+ %,.0f đ", transaction.getAmount());
            color = Color.parseColor("#4CAF50"); // Màu xanh lá cho Thu nhập
            holder.ivIcon.setColorFilter(color);
        }

        holder.tvAmount.setText(amountText);
        holder.tvAmount.setTextColor(color);

        // Bắt sự kiện click vào item
        holder.itemView.setOnClickListener(v -> listener.onItemClick(transaction));
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    // ViewHolder giúp tối ưu hiệu năng, tránh findViewById nhiều lần
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvCategory, tvDescription, tvAmount, tvDate;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ View từ layout XML item
            ivIcon = itemView.findViewById(R.id.ivTransactionIcon);
            tvCategory = itemView.findViewById(R.id.tvTransactionCategory);
            tvDescription = itemView.findViewById(R.id.tvTransactionDescription);
            tvAmount = itemView.findViewById(R.id.tvTransactionAmount);
            tvDate = itemView.findViewById(R.id.tvTransactionDate);
        }
    }
}