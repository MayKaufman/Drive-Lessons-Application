package com.example.myfirstapp;

public class Notification
{
    private String senderId;
    private String senderName;
    private String content;
    private String status;
    private String sendingTime;
    private String type;
    private String icon;

    public Notification(String senderId, String content, String status, String sendingTime,String type, String senderName)
    {
        this.senderId = senderId;
        this.content = content;
        this.status = status;
        this.sendingTime = sendingTime;
        this.type = type;
        this.senderName = senderName;

        if(this.status.equals("")) // if the status is unread
            this.icon = "unread_bell_image";
        else
            this.icon = "bell_img";
    }

    public String getSenderId()
    {
        return this.senderId;
    }

    public String getSenderName()
    {
        return this.senderName;
    }

    public String getContent()
    {
        return this.content;
    }

    public String getStatus()
    {
        return this.status;
    }

    public String getSendingTime()
    {
        return this.sendingTime;
    }

    public String getType(){
        return this.type;
    }

    public String getIcon()
    {
        return this.icon;
    }
}
