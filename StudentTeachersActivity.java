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

public class StudentTeachersActivity extends OptionsStudentActivity
{
    private String TAG;
    private Handler mHandler;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
    private TeacherAdapter teacherAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_teachers);
        setTitle("Teachers"); //top title

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

                if (command.equals("TEACHERSDATA"))
                {
                    //create a list of the teachers:
                    ArrayList<Teacher> teachers = new ArrayList<>();
                    for(int i=2; i<fields.length;i++)
                    {
                        String[] teacher_data = fields[i].split(Pattern.quote("#"));
                        teachers.add(new Teacher(teacher_data[0], teacher_data[1], teacher_data[2], teacher_data[3], teacher_data[4], teacher_data[5], teacher_data[6], teacher_data[7]));  // Id, name, area, phone, price, seniority, car
                    }
                    recyclerView = findViewById(R.id.teachersList);
                    recyclerView.setLayoutManager(layoutManager); // vertical recyclerview
                    teacherAdapter = new TeacherAdapter(teachers); // gives the teachers list
                    recyclerView.setAdapter(teacherAdapter);
                }
                else if(command.equals("ERROR")) // show error message
                {
                    String error_num = fields[2];
                    String error_message = fields[3];
                    showAlert("ERROR " + error_num + ": "+error_message);
                }
            }
        };

        sendTeachers(); // message to the server to get the teachers' data
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.student_search_menu, menu);
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
                teacherAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }


    //send the Teachers message-user wants to receive the teachers'  data: size|TEACHERS
    public void sendTeachers()
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
        String data = "TEACHERS";

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