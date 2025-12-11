package com.example.se07203_b5;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private final Context context;
    private final List<DayStats> days;
    private final OnDateClickListener listener;
    private int selectedPosition = -1;

    public interface OnDateClickListener {
        void onDateClick(DayStats dayStats);
    }

    public static class DayStats {
        public Calendar date;
        public double income;
        public double expense;
        public boolean isEmpty; // True nếu là ô trống (padding đầu tháng)

        public DayStats(Calendar date, double income, double expense, boolean isEmpty) {
            this.date = date;
            this.income = income;
            this.expense = expense;
            this.isEmpty = isEmpty;
        }
    }

    public CalendarAdapter(Context context, List<DayStats> days, OnDateClickListener listener) {
        this.context = context;
        this.days = days;
        this.listener = listener;
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
        // Căn chỉnh chiều cao để grid trông đều (tùy chọn, ở đây để wrap_content theo xml)
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        DayStats day = days.get(position);

        if (day.isEmpty) {
            holder.tvDayOfMonth.setText("");
            holder.tvDayIncome.setVisibility(View.GONE);
            holder.tvDayExpense.setVisibility(View.GONE);
            holder.itemView.setBackgroundResource(0);
            holder.itemView.setOnClickListener(null);
        } else {
            holder.tvDayOfMonth.setText(String.valueOf(day.date.get(Calendar.DAY_OF_MONTH)));

            // Hiển thị thu nhập
            if (day.income > 0) {
                holder.tvDayIncome.setVisibility(View.VISIBLE);
                holder.tvDayIncome.setText(String.format(Locale.getDefault(), "+%,.0fk", day.income / 1000));
            } else {
                holder.tvDayIncome.setVisibility(View.GONE);
            }

            // Hiển thị chi tiêu
            if (day.expense > 0) {
                holder.tvDayExpense.setVisibility(View.VISIBLE);
                holder.tvDayExpense.setText(String.format(Locale.getDefault(), "-%,.0fk", day.expense / 1000));
            } else {
                holder.tvDayExpense.setVisibility(View.GONE);
            }

            // Xử lý trạng thái Selected
            holder.itemView.setSelected(position == selectedPosition);
            
            // Background đã được define trong xml selector (calendar_day_bg)

            holder.itemView.setOnClickListener(v -> {
                selectedPosition = holder.getAdapterPosition();
                notifyDataSetChanged();
                listener.onDateClick(day);
            });
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayOfMonth, tvDayIncome, tvDayExpense;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayOfMonth = itemView.findViewById(R.id.tvDayOfMonth);
            tvDayIncome = itemView.findViewById(R.id.tvDayIncome);
            tvDayExpense = itemView.findViewById(R.id.tvDayExpense);
        }
    }
}