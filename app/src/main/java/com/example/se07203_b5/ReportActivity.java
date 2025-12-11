package com.example.se07203_b5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.ImageButton;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    // Định nghĩa các chế độ xem
    private enum ViewMode {
        MONTH, YEAR
    }

    PieChart pieChart;
    DatabaseHelper db;
    long userId;
    TextView tvChartDetails;
    TextView tvCurrentPeriod;
    Button btnMonthMode;
    Button btnYearMode;
    ImageButton btnPrevPeriod;
    ImageButton btnNextPeriod;

    private float totalExpense = 0f;
    private Calendar currentCalendar;
    private ViewMode currentMode = ViewMode.MONTH; // Mặc định xem theo Tháng

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Ánh xạ View
        pieChart = findViewById(R.id.pieChart);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        tvChartDetails = findViewById(R.id.tv_chart_details);
        tvCurrentPeriod = findViewById(R.id.tv_current_period);
        btnMonthMode = findViewById(R.id.btn_month_mode);
        btnYearMode = findViewById(R.id.btn_year_mode);
        btnPrevPeriod = findViewById(R.id.btn_prev_period);
        btnNextPeriod = findViewById(R.id.btn_next_period);

        db = new DatabaseHelper(this);

        // Khởi tạo Calendar với ngày/tháng hiện tại
        currentCalendar = Calendar.getInstance();

        // Lấy User ID từ SharedPreferences
        SharedPreferences sp = getSharedPreferences("AppData", MODE_PRIVATE);
        userId = sp.getLong("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy người dùng, vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupModeButtons();
        setupNavigationButtons();
        setupBottomMenu();
        loadChartData();
    }

    // =====================================================
    //    CÁC PHƯƠNG THỨC HỖ TRỢ ĐIỀU HƯỚNG VÀ CHẾ ĐỘ
    // =====================================================

    /**
     * Thiết lập các sự kiện cho nút chuyển chế độ xem (Tháng/Năm)
     */
    private void setupModeButtons() {
        btnMonthMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentMode != ViewMode.MONTH) {
                    currentMode = ViewMode.MONTH;
                    updateModeStyle();
                    loadChartData();
                }
            }
        });

        btnYearMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentMode != ViewMode.YEAR) {
                    currentMode = ViewMode.YEAR;
                    updateModeStyle();
                    loadChartData();
                }
            }
        });
        updateModeStyle();
    }

    /**
     * Cập nhật màu sắc cho các nút chuyển chế độ
     */
    private void updateModeStyle() {
        // Đảm bảo R.color.colorPrimary tồn tại
        int colorPrimary = ContextCompat.getColor(this, R.color.colorPrimary);
        int colorBlack = ContextCompat.getColor(this, android.R.color.black);
        int colorWhite = ContextCompat.getColor(this, android.R.color.white);

        if (currentMode == ViewMode.MONTH) {
            btnMonthMode.setBackgroundColor(colorPrimary);
            btnMonthMode.setTextColor(colorWhite);
            btnYearMode.setBackgroundColor(Color.TRANSPARENT);
            btnYearMode.setTextColor(colorBlack);
        } else {
            btnYearMode.setBackgroundColor(colorPrimary);
            btnYearMode.setTextColor(colorWhite);
            btnMonthMode.setBackgroundColor(Color.TRANSPARENT);
            btnMonthMode.setTextColor(colorBlack);
        }
    }

    /**
     * Cập nhật TextView hiển thị kỳ báo cáo (tháng/năm).
     */
    private void updatePeriodDisplay() {
        SimpleDateFormat sdf;
        if (currentMode == ViewMode.MONTH) {
            sdf = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
        } else { // ViewMode.YEAR
            sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
        }
        String period = sdf.format(currentCalendar.getTime());
        tvCurrentPeriod.setText(period);
    }

    /**
     * Di chuyển đến kỳ trước hoặc kỳ sau (tháng hoặc năm) và tải lại dữ liệu.
     */
    private void navigatePeriod(int change) {
        if (currentMode == ViewMode.MONTH) {
            currentCalendar.add(Calendar.MONTH, change);
        } else { // ViewMode.YEAR
            currentCalendar.add(Calendar.YEAR, change);
        }
        loadChartData();
    }

    /**
     * Thiết lập các sự kiện cho nút điều hướng tháng/năm.
     */
    private void setupNavigationButtons() {
        btnPrevPeriod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigatePeriod(-1); // Lùi 1 kỳ
            }
        });

        btnNextPeriod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigatePeriod(1); // Tiến 1 kỳ
            }
        });
    }


    // =====================================================
    //    LOAD DỮ LIỆU CHI TIÊU ĐỂ VẼ BIỂU ĐỒ TRÒN
    // =====================================================
    private void loadChartData() {

        updatePeriodDisplay();

        ArrayList<Transaction> list;

        int targetMonth = currentCalendar.get(Calendar.MONTH) + 1;
        int targetYear = currentCalendar.get(Calendar.YEAR);
        String periodName = (currentMode == ViewMode.MONTH) ?
                String.format(Locale.getDefault(), "tháng %02d/%d", targetMonth, targetYear) :
                "năm %d"; // %d cho năm

        // LỌC DỮ LIỆU TỪ DATABASE DỰA TRÊN currentMode
        if (currentMode == ViewMode.MONTH) {
            String monthYear = String.format(Locale.getDefault(), "%02d/%d", targetMonth, targetYear);
            list = db.getTransactionsByMonth(userId, monthYear);
        } else { // ViewMode.YEAR
            String year = String.valueOf(targetYear);
            list = db.getTransactionsByYear(userId, year);
        }

        // ********** KIỂM TRA DỮ LIỆU ĐỂ QUYẾT ĐỊNH VẼ HAY KHÔNG (Bước 1) **********

        if (list.isEmpty()) {
            pieChart.setNoDataText("Không có giao dịch nào trong " + periodName);
            pieChart.setData(null);
            pieChart.invalidate();
            tvChartDetails.setText("Chưa có dữ liệu giao dịch.");
            totalExpense = 0f;
            return;
        }

        // 2. Gom nhóm dữ liệu theo Danh mục
        HashMap<String, ArrayList<Transaction>> mapTransactions = new HashMap<>();
        HashMap<String, Float> mapAmounts = new HashMap<>();

        totalExpense = 0f;

        for (Transaction t : list) {
            if (!"EXPENSE".equals(t.getType())) continue;

            float amount = (float)t.getAmount();
            totalExpense += amount;

            String category = t.getCategory();

            // 1. Cập nhật tổng số tiền
            mapAmounts.put(category, mapAmounts.getOrDefault(category, 0f) + amount);

            // 2. Cập nhật danh sách giao dịch để lấy ngày gần nhất
            // Giả định list đã được sắp xếp giảm dần theo ngày (DESC) từ DB
            mapTransactions.computeIfAbsent(category, k -> new ArrayList<>()).add(t);
        }

        // 3. Chuyển đổi dữ liệu sang định dạng Entry của PieChart
        ArrayList<PieEntry> entries = new ArrayList<>();

        for (String category : mapAmounts.keySet()) {
            float amount = mapAmounts.get(category);
            ArrayList<Transaction> transactions = mapTransactions.get(category);

            // Lấy ngày của giao dịch đầu tiên (gần nhất)
            String dateString = (transactions != null && !transactions.isEmpty())
                    ? transactions.get(0).getDate()
                    : "N/A";

            // Lưu trữ ngày vào trường 'data' của PieEntry
            entries.add(new PieEntry(amount, category, dateString));
        }

        // ********** KIỂM TRA DỮ LIỆU CHI TIÊU (Bước 2) **********

        if (entries.isEmpty()) {
            pieChart.setNoDataText("Chưa có dữ liệu chi tiêu để hiển thị trong " + periodName);
            pieChart.setData(null);
            pieChart.invalidate();

            String periodText = (currentMode == ViewMode.MONTH) ? "tháng này" : "năm này";
            String totalExpenseText = String.format("TỔNG CHI TIÊU: %,.0f VNĐ trong %s", totalExpense, periodText);
            tvChartDetails.setText(totalExpenseText);
            return;
        }

        // 4. Cấu hình hiển thị Dataset
        PieDataSet dataSet = new PieDataSet(entries, "Danh mục chi tiêu");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setDrawValues(true);

        // 5. Đưa dữ liệu vào Biểu đồ
        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        // Cấu hình giao diện biểu đồ
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setCenterText("Chi tiêu");
        pieChart.setCenterTextSize(16f);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(true);

        // THIẾT LẬP TEXT BAN ĐẦU (TỔNG CHI TIÊU)
        String periodText = (currentMode == ViewMode.MONTH) ? "tháng này" : "năm này";
        String initialMessage = String.format("TỔNG CHI TIÊU: %,.0f VNĐ trong %s", totalExpense, periodText);
        tvChartDetails.setText(initialMessage);

        // *************** Xử lý sự kiện khi bấm vào miếng bánh ***************
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                PieEntry entry = (PieEntry) e;
                String category = entry.getLabel();
                float amount = entry.getValue();
                String dateString = (entry.getData() instanceof String) ? (String) entry.getData() : "";

                // Cập nhật TextView với thông tin chi tiết của danh mục và Ngày gần nhất
                String message = String.format(
                        "Danh mục: %s\nChi tiêu: %,.0f VNĐ\nNgày: %s",
                        category, amount, dateString
                );
                tvChartDetails.setText(message);

                // Cập nhật Center Text
                pieChart.setCenterText(category + "\n" + String.format("%,.0f", amount));
            }

            @Override
            public void onNothingSelected() {
                // Đặt lại Center Text
                pieChart.setCenterText("Chi tiêu");

                // Đặt lại nội dung TextView thành TỔNG CHI TIÊU của kỳ hiện tại
                String periodText = (currentMode == ViewMode.MONTH) ? "tháng này" : "năm này";
                String totalExpenseText = String.format("TỔNG CHI TIÊU: %,.0f VNĐ trong %s", totalExpense, periodText);
                tvChartDetails.setText(totalExpenseText);
            }
        });

        pieChart.animateY(1000);
        pieChart.invalidate();
    }


    // =====================================================
    //    BOTTOM NAVIGATION
    // =====================================================
    private void setupBottomMenu() {
        bottomNavigationView.setSelectedItemId(R.id.nav_report);

        bottomNavigationView.setOnItemSelectedListener(
                new NavigationBarView.OnItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        int id = item.getItemId();

                        if (id == R.id.nav_input) {
                            startActivity(new Intent(ReportActivity.this, MainActivity.class));
                            return true;

                        } else if (id == R.id.nav_calendar) {
                            Toast.makeText(ReportActivity.this, "Chức năng Lịch chưa cài đặt", Toast.LENGTH_SHORT).show();
                            return true;

                        } else if (id == R.id.nav_report) {
                            return true;

                        } else if (id == R.id.nav_notifications) {
                            Toast.makeText(ReportActivity.this, "Chức năng Thông báo chưa cài đặt", Toast.LENGTH_SHORT).show();
                            return true;

                        } else if (id == R.id.nav_more) {
                            Toast.makeText(ReportActivity.this, "Menu Khác chưa cài đặt", Toast.LENGTH_SHORT).show();
                            return true;
                        }

                        return false;
                    }
                }
        );
    }
}