package com.example.myfirstapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>
{
    private ArrayList<Notification> notifications;
    public Context context;
    private String TAG = "NotificationAdapter";
    private Handler mHandler = new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(Message message)
        {
            String data = (String) message.obj;
            Log.d(TAG,data);
            String[] fields = data.split(Pattern.quote("|")); // the actual value of: |

            String command = fields[1]; // in fields[0] = the size

            Log.d(TAG, "command = " + command);

            if (command.equals("ACK"))  // alert-the lesson canceled successfully!
                showAlert("The message was received in the system");

            else if(command.equals("OCCUPIED")) // in case the lesson was already occupied
                showAlert("The lesson is already occupied-the request was canceled automatically!");

            else if(command.equals("ERROR")) // show error message
            {
                String error_num = fields[2];
                String error_message = fields[3];
                showAlert("ERROR " + error_num + ": "+error_message);
            }
        }
    };

    public NotificationAdapter(ArrayList<Notification> notifications)
    {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View notificationView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_notification, parent, false);
        this.context = parent.getContext();
        return new NotificationViewHolder(notificationView);
    }

    // who need to be load
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position)
    {
        Notification currentNotification = notifications.get(position);
        // change the text to the received data from the server:
        holder.senderName.setText(currentNotification.getSenderName());
        holder.messageContent.setText(currentNotification.getContent());
        holder.sendingTime.setText(currentNotification.getSendingTime());
        holder.imageBellIcon.setImageResource(holder.senderName.getResources().getIdentifier(currentNotification.getIcon(), "drawable", holder.senderName.getContext().getPackageName()));

        if(currentNotification.getStatus().equals("")) // if the notification unread
        {
            holder.notificationsLayout.setBackgroundColor(Color.parseColor("#B3B3BF"));

            if(currentNotification.getType().equals("ANSWER") || currentNotification.getType().equals("CANCELREQUEST")) //no need the cancel button
            {
                holder.cancelButton.setVisibility(View.INVISIBLE);
                holder.okButton.setVisibility(View.VISIBLE);
                holder.okButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view) {
                        sendNotificationStatus(currentNotification, "True");
                    }
                });
            }
            else
            {
                //To open the confirm ok page
                holder.okButton.setVisibility(View.VISIBLE);
                holder.okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        AlertDialog.Builder builder= new AlertDialog.Builder(context); // a builder
                        builder.setMessage("Are you sure you want to accept the request?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i)
                                    {
                                        sendNotificationStatus(currentNotification, "True");
                                    }
                                }).setNegativeButton("No",null);
                        AlertDialog alert =builder.create(); // create the alert box
                        alert.show(); // show the alert
                    }
                });

                //To open the confirm cancel page
                holder.cancelButton.setVisibility(View.VISIBLE);
                holder.cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        AlertDialog.Builder builder= new AlertDialog.Builder(context); // a builder
                        // 2 buttons for the confirm : Yes/No
                        builder.setMessage("Are you sure you want to cancel the request?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i)
                                    {
                                        sendNotificationStatus(currentNotification, "False");
                                    }
                                }).setNegativeButton("No",null);
                        AlertDialog alert =builder.create(); // create the alert box
                        alert.show(); // show the alert
                    }
                });
            }
        }
        else  // no need in buttons
        {
            holder.notificationsLayout.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
            holder.okButton.setVisibility(View.INVISIBLE);
            holder.cancelButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount()  // return the number of notifications
    {
        return notifications.size();
    }

    //send the NOTIFICATIONSTATUS message: size|NOTIFICATIONSTATUS|senderId|receiverId|sendingTime|currentStatus
    public void sendNotificationStatus(Notification currentNotification, String status)
    {
        Log.d("is_ok","yes");
        Socket sk = SocketHandler.getSocket();

        if (sk != null)
        {
            try
            {
                sk.close();
                SocketHandler.setSocket(null);
            }
            catch (IOException e)
            {
                Log.e(TAG, "ERROR IOException close socket");
            }
        }
        String data = "NOTIFICATIONSTATUS|" + currentNotification.getSenderId() + "|" + com.example.myfirstapp.SessionData.getID() + "|" + currentNotification.getSendingTime() + "|" + status;

        Log.d(TAG,"before sending");
        Log.d(TAG,data);
        tcp_send_recv bg = new tcp_send_recv(mHandler);
        bg.execute(data);

        Log.d(TAG,"after sending");
    }

    //receives a string and show it on the phone's screen+ refresh the screen
    public void showAlert(String content)
    {
        AlertDialog.Builder builder= new AlertDialog.Builder(context);
        builder.setMessage(content)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) //for refresh
                    {
                        Intent refresh = new Intent(context, context.getClass());
                        context.startActivity(refresh);
                    }
                });
        AlertDialog alert = builder.create(); // create the alert box
        alert.show(); // show the alert
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderName;
        public TextView messageContent;
        public TextView sendingTime;
        public ImageView imageBellIcon;

        public ConstraintLayout notificationsLayout;
        public Button okButton;
        public Button cancelButton;

        public NotificationViewHolder(@NonNull View itemView)
        {
            super(itemView);
            this.senderName = itemView.findViewById(R.id.senderName);
            this.messageContent = itemView.findViewById(R.id.messageContent);
            this.sendingTime = itemView.findViewById(R.id.sendingTime);
            this.imageBellIcon = itemView.findViewById(R.id.imageBellIcon);

            this.notificationsLayout = itemView.findViewById(R.id.recyclerNotification);
            this.okButton = itemView.findViewById(R.id.okButton);
            this.cancelButton = itemView.findViewById(R.id.cancelButton);
        }
    }
}
