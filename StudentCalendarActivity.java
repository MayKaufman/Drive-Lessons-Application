package com.example.myfirstapp;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.CalendarView;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class StudentCalendarActivity extends OptionsStudentActivity
{
    private String TAG;
    private CalendarView calendarView;
    private Handler mHandler;

    private RecyclerView recyclerViewFreeLesson;
    private RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
    private FreeLessonAdapter freeLessonAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_calendar);

        setTitle("Calendar"); //top title

        this.TAG= this.getLocalClassName();
        this.calendarView = (CalendarView) findViewById(R.id.calendarView);

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

                if (command.equals("FREEDATA"))
                {
                    String date = fields[2];
                    String teacherId = fields[3];
                    //create a list of the lessons:
                    ArrayList<Lesson> freeLessons = new ArrayList<>();
                    for(int i=4; i<fields.length;i++)
                    {
                        if(fields[i].contains("#"))  // the student has a lesson
                        {
                            String[] lesson_data = fields[i].split(Pattern.quote("#"));
                            freeLessons.add(new Lesson(date, lesson_data[0], "", lesson_data[2], lesson_data[1], "" ));
                        }
                        else
                            freeLessons.add(new Lesson(date, fields[i], "", "", "", "" ));  // lesson's start time
                    }
                    recyclerViewFreeLesson = findViewById(R.id.freeLessonsList);
                    recyclerViewFreeLesson.setLayoutManager(layoutManager); // vertical recyclerview
                    freeLessonAdapter = new FreeLessonAdapter(freeLessons,teacherId, date); // gives the lessons list
                    recyclerViewFreeLesson.setAdapter(freeLessonAdapter);
                }
                else if(command.equals("NEEDTEACHER"))
                {
                    Log.d(TAG,"Doesn't have a teacher");
                    showAlert("You don't have a teacher!Join now!");
                }
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
                sendFree(date);
            }
        });
    }

    //send the FREE message: size|FREE|studentId|date
    public void sendFree(String date)
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
        String data = "FREE|" + com.example.myfirstapp.SessionData.getID() + "|" + date;

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

