package apps.envision.mychurch.interfaces;

import java.util.List;
import java.util.UUID;

import apps.envision.mychurch.pojo.Bible;
import apps.envision.mychurch.pojo.BibleVersions;
import apps.envision.mychurch.pojo.SBible;

/**
 * Created by Ray on 6/25/2018.
 */

public interface BibleClickListener {
   List<String> getDownloadedVersion();
   void show_bottom_layout();
   void hide_bottom_layout();
   void set_selected_verses(Bible bible, String version);
   void remove_selected_verses(Bible bible);
   boolean is_bible_verse_selected(Bible bible);
   void remove_highlight_verse(Bible bible);
   SBible is_bible_verse_highlighted(Bible bible);
   int get_total_selected_verses();
}
