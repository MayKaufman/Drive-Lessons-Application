package com.example.myfirstapp;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.Button;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class TeacherCalendarActivity extends OptionsTeacherActivity
{
    private String TAG;
    private CalendarView calendarView;
    private Handler mHandler;

    private RecyclerView recyclerViewTime;
    private RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
    private TimeAdapter timeAdapter;
    private Button cancelAllBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_calendar);
        setTitle("Calendar"); //top title

        this.TAG= this.getLocalClassName();
        this.calendarView = (CalendarView) findViewById(R.id.calendarView);
        this.cancelAllBtn = findViewById(R.id.cancelAllBtn);
        this.cancelAllBtn.setVisibility(View.INVISIBLE); // disappear button until user chooses a day

        Log.d(this.TAG, "start");

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

                if (command.equals("DAYDATA"))
                {
                    String date = fields[2];
                    //create a list of the lessons:
                    ArrayList<TimeLesson> lessons = new ArrayList<>();
                    for(int i=3; i<fields.length;i++)
                    {
                        String[] lesson_data = fields[i].split(Pattern.quote("#"));
                        lessons.add(new TimeLesson(lesson_data[0], lesson_data[1], lesson_data[2], lesson_data[3], date));  // time, studentId, location, studentName, date
                    }
                    recyclerViewTime = findViewById(R.id.timeLessonsList);
                    recyclerViewTime.setLayoutManager(layoutManager); // vertical recyclerview
                    timeAdapter = new TimeAdapter(lessons); // gives the lessons list
                    recyclerViewTime.setAdapter(timeAdapter);

                    //to cancel all day
                    cancelAllBtn.setVisibility(View.VISIBLE);
                    cancelAllBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(TeacherCalendarActivity.this);
                            builder.setMessage("Are you sure you want to cancel all the lessons on this day?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int i)
                                        {
                                            sendCancelAll(date, lessons);
                                        }
                                    }).setNegativeButton("No",null);
                            AlertDialog alert =builder.create(); // create the alert box
                            alert.show(); // show the alert
                        }
                    });
                }

                else if (command.equals("ACK"))  // alert- the requests were submitted
                    showAlert("Your requests were submitted- waiting for approval");

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
        //When user chooses a specific day:
        this.calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth)
            {
                String date = dayOfMonth + "." + (month+1) + "." + year;  // in android must add 1 to the month
                Log.d(TAG, date);
                sendDay(date);
            }
        });
    }

    //send the DAY message: size|DAY|teacherId|date
    public void sendDay(String date)
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
        String data = "DAY|" + com.example.myfirstapp.SessionData.getID() + "|" + date;

        Log.d(TAG,"before sending");
        Log.d(TAG,data);
        tcp_send_recv bg = new tcp_send_recv(mHandler);
        bg.execute(data);

        Log.d(TAG,"after sending");
    }

    //send the CANCELALLREQUEST message: size|CANCELALLREQUEST|teacherId|date|studentId#time
    public void sendCancelAll(String date,ArrayList<TimeLesson> lessons)
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
        String data = "CANCELALLREQUEST|" + com.example.myfirstapp.SessionData.getID() + "|" + date;

        for(int i=0; i<lessons.size(); i++) // adds the lesson's data
        {
            if(! lessons.get(i).getStudentName().equals(" ")) // to check if the lesson is occupied (not an empty lesson)
                data += "|" + lessons.get(i).getStudentId() + "#" + lessons.get(i).getTime();
        }

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
        builder.setMessage(content).setPositiveButton("Ok", null);
        AlertDialog alert = builder.create(); // create the alert box
        alert.show(); // show the alert
    }
}