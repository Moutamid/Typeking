package com.moutamid.tubeking.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.moutamid.tubeking.R;
import com.moutamid.tubeking.models.AllUserModel;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListVh> {
    Context context;
    ArrayList<AllUserModel> list;

    public UserListAdapter(Context context, ArrayList<AllUserModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public UserListVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.listof_viewers, parent, false);
        return new UserListVh(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserListVh holder, int position) {
        AllUserModel model = list.get(holder.getAdapterPosition());
        holder.date.setText(model.getDate());
        holder.name.setText(model.getName());

        Glide.with(context).load(model.getImage()).placeholder(R.drawable.profile_icon).into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class UserListVh extends RecyclerView.ViewHolder {

        TextView name, date;
        CircleImageView imageView;

        public UserListVh(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            date = itemView.findViewById(R.id.date);
            imageView = itemView.findViewById(R.id.profile);
        }
    }
}
