package apps.envision.mychurch.ui.fonts;

import android.content.Context;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

public class CavierDreamsTextView extends AppCompatTextView {

	private static Typeface CavierDreamsTextView;

	public CavierDreamsTextView(Context context) {
		super(context);
		if (isInEditMode()) return; //Won't work in Eclipse graphical layout
		setTypeface();
	}

	public CavierDreamsTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (isInEditMode()) return;
		setTypeface();
	}

	public CavierDreamsTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (isInEditMode()) return;
		setTypeface();
	}
	
	private void setTypeface() {
		if (CavierDreamsTextView == null) {
			CavierDreamsTextView = Typeface.createFromAsset(getContext().getAssets(), "fonts/CaviarDreams.ttf");
		}
		setTypeface(CavierDreamsTextView);
	}
}
