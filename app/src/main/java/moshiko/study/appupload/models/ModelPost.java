package moshiko.study.appupload.models;

public class ModelPost {
    String postId, postTitle, postDescr, postImage, postTime, userEmoji,
            userId, userDp, userName;

    public ModelPost() {
    }

    public ModelPost(String postId, String postTitle, String postDescr, String postImage, String postTime, String userEmoji, String userId, String userDp, String userName) {
        this.postId = postId;
        this.postTitle = postTitle;
        this.postDescr = postDescr;
        this.postImage = postImage;
        this.postTime = postTime;
        this.userEmoji = userEmoji;
        this.userId = userId;
        this.userDp = userDp;
        this.userName = userName;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getPostDescr() {
        return postDescr;
    }

    public void setPostDescr(String postDescr) {
        this.postDescr = postDescr;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getUserEmoji() {
        return userEmoji;
    }

    public void setUserEmoji(String userEmoji) {
        this.userEmoji = userEmoji;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserDp() {
        return userDp;
    }

    public void setUserDp(String userDp) {
        this.userDp = userDp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
