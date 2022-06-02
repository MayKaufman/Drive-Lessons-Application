package com.example.myfirstapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder>
{
    private ArrayList<Lesson> lessons;
    private Context context;
    private String TAG = "Lesson Adapter";
    private String studentId;
    private String studentName;

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

            if (command.equals("ACK"))  // alert
                showAlert("The lesson's payment status was changed successfully!");

            else if(command.equals("ERROR")) // show error message
            {
                String error_num = fields[2];
                String error_message = fields[3];
                showAlert("ERROR " + error_num + ": "+error_message);
            }
        }
    };

    public LessonAdapter(ArrayList<Lesson> lessons, String studentId, String studentName)
    {
        this.lessons = lessons;
        this.studentId = studentId;
        this.studentName = studentName;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View lessonView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_lesson_table, parent, false);
        this.context = parent.getContext();
        return new LessonViewHolder(lessonView);
    }

    // who need to be load
    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position)
    {
        Lesson currentLesson = lessons.get(position);
        // change the text to the received data from the server:
        holder.lessonDate.setText(currentLesson.getDate());
        holder.lessonTime.setText(currentLesson.getTime());
        holder.lessonDuration.setText(currentLesson.getDuration());
        holder.lessonPrice.setText(currentLesson.getPrice());
        holder.lessonLocation.setText(currentLesson.getLocation());
        holder.lessonIsPaid.setText(currentLesson.getIsPaid());

        //If the teacher wants to change the payment status-send the message Payment to the server
        if (com.example.myfirstapp.SessionData.getRole().equals("Teacher"))
        {
            holder.layoutLessonTable.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    AlertDialog.Builder builder= new AlertDialog.Builder(context); // a builder
                    builder.setMessage("Are you sure you want to change the lesson's status from "+currentLesson.getIsPaid()+"?"+"\nLesson's price: "+currentLesson.getPrice())
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i)
                                {
                                    sendPayment(currentLesson);
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

    //send the PAYMENT message: size|Payment|date|time|studentId|isPaid
    public void sendPayment(Lesson currentLesson)
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
        String data = "PAYMENT|" + currentLesson.getDate() + "|" + currentLesson.getTime() + "|" + studentId + "|" + currentLesson.getIsPaid();

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
                    public void onClick(DialogInterface dialogInterface, int i) //for refresh-studentData activity
                    {
                        Intent refresh = new Intent(context, context.getClass());
                        refresh.putExtra("EXTRA_STUDENT_ID", studentId); // values for studentData activity
                        refresh.putExtra("EXTRA_STUDENT_NAME", studentName);
                        context.startActivity(refresh);
                    }
                });
        AlertDialog alert = builder.create(); // create the alert box
        alert.show(); // show the alert
    }

    public static class LessonViewHolder extends RecyclerView.ViewHolder
    {
        public TextView lessonDate;
        public TextView lessonTime;
        public TextView lessonDuration;
        public TextView lessonPrice;
        public TextView lessonLocation;
        public TextView lessonIsPaid;

        public TableLayout layoutLessonTable;

        public LessonViewHolder(@NonNull View itemView)
        {
            super(itemView);
            this.lessonDate = itemView.findViewById(R.id.lessonDate);
            this.lessonTime = itemView.findViewById(R.id.lessonTime);
            this.lessonDuration = itemView.findViewById(R.id.lessonDuration);
            this.lessonPrice = itemView.findViewById(R.id.lessonPrice);
            this.lessonLocation = itemView.findViewById(R.id.lessonLocation);
            this.lessonIsPaid = itemView.findViewById(R.id.lessonIsPaid);

            this.layoutLessonTable = itemView.findViewById(R.id.layoutLessonTable);
        }
    }
}
