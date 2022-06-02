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

public class StudentProfileActivity extends OptionsStudentActivity
{
    private static String TAG;
    private Handler mHandler;
    private TextView name, Id, email, address, phoneNum, birthdate, teacherName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        TAG = this.getLocalClassName();
        this.name = findViewById(R.id.name);
        this.Id = findViewById(R.id.Id);
        this.email = findViewById(R.id.email);
        this.address = findViewById(R.id.address);
        this.phoneNum = findViewById(R.id.phoneNum);
        this.birthdate = findViewById(R.id.birthdate);
        this.teacherName = findViewById(R.id.teacherName);

        Log.d(TAG, "Start");

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message)
            {
                String data = (String) message.obj;
                Log.d(TAG,data);
                String[] fields = data.split(Pattern.quote("|")); // the actual value of: |

                String command = fields[1]; // in fields[0] = the size

                Log.d(TAG, "command = " + command);

                if (command.equals("PROFILEDATA"))
                {
                    // change the text to received from server
                    name.setText(new StringBuilder().append("Name: ").append(SessionData.getUsername()).toString());
                    Id.setText(new StringBuilder().append("ID: ").append(SessionData.getID()).toString());
                    email.setText(new StringBuilder().append("Email: ").append(fields[2]));
                    address.setText(new StringBuilder().append("Address: ").append(fields[3]));
                    phoneNum.setText(new StringBuilder().append("Phone Number: ").append(fields[4]));
                    birthdate.setText(new StringBuilder().append("Birthdate: ").append(fields[5]));

                    if (fields.length > 6)  // the student has a teacher:
                        teacherName.setText(new StringBuilder().append("Teacher: ").append(fields[7]));
                    else
                        teacherName.setText(new StringBuilder().append("Teacher: None"));
                }
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
        String data = "PROFILE|" + com.example.myfirstapp.SessionData.getRole() + "|" +com.example.myfirstapp.SessionData.getID();

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