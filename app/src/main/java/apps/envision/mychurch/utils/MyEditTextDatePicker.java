package apps.envision.mychurch.utils;

import android.app.DatePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;

import androidx.appcompat.widget.AppCompatEditText;

import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MyEditTextDatePicker extends TextInputEditText implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    public MyEditTextDatePicker(Context context) {
        super(context);
        setOnClickListener(this);
        setFocusable(false);
    }

    public MyEditTextDatePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
        setFocusable(false);
        setDate();
    }

    public MyEditTextDatePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnClickListener(this);
        setFocusable(false);
        setDate();
    }

    private void setDate(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int month= cal.get(Calendar.MONTH);
        String _month = String.valueOf(month);
        if(month<10){
            _month = String.format(Locale.getDefault(),"%02d", month);
        }
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int year = cal.get(Calendar.YEAR);
        String _day = String.valueOf(dayOfMonth);
        if(dayOfMonth<10){
            _day = String.format(Locale.getDefault(),"%02d", dayOfMonth);
        }
        setText(new StringBuilder().append(year).append("-").append(_month).append("-").append(_day));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        int month = monthOfYear + 1;
        String _month = String.valueOf(month);
        if(month<10){
            _month = String.format(Locale.getDefault(),"%02d", month);
        }
        String _day = String.valueOf(dayOfMonth);
        if(dayOfMonth<10){
            _day = String.format(Locale.getDefault(),"%02d", dayOfMonth);
        }
        setText(new StringBuilder().append(year).append("-").append(_month).append("-").append(_day));
    }

    @Override
    public void onClick(View v) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        if(this.getText()!=null) {
            String date = this.getText().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                calendar.setTime(sdf.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        DatePickerDialog dialog = new DatePickerDialog(getContext(), this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }
}
