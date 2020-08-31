package apps.envision.mychurch.ui.fonts;

import android.content.Context;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

public class RobotoRegularTextView extends AppCompatTextView {

	private static Typeface RobotoTextView;

	public RobotoRegularTextView(Context context) {
		super(context);
		if (isInEditMode()) return; //Won't work in Eclipse graphical layout
		setTypeface();
	}

	public RobotoRegularTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (isInEditMode()) return;
		setTypeface();
	}

	public RobotoRegularTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (isInEditMode()) return;
		setTypeface();
	}
	
	private void setTypeface() {
		if (RobotoTextView == null) {
			RobotoTextView = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
		}
		setTypeface(RobotoTextView);
	}
}
