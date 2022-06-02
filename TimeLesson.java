package com.example.myfirstapp;

public class TimeLesson
{
    private String time;
    private String studentName;
    private String location;
    private String studentId;
    private String date;

    public TimeLesson(String time,String studentId, String location, String studentName, String date)
    {
        this.time = time;
        this.studentId = studentId;
        this.location = location;
        this.studentName = studentName;
        this.date = date;
    }

    public String getTime()
    {
        return this.time;
    }

    public String getStudentName()
    {
        return this.studentName;
    }

    public String getLocation()
    {
        return this.location;
    }

    public String getStudentId()
    {
        return this.studentId;
    }

    public String getDate()
    {
        return this.date;
    }
}
