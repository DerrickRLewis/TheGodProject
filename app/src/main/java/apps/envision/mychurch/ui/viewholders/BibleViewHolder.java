package apps.envision.mychurch.ui.viewholders;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.interfaces.BibleClickListener;
import apps.envision.mychurch.libs.spans.Spanny;
import apps.envision.mychurch.pojo.Bible;
import apps.envision.mychurch.pojo.SBible;
import apps.envision.mychurch.utils.Constants;

public class BibleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView content,amp,kjv,nkjv,msg,nlt,niv,nrsv;
    private BibleClickListener bibleClickListener;
    private Bible bible;
    private String selected_version = "";

    public BibleViewHolder(View itemView, BibleClickListener bibleClickListener) {
        super(itemView);
        content = itemView.findViewById(R.id.content);

        amp = itemView.findViewById(R.id.amp);
        kjv = itemView.findViewById(R.id.kjv);
        nkjv = itemView.findViewById(R.id.nkjv);
        msg = itemView.findViewById(R.id.msg);
        nlt = itemView.findViewById(R.id.nlt);
        niv = itemView.findViewById(R.id.niv);
        nrsv = itemView.findViewById(R.id.nrsv);

        amp.setOnClickListener(this);
        kjv.setOnClickListener(this);
        nkjv.setOnClickListener(this);
        msg.setOnClickListener(this);
        niv.setOnClickListener(this);
        nlt.setOnClickListener(this);
        nrsv.setOnClickListener(this);
        content.setOnClickListener(this);

        this.bibleClickListener = bibleClickListener;
    }

    public void bind(Bible bible){
        this.bible = bible;

        List<String> versions = bibleClickListener.getDownloadedVersion();
        if(versions.indexOf(Constants.BIBLE_VERSIONS.AMP)!=-1){
            amp.setVisibility(View.VISIBLE);
        }else{
            amp.setVisibility(View.GONE);
        }

        if(versions.indexOf(Constants.BIBLE_VERSIONS.KJV)!=-1){
            kjv.setVisibility(View.VISIBLE);
        }else{
            kjv.setVisibility(View.GONE);
        }

        if(versions.indexOf(Constants.BIBLE_VERSIONS.NKJV)!=-1){
            nkjv.setVisibility(View.VISIBLE);
        }else{
            nkjv.setVisibility(View.GONE);
        }

        if(versions.indexOf(Constants.BIBLE_VERSIONS.NLT)!=-1){
            nlt.setVisibility(View.VISIBLE);
        }else{
            nlt.setVisibility(View.GONE);
        }

        if(versions.indexOf(Constants.BIBLE_VERSIONS.NIV)!=-1){
            niv.setVisibility(View.VISIBLE);
        }else{
            niv.setVisibility(View.GONE);
        }

        if(versions.indexOf(Constants.BIBLE_VERSIONS.NRSV)!=-1){
            nrsv.setVisibility(View.VISIBLE);
        }else{
            nrsv.setVisibility(View.GONE);
        }

        if(versions.indexOf(Constants.BIBLE_VERSIONS.MSG)!=-1){
            msg.setVisibility(View.VISIBLE);
        }else{
            msg.setVisibility(View.GONE);
        }

        selected_version = PreferenceSettings.getDefaultSelectedVersion();
        if(selected_version.equalsIgnoreCase("")) {
            selected_version = versions.get(0);
        }
        setText();
    }

    private void setText(){
        String txt = "";
        switch (selected_version){
            case Constants.BIBLE_VERSIONS.AMP:
                txt = bible.getAMP();
                break;
            case Constants.BIBLE_VERSIONS.KJV:
                txt = bible.getKJV();
                break;
            case Constants.BIBLE_VERSIONS.NKJV:
                txt = bible.getNKJV();
                break;
            case Constants.BIBLE_VERSIONS.NIV:
                txt = bible.getNIV();
                break;
            case Constants.BIBLE_VERSIONS.NLT:
                txt = bible.getNLT();
                break;
            case Constants.BIBLE_VERSIONS.MSG:
                txt = bible.getMSG();
                break;
            case Constants.BIBLE_VERSIONS.NRSV:
                txt = bible.getNRSV();
                break;
        }

        Spanny spanny = new Spanny(String.valueOf(bible.getVerse())+": ", new StyleSpan(Typeface.BOLD));
        if(bibleClickListener.is_bible_verse_selected(bible)){
            if(PreferenceSettings.getBibleThemeMode()==1){
                spanny.append(txt, new BackgroundColorSpan(Color.LTGRAY));
            }else{
                spanny.append(txt, new BackgroundColorSpan(App.getContext().getResources().getColor(R.color.black_transparent)));
            }
        }else {
            SBible sBible = bibleClickListener.is_bible_verse_highlighted(bible);
            if(sBible!=null){
                spanny.append(txt, new BackgroundColorSpan(sBible.getColorCode()));
            }else{
                spanny.append(txt);
            }
        }

        content.setText(spanny);
        setVersionColor();
    }

    private void setVersionColor(){
        if(PreferenceSettings.getBibleThemeMode()==1){
            amp.setTextColor(App.getContext().getResources().getColor(R.color.material_blue_grey_700));
            msg.setTextColor(App.getContext().getResources().getColor(R.color.material_blue_grey_700));
            kjv.setTextColor(App.getContext().getResources().getColor(R.color.material_blue_grey_700));
            nlt.setTextColor(App.getContext().getResources().getColor(R.color.material_blue_grey_700));
            niv.setTextColor(App.getContext().getResources().getColor(R.color.material_blue_grey_700));
            nkjv.setTextColor(App.getContext().getResources().getColor(R.color.material_blue_grey_700));
            nrsv.setTextColor(App.getContext().getResources().getColor(R.color.material_blue_grey_700));
        }else{
            amp.setTextColor(App.getContext().getResources().getColor(R.color.white));
            msg.setTextColor(App.getContext().getResources().getColor(R.color.white));
            kjv.setTextColor(App.getContext().getResources().getColor(R.color.white));
            nlt.setTextColor(App.getContext().getResources().getColor(R.color.white));
            niv.setTextColor(App.getContext().getResources().getColor(R.color.white));
            nkjv.setTextColor(App.getContext().getResources().getColor(R.color.white));
            nrsv.setTextColor(App.getContext().getResources().getColor(R.color.white));
        }


        switch (selected_version){
            case Constants.BIBLE_VERSIONS.AMP:
                amp.setTextColor(App.getContext().getResources().getColor(R.color.primary_dark));
                break;
            case Constants.BIBLE_VERSIONS.KJV:
                kjv.setTextColor(App.getContext().getResources().getColor(R.color.primary_dark));
                break;
            case Constants.BIBLE_VERSIONS.NKJV:
                nkjv.setTextColor(App.getContext().getResources().getColor(R.color.primary_dark));
                break;
            case Constants.BIBLE_VERSIONS.NIV:
                niv.setTextColor(App.getContext().getResources().getColor(R.color.primary_dark));
                break;
            case Constants.BIBLE_VERSIONS.NLT:
                nlt.setTextColor(App.getContext().getResources().getColor(R.color.primary_dark));
                break;
            case Constants.BIBLE_VERSIONS.MSG:
                msg.setTextColor(App.getContext().getResources().getColor(R.color.primary_dark));
                break;
            case Constants.BIBLE_VERSIONS.NRSV:
                nrsv.setTextColor(App.getContext().getResources().getColor(R.color.primary_dark));
                break;
        }
        content.setTextSize(TypedValue.COMPLEX_UNIT_SP, PreferenceSettings.getDefaultSelectedFont());
    }

    private int dp(int value) {
        return (int) Math.ceil(App.getContext().getResources().getDisplayMetrics().density * value);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.amp:
                selected_version = Constants.BIBLE_VERSIONS.AMP;
                setText();
                break;
            case R.id.kjv:
                selected_version = Constants.BIBLE_VERSIONS.KJV;
                setText();
                break;
            case R.id.nkjv:
                selected_version = Constants.BIBLE_VERSIONS.NKJV;
                setText();
                break;
            case R.id.niv:
                selected_version = Constants.BIBLE_VERSIONS.NIV;
                setText();
                break;
            case R.id.nlt:
                selected_version = Constants.BIBLE_VERSIONS.NLT;
                setText();
                break;
            case R.id.msg:
                selected_version = Constants.BIBLE_VERSIONS.MSG;
                setText();
                break;
            case R.id.nrsv:
                selected_version = Constants.BIBLE_VERSIONS.NRSV;
                setText();
                break;
            case R.id.content:
                if(bibleClickListener.is_bible_verse_highlighted(bible)!=null){
                    bibleClickListener.remove_highlight_verse(bible);
                    setText();
                }else {
                    if (!bibleClickListener.is_bible_verse_selected(bible)) {
                        bibleClickListener.set_selected_verses(bible, selected_version);
                    } else {
                        bibleClickListener.remove_selected_verses(bible);
                    }
                    setText();
                    if (bibleClickListener.get_total_selected_verses() == 0) {
                        bibleClickListener.hide_bottom_layout();
                    } else {
                        bibleClickListener.show_bottom_layout();
                    }
                }
                break;
        }
    }
}
