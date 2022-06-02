package com.example.myfirstapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> implements Filterable
{
    private ArrayList<Student> students;
    private ArrayList<Student> studentsListFull;
    public Context context;

    public StudentAdapter(ArrayList<Student> students)
    {
        this.students = students;
        this.studentsListFull = new ArrayList<>(students);  // a copy of the students list
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View studentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_student, parent, false);
        this.context = parent.getContext();
        return new StudentViewHolder(studentView);
    }

    // who need to be load
    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position)
    {
        Student currentStudent = students.get(position);
        // change the text to the received data from the server:
        holder.studentId.setText("ID: "+currentStudent.getId());
        holder.studentName.setText(currentStudent.getName());
        holder.studentAddress.setText(currentStudent.getAddress());
        holder.studentPhone.setText("Phone: "+currentStudent.getPhone());
        holder.studentDebt.setText("Debt: "+currentStudent.getDebt());
        holder.imagePersonIcon.setImageResource(holder.studentName.getResources().getIdentifier(currentStudent.getIcon(), "drawable", holder.studentName.getContext().getPackageName()));

        //To open the student's lessons data- StudentDataActivity
        holder.recyclerStudentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent student_data = new Intent(context, StudentDataActivity.class);
                student_data.putExtra("EXTRA_STUDENT_ID", currentStudent.getId());
                student_data.putExtra("EXTRA_STUDENT_NAME", currentStudent.getName());
                context.startActivity(student_data);
            }
        });
    }

    @Override
    public int getItemCount()  // return the number of students
    {
        return students.size();
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
            ArrayList<Student> filteredList = new ArrayList<>();

            if (charSequence == null || charSequence.length() == 0)
            {
                filteredList.addAll(studentsListFull); // show all the students
            }

            else
            {
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for (Student student: studentsListFull) // for every student:
                {
                    if (student.getName().toLowerCase().contains(filterPattern))
                        filteredList.add(student);
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
            students.clear();
            students.addAll((ArrayList)results.values); //contains only the filtered items
            notifyDataSetChanged();
        }
    };

    public static class StudentViewHolder extends RecyclerView.ViewHolder
    {
        public TextView studentId;
        public TextView studentName;
        public TextView studentAddress;
        public TextView studentPhone;
        public TextView studentDebt;
        public ImageView imagePersonIcon;

        public ConstraintLayout recyclerStudentLayout;

        public StudentViewHolder(@NonNull View itemView)
        {
            super(itemView);
            this.studentId = itemView.findViewById(R.id.studentId);
            this.studentName = itemView.findViewById(R.id.studentName);
            this.studentAddress = itemView.findViewById(R.id.studentAddress);
            this.studentPhone = itemView.findViewById(R.id.studentPhoneNum);
            this.studentDebt = itemView.findViewById(R.id.studentDebt);
            this.imagePersonIcon = itemView.findViewById(R.id.imagePersonIcon);

            this.recyclerStudentLayout = itemView.findViewById(R.id.recyclerStudentLayout);
        }
    }
}
