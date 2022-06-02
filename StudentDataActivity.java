package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class StudentDataActivity extends AppCompatActivity
{
    private String TAG;
    private Handler mHandler;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
    private LessonAdapter lessonAdapter;

    private String studentId, studentName;
    private TextView nameStudentLogo, totalLessons, totalPaid, totalDebt;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_data);
        setTitle("Lessons"); //top title

        this.studentId = getIntent().getStringExtra("EXTRA_STUDENT_ID");
        this.studentName = getIntent().getStringExtra("EXTRA_STUDENT_NAME");

        this.TAG = this.getLocalClassName();
        this.nameStudentLogo = findViewById(R.id.nameStudentLogo);
        this.totalLessons = findViewById(R.id.totalLessons);
        this.totalPaid = findViewById(R.id.totalPaid);
        this.totalDebt = findViewById(R.id.totalDebt);

        Log.d(TAG, "start");

        mHandler = new Handler(Looper.getMainLooper())
        {
            @SuppressLint("SetTextI18n")
            @Override
            public void handleMessage(Message message)
            {
                String data = (String) message.obj;
                Log.d(TAG,data);
                String[] fields = data.split(Pattern.quote("|")); // the actual value of: |

                String command = fields[1]; // in fields[0] = the size
                Log.d(TAG, "command = " + command);

                if (command.equals("STUDENTLESSONSDATA"))
                {
                    nameStudentLogo.setText(studentName);  //student name
                    totalLessons.setText("Total Lessons: "+fields[2]);
                    totalPaid.setText("Total Paid: "+fields[3]);
                    totalDebt.setText("Total Debt: "+fields[4]);

                    //create a list of the lessons:
                    ArrayList<Lesson> lessons = new ArrayList<>();
                    for(int i=5; i<fields.length;i++)
                    {
                        String[] lesson_data = fields[i].split(Pattern.quote("#"));
                        lessons.add(new Lesson(lesson_data[0], lesson_data[1], lesson_data[2], lesson_data[3], lesson_data[4], lesson_data[5]));  // date, time, duration, price, location, isPaid
                    }
                    recyclerView = findViewById(R.id.recyclerStudentTable);
                    recyclerView.setLayoutManager(layoutManager); // vertical recyclerview
                    lessonAdapter = new LessonAdapter(lessons, studentId, studentName); // gives the lessons list, the studentId and the studentName
                    recyclerView.setAdapter(lessonAdapter);
                }
                else if(command.equals("ERROR")) // show error message
                {
                    String error_num = fields[2];
                    String error_message = fields[3];
                    showAlert("ERROR " + error_num + ": "+error_message);
                }
            }
        };

        sendStudentLessons(); // message to the server to get the student's lessons' data
    }

    //send the STUDENTLESSONS message-client wants to receive the students' data: size|STUDENTLESSONS|studentId
    public void sendStudentLessons()
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
        String data = "STUDENTLESSONS|" + this.studentId;

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