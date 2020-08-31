package apps.envision.mychurch.db;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;

import apps.envision.mychurch.pojo.Bible;
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


/**
 * Created by ray on 08/09/2018.
 */

public class DataViewModel extends AndroidViewModel {

    private DataRepository mRepository;

    private LiveData<List<Media>> media;
    private LiveData<List<Media>> latestVideos;
    private LiveData<List<Media>> latestAudios;
    private LiveData<List<Categories>> categories;
    private LiveData<List<Search>> search;
    private LiveData<List<Bookmarks>> bookmarks;
    private LiveData<List<Playlist>> allPlaylist;
    private LiveData<List<Playlist>> playlist;
    private LiveData<List<PlaylistMedias>> playlistMedias;
    private LiveData<List<PlaylistMedias>> allPlaylistMedias;
    private LiveData<List<Playing_Videos>> playing_videos;
    private LiveData<List<Playing_Audios>> playing_audios;
    private LiveData<Download> current_download;
    private LiveData<List<Radio>> radio;
    private LiveData<List<Livestreams>> liveStreams;
    private LiveData<List<Inbox>> inbox;
    private LiveData<List<Bible>> bible;
    private LiveData<List<Bible>> bible_search;
    private LiveData<List<Bible>> biblesearch;
    private LiveData<List<Note>> notesList;
    private LiveData<Note> currentNote;
    private LiveData<List<Note>> searchNotesList;

    private LiveData<List<Hymns>> hymnsList;
    private LiveData<List<Hymns>> searchHymnsList;
    private LiveData<List<Slider>> sliderList;


    public DataViewModel(Application application) {
        super(application);
        mRepository = new DataRepository(application);
        categories = mRepository.getCategories();
        media = mRepository.getMedia();
        latestAudios = mRepository.getLatestAudios();
        latestVideos = mRepository.getLatestVideos();
        search = mRepository.getSearch();
        bookmarks = mRepository.getBookmarks();
        allPlaylist = mRepository.getAllPlaylists();
        playlist = mRepository.getPlaylists();
        playlistMedias = mRepository.getPlaylistsMedia();
        allPlaylistMedias = mRepository.getAllPlaylistMedias();
        current_download = mRepository.observeCurrentDownload();
        playing_videos = mRepository.getPlayingVideos();
        playing_audios = mRepository.getPlayingAudios();
        radio = mRepository.getRadio();
        liveStreams = mRepository.getAllLivestreams();
        inbox = mRepository.getAllInbox();
        bible = mRepository.getBible();
        bible_search = mRepository.searchBible();
        biblesearch = mRepository.searchBibleDatabase();
        notesList = mRepository.getNotes();
        currentNote = mRepository.getCurrentNote();
        searchNotesList = mRepository.searchNotes();

        hymnsList = mRepository.fetchHymns();
        searchHymnsList = mRepository.searchHymns();
        sliderList = mRepository.getSliderMedia();
    }

    public LiveData<List<Categories>> getCategories() { return categories; }

    public LiveData<List<Media>> getMedia() { return media; }

    public LiveData<List<Media>> getLatestVideos() { return latestVideos; }

    public LiveData<List<Media>> getLatestAudios() { return latestAudios; }

    public LiveData<List<Slider>> getSliderList() { return sliderList; }

    public LiveData<List<Search>> getSearch() {
        return search;
    }

    public LiveData<List<Radio>> getRadio() { return radio; }

    public LiveData<List<Note>> getNotesList() {
        return notesList;
    }

    public LiveData<List<Bookmarks>> getBookmarks() {
        return bookmarks;
    }

    public LiveData<List<Playlist>> getAllPlaylist() {
        return allPlaylist;
    }

    public LiveData<List<Playlist>> getPlaylists() {
        return playlist;
    }

    public LiveData<List<PlaylistMedias>> getPlaylistsMedia() {
        return playlistMedias;
    }

    public LiveData<List<PlaylistMedias>> getAllPlaylistsMedia() {
        return allPlaylistMedias;
    }

    public LiveData<List<Livestreams>> getAllLiveStreams() {
        return liveStreams;
    }

    public LiveData<Download> observeCurrentDownload() {
        return current_download;
    }

    public LiveData<List<Bible>> getBible() {
        return bible;
    }

    public LiveData<List<Inbox>> getAllInbox() {
        return inbox;
    }

    public LiveData<List<Bible>> searchBible() {
        return bible_search;
    }

    public LiveData<List<Bible>> searchBibleDatabase() { return biblesearch; }

    public LiveData<Note> getCurrentNote() { return currentNote; }

    public LiveData<List<Note>> searchNotes() { return searchNotesList; }

    public LiveData<List<Hymns>> searchHymns() { return searchHymnsList; }

    public LiveData<List<Hymns>> fetchHymns() { return hymnsList; }

    public void setBibleSearchQuery(SupportSQLiteQuery query){
        mRepository.setBibleSearchQuery(query);
    }

    public void setMediaFilterType(String media_type){
        mRepository.setMediaFilterType(media_type);
    }

    public void setBibleFilter(String book, int chapter){
        mRepository.setBibleFilterData(book,chapter);
    }

    public void setBibleSearchFilter(String[] book, String query, String version){
        mRepository.setBibleSearchFilterData(book,query,version);
    }

    public LiveData<List<Playing_Videos>> getPlayingVideos() {
        return playing_videos;
    }
    public LiveData<List<Playing_Audios>> getPlayingAudios() {
        return playing_audios;
    }

    public void setPlaylistMediaFilterId(long id){
        mRepository.setPlaylistMediaFilterId(id);
    }

    public void setSearchNotesFilter(String query){
        mRepository.setSearchNotesFilter(query);
    }

    public void setSearchHymnsFilter(String query){
        mRepository.setSearchHymnsFilter(query);
    }

    //categories
    public void insertCategory(Categories categories) { mRepository.insertCategory(categories); }
    public void insertAllCategories(List<Categories> categories) { mRepository.insertAllCategories(categories); }
    public void deleteAllCategories() { mRepository.deleteAllCategories(); }


    //search
    public void insertAllSearch(List<Search> search) { mRepository.insertAllSearch(search); }
    public void deleteAllSearch() { mRepository.deleteAllSearch(); }

    //media
    public void insertMedia(Media media) { mRepository.insertMedia(media); }
    public void insertAllMedia(List<Media> media) { mRepository.insertAllMedia(media); }
    public void deleteAllMedia(String media_type) { mRepository.deleteAllMediaByType(media_type); }

    //bookmarks
    public void bookmarkMedia(Bookmarks bookmarks) { mRepository.bookmarkMedia(bookmarks); }
    public void removeMediaFromBookmarks(long id) { mRepository.removeMediaFromBookmarks(id); }
    public void deleteAllBookmarks() { mRepository.deleteAllBookmarks(); }

    //playlist
    public void createPlaylist(Playlist playlist) {
        mRepository.createPlaylist(playlist);
    }

    public void deletePlaylist(long id) {
        mRepository.deletePlaylist(id);
    }

    //playlistMedia
    public void addMediaToPlaylist(PlaylistMedias media){
        mRepository.addMediaToPlaylist(media);
    }

    public void deletePlaylistMedia(long media_id, long playlist_id){
        mRepository.deletePlaylistMedia(media_id,playlist_id);
    }

    public void deleteAllPlaylistMedia(long playlist_id){
        mRepository.deleteAllPlaylistMedia(playlist_id);
    }

    //current downloads
    public void addCurrentDownload(Download download) {
        mRepository.addCurrentDownload(download);
    }

    public void updateCurrentDownload(Download download) {
        mRepository.updateCurrentDownload(download);
    }

    public void deleteCurrentDownload(long id) {
        mRepository.deleteCurrentDownload(id);
    }

    public void clearDownloads() {
        mRepository.clearDownloads();
    }

    //playing videos
    public void insertPlayingVideos(List<Playing_Videos> playing_videos) {
        mRepository.insertPlayingVideos(playing_videos);
    }

    public void updatePlayingVideos(Playing_Videos playing_videos) {
        mRepository.updatePlayingVideos(playing_videos);
    }


    public void clearPlayingVideos() {
        mRepository.clearPlayingVideos();
    }

    //playing audios
    public void insertPlayingAudios(List<Playing_Audios> playing_audios) {
        mRepository.insertPlayingAudios(playing_audios);
    }

    public void updatePlayingAudios(Playing_Audios playing_audios) {
        mRepository.updatePlayingAudios(playing_audios);
    }

    public void clearPlayingAudios() {
        mRepository.clearPlayingAudios();
    }

    public void insertRadio(Radio radio) { mRepository.insertRadio(radio); }
    public void deleteAllRadio() { mRepository.deleteRadio(); }

    //livestreams
    public void insertLivestream(Livestreams livestreams) { mRepository.insertLiveStream(livestreams); }
    public void insertAllLiveStreams(List<Livestreams> livestreams) { mRepository.insertAllLiveStreams(livestreams); }
    public void deleteAllLiveStreams() { mRepository.deleteAllLiveStreams(); }

    //inbox
    public void insertInbox(Inbox inbox) { mRepository.insertInbox(inbox); }
    public void insertAllInbox(List<Inbox> inbox) { mRepository.insertAllInbox(inbox); }
    public void deleteAllInbox() { mRepository.deleteAllInbox(); }


    //bible
    public void insertBible(Bible bible) {
        mRepository.insertBible(bible);
    }

    public void updateAMP(String content, String book, int chapter, int verse) {
        mRepository.updateAMP(content, book, chapter, verse);
    }

    public void updateKJV(String content, String book, int chapter, int verse) {
        mRepository.updateKJV(content, book, chapter, verse);
    }

    public void updateMSG(String content, String book, int chapter, int verse) {
        mRepository.updateMSG(content, book, chapter, verse);
    }

    public void updateNIV(String content, String book, int chapter, int verse) {
        mRepository.updateNIV(content, book, chapter, verse);
    }

    public void updateNKJV(String content, String book, int chapter, int verse) {
        mRepository.updateNKJV(content, book, chapter, verse);
    }

    public void updateNLT(String content, String book, int chapter, int verse) {
        mRepository.updateNLT(content, book, chapter, verse);
    }

    public void updateNRSV(String content, String book, int chapter, int verse) {
        mRepository.updateNRSV(content, book, chapter, verse);
    }

    //bookmarks
    public void saveNote(Note note) { mRepository.saveNote(note); }
    public void deleteNote(long id) { mRepository.deleteNote(id); }
    public void deleteAllNotes() { mRepository.deleteAllNotes(); }

    //hymns
    public void bookmarkHymns(Hymns hymns) { mRepository.bookmarkHymn(hymns); }
    public void deleteBookmarkedHymn(long id) { mRepository.deleteBookmarkedHymn(id); }

  /*  public void insertAllSlider(List<Slider> sliderList) { mRepository.insertAllSliderMedia(sliderList); }
    public void deleteAllSliderMedia() { mRepository.deleteAllSliderMedia(); }*/


}