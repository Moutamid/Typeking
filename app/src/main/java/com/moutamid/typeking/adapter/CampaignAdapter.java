package com.moutamid.typeking.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.moutamid.typeking.R;
import com.moutamid.typeking.utilis.Constants;
import com.moutamid.typeking.models.LikeTaskModel;
import com.moutamid.typeking.models.SubscribeTaskModel;
import com.moutamid.typeking.models.TasksTypeModel;
import com.moutamid.typeking.models.ViewTaskModel;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class CampaignAdapter extends RecyclerView.Adapter<CampaignAdapter.CampaignVH> {
    Context context;
    ArrayList<TasksTypeModel> list;

    public CampaignAdapter(Context context, ArrayList<TasksTypeModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public CampaignVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.campaign_item, parent, false);
        return new CampaignVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CampaignVH holder, int position) {
        TasksTypeModel model = list.get(holder.getAdapterPosition());

        AtomicReference<String> path = new AtomicReference<>("");

        if (model.getType().equals(Constants.TYPE_LIKE)) {
            holder.campaign.setText("Campaign : LIKE");
            dealWithViewItemLayouts(model.getLikeTaskModel(), holder, position);
        } if (model.getType().equals(Constants.TYPE_VIEW)) {
            holder.campaign.setText("Campaign : View");
            dealWithLikeItemLayouts(model.getViewTaskModel(), holder, position);
        } if (model.getType().equals(Constants.TYPE_SUBSCRIBE)) {
            holder.campaign.setText("Campaign : Subscribe");
            dealWithSubscribeItemLayouts(model.getSubscribeTaskModel(), holder, position);
        }


        holder.more.setOnClickListener(v -> {
            if (model.getType().equals(Constants.TYPE_LIKE)) {
                path.set(Constants.LIKE_TASKS);
                showAlertLike(model.getLikeTaskModel(), path.get(), holder.getAdapterPosition());
            }

            if (model.getType().equals(Constants.TYPE_VIEW)) {
                path.set(Constants.VIEW_TASKS);
                showAlertView(model.getViewTaskModel(), path.get(), holder.getAdapterPosition());
            }

            if (model.getType().equals(Constants.TYPE_SUBSCRIBE)) {
                path.set(Constants.SUBSCRIBE_TASKS);
                showAlertSub(model.getSubscribeTaskModel(), path.get(), holder.getAdapterPosition());
            }

        });

    }

    private void showAlertView(ViewTaskModel model, String path, int position) {
        new AlertDialog.Builder(context).setCancelable(true).setMessage("Do you really want to delete this task?")
                .setTitle("Are you sure??")
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Yes", (dialog, which) -> {
                    Constants.databaseReference().child(path).child(model.getTaskKey()).removeValue();
                    notifyItemRemoved(position);
                }).show();
    }

    private void showAlertLike(LikeTaskModel model, String path, int position) {
        new AlertDialog.Builder(context).setCancelable(true).setMessage("Do you really want to delete this task?")
                .setTitle("Are you sure??")
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Yes", (dialog, which) -> {
                    Constants.databaseReference().child(path).child(model.getTaskKey()).removeValue();
                    notifyItemRemoved(position);
                }).show();
    }

    private void showAlertSub(SubscribeTaskModel model, String path, int position) {
        new AlertDialog.Builder(context).setCancelable(true).setMessage("Do you really want to delete this task?")
                .setTitle("Are you sure??")
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Yes", (dialog, which) -> {
                    Constants.databaseReference().child(path).child(model.getTaskKey()).removeValue();
                    notifyItemRemoved(position);
                }).show();
    }

    private void dealWithViewItemLayouts(LikeTaskModel likeTaskModel, CampaignVH holder, int position) {
        Glide.with(context).load(likeTaskModel.getThumbnailUrl()).into(holder.thumbnail);

        holder.views.setText(
                likeTaskModel.getCurrentLikesQuantity() + "/" + likeTaskModel.getTotalLikesQuantity()
        );
        int max = Integer.parseInt(likeTaskModel.getTotalLikesQuantity());
        holder.progressIndicator.setMax(max);
        holder.progressIndicator.setProgress(likeTaskModel.getCurrentLikesQuantity());
    }

    private void dealWithLikeItemLayouts(ViewTaskModel viewTaskModel, CampaignVH holder, int position) {
        Glide.with(context).load(viewTaskModel.getThumbnailUrl()).into(holder.thumbnail);

        holder.views.setText(
                viewTaskModel.getCurrentViewsQuantity() + "/" + viewTaskModel.getTotalViewsQuantity()
        );
        int max = Integer.parseInt(viewTaskModel.getTotalViewsQuantity());
        holder.progressIndicator.setMax(max);
        holder.progressIndicator.setProgress(viewTaskModel.getCurrentViewsQuantity());
    }

    private void dealWithSubscribeItemLayouts(SubscribeTaskModel subscribeTaskModel, CampaignVH holder, int position) {
        Glide.with(context).load(subscribeTaskModel.getThumbnailUrl()).into(holder.thumbnail);

        holder.views.setText(
                subscribeTaskModel.getCurrentSubscribesQuantity() + "/" + subscribeTaskModel.getTotalSubscribesQuantity()
        );
        int max = Integer.parseInt(subscribeTaskModel.getTotalSubscribesQuantity());
        holder.progressIndicator.setMax(max);
        holder.progressIndicator.setProgress(subscribeTaskModel.getCurrentSubscribesQuantity());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class CampaignVH extends RecyclerView.ViewHolder{
        ImageView thumbnail;
        TextView views, campaign;
        LinearProgressIndicator progressIndicator;
        ImageButton more;
        public CampaignVH(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            views = itemView.findViewById(R.id.views);
            campaign = itemView.findViewById(R.id.campaign);
            progressIndicator = itemView.findViewById(R.id.progressIndicator);
            more = itemView.findViewById(R.id.more);
        }
    }

}
