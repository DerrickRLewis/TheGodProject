package apps.envision.mychurch.pojo;


public class MakePosts {

    private String content ="";
    private String visibility = "";
    private String email = "";
    private String mediaFiles = "";

    public MakePosts() {

    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(String mediaFiles) {
        this.mediaFiles = mediaFiles;
    }
}
