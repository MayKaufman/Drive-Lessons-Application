package com.example.myfirstapp;


public class SessionData
{
    private static String username;
    private static String ID;
    private static String role;

    public static synchronized String getUsername()
    {
        return username;
    }
    public static synchronized void setUsername(String username)
    {
        SessionData.username = username;
    }

    public static synchronized String getID()
    {
        return ID;
    }
    public static synchronized void setID(String ID)
    {
        SessionData.ID = ID;
    }

    public static synchronized String getRole()
    {
        return role;
    }
    public static synchronized void setRole(String role)
    {
        SessionData.role = role;
    }
}

