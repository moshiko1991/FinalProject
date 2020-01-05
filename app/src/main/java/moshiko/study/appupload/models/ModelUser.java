package moshiko.study.appupload.models;

public class ModelUser {
    String name, emoji, search, image, cover, email, userId, typingTo;

    public ModelUser(){
    }

    public ModelUser(String name, String emoji, String search, String image, String cover, String email, String id, String typingTo) {
        this.name = name;
        this.emoji = emoji;
        this.search = search;
        this.image = image;
        this.cover = cover;
        this.email = email;
        this.userId = userId;
        this.typingTo = typingTo;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
