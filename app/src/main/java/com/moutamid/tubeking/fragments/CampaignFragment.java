package com.moutamid.tubeking.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.fxn.stash.Stash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.tubeking.AddVideoActivity;
import com.moutamid.tubeking.R;
import com.moutamid.tubeking.adapter.CampaignAdapter;
import com.moutamid.tubeking.utilis.Constants;
import com.moutamid.tubeking.databinding.FragmentCampaignBinding;
import com.moutamid.tubeking.models.LikeTaskModel;
import com.moutamid.tubeking.models.SubscribeTaskModel;
import com.moutamid.tubeking.models.TasksTypeModel;
import com.moutamid.tubeking.models.ViewTaskModel;

import java.util.ArrayList;
import java.util.Collections;

public class CampaignFragment extends Fragment {
    FragmentCampaignBinding binding;
    ArrayList<TasksTypeModel> allTasksArrayList = new ArrayList<>();
    ArrayList<TasksTypeModel> views = new ArrayList<>();
    ArrayList<TasksTypeModel> likes = new ArrayList<>();
    ArrayList<TasksTypeModel> subs = new ArrayList<>();
    CampaignAdapter adapter;
    public CampaignFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCampaignBinding.inflate(getLayoutInflater(), container, false);

        binding.addBtn.setOnClickListener(v -> {
            showDialog();
        });

        binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recycler.setHasFixedSize(false);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        getViewTasksList();
    }

    private void getViewTasksList() {
        Constants.databaseReference().child(Constants.VIEW_TASKS).orderByChild("posterUid")
                .equalTo(Constants.auth().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            views.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                ViewTaskModel task = dataSnapshot.getValue(ViewTaskModel.class);
                                TasksTypeModel tasksTypeModel = new TasksTypeModel();
                                tasksTypeModel.setViewTaskModel(task);
                                tasksTypeModel.setType(Constants.TYPE_VIEW);
                                views.add(tasksTypeModel);
                            }
                        }
                        getLikeTasksList();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getLikeTasksList() {
        Constants.databaseReference().child(Constants.LIKE_TASKS).orderByChild("posterUid")
                .equalTo(Constants.auth().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            likes.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                                LikeTaskModel task = dataSnapshot.getValue(LikeTaskModel.class);

                                TasksTypeModel tasksTypeModel = new TasksTypeModel();
                                tasksTypeModel.setLikeTaskModel(task);
                                tasksTypeModel.setType(Constants.TYPE_LIKE);
                                likes.add(tasksTypeModel);
                            }
                        }
                        getSubscribeTasksList();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getSubscribeTasksList() {
        Constants.databaseReference().child(Constants.SUBSCRIBE_TASKS).orderByChild("posterUid")
                .equalTo(Constants.auth().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            subs.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                SubscribeTaskModel task = dataSnapshot.getValue(SubscribeTaskModel.class);
                                TasksTypeModel tasksTypeModel = new TasksTypeModel();
                                tasksTypeModel.setSubscribeTaskModel(task);
                                tasksTypeModel.setType(Constants.TYPE_SUBSCRIBE);
                                subs.add(tasksTypeModel);
                            }
                        }

                        initRecyclerView();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void initRecyclerView() {
        allTasksArrayList.clear();
        allTasksArrayList.addAll(views);
        allTasksArrayList.addAll(likes);
        allTasksArrayList.addAll(subs);
        Collections.reverse(allTasksArrayList);

        if (allTasksArrayList.size() > 0){
            binding.noCampLayout.setVisibility(View.GONE);
            binding.recycler.setVisibility(View.VISIBLE);

            adapter = new CampaignAdapter(requireContext(), allTasksArrayList);
            binding.recycler.setAdapter(adapter);
        } else {
            binding.noCampLayout.setVisibility(View.VISIBLE);
            binding.recycler.setVisibility(View.GONE);
        }
    }

    private void showDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.new_campaign_dialog);

        Button all = dialog.findViewById(R.id.all);
        Button view = dialog.findViewById(R.id.view);
        Button like = dialog.findViewById(R.id.like);
        Button cancel = dialog.findViewById(R.id.cancel);

        all.setOnClickListener(v -> {
            Stash.put(Constants.CAMPAIGN_SELECTION, 0);
            startActivity(new Intent(requireContext(), AddVideoActivity.class));
            dialog.dismiss();
        });

        view.setOnClickListener(v -> {
            Stash.put(Constants.CAMPAIGN_SELECTION, 1);
            startActivity(new Intent(requireContext(), AddVideoActivity.class));
            dialog.dismiss();
        });

        like.setOnClickListener(v -> {
            Stash.put(Constants.CAMPAIGN_SELECTION, 2);
            startActivity(new Intent(requireContext(), AddVideoActivity.class));
            dialog.dismiss();
        });

        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);

    }
}