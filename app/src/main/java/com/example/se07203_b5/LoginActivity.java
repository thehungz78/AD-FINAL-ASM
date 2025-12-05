package com.example.se07203_b5;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    Button btnSubmitLogin, btnGoToRegister;
    EditText edtUsername, edtPassword;

    DatabaseHelper databaseHelper;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo SharedPreferences và DB Helper sớm
        sharedPreferences = getSharedPreferences("AppData", MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(this);

        // --- KIỂM TRA ĐĂNG NHẬP TỰ ĐỘNG ---
        // Nếu đã có user_id (đã đăng nhập trước đó), chuyển thẳng vào MainActivity
        // Bỏ qua kiểm tra thời gian (Session Timeout) để giữ đăng nhập mãi mãi
        long userId = sharedPreferences.getLong("user_id", -1);
        if (userId > 0) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        btnSubmitLogin = findViewById(R.id.btnSubmitLogin);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);

        btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

        btnSubmitLogin.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ Username và Password", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = databaseHelper.getUserByUsernameAndPassword(username, password);

            if(user != null && user.getFullname() != null) {
                // Đăng nhập thành công -> Lưu thông tin
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", username);
                editor.putString("fullname", user.getFullname());
                editor.putLong("user_id", user.getId());
                // Không cần lưu thời gian last_active_time nữa
                editor.apply();

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Sai username hoặc password", Toast.LENGTH_SHORT).show();
            }
        });
    }
}