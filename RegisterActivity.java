package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioButton;

import java.io.IOException;
import java.net.Socket;
import java.util.regex.Pattern;
import android.util.Patterns;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class RegisterActivity extends AppCompatActivity
{
    public static String TAG;

    private EditText inputEmail, inputPassword, inputConfirmPassword, inputName, inputBirthdate, inputID, inputAddress, inputPhone;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private Handler mHandler;
    private String login_username = "", role;
    private CountDownTimer countDownTimer;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    "(?=\\S+$)" +           //no white spaces
                    ".{6,}" +               //at least 6 characters
                    "$");

    private static final Pattern ID_PATTERN = Pattern.compile("^[0-9]{9}$"); //only digits, 9 characters
    private static final Pattern PHONE_PATTERN = Pattern.compile("^05([0-9]){8}$"); //only digits, 10 characters, starts with 05
    private static final String DATE_PATTERN = "dd.MM.yyyy"; // for the birthdate

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        this.TAG= this.getLocalClassName();

        this.inputEmail = findViewById(R.id.inputEmail);
        this.inputPassword = findViewById(R.id.inputPassword);
        this.inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        this.inputName = findViewById(R.id.inputName);
        this.radioGroup = (RadioGroup) findViewById(R.id.radioGroup); // button for role
        this.inputBirthdate = findViewById(R.id.inputBirthdate);
        this.inputID = findViewById(R.id.inputID);
        this.inputAddress = findViewById(R.id.inputAddress);
        this.inputPhone = findViewById(R.id.inputPhoneNumber);

        Log.d(TAG, "start");

        Button registerBtn = (Button) findViewById(R.id.btnRegister);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SessionData.setUsername("");
                sendRegister(v); // send the input data to the server
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

                if(command.equals("ACK")) // the email was sent successfully
                {
                    showTempAlert();
                }
                else if (command.equals("LOGINDATA"))
                {
                    login_username = fields[2];
                    com.example.myfirstapp.SessionData.setUsername(login_username);
                    com.example.myfirstapp.SessionData.setID(fields[3]);
                    String role = fields[4];
                    com.example.myfirstapp.SessionData.setRole(role);

                    if(role.equals("Teacher"))
                    {
                        Intent teacher_info = new Intent(RegisterActivity.this, MoreInfoActivity.class);
                        startActivity(teacher_info);
                    }
                    else if(role.equals("Student"))
                    {
                        Intent student_profile = new Intent(RegisterActivity.this, StudentProfileActivity.class);
                        startActivity(student_profile);
                    }
                }
                else if(command.equals("DELETED"))
                {
                    Log.d(TAG, "timeout");

                    showAlert("Timeout-Your account was deleted!");
                    // delay for 3 seconds before restart:
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable()
                    {
                        public void run()
                        {
                            Intent login = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(login);
                            finishAffinity(); // for 'restart'
                        }}, 3000);   //3 seconds
                }
                else if(command.equals("ERROR")) // show error message
                {
                    Log.d(TAG, "in alert");
                    String error_num = fields[2];
                    String error_message = fields[3];

                    showAlert("ERROR " + error_num + ": "+error_message);
                }
            }
        };
    }

    //checks if the input's value is valid:
    private boolean validateInput(EditText input, Pattern pattern)
    {
        String InputString = input.getText().toString().trim();

        if (!pattern.matcher(InputString).matches()) // not like tha pattern-alert the user
        {
            if(input.getHint().toString().equals("Password"))
                input.setError("Password too weak");
            else if(input.getHint().toString().equals("Birthdate"))
                input.setError("A date should be like dd.mm.yyyy");
            else if(input.getHint().toString().equals("ID"))
                input.setError("An Israeli ID should be 9 digits");
            else
                input.setError("Not valid input");
            return false;
        }
        input.setError(null);
        return true;
    }

    //check if a date is in the valid date format
    private static boolean checkIfDateIsValid(String date, String pattern)
    {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        // With lenient parsing, the parser may use heuristics to interpret
        // inputs that do not precisely match this object's format.
        format.setLenient(false);
        try
        {
            format.parse(date);
        }
        catch (ParseException e)
        {
            return false;
        }
        return true;
    }

    //checks if all the input aren't empty and that are valid:
    private boolean confirmInput()
    {
        //checks for empty value:
        if (inputEmail.getText().toString().isEmpty()  || inputPassword.getText().toString().isEmpty() || inputConfirmPassword.getText().toString().isEmpty()
                || inputName.getText().toString().isEmpty() || inputBirthdate.getText().toString().isEmpty() || inputID.getText().toString().isEmpty()
                || inputAddress.getText().toString().isEmpty() || inputPhone.getText().toString().isEmpty()
                || radioGroup.getCheckedRadioButtonId() == -1)
        {
            showAlert("You must fill all register information");
            Log.d(TAG,"empty input");
            return false;
        }

        else if(! this.inputPassword.getText().toString().equals(this.inputConfirmPassword.getText().toString())) // comparing between the 2 received passwords
        {
            inputPassword.setError("The password and the confirmed password must be the same!");
            inputConfirmPassword.setError("The password and the confirmed password must be the same!");
            Log.d(TAG,"not the same input password");
            return false;
        }

        else if(!validateInput(inputEmail, Patterns.EMAIL_ADDRESS) || !validateInput(inputPassword, PASSWORD_PATTERN)
                || !validateInput(inputID, ID_PATTERN) || !validateInput(inputPhone, PHONE_PATTERN))
        {
            Log.d(TAG,"Not valid input");
            return false;
        }

        else if(!checkIfDateIsValid(inputBirthdate.getText().toString(), DATE_PATTERN))
        {
            inputBirthdate.setError("A date should be like dd.mm.yyyy"); // tells the user
            Log.d(TAG,"Not valid date- need to be by the pattern: dd.MM.yyyy");
            return false;
        }
        return true;
    }

    //send the register message: size|REGISTER|email|hashedPass|name|role|birthdate|id|phoneNum|address
    public void sendRegister(View v)
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

        if(!confirmInput())  // if all the input aren't valid- not sending the message!
            return;

        int selectedId = radioGroup.getCheckedRadioButtonId();// selected radio button's id from radioGroup
        radioButton = (RadioButton) findViewById(selectedId); // find the radiobutton by returned id
        role =  radioButton.getText().toString(); // the text of the chosen role -teacher/student

        String to_send = "REGISTER|" + inputEmail.getText().toString() + "|" + inputPassword.getText().toString() + "|" + inputName.getText().toString() + "|" + role + "|" + inputBirthdate.getText().toString() + "|"
                + inputID.getText().toString() + "|" + inputPhone.getText().toString() + "|" + inputAddress.getText().toString();

        Log.d(TAG,"before sending");
        Log.d(TAG,to_send);
        tcp_send_recv bg = new tcp_send_recv(mHandler);
        bg.execute(to_send);

        Log.d(TAG,"after sending");
    }

    public void sendMessage(String to_send)
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
        builder.setMessage(content).setPositiveButton("Ok", null);
        AlertDialog alert = builder.create(); // create the alert box
        alert.show(); // show the alert
    }

    //The alert for receiving the temporary password that was sent to the user's email- timeout after 1 minute.
    public void showTempAlert()
    {
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        final EditText inputTemp = new EditText(this);
        inputTemp.setInputType(InputType.TYPE_CLASS_TEXT);

        builder.setView(inputTemp); // a text box for input the location
        builder.setTitle("Please enter the temporary password that was sent to your email:");
        builder.setMessage(" ");
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i)
            {
                countDownTimer.cancel();
                String temp = inputTemp.getText().toString();
                String to_send = "TEMP|" + inputEmail.getText().toString() + "|" + temp + "|" + inputPassword.getText().toString();
                sendMessage(to_send); //send the Temp message: size|TEMP|email|temp_password|password
            }
        });

        AlertDialog alert = builder.create(); // create the alert box
        //updating the text after the dialog started:
        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface)
            {
                countDownTimer = new CountDownTimer(60000, 1000)
                {
                    @Override
                    public void onTick(long millisUntilFinished)
                    {
                        alert.setMessage("Seconds Remaining: "+ millisUntilFinished/1000);
                    }
                    @Override
                    public void onFinish()
                    {
                        Log.d(TAG, "in finish");
                        if(alert.isShowing())  // close the alert dialog
                            alert.dismiss();
                        String to_send = "TIMEOUT|" + inputEmail.getText().toString();
                        sendMessage(to_send); //send the Timeout message: size|TIMEOUT|email
                        countDownTimer.cancel();
                    }
                }.start();
            }
        });
        alert.show(); // show the alert
    }
}