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
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.net.Socket;
import java.util.regex.Pattern;
import android.util.Patterns;

public class ForgotPassActivity extends AppCompatActivity
{
    public static String TAG;
    private EditText inputEmail;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);

        this.TAG= this.getLocalClassName();
        this.inputEmail = findViewById(R.id.inputEmail);

        Log.d(this.TAG, "start");

        Button passBtn = (Button) findViewById(R.id.buttonForgot);
        passBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendEmail(v); // send the input data to the server
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

                if (command.equals("ACK"))
                {
                    Intent new_pass = new Intent(ForgotPassActivity.this, NewPassActivity.class);
                    new_pass.putExtra("EXTRA_email", inputEmail.getText().toString());
                    startActivity(new_pass);
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

    //send the requested email to the server
    public void SendEmail(View v)
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

        //checks if the email isn't empty and valid:
        if (inputEmail.getText().toString().isEmpty()) // empty input
        {
            showAlert("You must fill all information");
            Log.d(TAG,"empty input");
            return;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString().trim()).matches())
        {
            inputEmail.setError("Please enter a valid email address");
            Log.d(TAG,"Invalid input");
            return;
        }

        String data = "FORGOT|" + inputEmail.getText().toString();

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