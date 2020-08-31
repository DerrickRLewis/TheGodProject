package apps.envision.mychurch.pojo;


public class LikeUpdate {

    public int count = 0;
    public int position = 0;
    public String option = "like";
    public boolean refresh = false;

    public LikeUpdate(int position, int count, String option, boolean refresh) {
       this.option = option;
       this.count = count;
       this.option = option;
       this.refresh = refresh;
    }
}
