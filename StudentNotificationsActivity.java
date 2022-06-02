package com.example.myfirstapp;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class StudentNotificationsActivity extends OptionsStudentActivity
{
    private String TAG;
    private Handler mHandler;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
    private NotificationAdapter notificationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        setTitle("Notifications"); //top title

        TAG = this.getLocalClassName();
        Log.d(TAG, "start");

        mHandler = new Handler(Looper.getMainLooper())
        {
            @Override
            public void handleMessage(Message message)
            {
                String data = (String) message.obj;
                Log.d(TAG,data);
                String[] fields = data.split(Pattern.quote("|")); // the actual value of: |

                String command = fields[1]; // in fields[0] = the size

                Log.d(TAG, "command = " + command);

                if (command.equals("NOTIFICATIONSDATA"))
                {
                    //create a list of the notifications:
                    ArrayList<Notification> notifications = new ArrayList<>();
                    for(int i=2; i<fields.length;i++)
                    {
                        String[] notification_data = fields[i].split(Pattern.quote("#"));
                        notifications.add(new Notification(notification_data[0], notification_data[1], notification_data[2], notification_data[3], notification_data[4], notification_data[5]));  // senderId, content, status, sendingTime,type, senderName
                    }
                    recyclerView = findViewById(R.id.notificationsList);
                    recyclerView.setLayoutManager(layoutManager); // vertical recyclerview
                    notificationAdapter = new NotificationAdapter(notifications); // gives the notifications list
                    recyclerView.setAdapter(notificationAdapter);

                    //initialize the touch helper
                    new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT)
                    {
                        @Override
                        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target)
                        {
                            return false;
                        }

                        @Override
                        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction)
                        {
                            Notification currentNotification = notifications.get(viewHolder.getAbsoluteAdapterPosition()); // the current notification
                            if (currentNotification.getStatus().equals(""))
                            {
                                notificationAdapter.notifyDataSetChanged();
                                showAlert("You can't delete a notification before answering it!");
                            }
                            else
                            {
                                notifications.remove(viewHolder.getAbsoluteAdapterPosition());
                                notificationAdapter.notifyDataSetChanged();
                                sendDeleteNotification(currentNotification);
                            }
                        }
                    }).attachToRecyclerView(recyclerView);
                }
                else if(command.equals("ERROR")) // show error message
                {
                    String error_num = fields[2];
                    String error_message = fields[3];
                    showAlert("ERROR " + error_num + ": "+error_message);
                }
            }
        };

        sendNotifications(); // message to the server to get the users' notifications
    }

    //send the NOTIFICATIONS message-client wants to receive the users' notifications: size|NOTIFICATIONS|Id
    public void sendNotifications()
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
        String data = "NOTIFICATIONS|" +com.example.myfirstapp.SessionData.getID();

        Log.d(TAG,"before sending");
        Log.d(TAG,data);
        tcp_send_recv bg = new tcp_send_recv(mHandler);
        bg.execute(data);

        Log.d(TAG,"after sending");
    }

    //send the DELETENOTIFICATION message-client wants to delete a notification: size|DELETENOTIFICATION|senderId|receiverId|sendingTime
    private void sendDeleteNotification(Notification current)
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
        String data = "DELETENOTIFICATION|" + current.getSenderId() + "|" + com.example.myfirstapp.SessionData.getID() + "|" + current.getSendingTime();

        Log.d(TAG,"before sending");
        Log.d(TAG,data);
        tcp_send_recv bg = new tcp_send_recv(mHandler);
        bg.execute(data);

        Log.d(TAG,"after sending");
    }

    //receives a string and show it on the phone's screen.
    public void showAlert(String content)
    {
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setMessage(content)
                .setPositiveButton("Ok", null);
        AlertDialog alert = builder.create(); // create the alert box
        alert.show(); // show the alert
    }
}