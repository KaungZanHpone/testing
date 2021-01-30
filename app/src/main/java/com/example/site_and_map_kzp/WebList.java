package com.example.site_and_map_kzp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WebList extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<Blog> blogs=new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blog_view);
        recyclerView=findViewById(R.id.blogView);
        blogs.add(new Blog("Infant CPR and Choking",R.drawable.infantcpr,"https://nhcps.com/lesson/cpr-first-aid-aed-infants/"));
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        BlogAdapter blogAdapter=new BlogAdapter(blogs,getApplicationContext());
        recyclerView.setAdapter(blogAdapter);
        blogAdapter.notifyDataSetChanged();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        switch (id){
            case R.id.map:
                startActivity(new Intent(WebList.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            case R.id.blog:
                startActivity(new Intent(WebList.this,WebList.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                break;
            case R.id.reminders:
                startActivity(new Intent(WebList.this,Reminders.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
        }
        return true;
    }
}
