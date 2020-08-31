package apps.envision.mychurch.db;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.lifecycle.Transformations;
import androidx.sqlite.db.SupportSQLiteQuery;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.pojo.Bible;
import apps.envision.mychurch.pojo.BibleMediator;
import apps.envision.mychurch.pojo.Bookmarks;
import apps.envision.mychurch.pojo.Categories;
import apps.envision.mychurch.pojo.Download;
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
import apps.envision.mychurch.utils.Constants;

/**
 * Repository handling the work with POJO objects.
 */
public class DataRepository {

    private DataInterfaceDao dataInterfaceDao;
    private LiveData<List<Categories>> categories;
    private LiveData<List<Media>> media;
    private LiveData<List<Media>> latestVideos;
    private LiveData<List<Media>> latestAudios;
    private LiveData<List<Search>> search;
    private LiveData<List<Bookmarks>> bookmarks;
    private MutableLiveData<String> mediaFilterType = new MutableLiveData<String>();
    private LiveData<List<Note>> noteList;
    private LiveData<Note> currentNote;
    private MutableLiveData<String> filterCurrentNote = new MutableLiveData<String>();
    private LiveData<List<Note>> searchNotesList;

    private MutableLiveData<String> filterCurrentHymn = new MutableLiveData<String>();
    private LiveData<List<Hymns>> searchHymnsList;
    private LiveData<List<Hymns>> hymnsList;
    private LiveData<List<Slider>> slider;

    private LiveData<List<Playlist>> playlist;
    private LiveData<List<Playlist>> allPlaylist;
    private LiveData<List<PlaylistMedias>> playlistMedias;
    private LiveData<List<PlaylistMedias>> allPlaylistMedias;
    private MutableLiveData<Long> filterPlaylistMedia = new MutableLiveData<Long>();
    private LiveData<List<Playing_Videos>> playing_videos;
    private LiveData<List<Playing_Audios>> playing_audios;
    private LiveData<List<Radio>> radio;
    private LiveData<List<Livestreams>> liveStreams;
    private LiveData<List<Inbox>> inbox;
    private LiveData<Download> current_download;
    private LiveData<List<Bible>> bible;
    private LiveData<List<Bible>> bible_search;
    private LiveData<List<Bible>> biblesearch;
    private final MutableLiveData<BibleFilter> bibleFilterMutableLiveData;
    private final MutableLiveData<BibleSearchFilter> bibleSearchFilterMutableLiveData;
    private MutableLiveData<SupportSQLiteQuery> filterBibleQuery = new MutableLiveData<SupportSQLiteQuery>();

    private ExecutorService executorService;

    public DataRepository(Application application) {
        AppDb db = AppDb.getDatabase(application);
        executorService = Executors.newSingleThreadExecutor();
        dataInterfaceDao = db.dataDbInterface();
        categories = dataInterfaceDao.getAllCategories();
        media = Transformations.switchMap(mediaFilterType, v -> dataInterfaceDao.getAllMediaByType(v));
        latestVideos = dataInterfaceDao.getAllMediaByType(App.getContext().getResources().getString(R.string.video));
        latestAudios = dataInterfaceDao.getAllMediaByType(App.getContext().getResources().getString(R.string.audio));
        search = dataInterfaceDao.getAllSearch();
        bookmarks = dataInterfaceDao.getAllBookmarks();
        noteList = dataInterfaceDao.getAllNotes();
        allPlaylist = dataInterfaceDao.getAllPlaylists();
        playlist  = Transformations.switchMap(mediaFilterType, v -> dataInterfaceDao.getAllPlaylists(v));
        playlistMedias = Transformations.switchMap(filterPlaylistMedia, v -> dataInterfaceDao.getPlaylistsMedia(v));
        allPlaylistMedias = dataInterfaceDao.getAllPlaylistsMedia();
        current_download = dataInterfaceDao.observeCurrentDownload();
        playing_videos = dataInterfaceDao.getPlayingVideos();
        playing_audios = dataInterfaceDao.getPlayingAudios();
        radio = dataInterfaceDao.getAllRadio();
        liveStreams = dataInterfaceDao.getAllLiveStreams();
        inbox = dataInterfaceDao.getAllInBox();
        bibleFilterMutableLiveData = new MutableLiveData<>();
        bible = Transformations.switchMap(bibleFilterMutableLiveData, input -> dataInterfaceDao.getBibleByChapters(input.book, input.chapter));
        bibleSearchFilterMutableLiveData = new MutableLiveData<>();
        bible_search = Transformations.switchMap(bibleSearchFilterMutableLiveData, input -> {
            switch (input.version){
                case Constants.BIBLE_VERSIONS.AMP: default:
                    return dataInterfaceDao.searchBibleAMP(input.books,input.query);
                case Constants.BIBLE_VERSIONS.KJV:
                    return dataInterfaceDao.searchBibleKJV(input.books,input.query);
                case Constants.BIBLE_VERSIONS.NKJV:
                    return dataInterfaceDao.searchBibleNKJV(input.books,input.query);
                case Constants.BIBLE_VERSIONS.NLT:
                    return dataInterfaceDao.searchBibleNLT(input.books,input.query);
                case Constants.BIBLE_VERSIONS.NIV:
                    return dataInterfaceDao.searchBibleNIV(input.books,input.query);
                case Constants.BIBLE_VERSIONS.MSG:
                    return dataInterfaceDao.searchBibleMSG(input.books,input.query);
                case Constants.BIBLE_VERSIONS.NRSV:
                    return dataInterfaceDao.searchBibleNRSV(input.books,input.query);
            }
        });
       // biblesearch = Transformations.switchMap(filterBibleQuery, v -> dataInterfaceDao.searchBibleDatabase(v));
        //currentNote = Transformations.switchMap(filterCurrentNote, v -> dataInterfaceDao.getNoteById(v));
        searchNotesList = Transformations.switchMap(filterCurrentNote, v -> dataInterfaceDao.searchNotes(v));
        searchHymnsList = Transformations.switchMap(filterCurrentHymn, v -> dataInterfaceDao.searchHymns(v));
        hymnsList = dataInterfaceDao.getAllBookmarkedHymns();
       // slider = dataInterfaceDao.getAllSliderMedia();
    }

    static class BibleFilter {
        final String book;
        final int chapter;

        BibleFilter(String book, int chapter) {
            this.book = book.trim();
            this.chapter = chapter;
        }
    }

    static class BibleSearchFilter {
        final String[] books;
        final String query;
        final String version;

        BibleSearchFilter(String[] books, String query, String version) {
            this.books = books;
            this.query = query.trim();
            this.version = version;
        }
    }

    public LiveData<List<Bible>> getBible() {
        return bible;
    }

    public void setBibleFilterData(String book, int chapter) {
        BibleFilter update = new BibleFilter(book, chapter);
        bibleFilterMutableLiveData.setValue(update);
    }

    public LiveData<List<Bible>> searchBible() {
        return bible_search;
    }

    public void setBibleSearchFilterData(String[] books, String query, String version) {
        BibleSearchFilter update = new BibleSearchFilter(books, query,version);
        bibleSearchFilterMutableLiveData.setValue(update);
    }




    //used to filter media by type from our database
    void setMediaFilterType(String mediaFilterType){
        this.mediaFilterType.setValue(mediaFilterType.toLowerCase());
    }

    //used to filter bible search from our database
    void setBibleSearchQuery(SupportSQLiteQuery query){
        this.filterBibleQuery.setValue(query);
    }

    public LiveData<List<Bible>> searchBibleDatabase() {
        return biblesearch;
    }

    LiveData<List<Categories>> getCategories() {
        return categories;
    }

    LiveData<List<Note>> searchNotes() {
        return searchNotesList;
    }

    LiveData<List<Hymns>> searchHymns() {
        return searchHymnsList;
    }

    LiveData<List<Hymns>> fetchHymns() {
        return hymnsList;
    }

    LiveData<List<Media>> getMedia() {
        return media;
    }

    LiveData<List<Media>> getLatestVideos() {
        return latestVideos;
    }

    LiveData<List<Media>> getLatestAudios() {
        return latestAudios;
    }

    LiveData<List<Search>> getSearch() {
        return search;
    }

    LiveData<List<Bookmarks>> getBookmarks() {
        return bookmarks;
    }

    LiveData<List<Note>> getNotes() {
        return noteList;
    }

    LiveData<List<Playlist>> getAllPlaylists() {
        return allPlaylist;
    }

    LiveData<List<Playlist>> getPlaylists() {
        return playlist;
    }

    LiveData<List<Radio>> getRadio() {
        return radio;
    }

    LiveData<List<Slider>> getSliderMedia() {
        return slider;
    }

    LiveData<List<PlaylistMedias>> getPlaylistsMedia(){
        return playlistMedias;
    }

    void setPlaylistMediaFilterId(long id){
        filterPlaylistMedia.setValue(id);
    }

    LiveData<List<PlaylistMedias>> getAllPlaylistMedias(){
        return allPlaylistMedias;
    }

    LiveData<Download> observeCurrentDownload() {
        return current_download;
    }

    LiveData<List<Playing_Videos>> getPlayingVideos() {
        return playing_videos;
    }

    LiveData<List<Playing_Audios>> getPlayingAudios() {
        return playing_audios;
    }

    LiveData<List<Livestreams>> getAllLivestreams() {
        return liveStreams;
    }

    LiveData<List<Inbox>> getAllInbox() {
        return inbox;
    }

    LiveData<Note> getCurrentNote(){
        return currentNote;
    }

    void setSearchNotesFilter(String query){
        filterCurrentNote.setValue(query);
    }

    void setSearchHymnsFilter(String query){
        filterCurrentHymn.setValue(query);
    }


    //categories
    public void insertCategory(Categories categories) {
        executorService.execute(() -> dataInterfaceDao.insertCategory(categories));
    }

    public void insertAllCategories(List<Categories> categories) {
        executorService.execute(() -> dataInterfaceDao.insertAllCategories(categories));
    }

    public void deleteAllCategories() {
        executorService.execute(() -> dataInterfaceDao.deleteAllCategories());
    }


    //search
    public void insertAllSearch(List<Search> search) {
        executorService.execute(() -> dataInterfaceDao.insertAllSearch(search));
    }
    public void deleteAllSearch() {
        executorService.execute(() -> dataInterfaceDao.deleteAllSearch());
    }


    //media
    public void insertMedia(Media media) {
        executorService.execute(() -> dataInterfaceDao.insertMedia(media));
    }

    public void insertAllMedia(List<Media> media) {
        executorService.execute(() -> dataInterfaceDao.insertAllMedia(media));
    }

    public void deleteAllMediaByType(String media_type) {
        executorService.execute(() -> dataInterfaceDao.deleteAllMediaType(media_type));
    }


    //bookmarks
    public void bookmarkMedia(Bookmarks bookmarks) {
        executorService.execute(() -> dataInterfaceDao.bookmarkMedia(bookmarks));
    }

    public void removeMediaFromBookmarks(long id) {
        executorService.execute(() -> dataInterfaceDao.removeMediaFromBookmarks(id));
    }

    public void deleteAllBookmarks() {
        executorService.execute(() -> dataInterfaceDao.deleteAllBookmarks());
    }


    //playlist
    public void createPlaylist(Playlist playlist) {
        executorService.execute(() -> dataInterfaceDao.createPlaylist(playlist));
    }

    public void deletePlaylist(long id) {
        executorService.execute(() -> dataInterfaceDao.deletePlaylist(id));
    }


    //playlistMedia
    public void addMediaToPlaylist(PlaylistMedias media){
        executorService.execute(() -> dataInterfaceDao.addMediaToPlaylist(media));
    }

    public void deletePlaylistMedia(long media_id, long playlist_id){
        executorService.execute(() -> dataInterfaceDao.deletePlaylistMedia(media_id,playlist_id));
    }

    public void deleteAllPlaylistMedia(long playlist_id){
        executorService.execute(() -> dataInterfaceDao.deleteAllPlaylistMedia(playlist_id));
    }


    //current downloads
    public void addCurrentDownload(Download download) {
        executorService.execute(() -> dataInterfaceDao.addCurrentDownload(download));
    }

    public void updateCurrentDownload(Download download) {
        executorService.execute(() -> dataInterfaceDao.updateCurrentDownload(download));
    }

    public void deleteCurrentDownload(long id) {
        executorService.execute(() -> dataInterfaceDao.deleteCurrentDownload(id));
    }

    public void clearDownloads() {
        executorService.execute(() -> dataInterfaceDao.clearDownloads());
    }


    //playing media
    public void insertPlayingVideos(List<Playing_Videos> playing_videos) {
        executorService.execute(() -> dataInterfaceDao.insertPlayingVideos(playing_videos));
    }

    public void updatePlayingVideos(Playing_Videos playing_videos) {
        executorService.execute(() -> dataInterfaceDao.updatePlayingVideos(playing_videos));
    }

    public void clearPlayingVideos() {
        executorService.execute(() -> dataInterfaceDao.clearPlayingVideos());
    }

    //playing audio
    public void insertPlayingAudios(List<Playing_Audios> playing_audios) {
        executorService.execute(() -> dataInterfaceDao.insertPlayingAudios(playing_audios));
    }

    public void updatePlayingAudios(Playing_Audios playing_audios) {
        executorService.execute(() -> dataInterfaceDao.updatePlayingAudios(playing_audios));
    }

    public void clearPlayingAudios() {
        executorService.execute(() -> dataInterfaceDao.clearPlayingAudios());
    }

    //slider media
    public void insertRadio(Radio radio) {
        executorService.execute(() -> dataInterfaceDao.insertRadio(radio));
    }

    public void deleteRadio() {
        executorService.execute(() -> dataInterfaceDao.deleteAllRadio());
    }

    //livestreams
    public void insertLiveStream(Livestreams livestreams) {
        executorService.execute(() -> dataInterfaceDao.insertLiveStreams(livestreams));
    }

    public void insertAllLiveStreams(List<Livestreams> livestreams) {
        executorService.execute(() -> dataInterfaceDao.insertAllLiveStreams(livestreams));
    }

    public void deleteAllLiveStreams() {
        executorService.execute(() -> dataInterfaceDao.deleteAllLiveStreams());
    }

    //inbox
    public void insertInbox(Inbox inbox) {
        executorService.execute(() -> dataInterfaceDao.insertInbox(inbox));
    }

    public void insertAllInbox(List<Inbox> inbox) {
        executorService.execute(() -> dataInterfaceDao.insertAllInBox(inbox));
    }

    public void deleteAllInbox() {
        executorService.execute(() -> dataInterfaceDao.deleteAllInBox());
    }

    //bible
    public void insertBible(Bible bible) {
        executorService.execute(() -> dataInterfaceDao.insertBible(bible));
    }

    public void updateAMP(String content, String book, int chapter, int verse) {
        executorService.execute(() -> dataInterfaceDao.updateAMP(content, book, chapter, verse));
    }

    public void updateKJV(String content, String book, int chapter, int verse) {
        executorService.execute(() -> dataInterfaceDao.updateKJV(content, book, chapter, verse));
    }

    public void updateMSG(String content, String book, int chapter, int verse) {
        executorService.execute(() -> dataInterfaceDao.updateMSG(content, book, chapter, verse));
    }

    public void updateNIV(String content, String book, int chapter, int verse) {
        executorService.execute(() -> dataInterfaceDao.updateNIV(content, book, chapter, verse));
    }

    public void updateNKJV(String content, String book, int chapter, int verse) {
        executorService.execute(() -> dataInterfaceDao.updateNKJV(content, book, chapter, verse));
    }

    public void updateNLT(String content, String book, int chapter, int verse) {
        executorService.execute(() -> dataInterfaceDao.updateNLT(content, book, chapter, verse));
    }

    public void updateNRSV(String content, String book, int chapter, int verse) {
        executorService.execute(() -> dataInterfaceDao.updateNRSV(content, book, chapter, verse));
    }

    //notes
    public void saveNote(Note note) {
        executorService.execute(() -> dataInterfaceDao.saveNote(note));
    }

    public void deleteNote(long id) {
        executorService.execute(() -> dataInterfaceDao.deleteNote(id));
    }

    public void deleteAllNotes() {
        executorService.execute(() -> dataInterfaceDao.deleteAllNotes());
    }

    //notes
    public void bookmarkHymn(Hymns hymns) {
        executorService.execute(() -> dataInterfaceDao.bookmarkHymn(hymns));
    }

    public void deleteBookmarkedHymn(long id) {
        executorService.execute(() -> dataInterfaceDao.deleteBookmarkedHymn(id));
    }

    //slider media
   /* public void insertAllSliderMedia(List<Slider> sliderList) {
        executorService.execute(() -> dataInterfaceDao.insertAllSliderMedia(sliderList));
    }

    public void deleteAllSliderMedia() {
        executorService.execute(() -> dataInterfaceDao.deleteAllSLiderMedia());
    }*/
}