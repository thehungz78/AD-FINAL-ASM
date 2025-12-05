package com.example.se07203_b5;

public class User {
    private int id;
    private String username;
    private String password;
    private String fullname;

    public User(int id, String username, String password, String fullname)
    {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullname = fullname;
    }

    public String getUsername(){
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }
    public String getFullname(){
        return this.fullname;
    }

    public int getId(){
        return this.id;
    }
}
