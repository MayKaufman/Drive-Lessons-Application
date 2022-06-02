package com.example.myfirstapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import android.widget.Filter;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder> implements Filterable
{
    private ArrayList<Teacher> teachers;
    private ArrayList<Teacher> teachersListFull;
    public Context context;
    private String TAG = "TeacherAdapter";
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

            if (command.equals("ACK"))  // alert-the lesson request was submitted!
                showAlert("Your request was submitted- waiting for approval");

            else if(command.equals("ERROR")) // show error message
            {
                String error_num = fields[2];
                String error_message = fields[3];
                showAlert("ERROR " + error_num + ": "+error_message);
            }
        }
    };

    public TeacherAdapter(ArrayList<Teacher> teachers)
    {
        this.teachers = teachers;
        this.teachersListFull = new ArrayList<>(teachers);  // a copy of the teachers list
    }

    @NonNull
    @Override
    public TeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View teacherView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_teacher, parent, false);
        this.context = parent.getContext();
        return new TeacherViewHolder(teacherView);
    }

    // who need to be load
    @Override
    public void onBindViewHolder(@NonNull TeacherViewHolder holder, int position)
    {
        Teacher currentTeacher = teachers.get(position);

        // change the text to the received data from the server:
        holder.teacherName.setText(currentTeacher.getName());
        holder.teacherArea.setText(currentTeacher.getArea());
        holder.teacherPhone.setText(currentTeacher.getPhone());
        holder.teacherPrice.setText("Price: "+currentTeacher.getPrice());
        holder.teacherSeniority.setText("Seniority: "+currentTeacher.getSeniority());
        holder.teacherCar.setText("Car: "+currentTeacher.getCar());
        holder.teacherCarType.setText(currentTeacher.getCarType());
        holder.imagePersonIcon.setImageResource(holder.teacherName.getResources().getIdentifier(currentTeacher.getIcon(), "drawable", holder.teacherName.getContext().getPackageName()));

        //the student wants to join to a teacher:
        holder.requestBtn.setVisibility(View.VISIBLE);
        holder.requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                AlertDialog.Builder builder= new AlertDialog.Builder(context); // a builder

                builder.setMessage("Are you sure you want to join to the teacher "+currentTeacher.getName() +"?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i)
                            {
                                sendJoinRequest(currentTeacher.getId());
                            }
                        }).setNegativeButton("Cancel",null);

                AlertDialog alert =builder.create(); // create the alert box
                alert.show(); // show the alert
            }
        });
    }

    @Override
    public int getItemCount()  // return the number of teachers
    {
        return teachers.size();
    }

    @Override
    public Filter getFilter()
    {
        return filter;
    }

    private Filter filter = new Filter()
    {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<Teacher> filteredList = new ArrayList<>();

            if (charSequence == null || charSequence.length() == 0)
            {
                filteredList.addAll(teachersListFull); // show all the accepted teachers
            }

            else
            {
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for (Teacher teacher: teachersListFull) // for every teacher in the accepted teachers:
                {
                    if (teacher.getName().toLowerCase().contains(filterPattern) || teacher.getArea().toLowerCase().contains(filterPattern) || teacher.getCarType().toLowerCase().contains(filterPattern))
                        filteredList.add(teacher);
                }
            }
            // returns the results list: (after the filtering)
            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults results)
        {
            teachers.clear();
            //contains only the filtered items
            teachers.addAll((ArrayList)results.values);
            notifyDataSetChanged();
        }
    };

    //send the JOINREQUEST message: size|JOINREQUEST|studentId|teacherId
    public void sendJoinRequest(String teacherId)
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
        String data = "JOINREQUEST|" + com.example.myfirstapp.SessionData.getID() + "|" + teacherId;

        Log.d(TAG,"before sending");
        Log.d(TAG,data);
        tcp_send_recv bg = new tcp_send_recv(mHandler);
        bg.execute(data);

        Log.d(TAG,"after sending");
    }

    //receives a string and show it on the phone's screen.
    public void showAlert(String content)
    {
        AlertDialog.Builder builder= new AlertDialog.Builder(context);
        builder.setMessage(content)
                .setPositiveButton("Ok", null);
        AlertDialog alert = builder.create(); // create the alert box
        alert.show(); // show the alert
    }

    public static class TeacherViewHolder extends RecyclerView.ViewHolder
    {
        public TextView teacherName;
        public TextView teacherArea;
        public TextView teacherPhone;
        public TextView teacherPrice;
        public TextView teacherSeniority;
        public TextView teacherCar;
        public TextView teacherCarType;
        public ImageView imagePersonIcon;
        public Button requestBtn;

        public TeacherViewHolder(@NonNull View itemView)
        {
            super(itemView);
            this.teacherName = itemView.findViewById(R.id.teacherName);
            this.teacherArea = itemView.findViewById(R.id.teacherArea);
            this.teacherPhone = itemView.findViewById(R.id.teacherPhone);
            this.teacherPrice = itemView.findViewById(R.id.teacherPrice);
            this.teacherSeniority = itemView.findViewById(R.id.teacherSeniority);
            this.teacherCar = itemView.findViewById(R.id.teacherCar);
            this.teacherCarType = itemView.findViewById(R.id.teacherCarType);
            this.imagePersonIcon = itemView.findViewById(R.id.imagePersonIcon);
            this.requestBtn = itemView.findViewById(R.id.requestBtn);
        }
    }
}
