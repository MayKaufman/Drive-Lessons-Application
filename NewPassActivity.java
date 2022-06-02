package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;

import java.io.IOException;
import java.net.Socket;
import java.util.regex.Pattern;

public class NewPassActivity extends AppCompatActivity
{
    public static String TAG;
    private EditText inputTempPass, inputPassword, inputConfirmPassword;

    private String email = "";
    private String login_username = "";
    private Handler mHandler;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    "(?=\\S+$)" +           //no white spaces
                    ".{6,}" +               //at least 6 characters
                    "$");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_pass);

        this.TAG= this.getLocalClassName();
        this.inputTempPass = findViewById(R.id.inputTempPass);
        this.inputPassword = findViewById(R.id.inputPassword);
        this.inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        this.email = getIntent().getStringExtra("EXTRA_email");

        Log.d(this.TAG, "start");

        Button newPassBtn = (Button) findViewById(R.id.buttonNewPass);
        newPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SessionData.setUsername("");
                sendNewPass(v); // send the input data to the server
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
                        Intent teacher_profile = new Intent(NewPassActivity.this, TeacherProfileActivity.class);
                        startActivity(teacher_profile);
                    }
                    else if(role.equals("Student"))
                    {
                        Intent student_profile = new Intent(NewPassActivity.this, StudentProfileActivity.class);
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

    public void sendNewPass(View v)
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

        //checks if all inputs are valid and not empty:
        if (this.inputTempPass.getText().toString().isEmpty()  || this.inputPassword.getText().toString().isEmpty() || this.inputConfirmPassword.getText().toString().isEmpty()) // empty input
        {
            Log.d(TAG,"empty input");
            showAlert("You must fill all information");
            return;
        }

        else if(! this.inputPassword.getText().toString().equals(this.inputConfirmPassword.getText().toString())) // comparing between the 2 received passwords
        {
            Log.d(TAG,"not the same input password");
            inputPassword.setError("The password and the confirmed password must be the same!");
            inputConfirmPassword.setError("The password and the confirmed password must be the same!");
            return;
        }

        else if (!PASSWORD_PATTERN.matcher(inputPassword.getText().toString().trim()).matches()) {
            inputPassword.setError("Password too weak");
            Log.d(TAG,"Weak password");
            return;
        }

        String data = "NEWPASS|" + this.email + "|"+ this.inputTempPass.getText().toString() + "|" + inputPassword.getText().toString();

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