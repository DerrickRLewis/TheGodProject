package apps.envision.mychurch.interfaces;


import apps.envision.mychurch.pojo.Note;

/**
 * Created by Ray on 6/25/2018.
 */

public interface NotesListener {
   void open_note(Note note);
   void edit_note(Note note);
   void delete_note(Note note);
   void share_note(Note note);
   void copy_note(Note note);
   void add_new_note();
}
