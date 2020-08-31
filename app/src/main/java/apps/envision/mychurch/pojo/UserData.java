package apps.envision.mychurch.pojo;

/**
 * Created by ray on 26/12/2018.
 */
public class UserData {

    private String email="",name="",avatar="",cover_photo="", gender="";
    private boolean isBlocked = false;
    private boolean verified = false;
    private boolean subscribed = false;
    private long subscriptionExpirydate = 0;
    private String subscription_plan = "";

    private String date_of_birth="", phone="", about_me="",location="",
            qualification="", facebook="", twitter="", linkdln="", notify_token="";
    private boolean activated = false, isFollowing = false;

    public UserData() {

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }


    public String getSubscription_plan() {
        return subscription_plan;
    }

    public void setSubscription_plan(String subscription_plan) {
        this.subscription_plan = subscription_plan;
    }

    public long getSubscriptionExpirydate() {
        return subscriptionExpirydate;
    }

    public void setSubscriptionExpirydate(long subscriptionExpirydate) {
        this.subscriptionExpirydate = subscriptionExpirydate;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getCover_photo() {
        return cover_photo;
    }

    public void setCover_photo(String cover_photo) {
        this.cover_photo = cover_photo;
    }

    public String getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(String date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAbout_me() {
        return about_me;
    }

    public void setAbout_me(String about_me) {
        this.about_me = about_me;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getLinkdln() {
        return linkdln;
    }

    public void setLinkdln(String linkdln) {
        this.linkdln = linkdln;
    }

    public String getNotify_token() {
        return notify_token;
    }

    public void setNotify_token(String notify_token) {
        this.notify_token = notify_token;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
    }
}
