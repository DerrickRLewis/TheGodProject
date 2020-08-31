package apps.envision.mychurch.ui.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class RobotoBoldTextView extends AppCompatTextView {

	private static Typeface RobotoTextView;

	public RobotoBoldTextView(Context context) {
		super(context);
		if (isInEditMode()) return; //Won't work in Eclipse graphical layout
		setTypeface();
	}

	public RobotoBoldTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (isInEditMode()) return;
		setTypeface();
	}

	public RobotoBoldTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (isInEditMode()) return;
		setTypeface();
	}
	
	private void setTypeface() {
		if (RobotoTextView == null) {
			RobotoTextView = Typeface.createFromAsset(getContext().getAssets(), "fonts/roboto-bold.ttf");
		}
		setTypeface(RobotoTextView);
	}
}
