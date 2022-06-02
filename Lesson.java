package com.example.myfirstapp;

public class Lesson
{
    private String date;
    private String time;
    private String duration;
    private String price;
    private String location;
    private String isPaid;

    public Lesson(String date, String time, String duration, String price, String location, String isPaid)
    {
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.price = price;
        this.location = location;
        this.isPaid = isPaid;
    }

    public String getDate()
    {
        return this.date;
    }

    public String getTime()
    {
        return this.time;
    }

    public String getDuration()
    {
        return this.duration;
    }

    public String getPrice()
    {
        return this.price;
    }

    public String getLocation()
    {
        return this.location;
    }

    public String getIsPaid()
    {
        return this.isPaid;
    }
}
