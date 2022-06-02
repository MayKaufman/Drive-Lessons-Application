package com.example.myfirstapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class TimeAdapter extends RecyclerView.Adapter<TimeAdapter.TimeLessonViewHolder>
{
    private ArrayList<TimeLesson> lessons;
    public Context context;
    private String TAG = "TimeAdapter";
    private Handler mHandler = new Handler(Looper.getMainLooper())
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
                showAlert("The message was received in the system- waiting for approval!");

            else if(command.equals("ERROR")) // show error message
            {
                String error_num = fields[2];
                String error_message = fields[3];
                showAlert("ERROR " + error_num + ": "+error_message);
            }
        }
    };

    public TimeAdapter(ArrayList<TimeLesson> lessons)
    {
        this.lessons = lessons;
    }

    @NonNull
    @Override
    public TimeLessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View lessonView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_calendar_time, parent, false);
        this.context = parent.getContext();
        return new TimeLessonViewHolder(lessonView);
    }

    // who need to be load
    @Override
    public void onBindViewHolder(@NonNull TimeLessonViewHolder holder, int position)
    {
        TimeLesson currentLesson = lessons.get(position);

        // change the text to the received data from the server:
        holder.startTime.setText(currentLesson.getTime());
        holder.studentName.setText(currentLesson.getStudentName());
        holder.location.setText(currentLesson.getLocation());

        if (currentLesson.getStudentName().equals(" ")) // empty lesson
            holder.cancelButton.setVisibility(View.INVISIBLE);
        else
        {
            //To open the confirm cancel page
            holder.cancelButton.setVisibility(View.VISIBLE);
            holder.cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    AlertDialog.Builder builder= new AlertDialog.Builder(context); // a builder
                    // 2 buttons for the confirm : Yes/No
                    builder.setMessage("Are you sure you want to cancel the lesson?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i)
                                {
                                    sendCancelLesson(currentLesson);
                                }
                            }).setNegativeButton("No",null);
                    AlertDialog alert =builder.create(); // create the alert box
                    alert.show(); // show the alert
                }
            });
        }
    }


    @Override
    public int getItemCount()  // return the number of students
    {
        return lessons.size();
    }

    //send the CANCELREQUEST message: size|CANCELREQUEST|senderId|receiverId|date|time
    public void sendCancelLesson(TimeLesson currentLesson)
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
        String data = "CANCELREQUEST|" + com.example.myfirstapp.SessionData.getID() + "|" + currentLesson.getStudentId()+ "|" + currentLesson.getDate() + "|" + currentLesson.getTime();

        Log.d(TAG,"before sending");
        Log.d(TAG,data);
        tcp_send_recv bg = new tcp_send_recv(mHandler);
        bg.execute(data);

        Log.d(TAG,"after sending");
    }

    //receives a string and show it on the phone's screen+ refresh the screen
    public void showAlert(String content)
    {
        AlertDialog.Builder builder= new AlertDialog.Builder(context);
        builder.setMessage(content)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) //for refresh
                    {
                        Intent refresh = new Intent(context, context.getClass());
                        context.startActivity(refresh);
                    }
                });
        AlertDialog alert = builder.create(); // create the alert box
        alert.show(); // show the alert
    }

    public static class TimeLessonViewHolder extends RecyclerView.ViewHolder
    {
        public TextView startTime;
        public TextView studentName;
        public TextView location;
        public Button cancelButton;

        public TimeLessonViewHolder(@NonNull View itemView)
        {
            super(itemView);
            this.startTime = itemView.findViewById(R.id.startTime);
            this.studentName = itemView.findViewById(R.id.studentName);
            this.location = itemView.findViewById(R.id.location);
            this.cancelButton = itemView.findViewById(R.id.cancelButton);
        }
    }
}
