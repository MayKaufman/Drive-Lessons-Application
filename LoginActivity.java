package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import android.os.Message;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import java.util.regex.Pattern;
import java.io.IOException;
import java.net.Socket;

public class LoginActivity extends AppCompatActivity
{
    public static String TAG;

    private EditText inputEmail, inputPassword;

    private Handler mHandler;
    String login_username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login"); //top title

        this.TAG= this.getLocalClassName();

        this.inputEmail = findViewById(R.id.inputEmail);
        this.inputPassword = findViewById(R.id.inputPassword);

        Log.d(this.TAG, "start");

        Button loginBtn = (Button) findViewById(R.id.buttonLogin);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SessionData.setUsername("");
                sendLogin(v); // send the input data to the server
            }
        });

        // go to register activity
        TextView signUpBtn = findViewById(R.id.textSignUp);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
             }
        });

        //go to forgot password activity
        TextView forgotPassBtn = findViewById(R.id.forgotPassword);
        forgotPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPassActivity.class));
            }
        });

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

                if (command.equals("LOGINDATA"))
                {
                    login_username = fields[2];
                    com.example.myfirstapp.SessionData.setUsername(login_username);
                    com.example.myfirstapp.SessionData.setID(fields[3]);
                    String role = fields[4];
                    com.example.myfirstapp.SessionData.setRole(role);

                    if(role.equals("Teacher"))
                    {
                        Intent teacher_profile = new Intent(LoginActivity.this, TeacherProfileActivity.class);
                        startActivity(teacher_profile);
                    }
                    else if(role.equals("Student"))
                    {
                        Intent student_profile = new Intent(LoginActivity.this, StudentProfileActivity.class);
                        startActivity(student_profile);
                    }
                }
                else if(command.equals("ERROR")) // show error message
                {
                    String error_num = fields[2];
                    String error_message = fields[3];
                    showAlert("ERROR " + error_num + ": "+error_message);
                }
            }
        };
    }

    //send the login message: size|LOGIN|email|hashpassword
    public void sendLogin(View v)
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
        if (inputEmail.getText().toString().isEmpty()  || inputPassword.getText().toString().isEmpty()) // empty input
        {
            showAlert("You must fill login information");
            Log.d(TAG,"empty input");
            return;
        }
        String data = "LOGIN|" + inputEmail.getText().toString() + "|" + inputPassword.getText().toString();

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