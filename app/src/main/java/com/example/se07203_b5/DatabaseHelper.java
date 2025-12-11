
package com.example.se07203_b5;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SE07203Expense.db";
    private static final int DATABASE_VERSION = 5; // Increased version for new table

    // Bảng Transaction
    private static final String TABLE_TRANSACTION = "transactions";
    private static final String TRANS_ID = "id";
    public static final String TRANS_USER_ID = "user_id";
    public static final String TRANS_AMOUNT = "amount";
    public static final String TRANS_CATEGORY = "category";
    public static final String TRANS_DESCRIPTION = "description";
    public static final String TRANS_DATE = "date"; // Định dạng: DD/MM/YYYY
    public static final String TRANS_TYPE = "type";

    // Bảng User
    private static final String TABLE_USER = "users";
    private static final String USER_ID = "id";
    private static final String USER_USERNAME = "username";
    private static final String USER_PASSWORD = "password";
    private static final String USER_FULLNAME = "fullname";
    
    // Bảng Notification
    private static final String TABLE_NOTIFICATION = "notifications";
    private static final String NOTIF_ID = "id";
    private static final String NOTIF_USER_ID = "user_id";
    private static final String NOTIF_MESSAGE = "message";
    private static final String NOTIF_TIMESTAMP = "timestamp";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USER + "(" +
                USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USER_USERNAME + " TEXT, " +
                USER_FULLNAME + " TEXT, " +
                USER_PASSWORD + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_TRANSACTION + "(" +
                TRANS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TRANS_USER_ID + " INTEGER, " +
                TRANS_AMOUNT + " REAL, " +
                TRANS_CATEGORY + " TEXT, " +
                TRANS_DESCRIPTION + " TEXT, " +
                TRANS_DATE + " TEXT, " +
                TRANS_TYPE + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_NOTIFICATION + "(" +
                NOTIF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NOTIF_USER_ID + " INTEGER, " +
                NOTIF_MESSAGE + " TEXT, " +
                NOTIF_TIMESTAMP + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 5) {
             db.execSQL("CREATE TABLE " + TABLE_NOTIFICATION + "(" +
                NOTIF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NOTIF_USER_ID + " INTEGER, " +
                NOTIF_MESSAGE + " TEXT, " +
                NOTIF_TIMESTAMP + " TEXT)");
        } else {
             db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATION);
             db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
             db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
             onCreate(db);
        }
    }

    // --- TRANSACTION CRUD ---

    public long addTransaction(Transaction t) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(TRANS_USER_ID, t.getUserId());
        v.put(TRANS_AMOUNT, t.getAmount());
        v.put(TRANS_CATEGORY, t.getCategory());
        v.put(TRANS_DESCRIPTION, t.getDescription());
        v.put(TRANS_DATE, t.getDate());
        v.put(TRANS_TYPE, t.getType());
        long id = db.insert(TABLE_TRANSACTION, null, v);
        db.close();
        return id;
    }

    public boolean updateTransaction(Transaction t) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(TRANS_AMOUNT, t.getAmount());
        v.put(TRANS_CATEGORY, t.getCategory());
        v.put(TRANS_DESCRIPTION, t.getDescription());
        v.put(TRANS_DATE, t.getDate());
        v.put(TRANS_TYPE, t.getType());
        int result = db.update(TABLE_TRANSACTION, v, TRANS_ID + "=?", new String[]{String.valueOf(t.getId())});
        db.close();
        return result > 0;
    }

    public boolean removeTransactionById(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TABLE_TRANSACTION, TRANS_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }

    public Transaction getTransactionById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_TRANSACTION, null, TRANS_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        Transaction t = null;
        if (c.moveToFirst()) {
            t = new Transaction(c.getLong(0), c.getLong(1), c.getDouble(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6));
        }
        c.close();
        db.close();
        return t;
    }

    public ArrayList<Transaction> getTransactionsByUserId(long userId) {
        ArrayList<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_TRANSACTION, null, TRANS_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, TRANS_ID + " DESC");
        if (c.moveToFirst()) {
            do {
                list.add(new Transaction(c.getLong(0), c.getLong(1), c.getDouble(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6)));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    // Lấy giao dịch theo ngày cụ thể (vd: 04/12/2025)
    public ArrayList<Transaction> getTransactionsByDate(long userId, String date) {
        ArrayList<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_TRANSACTION, null, TRANS_USER_ID + "=? AND " + TRANS_DATE + "=?", new String[]{String.valueOf(userId), date}, null, null, TRANS_ID + " DESC");
        if (c.moveToFirst()) {
            do {
                list.add(new Transaction(c.getLong(0), c.getLong(1), c.getDouble(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6)));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    /**
     * Lọc giao dịch theo Tháng và Năm.
     * Dựa trên định dạng TRANS_DATE là DD/MM/YYYY.
     * @param userId ID người dùng.
     * @param monthYear Chuỗi MM/YYYY (ví dụ: "12/2025").
     * @return Danh sách giao dịch.
     */
    public ArrayList<Transaction> getTransactionsByMonth(long userId, String monthYear) {
        ArrayList<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        // Ví dụ: date LIKE '%/12/2025'
        // Cần chỉnh lại LIKE để khớp chính xác MM/YYYY: '%%/MM/YYYY'
        Cursor c = db.query(TABLE_TRANSACTION, null,
                TRANS_USER_ID + "=? AND " + TRANS_DATE + " LIKE ?",
                new String[]{String.valueOf(userId), "%/" + monthYear},
                null, null, null);
        if (c.moveToFirst()) {
            do {
                list.add(new Transaction(c.getLong(0), c.getLong(1), c.getDouble(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6)));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    /**
     * Lọc giao dịch theo Năm.
     * Dựa trên định dạng TRANS_DATE là DD/MM/YYYY.
     * @param userId ID người dùng.
     * @param year Chuỗi YYYY (ví dụ: "2025").
     * @return Danh sách giao dịch.
     */
    public ArrayList<Transaction> getTransactionsByYear(long userId, String year) {
        ArrayList<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        // Ví dụ: date LIKE '%/2025' (Khớp YYYY ở cuối chuỗi)
        Cursor c = db.query(TABLE_TRANSACTION, null,
                TRANS_USER_ID + "=? AND " + TRANS_DATE + " LIKE ?",
                new String[]{String.valueOf(userId), "%/" + year},
                null, null, null);
        if (c.moveToFirst()) {
            do {
                list.add(new Transaction(c.getLong(0), c.getLong(1), c.getDouble(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6)));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    // --- AGGREGATE ---
    public double getTotalAmountByType(long userId, String type) {
        SQLiteDatabase db = getReadableDatabase();
        double total = 0;
        Cursor c = db.rawQuery("SELECT SUM(" + TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTION + " WHERE " + TRANS_USER_ID + "=? AND " + TRANS_TYPE + "=?", new String[]{String.valueOf(userId), type});
        if (c.moveToFirst()) total = c.getDouble(0);
        c.close();
        db.close();
        return total;
    }

    /**
     * Tính tổng chi tiêu/thu nhập với bộ lọc ngày tháng tùy chỉnh.
     * @param userId ID người dùng.
     * @param type Loại giao dịch ("EXPENSE" hoặc "INCOME").
     * @param dateFilter Điều kiện lọc ngày tháng SQL (Ví dụ: "date LIKE '%%/12/2025'").
     * @return Tổng số tiền.
     */
    public double getTotalAmountByTypeAndFilter(long userId, String type, String dateFilter) {
        SQLiteDatabase db = getReadableDatabase();
        double total = 0;
        // Sử dụng WHERE user_id = ? AND type = ? AND [dateFilter]
        // dateFilter phải chứa tên cột TRANS_DATE và điều kiện.
        Cursor c = db.rawQuery("SELECT SUM(" + TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTION
                        + " WHERE " + TRANS_USER_ID + "=? AND " + TRANS_TYPE + "=? AND " + dateFilter,
                new String[]{String.valueOf(userId), type});
        if (c.moveToFirst()) total = c.getDouble(0);
        c.close();
        db.close();
        return total;
    }

    // --- USER ---
    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_USERNAME, user.getUsername());
        values.put(USER_PASSWORD, user.getPassword());
        values.put(USER_FULLNAME, user.getFullname());
        long id = db.insert(TABLE_USER, null, values);
        db.close();
        return id;
    }

    public User getUserByUsernameAndPassword(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER, null, USER_USERNAME + "=? AND " + USER_PASSWORD + "=?", new String[]{username, password}, null, null, null);
        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(cursor.getInt(0), cursor.getString(1), cursor.getString(3), cursor.getString(2));
        }
        cursor.close();
        db.close();
        return user;
    }
    
    // --- NOTIFICATION ---
    public long addNotification(int userId, String message, String timestamp) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(NOTIF_USER_ID, userId);
        v.put(NOTIF_MESSAGE, message);
        v.put(NOTIF_TIMESTAMP, timestamp);
        long id = db.insert(TABLE_NOTIFICATION, null, v);
        db.close();
        return id;
    }

    public ArrayList<Notification> getNotificationsByUserId(int userId) {
        ArrayList<Notification> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_NOTIFICATION, null, NOTIF_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, NOTIF_ID + " DESC");
        if (c.moveToFirst()) {
            do {
                list.add(new Notification(c.getInt(0), c.getInt(1), c.getString(2), c.getString(3)));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }
}
