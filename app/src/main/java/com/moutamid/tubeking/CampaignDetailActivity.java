package com.moutamid.tubeking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.google.firebase.database.DataSnapshot;
import com.moutamid.tubeking.adapter.UserListAdapter;
import com.moutamid.tubeking.databinding.ActivityCampaignDetailBinding;
import com.moutamid.tubeking.models.AllUserModel;
import com.moutamid.tubeking.models.LikeTaskModel;
import com.moutamid.tubeking.models.SubscribeTaskModel;
import com.moutamid.tubeking.models.TasksTypeModel;
import com.moutamid.tubeking.models.UserDetails;
import com.moutamid.tubeking.models.ViewTaskModel;
import com.moutamid.tubeking.models.ViewersModel;
import com.moutamid.tubeking.utilis.Constants;

import java.util.ArrayList;

public class CampaignDetailActivity extends AppCompatActivity {
    ActivityCampaignDetailBinding binding;
    TasksTypeModel model;
    SubscribeTaskModel subscribeTaskModel;
    ViewTaskModel viewTaskModel;
    LikeTaskModel likeTaskModel;
    UserListAdapter adapter;
    ArrayList<ViewersModel> list;
    ArrayList<AllUserModel> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCampaignDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.calledIniti(this);
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        list = new ArrayList<>();
        users = new ArrayList<>();
        model = (TasksTypeModel) Stash.getObject(Constants.MODEL, TasksTypeModel.class);

        if (!Stash.getBoolean(Constants.VIP_STATUS)){
            Constants.loadIntersAD(CampaignDetailActivity.this, CampaignDetailActivity.this);
            Constants.showBannerAd(binding.adView);
        } else {
            binding.adView.setVisibility(View.GONE);
        }

        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setHasFixedSize(false);

        if (model.getType().equals(Constants.TYPE_LIKE)) {
            showLikersData();
        }

        if (model.getType().equals(Constants.TYPE_VIEW)) {
            showViewersData();
        }

        if (model.getType().equals(Constants.TYPE_SUBSCRIBE)) {
            showSubscribersData();
        }

    }
    private void showSubscribersData() {
        subscribeTaskModel = model.getSubscribeTaskModel();

        Glide.with(this).load(subscribeTaskModel.getThumbnailUrl()).into(binding.thumbnail);

        binding.createdDate.setText(subscribeTaskModel.getCreatedTime());
        if (subscribeTaskModel.getCompletedDate().equals("error")){
            binding.endDate.setText("Not Ended Yet");
        } else binding.endDate.setText(subscribeTaskModel.getCompletedDate());

        int pro = subscribeTaskModel.getCurrentSubscribesQuantity();
        int tot = Integer.parseInt(subscribeTaskModel.getTotalSubscribesQuantity());

        String view = pro + "/" + tot;
        binding.views.setText(view);

        binding.progressIndicator.setMax(tot);
        binding.progressIndicator.setProgress(pro);

        getSubscribers();
    }
    private void getViewers() {

        Constants.databaseReference().child(Constants.VIEW_TASKS).child(viewTaskModel.getTaskKey())
                .child(Constants.VIEWER_PATH)
                .get().addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()){
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            ViewersModel viewersModel = snapshot.getValue(ViewersModel.class);
                            list.add(viewersModel);
                        }
                        getUsers();
                    }
                }).addOnFailureListener(e -> Toast.makeText(CampaignDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void showViewersData() {
        viewTaskModel = model.getViewTaskModel();
        Glide.with(this).load(viewTaskModel.getThumbnailUrl()).into(binding.thumbnail);

        binding.createdDate.setText(viewTaskModel.getCreatedTime());
        if (viewTaskModel.getCompletedDate().equals("error")){
            binding.endDate.setText("Not Ended Yet");
        } else binding.endDate.setText(viewTaskModel.getCompletedDate());

        int pro = viewTaskModel.getCurrentViewsQuantity();
        int tot = Integer.parseInt(viewTaskModel.getTotalViewsQuantity());

        String view = pro + "/" + tot;
        binding.views.setText(view);

        binding.progressIndicator.setMax(tot);
        binding.progressIndicator.setProgress(pro);

        getViewers();
    }
    private void getSubscribers() {
        Constants.databaseReference().child(Constants.SUBSCRIBE_TASKS).child(subscribeTaskModel.getTaskKey())
                .child(Constants.SUBSCRIBER_PATH)
                .get().addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()){
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            ViewersModel viewersModel = snapshot.getValue(ViewersModel.class);
                            list.add(viewersModel);
                        }
                        getUsers();
                    }
                }).addOnFailureListener(e -> Toast.makeText(CampaignDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void showLikersData() {
        likeTaskModel = model.getLikeTaskModel();
        Glide.with(this).load(likeTaskModel.getThumbnailUrl()).into(binding.thumbnail);

        binding.createdDate.setText(likeTaskModel.getCreatedTime());
        if (likeTaskModel.getCompletedDate().equals("error")){
            binding.endDate.setText("Not Ended Yet");
        } else binding.endDate.setText(likeTaskModel.getCompletedDate());

        int pro = likeTaskModel.getCurrentLikesQuantity();
        int tot = Integer.parseInt(likeTaskModel.getTotalLikesQuantity());

        String view = pro + "/" + tot;
        binding.views.setText(view);

        binding.progressIndicator.setMax(tot);
        binding.progressIndicator.setProgress(pro);

        getLikers();
    }
    private void getLikers() {
        Constants.databaseReference().child(Constants.LIKE_TASKS).child(likeTaskModel.getTaskKey())
                .child(Constants.LIKERS_PATH)
                .get().addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()){
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            ViewersModel viewersModel = snapshot.getValue(ViewersModel.class);
                            list.add(viewersModel);
                        }
                        getUsers();
                    }
                }).addOnFailureListener(e -> Toast.makeText(CampaignDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void getUsers() {
        for(ViewersModel id : list){
            Constants.databaseReference().child(Constants.USER).child(id.getUser())
                    .get().addOnSuccessListener(dataSnapshot -> {
                        UserDetails userDetails = dataSnapshot.getValue(UserDetails.class);
                        AllUserModel allUserModel = new AllUserModel(userDetails.getName(), id.getDate(), userDetails.getImage());
                        users.add(allUserModel);
                        adapter = new UserListAdapter(this, users);
                        binding.recycler.setAdapter(adapter);
                    }).addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}