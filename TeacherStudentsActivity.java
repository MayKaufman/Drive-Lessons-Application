package com.example.myfirstapp;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class TeacherStudentsActivity extends OptionsTeacherActivity
{
    private String TAG;
    private Handler mHandler;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
    private StudentAdapter studentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_students);
        setTitle("Students"); //top title

        TAG = this.getLocalClassName();
        Log.d(TAG, "start");

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

                if (command.equals("STUDENTSDATA"))
                {
                    //create a list of the students:
                    ArrayList<Student> students = new ArrayList<>();
                    for(int i=2; i<fields.length;i++)
                    {
                        String[] student_data = fields[i].split(Pattern.quote("#"));
                        students.add(new Student(student_data[0], student_data[1], student_data[2], student_data[3], student_data[4]));  // Id, name, address, phone, debt
                    }
                    recyclerView = findViewById(R.id.studentsList);
                    recyclerView.setLayoutManager(layoutManager); // vertical recyclerview
                    studentAdapter = new StudentAdapter(students); // gives the students list
                    recyclerView.setAdapter(studentAdapter);
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

        sendStudents(); // message to the server to get the teacher's students' data
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.teacher_search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                studentAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    //send the STUDENTS message-client wants to receive the teacher's students' data: size|STUDENTS|teacherId
    public void sendStudents()
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
        String data = "STUDENTS|" +com.example.myfirstapp.SessionData.getID();

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