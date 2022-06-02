package com.example.myfirstapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class FreeLessonAdapter extends RecyclerView.Adapter<FreeLessonAdapter.FreeLessonViewHolder>
{
    private ArrayList<Lesson> freeLessons;
    private Context context;
    private String TAG = "Lesson Adapter";
    private String teacherId, date;
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

            if (command.equals("ACK"))  // alert-the lesson request/cancel was submitted!
                showAlert("Your request was submitted- waiting for approval");

            else if(command.equals("ERROR")) // show error message
            {
                String error_num = fields[2];
                String error_message = fields[3];
                showAlert("ERROR " + error_num + ": "+error_message);
            }
        }
    };

    public FreeLessonAdapter(ArrayList<Lesson> freeLessons, String teacherId, String date)
    {
        this.freeLessons = freeLessons;
        this.teacherId = teacherId;
        this.date = date;
    }

    @NonNull
    @Override
    public FreeLessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View lessonView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_free_lesson, parent, false);
        this.context = parent.getContext();
        return new FreeLessonViewHolder(lessonView);
    }

    // who need to be load
    @Override
    public void onBindViewHolder(@NonNull FreeLessonViewHolder holder, int position)
    {
        Lesson currentTime = freeLessons.get(position);
        // change the text to the received data from the server:
        holder.lessonTime.setText(currentTime.getTime());

        // check if it's an empty lesson or it's the student's lesson:
        if(currentTime.getLocation().equals("")) // empty lesson:
        {
            holder.location.setText("");
            holder.price.setText("");
            holder.freeLayout.setBackgroundColor(Color.parseColor("#C6EBFD"));
            holder.sendButton.setVisibility(View.VISIBLE);
            holder.sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    AlertDialog.Builder builder= new AlertDialog.Builder(context); // a builder
                    final EditText inputLocation = new EditText(context);
                    inputLocation.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(inputLocation); // a text box for input the location

                    builder.setMessage("Please enter the pick up location:")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i)
                                {
                                    String location = inputLocation.getText().toString();
                                    sendLessonRequest(currentTime.getTime(), location);
                                }
                            }).setNegativeButton("Cancel",null);

                    AlertDialog alert =builder.create(); // create the alert box
                    alert.show(); // show the alert
                }
            });
        }

        else
        {
            holder.location.setText("Location: "+currentTime.getLocation());
            holder.price.setText("Price: "+currentTime.getPrice());
            holder.freeLayout.setBackgroundColor(Color.parseColor("#43BFF0"));
            holder.sendButton.setVisibility(View.VISIBLE);
            holder.sendButton.setText("CANCEL");
            holder.sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    AlertDialog.Builder builder= new AlertDialog.Builder(context); // a builder

                    builder.setMessage("Are you sure you want to cancel the lesson?")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i)
                                {
                                    sendCancelRequest(currentTime.getTime());
                                }
                            }).setNegativeButton("Cancel",null);

                    AlertDialog alert =builder.create(); // create the alert box
                    alert.show(); // show the alert
                }
            });
        }

    }

    @Override
    public int getItemCount()  // return the number of students
    {
        return freeLessons.size();
    }

    //send the LESSONREQUEST message: size|LESSONREQUEST|date|time|studentId|teacherId|location
    public void sendLessonRequest(String startTime, String location)
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
        String data = "LESSONREQUEST|" + this.date + "|" + startTime + "|" + com.example.myfirstapp.SessionData.getID() + "|" + this.teacherId + "|" + location;

        Log.d(TAG,"before sending");
        Log.d(TAG,data);
        tcp_send_recv bg = new tcp_send_recv(mHandler);
        bg.execute(data);

        Log.d(TAG,"after sending");
    }

    //send the CANCELREQUEST message: size|CANCELREQUEST|senderId|receiverId|date|time
    public void sendCancelRequest(String startTime)
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
        String data = "CANCELREQUEST|" + com.example.myfirstapp.SessionData.getID() + "|" + this.teacherId + "|" + this.date + "|" + startTime;

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

    public static class FreeLessonViewHolder extends RecyclerView.ViewHolder
    {
        public TextView lessonTime;
        public Button sendButton;

        public TextView location;
        public TextView price;
        public ConstraintLayout freeLayout;

        public FreeLessonViewHolder(@NonNull View itemView)
        {
            super(itemView);
            this.lessonTime = itemView.findViewById(R.id.startTime);
            this.sendButton = itemView.findViewById(R.id.sendButton);

            this.location = itemView.findViewById(R.id.location);
            this.price = itemView.findViewById(R.id.price);
            this.freeLayout = itemView.findViewById(R.id.recyclerFreeLessons);
        }
    }
}
