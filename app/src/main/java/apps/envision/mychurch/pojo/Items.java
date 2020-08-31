package apps.envision.mychurch.pojo;

import android.graphics.drawable.Drawable;

public class Items {
    public Drawable icon;
    public String title;
    public String description;

    public Items() {

    }
    public Items(String title, String description, Drawable icon) {
        this.title = title;
        this.icon = icon;
        this.description = description;
    }
}
