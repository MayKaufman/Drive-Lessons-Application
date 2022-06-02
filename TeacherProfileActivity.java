package com.example.myfirstapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.net.Socket;
import java.util.regex.Pattern;

public class TeacherProfileActivity extends OptionsTeacherActivity
{
    private static String TAG;
    private Handler mHandler;
    private TextView name, Id, email, area, phoneNum, car, teacherLessonPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_profile);

        TAG = this.getLocalClassName();
        this.name = findViewById(R.id.name);
        this.Id = findViewById(R.id.Id);
        this.email = findViewById(R.id.email);
        this.area = findViewById(R.id.area);
        this.phoneNum = findViewById(R.id.phoneNum);
        this.car = findViewById(R.id.car1);
        this.teacherLessonPrice = findViewById(R.id.teacherLessonPrice);

        Log.d(TAG, "Start");

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message)
            {
                String data = (String) message.obj;
                Log.d(TAG,data);
                Log.d("is in message?", "yes");
                String[] fields = data.split(Pattern.quote("|")); // the actual value of: |

                String command = fields[1]; // in fields[0] = the size

                Log.d(TAG, "command = " + command);

                if (command.equals("PROFILEDATA"))
                {
                    // change the text to received from server
                    name.setText(new StringBuilder().append("Name: ").append(SessionData.getUsername()).toString());
                    Id.setText(new StringBuilder().append("ID: ").append(SessionData.getID()).toString());
                    email.setText(new StringBuilder().append("Email: ").append(fields[2]));
                    area.setText(new StringBuilder().append("Area: ").append(fields[3]));
                    phoneNum.setText(new StringBuilder().append("Phone Number: ").append(fields[4]));
                    teacherLessonPrice.setText(new StringBuilder().append("Lesson Price: ").append(fields[5]));
                    car.setText(new StringBuilder().append("Car: ").append(fields[6]).append(" ").append(fields[7]));
                }
                else if(command.equals("NEEDACCEPT"))
                    showAlert("The admin has not confirmed you yet");

                else if(command.equals("ERROR")) // show error message
                {
                    String error_num = fields[2];
                    String error_message = fields[3];
                    showAlert("ERROR " + error_num + ": "+error_message);
                }
            }
        };

        sendProfile(); // message to the server to get the user's profile data
    }

    //send the profile message-client wants to receive the user's profile data: size|PROFILE|id
    public void sendProfile()
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
        String data = "PROFILE|" +com.example.myfirstapp.SessionData.getRole() + "|" +com.example.myfirstapp.SessionData.getID();

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