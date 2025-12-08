
package com.example.se07203_b5;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateTransactionActivity extends AppCompatActivity {

    // Khai báo các View trong layout mới
    private TextView tabExpense, tabIncome, tvDateDisplay, tvAmountLabel;
    private EditText edtAmount, edtDesc;
    private RecyclerView rvCategories;
    private Button btnSubmit, btnBack;

    // Adapter và dữ liệu
    private CategoryAdapter categoryAdapter;
    private String selectedCategory = "";
    private String currentType = "EXPENSE"; // Mặc định là Chi tiêu

    // Danh sách danh mục (Bạn có thể thêm bớt tùy ý)
    private final List<String> expenseCategories = Arrays.asList(
            "Ăn uống", "Di chuyển", "Nhà ở", "Hóa đơn",
            "Mỹ phẩm", "Phí giao lưu", "Y tế", "Giáo dục",
            "Tiền điện", "Đi lại", "Quần áo", "Khác"
    );

    private final List<String> incomeCategories = Arrays.asList(
            "Lương", "Thưởng", "Đầu tư", "Phụ cấp", "Thu nhập phụ", "Khác"
    );

    // Biến xử lý Logic
    private boolean isEditMode = false;
    private long transactionId = -1;
    private long userId;
    private DatabaseHelper dbHelper;
    private final Calendar calendar = Calendar.getInstance();

    // Format hiển thị: 04/12/2025 (Th 5)
    private final SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd/MM/yyyy (EEE)", new Locale("vi", "VN"));
    // Format lưu DB: 04/12/2025
    private final SimpleDateFormat sdfSave = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    // Format timestamp cho notification
    private final SimpleDateFormat sdfTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_transaction);

        // Khởi tạo DB
        dbHelper = new DatabaseHelper(this);
        SharedPreferences sp = getSharedPreferences("AppData", MODE_PRIVATE);
        userId = sp.getLong("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Lỗi phiên đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        checkMode();
    }

    private void initViews() {
        // Ánh xạ View đúng với ID trong XML mới
        tabExpense = findViewById(R.id.tabExpense);
        tabIncome = findViewById(R.id.tabIncome);
        tvDateDisplay = findViewById(R.id.tvDateDisplay);
        tvAmountLabel = findViewById(R.id.tvAmountLabel);

        edtAmount = findViewById(R.id.edtTransactionAmount);
        edtDesc = findViewById(R.id.edtTransactionDescription);

        rvCategories = findViewById(R.id.rvCategories);
        btnSubmit = findViewById(R.id.btnSubmitCreate);
        btnBack = findViewById(R.id.btnBackToMain);

        // Cấu hình RecyclerView hiển thị dạng Lưới (Grid) 3 cột
        rvCategories.setLayoutManager(new GridLayoutManager(this, 3));

        // Mặc định load Tab Chi tiêu
        switchTab("EXPENSE");
    }

    private void setupListeners() {
        // Chuyển Tab
        tabExpense.setOnClickListener(v -> switchTab("EXPENSE"));
        tabIncome.setOnClickListener(v -> switchTab("INCOME"));

        // Chọn ngày
        tvDateDisplay.setOnClickListener(v -> showDatePicker());

        // Nút Back
        btnBack.setOnClickListener(v -> finish());

        // Nút Lưu
        btnSubmit.setOnClickListener(v -> handleSubmit());
    }

    // Hàm xử lý logic chuyển Tab màu sắc và dữ liệu
    private void switchTab(String type) {
        currentType = type;
        if (type.equals("EXPENSE")) {
            // Tab CHI TIÊU: Màu Cam
            tabExpense.setBackgroundColor(Color.parseColor("#FF9800"));
            tabExpense.setTextColor(Color.WHITE);

            tabIncome.setBackgroundColor(Color.parseColor("#F0F0F0"));
            tabIncome.setTextColor(Color.parseColor("#757575"));

            tvAmountLabel.setText("Tiền chi");
            btnSubmit.setText("NHẬP KHOẢN CHI");
            btnSubmit.setBackgroundColor(Color.parseColor("#FF9800"));

            loadCategories(expenseCategories);
        } else {
            // Tab THU NHẬP: Cũng dùng màu Cam (hoặc Xanh nếu bạn thích)
            tabIncome.setBackgroundColor(Color.parseColor("#FF9800"));
            tabIncome.setTextColor(Color.WHITE);

            tabExpense.setBackgroundColor(Color.parseColor("#F0F0F0"));
            tabExpense.setTextColor(Color.parseColor("#757575"));

            tvAmountLabel.setText("Tiền thu");
            btnSubmit.setText("NHẬP KHOẢN THU");

            loadCategories(incomeCategories);
        }
        selectedCategory = ""; // Reset danh mục đã chọn
    }

    private void loadCategories(List<String> categories) {
        categoryAdapter = new CategoryAdapter(this, categories, category -> {
            selectedCategory = category;
        });
        rvCategories.setAdapter(categoryAdapter);
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, day) -> {
            calendar.set(year, month, day);
            tvDateDisplay.setText(sdfDisplay.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void checkMode() {
        Intent intent = getIntent();
        transactionId = intent.getLongExtra("transactionId", -1);

        // Nếu chưa có ngày, set mặc định hôm nay
        if (tvDateDisplay.getText().toString().isEmpty()) {
            tvDateDisplay.setText(sdfDisplay.format(calendar.getTime()));
        }

        if (transactionId != -1) {
            isEditMode = true;
            loadTransaction(transactionId);
        }
    }

    private void loadTransaction(long id) {
        Transaction t = dbHelper.getTransactionById(id);
        if (t != null) {
            edtAmount.setText(String.valueOf((int)t.getAmount()));
            edtDesc.setText(t.getDescription());

            // Hiển thị ngày (Lưu ý: DB lưu dd/MM/yyyy nên hiển thị thẳng lên cũng được)
            tvDateDisplay.setText(t.getDate());

            switchTab(t.getType());

            // Chọn lại category cũ
            selectedCategory = t.getCategory();
            if (categoryAdapter != null) {
                categoryAdapter.setSelectedCategory(selectedCategory);
            }
        }
    }

    private void handleSubmit() {
        String amountStr = edtAmount.getText().toString().trim();
        String desc = edtDesc.getText().toString().trim();

        // Xử lý ngày: Lấy chuỗi hiển thị, cắt bỏ phần "(Th 5)" để lưu gọn
        String dateDisplay = tvDateDisplay.getText().toString();
        String dateToSave = dateDisplay;
        if(dateDisplay.contains(" ")) {
            dateToSave = dateDisplay.split(" ")[0];
        }

        if (TextUtils.isEmpty(amountStr)) {
            edtAmount.setError("Vui lòng nhập số tiền");
            return;
        }

        if (TextUtils.isEmpty(selectedCategory)) {
            Toast.makeText(this, "Vui lòng chọn danh mục!", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        Transaction t;
        String notificationMessage = "";

        if (isEditMode) {
            t = new Transaction(transactionId, userId, amount, selectedCategory, desc, dateToSave, currentType);
            boolean success = dbHelper.updateTransaction(t);
            if(success) {
                Toast.makeText(this, "Đã cập nhật!", Toast.LENGTH_SHORT).show();
                notificationMessage = "Đã cập nhật " + (currentType.equals("EXPENSE") ? "khoản chi" : "khoản thu") + ": " + String.format("%,.0f", amount) + " (" + selectedCategory + ")";
            }
        } else {
            t = new Transaction(userId, amount, selectedCategory, desc, dateToSave, currentType);
            dbHelper.addTransaction(t);
            Toast.makeText(this, "Đã thêm mới!", Toast.LENGTH_SHORT).show();
            notificationMessage = "Đã thêm " + (currentType.equals("EXPENSE") ? "khoản chi" : "khoản thu") + " mới: " + String.format("%,.0f", amount) + " (" + selectedCategory + ")";
        }

        if (!notificationMessage.isEmpty()) {
            String timestamp = sdfTimestamp.format(Calendar.getInstance().getTime());
            dbHelper.addNotification((int) userId, notificationMessage, timestamp);
        }

        // Quay về Dashboard và xóa các activity cũ trong stack để refresh data
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
