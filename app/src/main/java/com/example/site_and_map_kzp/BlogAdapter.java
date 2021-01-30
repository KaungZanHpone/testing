package com.example.site_and_map_kzp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.BlogHolder> {
    Context context;
    ArrayList<Blog> blogs=new ArrayList<>();
    public BlogAdapter(ArrayList<Blog> blogs,Context context){
        this.blogs=blogs;
        this.context=context;
    }
    @NonNull
    @Override
    public BlogHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(context).inflate(R.layout.blog_item_layout,parent,false);
        return new BlogHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogHolder holder, int position) {
        Blog blog=blogs.get(position);
        holder.setTitle(blog.getTitle());
        holder.setImage(blog.getImage());
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(blog.getUrl())));
            }
        });
    }

    @Override
    public int getItemCount() {
        return blogs==null?0:blogs.size();
    }

    public class BlogHolder extends RecyclerView.ViewHolder{
        TextView title;
        ImageView image;
        public BlogHolder(@NonNull View itemView) {
            super(itemView);
            title=itemView.findViewById(R.id.blog_item_title);
            image=itemView.findViewById(R.id.blog_item_image);
        }
        public void setTitle(String title){
            this.title.setText(title);
        }
        public void setImage(int image){
            this.image.setImageResource(image);
        }
    }
}
