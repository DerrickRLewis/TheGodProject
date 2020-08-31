package apps.envision.mychurch.pojo;


public class RecyclerviewState {

    public int currentPosition = 0;
    public int visiblePercentage = 0;

    public RecyclerviewState(int currentPosition, int visiblePercentage) {
       this.currentPosition = currentPosition;
        this.visiblePercentage = visiblePercentage;
    }
}
