
package com.example.se07203_b5;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerViewNotifications;
    private NotificationAdapter notificationAdapter;
    private ArrayList<Notification> notificationList;
    private DatabaseHelper databaseHelper;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);
        btnBack = findViewById(R.id.btnBack); // Cần thêm nút này vào XML hoặc xử lý null nếu dùng ActionBar

        // Setup RecyclerView
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));

        databaseHelper = new DatabaseHelper(this);

        // Lấy ID người dùng từ SharedPreferences (giống MainActivity)
        SharedPreferences sp = getSharedPreferences("AppData", MODE_PRIVATE);
        long currentUserId = sp.getLong("user_id", -1);

        if (currentUserId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Lấy dữ liệu
        notificationList = databaseHelper.getNotificationsByUserId((int) currentUserId);
        notificationAdapter = new NotificationAdapter(notificationList);
        recyclerViewNotifications.setAdapter(notificationAdapter);

        // Xử lý nút Back nếu có trong layout, hoặc enable ActionBar back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        } else if (getSupportActionBar() != null) {
             getSupportActionBar().setDisplayHomeAsUpEnabled(true);
             getSupportActionBar().setTitle("Thông báo");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
