package com.example.se07203_b5;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private final List<String> categories;
    private int selectedPosition = -1;
    private final OnCategorySelectedListener listener;

    public interface OnCategorySelectedListener {
        void onCategorySelected(String category);
    }

    public CategoryAdapter(Context context, List<String> categories, OnCategorySelectedListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categories.get(position);
        holder.tvName.setText(category);

        // --- LẤY EMOJI CHO DANH MỤC ---
        String emoji = getEmojiForCategory(category);
        holder.tvIcon.setText(emoji);
        // ------------------------------

        // Hiệu ứng khi chọn (Style giống Money Lover)
        if (selectedPosition == position) {
            // KHI CHỌN: Nền trắng, Viền cam đậm, Chữ cam
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            holder.cardView.setStrokeColor(Color.parseColor("#FF9800")); // Màu cam
            holder.cardView.setStrokeWidth(3); // Viền dày hơn chút
            holder.tvName.setTextColor(Color.parseColor("#FF9800"));
        } else {
            // KHI CHƯA CHỌN: Nền kem nhạt, Viền xám mờ, Chữ nâu đất
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FFFDF0")); // Màu kem nhạt
            holder.cardView.setStrokeColor(Color.parseColor("#E0E0E0"));
            holder.cardView.setStrokeWidth(1); // Viền mỏng
            holder.tvName.setTextColor(Color.parseColor("#5D4037")); // Màu nâu đất
        }

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();
            listener.onCategorySelected(category);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    // Hàm này giúp chọn lại đúng icon khi mở lại giao dịch để sửa
    public void setSelectedCategory(String categoryName) {
        selectedPosition = categories.indexOf(categoryName);
        notifyDataSetChanged();
    }

    // --- KHO EMOJI (Bạn có thể thêm tùy thích) ---
    private String getEmojiForCategory(String categoryName) {
        switch (categoryName) {
            // --- CHI TIÊU ---
            case "Ăn uống": return "🍜";
            case "Di chuyển": return "🛵";
            case "Nhà ở":
            case "Tiền nhà": return "🏠";
            case "Hóa đơn": return "🧾";
            case "Mỹ phẩm": return "💄";
            case "Phí giao lưu": return "🍻";
            case "Y tế": return "💊";
            case "Giáo dục": return "📚";
            case "Tiền điện": return "⚡";
            case "Đi lại": return "🚆";
            case "Quần áo": return "👕";
            case "Mua sắm": return "🛍️";
            case "Phí liên lạc": return "📱";
            case "Chi tiêu hàng ngày": return "🧴"; // Ví dụ chai nước rửa chén/dầu gội

            // --- THU NHẬP ---
            case "Lương":
            case "Tiền lương": return "💰";
            case "Thưởng":
            case "Tiền thưởng": return "🎁";
            case "Đầu tư": return "📈";
            case "Phụ cấp":
            case "Tiền phụ cấp": return "💎";
            case "Thu nhập phụ": return "💸";
            case "Thu nhập tạm tính": return "🤲";

            default: return "📦"; // Icon mặc định nếu không tìm thấy tên
        }
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvIcon; // Lưu ý: Đã đổi từ ImageView sang TextView
        com.google.android.material.card.MaterialCardView cardView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            // Ánh xạ ID của TextView hiển thị Emoji trong file item_category.xml
            tvIcon = itemView.findViewById(R.id.tvCategoryIcon);
            cardView = itemView.findViewById(R.id.cardCategory);
        }
    }
}