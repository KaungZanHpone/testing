package com.example.site_and_map_kzp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Reminders extends AppCompatActivity {
    FrameLayout fraghost;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reminders_bottom_nav);
        fraghost=findViewById(R.id.frag_host);
        bottomNavigationView=findViewById(R.id.reminders_bottom_nav);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id=item.getItemId();
                switch (id){
                    case R.id.pills:
                        Toast.makeText(Reminders.this, "Pills selected", Toast.LENGTH_SHORT).show();
                        openFragment(new pills());
                        break;
                    case R.id.bookings:
                        Toast.makeText(Reminders.this, "Reservations selected", Toast.LENGTH_SHORT).show();
                        openFragment(new reservations());
                        break;
                }
                return true;
            }
        });
    }
    public void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frag_host, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.map:
                startActivity(new Intent(Reminders.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            case R.id.reminders:
                startActivity(new Intent(Reminders.this,Reminders.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            case R.id.blog:
                startActivity(new Intent(Reminders.this,WebList.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
        }
        return true;
    }
}
