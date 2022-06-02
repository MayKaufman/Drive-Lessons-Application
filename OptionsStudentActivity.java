package com.example.myfirstapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class OptionsStudentActivity extends AppCompatActivity
{
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.student_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.menuHomeStudent:
                Intent teacher_profile = new Intent(OptionsStudentActivity.this, StudentProfileActivity.class);
                startActivity(teacher_profile);
                return true;

            case R.id.menuNotificationsStudent:
                Intent student_notifications = new Intent(OptionsStudentActivity.this, StudentNotificationsActivity.class);
                startActivity(student_notifications);
                return true;

            case R.id.menuCalendarStudent:
                Intent student_calendar = new Intent(OptionsStudentActivity.this, StudentCalendarActivity.class);
                startActivity(student_calendar);
                return true;

            case R.id.menuLessonsStudent:
                Intent student_lessons = new Intent(OptionsStudentActivity.this, StudentDataActivity.class);
                student_lessons.putExtra("EXTRA_STUDENT_ID", com.example.myfirstapp.SessionData.getID());
                student_lessons.putExtra("EXTRA_STUDENT_NAME", com.example.myfirstapp.SessionData.getUsername());
                startActivity(student_lessons);
                return true;

            case R.id.menuTeachersStudent:
                Intent student_teachers = new Intent(OptionsStudentActivity.this, StudentTeachersActivity.class);
                startActivity(student_teachers);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
