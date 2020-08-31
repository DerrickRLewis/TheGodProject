package apps.envision.mychurch.ui.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class QuickSandsBoldTextView extends AppCompatTextView {

	private static Typeface CavierDreamsTextView;

	public QuickSandsBoldTextView(Context context) {
		super(context);
		if (isInEditMode()) return; //Won't work in Eclipse graphical layout
		setTypeface();
	}

	public QuickSandsBoldTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (isInEditMode()) return;
		setTypeface();
	}

	public QuickSandsBoldTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (isInEditMode()) return;
		setTypeface();
	}
	
	private void setTypeface() {
		if (CavierDreamsTextView == null) {
			CavierDreamsTextView = Typeface.createFromAsset(getContext().getAssets(), "fonts/quicksand/Quicksand-Bold.ttf");
		}
		setTypeface(CavierDreamsTextView);
	}
}
