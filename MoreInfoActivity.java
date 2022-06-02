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
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.IOException;
import java.net.Socket;
import java.util.regex.Pattern;

public class MoreInfoActivity extends AppCompatActivity
{
    public static String TAG;

    private EditText inputArea, inputSeniority, inputCarModel, inputYear,  inputCategorySize, inputLessonPrice;
    private String carType;

    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_info);

        TAG= this.getLocalClassName();

        this.inputArea = findViewById(R.id.inputArea);
        this.inputSeniority = findViewById(R.id.inputSeniority);
        this.inputCarModel = findViewById(R.id.inputCarModel);
        this.inputYear = findViewById(R.id.inputYear);
        this.inputCategorySize = findViewById(R.id.inputCategorySize);
        this.inputLessonPrice = findViewById(R.id.inputLessonPrice);
        this.radioGroup = (RadioGroup) findViewById(R.id.radioGroup); // button for car type

        Log.d(TAG, "start");

        Button submitBtn = (Button) findViewById(R.id.btnSubmit);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get selected radio button from radioGroup
                int selectedId = radioGroup.getCheckedRadioButtonId();

                // find the radiobutton by returned id
                radioButton = (RadioButton) findViewById(selectedId);
                carType = radioButton.getText().toString(); // the text of the chosen role -teacher/student

                SessionData.setUsername("");
                sendSubmit(v); // send the input data to the server
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
                    Intent teacher_info = new Intent(MoreInfoActivity.this, TeacherProfileActivity.class);
                    startActivity(teacher_info);
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

    public void sendSubmit(View v)
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
        if (inputArea.getText().toString().isEmpty()  || inputSeniority.getText().toString().isEmpty()
                || inputCarModel.getText().toString().isEmpty() || inputYear.getText().toString().isEmpty()
                || inputCategorySize.getText().toString().isEmpty() || inputLessonPrice.getText().toString().isEmpty()
                || radioGroup.getCheckedRadioButtonId() == -1) // empty input
        {
            showAlert("You must fill all register information");
            Log.d(TAG,"empty input");
            return;
        }

        String to_send = "INFO|" +com.example.myfirstapp.SessionData.getID() + "|" + inputArea.getText().toString() +
                "|" + inputSeniority.getText().toString() + "|" + inputCarModel.getText().toString()
                + "|" + inputYear.getText().toString() + "|" + inputCategorySize.getText().toString() + "|" + carType + "|" + inputLessonPrice.getText().toString();

        Log.d(TAG,"before sending");
        Log.d(TAG,to_send);
        tcp_send_recv bg = new tcp_send_recv(mHandler);
        bg.execute(to_send);

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