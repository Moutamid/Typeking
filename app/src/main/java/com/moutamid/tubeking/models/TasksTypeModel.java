package com.moutamid.tubeking.models;

public class TasksTypeModel {
    ViewTaskModel viewTaskModel;
    LikeTaskModel likeTaskModel;
    SubscribeTaskModel subscribeTaskModel;
    String type;

    public TasksTypeModel() {
    }

    public TasksTypeModel(ViewTaskModel viewTaskModel, LikeTaskModel likeTaskModel, SubscribeTaskModel subscribeTaskModel, String type) {
        this.viewTaskModel = viewTaskModel;
        this.likeTaskModel = likeTaskModel;
        this.subscribeTaskModel = subscribeTaskModel;
        this.type = type;
    }

    public TasksTypeModel(ViewTaskModel viewTaskModel, String type) {
        this.viewTaskModel = viewTaskModel;
        this.type = type;
    }

    public TasksTypeModel(LikeTaskModel likeTaskModel, String type) {
        this.likeTaskModel = likeTaskModel;
        this.type = type;
    }

    public TasksTypeModel(SubscribeTaskModel subscribeTaskModel, String type) {
        this.subscribeTaskModel = subscribeTaskModel;
        this.type = type;
    }

    public ViewTaskModel getViewTaskModel() {
        return viewTaskModel;
    }

    public void setViewTaskModel(ViewTaskModel viewTaskModel) {
        this.viewTaskModel = viewTaskModel;
    }

    public LikeTaskModel getLikeTaskModel() {
        return likeTaskModel;
    }

    public void setLikeTaskModel(LikeTaskModel likeTaskModel) {
        this.likeTaskModel = likeTaskModel;
    }

    public SubscribeTaskModel getSubscribeTaskModel() {
        return subscribeTaskModel;
    }

    public void setSubscribeTaskModel(SubscribeTaskModel subscribeTaskModel) {
        this.subscribeTaskModel = subscribeTaskModel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
