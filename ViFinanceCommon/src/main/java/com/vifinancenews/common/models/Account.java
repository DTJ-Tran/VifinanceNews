package com.vifinancenews.common.models;

public class Account {
    private String userId; // Reflects 'user_id' in database (hashed UUID)
    private String userName;
    private String avatarLink;
    private String bio;

    public Account(String userId, String userName, String avatarLink, String bio) {
        this.userId = userId;
        this.userName = userName;
        this.avatarLink = avatarLink;
        this.bio = bio;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAvatarLink() {
        return avatarLink;
    }

    public void setAvatarLink(String avatarLink) {
        this.avatarLink = avatarLink;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    @Override
    public String toString() {
        return "Account{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", avatarLink='" + avatarLink + '\'' +
                ", bio='" + bio + '\'' +
                '}';
    }
}
