
package com.whatsapp.app.Models;


public class User {


    private String name;
    private String phoneNumber;
    private String profilePicUrl = "";
    private String lastSeen = "";
    private String status = "";
    private String uid = "";

    public User(String uid,String name,String phoneNumber,String profilePicUrl){
        this.uid=uid;
        this.name=name;
        this.phoneNumber=phoneNumber;
        this.profilePicUrl=profilePicUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
