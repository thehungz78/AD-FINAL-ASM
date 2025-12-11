
package com.example.se07203_b5;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // --- Khai báo các View ---
    private TextView tvCurrentBalance;
    private TextView tvTotalIncome;
    private TextView tvTotalExpense;
    private RecyclerView rvRecentTransactions;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAdd;

    // --- Biến hỗ trợ ---
    private SharedPreferences sharedPreferences;
    private DatabaseHelper dbHelper;
    private long currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo Database và lấy User ID
        sharedPreferences = getSharedPreferences("AppData", MODE_PRIVATE);
        dbHelper = new DatabaseHelper(this);
        currentUserId = sharedPreferences.getLong("user_id", -1);

        // Kiểm tra đăng nhập (nếu chưa có user_id thì về màn hình Login)
        if (currentUserId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu mỗi khi màn hình hiện lên (ví dụ sau khi thêm mới xong quay lại)
        // Đã XÓA tính năng checkSession() để không bị logout tự động
        loadDashboardData();

        // Highlight đúng menu item (Input/Home)
        if(bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_input);
        }
    }

    private void initViews() {
        // Ánh xạ các View từ Layout XML
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);

        // Cấu hình RecyclerView (Danh sách giao dịch gần đây)
        rvRecentTransactions = findViewById(R.id.rvRecentTransactions);
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(this));

        // --- CẤU HÌNH NÚT TẠO MỚI (FAB) ---
        fabAdd = findViewById(R.id.fabAddTransaction);
        fabAdd.setOnClickListener(v -> {
            // Khi bấm nút dấu cộng -> Mở màn hình Thêm giao dịch
            Intent intent = new Intent(MainActivity.this, CreateTransactionActivity.class);
            startActivity(intent);
        });

        // --- CẤU HÌNH MENU DƯỚI ĐÁY ---
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Xử lý sự kiện chọn item trong menu
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_report) {
                // Chuyển sang màn hình Báo cáo
                startActivity(new Intent(this, ReportActivity.class));
                return true;
            }
            else if (id == R.id.nav_calendar) {
                // Chuyển sang màn hình Lịch
                startActivity(new Intent(this, CalendarActivity.class));
                return true;
            }
            else if (id == R.id.nav_notifications) {
                // Chuyển sang màn hình Thông báo
                startActivity(new Intent(this, NotificationActivity.class));
                return true;
            }
            else if (id == R.id.nav_input) {
                // Nút "Nhập vào" (hình cây bút) giờ đóng vai trò là nút Trang chủ (Dashboard)
                return true;
            }
            else if (id == R.id.nav_more) {
                // Xử lý nút "Khác": Hiển thị menu tùy chọn (Profile, Logout...)
                showMoreOptions();
                return true;
            }
            // Các nút khác (Thông báo...) có thể thêm logic sau này
            return true;
        });
    }

    // --- HÀM HIỂN THỊ CÁC CHỨC NĂNG KHÁC (Profile, Logout...) ---
    private void showMoreOptions() {
        String[] options = {"👤 Thông tin cá nhân", "⚙️ Cài đặt", "ℹ️ Giới thiệu", "🚪 Đăng xuất"};

        new AlertDialog.Builder(this)
                .setTitle("Chức năng khác")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Profile
                            showUserProfile();
                            break;
                        case 1: // Settings
                            Toast.makeText(this, "Chức năng Cài đặt đang được phát triển!", Toast.LENGTH_SHORT).show();
                            break;
                        case 2: // About
                            showAboutDialog();
                            break;
                        case 3: // Logout
                            performLogout();
                            break;
                    }
                })
                .show();
    }

    private void showUserProfile() {
        // Lấy thông tin từ SharedPreferences đã lưu lúc Login
        String fullname = sharedPreferences.getString("fullname", "Người dùng");
        String username = sharedPreferences.getString("username", "N/A");

        new AlertDialog.Builder(this)
                .setTitle("Thông tin cá nhân")
                .setMessage("Họ tên: " + fullname + "\nTài khoản: " + username)
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Về ứng dụng")
                .setMessage("Ứng dụng Quản lý chi tiêu\nPhiên bản: 1.0\nGiúp bạn quản lý tài chính hiệu quả hơn.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void performLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    // Xóa thông tin đăng nhập
                    sharedPreferences.edit().clear().apply();

                    // Chuyển về màn hình Login
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xóa hết activity cũ
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadDashboardData() {
        // Lấy tháng hiện tại (ví dụ: 12/2025) để lọc dữ liệu hiển thị
        Calendar calendar = Calendar.getInstance();
        String currentMonth = new SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(calendar.getTime());
        String monthFilter = DatabaseHelper.TRANS_DATE + " LIKE '%/" + currentMonth + "'"; // SQL Filter

        // Tính toán các chỉ số từ Database
        double incomeMonth = dbHelper.getTotalAmountByTypeAndFilter(currentUserId, "INCOME", monthFilter);
        double expenseMonth = dbHelper.getTotalAmountByTypeAndFilter(currentUserId, "EXPENSE", monthFilter);

        // Tính tổng số dư (Tổng thu tất cả - Tổng chi tất cả)
        double totalIncomeAll = dbHelper.getTotalAmountByType(currentUserId, "INCOME");
        double totalExpenseAll = dbHelper.getTotalAmountByType(currentUserId, "EXPENSE");

        // Hiển thị lên màn hình
        tvTotalIncome.setText(String.format(Locale.getDefault(), "%,.0f đ", incomeMonth));
        tvTotalExpense.setText(String.format(Locale.getDefault(), "%,.0f đ", expenseMonth));
        tvCurrentBalance.setText(String.format(Locale.getDefault(), "%,.0f đ", totalIncomeAll - totalExpenseAll));

        // Tải danh sách giao dịch gần đây
        loadRecentTransactions();
    }

    private void loadRecentTransactions() {
        ArrayList<Transaction> list = dbHelper.getTransactionsByUserId(currentUserId);

        // Gắn Adapter vào RecyclerView và xử lý sự kiện click vào từng dòng
        TransactionAdapter adapter = new TransactionAdapter(this, list, this::showEditDeleteDialog);
        rvRecentTransactions.setAdapter(adapter);
    }

    // Hiển thị hộp thoại tùy chọn khi nhấn vào 1 giao dịch
    private void showEditDeleteDialog(Transaction transaction) {
        String[] options = {"Sửa", "Xóa"};
        new AlertDialog.Builder(this)
                .setTitle(transaction.getDescription())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Chọn Sửa -> Mở màn hình CreateTransactionActivity kèm theo ID giao dịch
                        Intent intent = new Intent(this, CreateTransactionActivity.class);
                        intent.putExtra("transactionId", transaction.getId());
                        startActivity(intent);
                    } else {
                        // Chọn Xóa -> Xác nhận rồi xóa khỏi DB
                        new AlertDialog.Builder(this)
                                .setMessage("Bạn có chắc muốn xóa giao dịch này?")
                                .setPositiveButton("Xóa", (d, w) -> {
                                    if (dbHelper.removeTransactionById(transaction.getId())) {
                                        Toast.makeText(this, "Đã xóa!", Toast.LENGTH_SHORT).show();
                                        
                                        // Tạo thông báo khi xóa
                                        String message = "Đã xóa giao dịch: " + transaction.getType() + " " + String.format("%,.0f", transaction.getAmount()) + " - " + transaction.getCategory();
                                        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());
                                        dbHelper.addNotification((int)currentUserId, message, timestamp);

                                        loadDashboardData(); // Tải lại dữ liệu sau khi xóa
                                    } else {
                                        Toast.makeText(this, "Lỗi khi xóa", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("Hủy", null).show();
                    }
                }).show();
    }
}
