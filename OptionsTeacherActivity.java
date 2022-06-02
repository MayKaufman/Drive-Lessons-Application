package com.example.myfirstapp;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class OptionsTeacherActivity extends AppCompatActivity
{
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.teacher_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.menuHomeTeacher:
                Intent teacher_profile = new Intent(OptionsTeacherActivity.this, TeacherProfileActivity.class);
                startActivity(teacher_profile);
                return true;

            case R.id.menuCalendarTeacher:
                Intent teacher_calendar = new Intent(OptionsTeacherActivity.this, TeacherCalendarActivity.class);
                startActivity(teacher_calendar);
                return true;

            case R.id.menuStudentsTeacher:
                Intent teacher_students = new Intent(OptionsTeacherActivity.this, TeacherStudentsActivity.class);
                startActivity(teacher_students);
                return true;

            case R.id.menuNotificationsTeacher:
                Intent teacher_notifications = new Intent(OptionsTeacherActivity.this, TeacherNotificationsActivity.class);
                startActivity(teacher_notifications);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
