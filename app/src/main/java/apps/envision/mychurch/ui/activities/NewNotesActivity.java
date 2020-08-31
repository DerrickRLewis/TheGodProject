package apps.envision.mychurch.ui.activities;

import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import com.even.mricheditor.ActionType;
import com.even.mricheditor.RichEditorAction;
import com.even.mricheditor.RichEditorCallback;
import com.even.mricheditor.ui.ActionImageView;
import com.google.gson.Gson;
import java.util.Arrays;
import java.util.List;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.DataViewModel;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.libs.html.fragment.EditHyperlinkFragment;
import apps.envision.mychurch.libs.html.fragment.EditTableFragment;
import apps.envision.mychurch.libs.html.fragment.EditorMenuFragment;
import apps.envision.mychurch.libs.html.interfaces.OnActionPerformListener;
import apps.envision.mychurch.libs.html.keyboard.KeyboardHeightObserver;
import apps.envision.mychurch.libs.html.keyboard.KeyboardHeightProvider;
import apps.envision.mychurch.libs.html.keyboard.KeyboardUtils;
import apps.envision.mychurch.libs.html.util.FileIOUtil;
import apps.envision.mychurch.pojo.Note;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class NewNotesActivity extends AppCompatActivity implements KeyboardHeightObserver {

    private Note note;
    private String content = "";
    private boolean isSaved = false;
    private DataViewModel dataViewModel;

    @BindView(R.id.wv_container)
    WebView mWebView;
    @BindView(R.id.fl_action)
    FrameLayout flAction;
    @BindView(R.id.ll_action_bar_container)
    LinearLayout llActionBarContainer;

    /** The keyboard height provider */
    private KeyboardHeightProvider keyboardHeightProvider;
    private boolean isKeyboardShowing;
    private String htmlContent = "<p>Add your notes here</p>";

    private RichEditorAction mRichEditorAction;
    private RichEditorCallback mRichEditorCallback;

    private EditorMenuFragment mEditorMenuFragment;

    private List<ActionType> mActionTypeList =
            Arrays.asList(ActionType.BOLD, ActionType.ITALIC, ActionType.UNDERLINE,
                    ActionType.STRIKETHROUGH, ActionType.SUBSCRIPT, ActionType.SUPERSCRIPT,
                    ActionType.NORMAL, ActionType.H1, ActionType.H2, ActionType.H3, ActionType.H4,
                    ActionType.H5, ActionType.H6, ActionType.INDENT, ActionType.OUTDENT,
                    ActionType.JUSTIFY_LEFT, ActionType.JUSTIFY_CENTER, ActionType.JUSTIFY_RIGHT,
                    ActionType.JUSTIFY_FULL, ActionType.ORDERED, ActionType.UNORDERED, ActionType.LINE,
                    ActionType.BLOCK_CODE, ActionType.BLOCK_QUOTE, ActionType.CODE_VIEW);

    private List<Integer> mActionTypeIconList =
            Arrays.asList(R.drawable.ic_format_bold, R.drawable.ic_format_italic,
                    R.drawable.ic_format_underlined, R.drawable.ic_format_strikethrough,
                    R.drawable.ic_format_subscript, R.drawable.ic_format_superscript,
                    R.drawable.ic_format_para, R.drawable.ic_format_h1, R.drawable.ic_format_h2,
                    R.drawable.ic_format_h3, R.drawable.ic_format_h4, R.drawable.ic_format_h5,
                    R.drawable.ic_format_h6, R.drawable.ic_format_indent_decrease,
                    R.drawable.ic_format_indent_increase, R.drawable.ic_format_align_left,
                    R.drawable.ic_format_align_center, R.drawable.ic_format_align_right,
                    R.drawable.ic_format_align_justify, R.drawable.ic_format_list_numbered,
                    R.drawable.ic_format_list_bulleted, R.drawable.ic_line, R.drawable.ic_code_block,
                    R.drawable.ic_format_quote, R.drawable.ic_code_review);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_notes);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.my_notes));

        // Get a new or existing ViewModel from the ViewModelProvider.
        dataViewModel = ViewModelProviders.of(this).get(DataViewModel.class);

        if(getIntent().getStringExtra("notes")!=null){
            Gson gson = new Gson();
            note = gson.fromJson(getIntent().getStringExtra("notes"), Note.class);
        }
        if(getIntent().getStringExtra("content")!=null){
            content = getIntent().getStringExtra("content");
        }

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    content = sharedText.trim();
                }
            }
        }

        ButterKnife.bind(this);
        initView();

        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40,
                getResources().getDisplayMetrics());
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 9,
                getResources().getDisplayMetrics());
        for (int i = 0, size = mActionTypeList.size(); i < size; i++) {
            final ActionImageView actionImageView = new ActionImageView(this);
            actionImageView.setLayoutParams(new LinearLayout.LayoutParams(width, width));
            actionImageView.setPadding(padding, padding, padding, padding);
            actionImageView.setActionType(mActionTypeList.get(i));
            actionImageView.setTag(mActionTypeList.get(i));
            actionImageView.setActivatedColor(R.color.colorAccent);
            actionImageView.setDeactivatedColor(R.color.tintColor);
            actionImageView.setRichEditorAction(mRichEditorAction);
            actionImageView.setBackgroundResource(R.drawable.btn_colored_material);
            actionImageView.setImageResource(mActionTypeIconList.get(i));
            actionImageView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    actionImageView.command();
                }
            });
            llActionBarContainer.addView(actionImageView);
        }

        mEditorMenuFragment = new EditorMenuFragment();
        mEditorMenuFragment.setActionClickListener(new MOnActionPerformListener(mRichEditorAction));
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .add(R.id.fl_action, mEditorMenuFragment, EditorMenuFragment.class.getName())
                .commit();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            case R.id.redo:
                mRichEditorAction.redo();
                return true;
            case R.id.undo:
                mRichEditorAction.undo();
                return true;
            case R.id.save:
                mRichEditorAction.refreshHtml(mRichEditorCallback, onGetHtmlListener);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_notes, menu);
        return true;
    }

    private void save_note(String content_){
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.save_note_alert, null);

        final EditText title = (EditText) dialogView.findViewById(R.id.title);
        Button button1 = (Button) dialogView.findViewById(R.id.buttonSubmit);
        Button button2 = (Button) dialogView.findViewById(R.id.buttonCancel);
        if(note!=null){
            title.setText(note.getTitle());
        }

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBuilder.dismiss();
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // DO SOMETHINGS
                dialogBuilder.dismiss();
                String title_ = title.getText().toString();
                if(title_.equalsIgnoreCase("")){
                    Toast.makeText(App.getContext(), getString(R.string.enter_note_title_hint),Toast.LENGTH_SHORT).show();
                }else{
                    if(note!=null){
                        note.setTitle(title_);
                        note.setContent(content_.replace("\"", "").replace("'", ""));
                        dataViewModel.saveNote(note);
                    }else{
                        Note note = new Note();
                        note.setTitle(title_);
                        note.setContent(content_.replace("\"", "").replace("'", ""));
                        note.setTime(System.currentTimeMillis());
                        dataViewModel.saveNote(note);
                    }

                    finish();
                }
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    private void initView() {
        mWebView.setWebViewClient(new WebViewClient() {
            @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        mWebView.setWebChromeClient(new CustomWebChromeClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mRichEditorCallback = new MRichEditorCallback();
        mWebView.addJavascriptInterface(mRichEditorCallback, "MRichEditor");
        mWebView.loadUrl("file:///android_asset/richEditor.html");
        mRichEditorAction = new RichEditorAction(mWebView);

        keyboardHeightProvider = new KeyboardHeightProvider(this);
        findViewById(R.id.fl_container).post(new Runnable() {
            @Override public void run() {
                keyboardHeightProvider.start();
            }
        });

        findViewById(R.id.finish_notes_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private class CustomWebChromeClient extends WebChromeClient {
        @Override public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
           // Toast.makeText(App.getContext(),String.valueOf(newProgress),Toast.LENGTH_SHORT).show();
            if (newProgress == 100) {
                if(note!=null){
                    mRichEditorAction.insertHtml("<p>"+note.getContent().replace("\"", "").replace("'", "")+"</p>");
                }else if(!content.equalsIgnoreCase("")){
                    //Log.e("content",content);
                    mRichEditorAction.insertHtml(content.replace("\"", "").replace("'", ""));
                }else if (!TextUtils.isEmpty(htmlContent)) {
                    mRichEditorAction.insertHtml(htmlContent);
                }
                KeyboardUtils.showSoftInput(NewNotesActivity.this);
            }
        }

        @Override public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }
    }

    @OnClick(R.id.iv_action) void onClickAction() {
        if (flAction.getVisibility() == View.VISIBLE) {
            flAction.setVisibility(View.GONE);
        } else {
            if (isKeyboardShowing) {
                KeyboardUtils.hideSoftInput(NewNotesActivity.this);
            }
            flAction.setVisibility(View.VISIBLE);
        }
    }

    private RichEditorCallback.OnGetHtmlListener onGetHtmlListener =
            html -> {
                if (TextUtils.isEmpty(html)) {
                    Toast.makeText(NewNotesActivity.this, "Empty String", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                save_note(html);
            };

    @OnClick(R.id.iv_action_txt_color) void onClickTextColor() {
        mRichEditorAction.foreColor("blue");
    }

    @OnClick(R.id.iv_action_txt_bg_color) void onClickHighlight() {
        mRichEditorAction.backColor("red");
    }

    @OnClick(R.id.iv_action_line_height) void onClickLineHeight() {
        mRichEditorAction.lineHeight(20);
    }

    private static String encodeFileToBase64Binary(String filePath) {
        byte[] bytes = FileIOUtil.readFile2BytesByStream(filePath);
        byte[] encoded = Base64.encode(bytes, Base64.NO_WRAP);
        return new String(encoded);
    }

    @OnClick(R.id.iv_action_insert_link) void onClickInsertLink() {
        KeyboardUtils.hideSoftInput(NewNotesActivity.this);
        EditHyperlinkFragment fragment = new EditHyperlinkFragment();
        fragment.setOnHyperlinkListener(new EditHyperlinkFragment.OnHyperlinkListener() {
            @Override public void onHyperlinkOK(String address, String text) {
                mRichEditorAction.createLink(text, address);
            }
        });
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fl_container, fragment, EditHyperlinkFragment.class.getName())
                .commit();
    }

    @OnClick(R.id.iv_action_table) void onClickInsertTable() {
        KeyboardUtils.hideSoftInput(NewNotesActivity.this);
        EditTableFragment fragment = new EditTableFragment();
        fragment.setOnTableListener(new EditTableFragment.OnTableListener() {
            @Override public void onTableOK(int rows, int cols) {
                mRichEditorAction.insertTable(rows, cols);
            }
        });
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fl_container, fragment, EditHyperlinkFragment.class.getName())
                .commit();
    }

    @Override public void onResume() {
        super.onResume();
        keyboardHeightProvider.setKeyboardHeightObserver(this);
    }

    @Override public void onPause() {
        super.onPause();
        keyboardHeightProvider.setKeyboardHeightObserver(null);
        if (flAction.getVisibility() == View.INVISIBLE) {
            flAction.setVisibility(View.GONE);
        }
    }

    @Override public void onDestroy() {
        super.onDestroy();
        keyboardHeightProvider.close();
    }

    @Override public void onKeyboardHeightChanged(int height, int orientation) {
        isKeyboardShowing = height > 0;
        if (height != 0) {
            flAction.setVisibility(View.INVISIBLE);
            ViewGroup.LayoutParams params = flAction.getLayoutParams();
            params.height = height;
            flAction.setLayoutParams(params);
            performInputSpaceAndDel();
        } else if (flAction.getVisibility() != View.VISIBLE) {
            flAction.setVisibility(View.GONE);
        }
    }

    //TODO not a good solution
    private void performInputSpaceAndDel() {
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    Thread.sleep(100);
                    Instrumentation instrumentation = new Instrumentation();
                    instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_SPACE);
                    instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    class MRichEditorCallback extends RichEditorCallback {

        @Override public void notifyFontStyleChange(ActionType type, final String value) {
            ActionImageView actionImageView =
                    (ActionImageView) llActionBarContainer.findViewWithTag(type);
            if (actionImageView != null) {
                actionImageView.notifyFontStyleChange(type, value);
            }

            if (mEditorMenuFragment != null) {
                mEditorMenuFragment.updateActionStates(type, value);
            }
        }
    }

    public class MOnActionPerformListener implements OnActionPerformListener {
        private RichEditorAction mRichEditorAction;

        public MOnActionPerformListener(RichEditorAction mRichEditorAction) {
            this.mRichEditorAction = mRichEditorAction;
        }

        @Override public void onActionPerform(ActionType type, Object... values) {
            if (mRichEditorAction == null) {
                return;
            }

            String value = null;
            if (values != null && values.length > 0) {
                value = (String) values[0];
            }

            switch (type) {
                case SIZE:
                    mRichEditorAction.fontSize(Double.valueOf(value));
                    break;
                case LINE_HEIGHT:
                    mRichEditorAction.lineHeight(Double.valueOf(value));
                    break;
                case FORE_COLOR:
                    mRichEditorAction.foreColor(value);
                    break;
                case BACK_COLOR:
                    mRichEditorAction.backColor(value);
                    break;
                case FAMILY:
                    mRichEditorAction.fontName(value);
                    break;
                case LINK:
                    onClickInsertLink();
                    break;
                case TABLE:
                    onClickInsertTable();
                    break;
                case BOLD:
                case ITALIC:
                case UNDERLINE:
                case SUBSCRIPT:
                case SUPERSCRIPT:
                case STRIKETHROUGH:
                case JUSTIFY_LEFT:
                case JUSTIFY_CENTER:
                case JUSTIFY_RIGHT:
                case JUSTIFY_FULL:
                case CODE_VIEW:
                case ORDERED:
                case UNORDERED:
                case INDENT:
                case OUTDENT:
                case BLOCK_QUOTE:
                case BLOCK_CODE:
                case NORMAL:
                case H1:
                case H2:
                case H3:
                case H4:
                case H5:
                case H6:
                case LINE:
                    ActionImageView actionImageView =
                            (ActionImageView) llActionBarContainer.findViewWithTag(type);
                    if (actionImageView != null) {
                        actionImageView.performClick();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        check_and_finish_activity();
    }

    private void check_and_finish_activity(){
        AlertDialog.Builder builder = new AlertDialog.Builder(NewNotesActivity.this);
        builder.setTitle(getString(R.string.discard_changes));
        builder.setMessage(getString(R.string.discard_changes_hint));

        builder.setPositiveButton("Ok",
                (dialog, which) -> {
                    // positive button logic
                    dialog.dismiss();
                    finish();

                });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        // display dialog
        dialog.show();
    }
}
