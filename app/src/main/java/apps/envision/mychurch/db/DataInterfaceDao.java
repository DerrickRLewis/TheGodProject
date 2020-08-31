package apps.envision.mychurch.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;

import apps.envision.mychurch.pojo.Bible;
import apps.envision.mychurch.pojo.Bookmarks;
import apps.envision.mychurch.pojo.Categories;
import apps.envision.mychurch.pojo.Devotionals;
import apps.envision.mychurch.pojo.Download;
import apps.envision.mychurch.pojo.Events;
import apps.envision.mychurch.pojo.Hymns;
import apps.envision.mychurch.pojo.Inbox;
import apps.envision.mychurch.pojo.Livestreams;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.pojo.Note;
import apps.envision.mychurch.pojo.Playing_Audios;
import apps.envision.mychurch.pojo.Playing_Videos;
import apps.envision.mychurch.pojo.Playlist;
import apps.envision.mychurch.pojo.PlaylistMedias;
import apps.envision.mychurch.pojo.Radio;
import apps.envision.mychurch.pojo.Search;
import apps.envision.mychurch.pojo.Slider;

/**
 * Created by ray on 08/09/2018.
 * database query class
 */
@Dao
public interface DataInterfaceDao {

    //bible CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBible(Bible bible);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllBible(List<Bible> bibleList);

    @Query("SELECT * from bible_table")
    List<Bible> getAllBible();

    @Query("SELECT * from bible_table WHERE book COLLATE NOCASE = :book AND chapter = :chapter AND verse = :verse")
    Bible getBible(String book, int chapter, int verse);

    @Query("UPDATE bible_table SET AMP =:amp WHERE book COLLATE NOCASE = :book AND chapter = :chapter AND verse = :verse")
    void updateAMP(String amp, String book, int chapter, int verse);

    @Query("UPDATE bible_table SET KJV =:amp WHERE book COLLATE NOCASE = :book AND chapter = :chapter AND verse = :verse")
    void updateKJV(String amp, String book, int chapter, int verse);

    @Query("UPDATE bible_table SET MSG =:amp WHERE book COLLATE NOCASE = :book AND chapter = :chapter AND verse = :verse")
    void updateMSG(String amp, String book, int chapter, int verse);

    @Query("UPDATE bible_table SET NIV =:amp WHERE book COLLATE NOCASE = :book AND chapter = :chapter AND verse = :verse")
    void updateNIV(String amp, String book, int chapter, int verse);

    @Query("UPDATE bible_table SET NKJV =:amp WHERE book COLLATE NOCASE = :book AND chapter = :chapter AND verse = :verse")
    void updateNKJV(String amp, String book, int chapter, int verse);

    @Query("UPDATE bible_table SET NLT =:amp WHERE book COLLATE NOCASE = :book AND chapter = :chapter AND verse = :verse")
    void updateNLT(String amp, String book, int chapter, int verse);

    @Query("UPDATE bible_table SET NRSV =:amp WHERE book COLLATE NOCASE = :book AND chapter = :chapter AND verse = :verse")
    void updateNRSV(String amp, String book, int chapter, int verse);

    @Query("SELECT COUNT(*) from bible_table WHERE AMP !='' ")
    int getBibleAMP();

    @Query("SELECT COUNT(*) from bible_table WHERE KJV !='' ")
    int getBibleKJV();

    @Query("SELECT COUNT(*) from bible_table WHERE MSG !='' ")
    int getBibleMSG();

    @Query("SELECT COUNT(*) from bible_table WHERE NIV !='' ")
    int getBibleNIV();

    @Query("SELECT COUNT(*) from bible_table WHERE NKJV !='' ")
    int getBibleNKJV();

    @Query("SELECT COUNT(*) from bible_table WHERE NLT !='' ")
    int getBibleNLT();

    @Query("SELECT COUNT(*) from bible_table WHERE NRSV !='' ")
    int getBibleNRSV();

    @Query("SELECT * from bible_table WHERE book COLLATE NOCASE = :book AND chapter = :chapter")
    LiveData<List<Bible>> getBibleByChapters(String book, int chapter);

    @Query("SELECT * FROM bible_table WHERE book IN(:books) AND AMP LIKE :query LIMIT 50")
    LiveData<List<Bible>> searchBibleAMP(String[] books,String query);

    @Query("SELECT * FROM bible_table WHERE book IN(:books) AND KJV LIKE :query LIMIT 50")
    LiveData<List<Bible>> searchBibleKJV(String[] books,String query);

    @Query("SELECT * FROM bible_table WHERE book IN(:books) AND NKJV LIKE :query LIMIT 50")
    LiveData<List<Bible>> searchBibleNKJV(String[] books,String query);

    @Query("SELECT * FROM bible_table WHERE book IN(:books) AND NLT LIKE :query LIMIT 50")
    LiveData<List<Bible>> searchBibleNLT(String[] books,String query);

    @Query("SELECT * FROM bible_table WHERE book IN(:books) AND NIV LIKE :query LIMIT 50")
    LiveData<List<Bible>> searchBibleNIV(String[] books,String query);

    @Query("SELECT * FROM bible_table WHERE book IN(:books) AND MSG LIKE :query LIMIT 50")
    LiveData<List<Bible>> searchBibleMSG(String[] books,String query);

    @Query("SELECT * FROM bible_table WHERE book IN(:books) AND NRSV LIKE :query LIMIT 50")
    LiveData<List<Bible>> searchBibleNRSV(String[] books,String query);

    //@RawQuery(observedEntities = Bible.class)
    //LiveData<List<Bible>> searchBibleDatabase(SupportSQLiteQuery query);

    @Query("DELETE FROM bible_table")
    void deleteAllBible();

    @Query("SELECT COUNT(id) FROM bible_table")
    int getBibleCount();

    //categories CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategory(Categories categories);

    @Query("SELECT * from categories_table WHERE id= :id LIMIT 1")
    Categories getCategory(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllCategories(List<Categories> categories);

    @Query("DELETE FROM categories_table")
    void deleteAllCategories();

    @Query("SELECT * from categories_table")
    LiveData<List<Categories>> getAllCategories();

    //media CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMedia(Media media);

    @Query("SELECT * from media_table WHERE id= :id LIMIT 1")
    Media getMedia(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllMedia(List<Media> mediaList);

    @Query("DELETE FROM media_table WHERE media_type COLLATE NOCASE  = :media_type")
    void deleteAllMediaType(String media_type);

    @Query("SELECT * from media_table WHERE media_type COLLATE NOCASE = :media_type")
    LiveData<List<Media>> getAllMediaByType(String media_type);

    //search CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllSearch(List<Search> search);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSearch(Search search);

    @Query("DELETE FROM search_table")
    void deleteAllSearch();

    @Query("SELECT * from search_table")
    LiveData<List<Search>> getAllSearch();


    //bookmarks CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void bookmarkMedia(Bookmarks bookmarks);

    @Query("DELETE FROM bookmarks_table")
    void deleteAllBookmarks();

    @Query("DELETE FROM bookmarks_table WHERE id= :id")
    void removeMediaFromBookmarks(long id);

    @Query("SELECT * from bookmarks_table WHERE id= :id LIMIT 1")
    Bookmarks getBookmark(long id);

    @Query("SELECT * from bookmarks_table ORDER BY addTimeStamp DESC")
    LiveData<List<Bookmarks>> getAllBookmarks();


    //playlist CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long createPlaylist(Playlist playlist);

    @Query("DELETE FROM playlist_table WHERE id= :id")
    void deletePlaylist(long id);

    @Query("SELECT * from playlist_table WHERE media_type COLLATE NOCASE  = :media_type")
    LiveData<List<Playlist>> getAllPlaylists(String media_type);

    @Query("SELECT * from playlist_table")
    LiveData<List<Playlist>> getAllPlaylists();


    //playlistMedia CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addMediaToPlaylist(PlaylistMedias playlistMedias);

    @Query("SELECT * from playlistmedias_table WHERE media_id = :id LIMIT 1")
    PlaylistMedias fetchPlaylistMedia(long id);

    @Query("DELETE FROM playlistmedias_table WHERE media_id= :media_id AND playlist_id=:playlist_id")
    void deletePlaylistMedia(long media_id, long playlist_id);

    @Query("SELECT * from playlistmedias_table")
    LiveData<List<PlaylistMedias>> getAllPlaylistsMedia();

    @Query("SELECT * from playlistmedias_table WHERE playlist_id = :playlist_id")
    LiveData<List<PlaylistMedias>> getPlaylistsMedia(long playlist_id);

    @Query("SELECT count(*) from playlistmedias_table WHERE playlist_id = :playlist_id")
    int countPlaylistMedia(long playlist_id);

    @Query("DELETE FROM playlistmedias_table WHERE playlist_id= :playlist_id")
    void deleteAllPlaylistMedia(long playlist_id);

    @Query("SELECT * from playlistmedias_table WHERE playlist_id= :playlist_id LIMIT 1")
    PlaylistMedias getPlaylistMedia(long playlist_id);


    //current downloads CRUD
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long addCurrentDownload(Download download);

    //this will update the download data
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long updateCurrentDownload(Download download);

    @Query("DELETE FROM current_downloads_table WHERE id= :id")
    void deleteCurrentDownload(long id);

    @Query("SELECT * from current_downloads_table LIMIT 1")
    LiveData<Download> observeCurrentDownload();

    @Query("DELETE FROM current_downloads_table")
    void clearDownloads();

    //Playing videos CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPlayingVideos(List<Playing_Videos> playing_videos);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long updatePlayingVideos(Playing_Videos playing_videos);

    @Query("DELETE FROM playing_video_table")
    void clearPlayingVideos();

    @Query("SELECT * from playing_video_table")
    LiveData<List<Playing_Videos>> getPlayingVideos();

    //Playing audios CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPlayingAudios(List<Playing_Audios> playing_audios);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long updatePlayingAudios(Playing_Audios playing_audios);

    @Query("DELETE FROM playing_audio_table")
    void clearPlayingAudios();

    @Query("SELECT * from playing_audio_table")
    LiveData<List<Playing_Audios>> getPlayingAudios();


    //livestreams CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllLiveStreams(List<Livestreams> sliderList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLiveStreams(Livestreams sliderList);

    @Query("DELETE FROM livestreams_table")
    void deleteAllLiveStreams();

    @Query("SELECT * from livestreams_table")
    LiveData<List<Livestreams>> getAllLiveStreams();

    //radio CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRadio(Radio radio);

    @Query("DELETE FROM radio_table")
    void deleteAllRadio();

    @Query("SELECT * from radio_table")
    LiveData<List<Radio>> getAllRadio();

    //devotional CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDevotional(Devotionals devotionals);

    @Query("SELECT * from devotionals_table WHERE date= :date LIMIT 1")
    Devotionals getDevotional(String date);

    //livestreams CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllEvents(List<Events> events);

    @Query("SELECT * from events_table  WHERE date= :date")
    List<Events> getAllEvents(String date);

    //inbox CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertInbox(Inbox inbox);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllInBox(List<Inbox> inboxes);

    @Query("DELETE FROM inbox_table")
    void deleteAllInBox();

    @Query("SELECT * from inbox_table ORDER BY date desc")
    LiveData<List<Inbox>> getAllInBox();


    //notes CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveNote(Note note);

    @Query("DELETE FROM notes_table")
    void deleteAllNotes();

    @Query("DELETE FROM notes_table WHERE id= :id")
    void deleteNote(long id);

    @Query("SELECT * from notes_table WHERE id= :id LIMIT 1")
    Note getNote(long id);

    @Query("SELECT * from notes_table ORDER BY time DESC")
    LiveData<List<Note>> getAllNotes();

    @Query("SELECT * from notes_table WHERE id= :id LIMIT 1")
    LiveData<Note> getNoteById(long id);

    @Query("SELECT * FROM notes_table WHERE title LIKE :query OR content LIKE :query")
    LiveData<List<Note>> searchNotes(String query);

    @Query("SELECT * FROM notes_table WHERE title LIKE :query OR content LIKE :query")
    List<Note> queryNotes(String query);



    //hymns CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void bookmarkHymn(Hymns hymns);

    @Query("DELETE FROM hymns_table WHERE id= :id")
    void deleteBookmarkedHymn(long id);

    @Query("SELECT * from hymns_table ORDER BY time DESC")
    LiveData<List<Hymns>> getAllBookmarkedHymns();

    @Query("SELECT * FROM hymns_table WHERE title LIKE :query OR content LIKE :query")
    LiveData<List<Hymns>> searchHymns(String query);

    //slider media CRUD
   /* @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllSliderMedia(List<Slider> sliderList);

    @Query("DELETE FROM slider_table")
    void deleteAllSLiderMedia();

    @Query("SELECT * from slider_table")
    LiveData<List<Slider>> getAllSliderMedia();*/
}
