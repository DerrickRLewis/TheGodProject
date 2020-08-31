package apps.envision.mychurch.ui.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.DataViewModel;
import apps.envision.mychurch.interfaces.NotesListener;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.Note;
import apps.envision.mychurch.ui.activities.NewNotesActivity;
import apps.envision.mychurch.ui.activities.NotesActivity;
import apps.envision.mychurch.ui.activities.NotesViewerActivity;
import apps.envision.mychurch.ui.activities.RadioPlayerActivity;
import apps.envision.mychurch.ui.adapters.MediaFragmentAdapter;
import apps.envision.mychurch.ui.adapters.NotesAdapter;
import apps.envision.mychurch.utils.ObjectMapper;
import apps.envision.mychurch.utils.Utility;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyNotesFragment extends Fragment implements View.OnClickListener, NotesListener, NotesActivity.OnSearchListener {

    private NotesAdapter notesAdapter;
    private View view;
    private List<Note> noteList = new ArrayList<>();
    private DataViewModel dataViewModel;
    private boolean isSearch = false;

    /**
     * @return
     */
    public static MyNotesFragment newInstance() {
        return new MyNotesFragment();
    }

    /**
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notes, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        notesAdapter = new NotesAdapter(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(notesAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (dy > 0) {
                    LocalMessageManager.getInstance().send(R.id.hide_fab);
                } else if (dy < 0) {
                    LocalMessageManager.getInstance().send(R.id.show_fab);
                }
            }
        });

        // Get a new or existing ViewModel from the ViewModelProvider.
        dataViewModel = ViewModelProviders.of(this).get(DataViewModel.class);
        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        dataViewModel.getNotesList().observe(this, noteList -> {
            // Update the cached copy of the bookmarks in the adapter.
            this.noteList = noteList;
            if(this.noteList==null || this.noteList.size()==0){
                    notesAdapter.setInfo(getString(R.string.note_welcome_text));
            }else{
                notesAdapter.setAdapter(noteList);
            }
        });
        dataViewModel.searchNotes().observe(this, noteList -> {
            // Update the cached copy of the bookmarks in the adapter.
            if(!isSearch)return;
            if(noteList==null || noteList.size()==0){
                notesAdapter.setInfo(getString(R.string.search_notes_empty_hint));
            }else{
                notesAdapter.setSearchAdapter(noteList);
            }
        });
        return view;
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
    }

    @Override
    public void open_note(Note note) {
        Gson gson = new Gson();
        String myJson = gson.toJson(note);
        Intent intent = new Intent(getActivity(), NotesViewerActivity.class);
        intent.putExtra("note", myJson);
        startActivity(intent);
    }

    @Override
    public void edit_note(Note note) {
        Gson gson = new Gson();
        String myJson = gson.toJson(note);
        Intent intent = new Intent(getActivity(), NewNotesActivity.class);
        intent.putExtra("notes", myJson);
        startActivity(intent);
    }

    @Override
    public void delete_note(Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.delete_noe));
        builder.setMessage(getString(R.string.delete_note_hint));
        builder.setPositiveButton("Yes, Delete",
                (dialog, which) -> {
                    // positive button logic
                    dialog.dismiss();
                    dataViewModel.deleteNote(note.getId());

                });
        builder.setNegativeButton("Cancel",
                (dialog, which) -> {
                    // positive button logic
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        // display dialog
        dialog.show();
    }

    @Override
    public void share_note(Note note) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, note.getTitle());
        sharingIntent.putExtra(Intent.EXTRA_TEXT, Utility.stripHtml(note.getContent()));
        startActivity(Intent.createChooser(sharingIntent, "Share Via"));
    }

    @Override
    public void copy_note(Note note) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(getActivity().CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", Utility.stripHtml(note.getContent()));
        clipboard.setPrimaryClip(clip);
        Toast.makeText(App.getContext(),"Copied to clipboard",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void add_new_note() {
        startActivity(new Intent(getActivity(), NewNotesActivity.class));
    }

    @Override
    public void perform_search(String query) {
        isSearch = true;
        dataViewModel.setSearchNotesFilter("%"+query+"%");
    }

    @Override
    public void end_search() {
        isSearch = false;
        if(this.noteList==null || this.noteList.size()==0){
            notesAdapter.setInfo("No notes found.\nTo add a new note, \nTap on the plus icon below.");
        }else{
            notesAdapter.setAdapter(noteList);
        }
    }
}

