package com.moutamid.typeking.fragments;

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
import com.moutamid.typeking.AddVideoActivity;
import com.moutamid.typeking.R;
import com.moutamid.typeking.adapter.CampaignAdapter;
import com.moutamid.typeking.utilis.Constants;
import com.moutamid.typeking.databinding.FragmentCampaignBinding;
import com.moutamid.typeking.models.LikeTaskModel;
import com.moutamid.typeking.models.SubscribeTaskModel;
import com.moutamid.typeking.models.TasksTypeModel;
import com.moutamid.typeking.models.ViewTaskModel;

import java.util.ArrayList;
import java.util.Collections;

public class CampaignFragment extends Fragment {
    FragmentCampaignBinding binding;
    ArrayList<TasksTypeModel> allTasksArrayList = new ArrayList<>();
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
                        allTasksArrayList.clear();
                        if (snapshot.exists()){
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                ViewTaskModel task = dataSnapshot.getValue(ViewTaskModel.class);
                                TasksTypeModel tasksTypeModel = new TasksTypeModel();
                                tasksTypeModel.setViewTaskModel(task);
                                tasksTypeModel.setType(Constants.TYPE_VIEW);

                                allTasksArrayList.add(tasksTypeModel);
                            }
                            getLikeTasksList();
                        } else {
                            getLikeTasksList();
                        }
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
                        if (snapshot.exists()){
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                                LikeTaskModel task = dataSnapshot.getValue(LikeTaskModel.class);

                                TasksTypeModel tasksTypeModel = new TasksTypeModel();
                                tasksTypeModel.setLikeTaskModel(task);
                                tasksTypeModel.setType(Constants.TYPE_LIKE);

                                allTasksArrayList.add(tasksTypeModel);

                            }
                            Collections.reverse(allTasksArrayList);
                            getSubscribeTasksList();
                        } else {
                            Collections.reverse(allTasksArrayList);
                            getSubscribeTasksList();
                        }
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
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                                SubscribeTaskModel task = dataSnapshot.getValue(SubscribeTaskModel.class);

                                TasksTypeModel tasksTypeModel = new TasksTypeModel();
                                tasksTypeModel.setSubscribeTaskModel(task);
                                tasksTypeModel.setType(Constants.TYPE_SUBSCRIBE);

                                allTasksArrayList.add(tasksTypeModel);

                            }
                            Collections.reverse(allTasksArrayList);
                            initRecyclerView();
                        } else {
                            Collections.reverse(allTasksArrayList);
                            initRecyclerView();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void initRecyclerView() {
        if (allTasksArrayList.size() > 0){
            binding.noCampLayout.setVisibility(View.GONE);
            binding.recycler.setVisibility(View.VISIBLE);

            adapter = new CampaignAdapter(requireContext(), allTasksArrayList);
            binding.recycler.setAdapter(adapter);
            adapter.notifyDataSetChanged();

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