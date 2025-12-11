package com.example.se07203_b5;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private TextView tvMonthYear, tvSummaryIncome, tvSummaryExpense, tvSummaryTotal, tvDateHeader;
    private RecyclerView rvCalendar, rvDayTransactions;
    private ImageButton btnPrevMonth, btnNextMonth;
    private BottomNavigationView bottomNavigationView;

    private Calendar currentMonthCalendar;
    private Calendar selectedDateCalendar;
    private DatabaseHelper dbHelper;
    private long userId;
    private CalendarAdapter calendarAdapter;
    private TransactionAdapter transactionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences sp = getSharedPreferences("AppData", MODE_PRIVATE);
        userId = sp.getLong("user_id", -1);

        if (userId == -1) {
            finish();
            return;
        }

        currentMonthCalendar = Calendar.getInstance();
        currentMonthCalendar.set(Calendar.DAY_OF_MONTH, 1); // Set về ngày 1 đầu tháng
        
        selectedDateCalendar = Calendar.getInstance(); // Mặc định chọn hôm nay

        initViews();
        setupListeners();
        
        loadCalendarData();
        loadDayDetails(selectedDateCalendar);
    }

    private void initViews() {
        tvMonthYear = findViewById(R.id.tvMonthYear);
        tvSummaryIncome = findViewById(R.id.tvSummaryIncome);
        tvSummaryExpense = findViewById(R.id.tvSummaryExpense);
        tvSummaryTotal = findViewById(R.id.tvSummaryTotal);
        tvDateHeader = findViewById(R.id.tvDateHeader);

        rvCalendar = findViewById(R.id.rvCalendar);
        rvDayTransactions = findViewById(R.id.rvDayTransactions);
        
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        rvCalendar.setLayoutManager(new GridLayoutManager(this, 7));
        rvDayTransactions.setLayoutManager(new LinearLayoutManager(this));
        
        // Highlight menu Lịch
        bottomNavigationView.setSelectedItemId(R.id.nav_calendar);
    }

    private void setupListeners() {
        btnPrevMonth.setOnClickListener(v -> {
            currentMonthCalendar.add(Calendar.MONTH, -1);
            loadCalendarData();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonthCalendar.add(Calendar.MONTH, 1);
            loadCalendarData();
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_calendar) return true;
            
            if (id == R.id.nav_input) {
                // Quay về Dashboard (MainActivity)
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_report) {
                 startActivity(new Intent(this, ReportActivity.class));
                 finish();
                 return true;
            }
            // Các case khác xử lý tương tự hoặc để trống
            return true;
        });
    }

    private void loadCalendarData() {
        // Cập nhật tiêu đề tháng
        SimpleDateFormat sdfMonth = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
        tvMonthYear.setText(sdfMonth.format(currentMonthCalendar.getTime()));

        // Lấy danh sách giao dịch trong tháng
        String monthStr = sdfMonth.format(currentMonthCalendar.getTime());
        ArrayList<Transaction> monthlyTransactions = dbHelper.getTransactionsByMonth(userId, monthStr);

        // Tính toán dữ liệu cho từng ngày trong lưới lịch
        List<CalendarAdapter.DayStats> days = new ArrayList<>();
        
        // Xác định ngày bắt đầu của tuần (CN hay T2) -> ở đây layout T2..CN
        // Calendar.DAY_OF_WEEK: CN=1, T2=2, ... T7=7
        // Cần padding days đầu tháng
        int maxDays = currentMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int dayOfWeek = currentMonthCalendar.get(Calendar.DAY_OF_WEEK); 
        // Logic để T2 là ô đầu tiên:
        // Nếu ngày 1 là T2 (2) -> padding 0
        // Nếu ngày 1 là T3 (3) -> padding 1
        // Nếu ngày 1 là CN (1) -> padding 6
        int emptyCells = (dayOfWeek + 5) % 7; 

        for (int i = 0; i < emptyCells; i++) {
            days.add(new CalendarAdapter.DayStats(null, 0, 0, true));
        }

        // Fill các ngày trong tháng
        for (int d = 1; d <= maxDays; d++) {
            Calendar date = (Calendar) currentMonthCalendar.clone();
            date.set(Calendar.DAY_OF_MONTH, d);
            
            // Tính tổng thu/chi ngày này
            String dateStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date.getTime());
            double inc = 0, exp = 0;
            
            for (Transaction t : monthlyTransactions) {
                // Date trong DB dạng dd/MM/yyyy
                if (t.getDate().equals(dateStr)) {
                    if ("INCOME".equals(t.getType())) inc += t.getAmount();
                    else exp += t.getAmount();
                }
            }
            days.add(new CalendarAdapter.DayStats(date, inc, exp, false));
        }

        calendarAdapter = new CalendarAdapter(this, days, dayStats -> {
            selectedDateCalendar = dayStats.date;
            loadDayDetails(selectedDateCalendar);
        });
        
        rvCalendar.setAdapter(calendarAdapter);
        
        // Highlight ngày đang chọn nếu nó nằm trong tháng này
        // (Logic đơn giản: nếu selectedDate cùng tháng/năm với currentMonth thì highlight)
        // Hiện tại chỉ load lại transaction của ngày đang chọn, chưa focus ô ngày trên lịch (nâng cao sau)
    }

    private void loadDayDetails(Calendar date) {
        if (date == null) return;
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateStr = sdf.format(date.getTime());
        
        // Cập nhật header
        SimpleDateFormat sdfHeader = new SimpleDateFormat("dd/MM/yyyy (EEE)", new Locale("vi", "VN"));
        
        // Lấy list transaction
        ArrayList<Transaction> dayTransactions = dbHelper.getTransactionsByDate(userId, dateStr);
        
        // Tính tổng
        double totalInc = 0;
        double totalExp = 0;
        for (Transaction t : dayTransactions) {
            if ("INCOME".equals(t.getType())) totalInc += t.getAmount();
            else totalExp += t.getAmount();
        }
        
        tvSummaryIncome.setText(String.format(Locale.getDefault(), "+%,.0fđ", totalInc));
        tvSummaryExpense.setText(String.format(Locale.getDefault(), "-%,.0fđ", totalExp));
        
        double total = totalInc - totalExp;
        String sign = total >= 0 ? "+" : "";
        tvSummaryTotal.setText(String.format(Locale.getDefault(), "%s%,.0fđ", sign, total));
        
        tvDateHeader.setText(sdfHeader.format(date.getTime()) + " - " + sign + String.format(Locale.getDefault(), "%,.0fđ", total));

        // Setup adapter
        transactionAdapter = new TransactionAdapter(this, dayTransactions, transaction -> {
        });
        rvDayTransactions.setAdapter(transactionAdapter);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadCalendarData();
        if(selectedDateCalendar != null) {
            loadDayDetails(selectedDateCalendar);
        }
    }
}