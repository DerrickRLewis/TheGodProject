package apps.envision.mychurch.utils;

import java.util.ArrayList;
import java.util.List;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.pojo.Bookmarks;
import apps.envision.mychurch.pojo.Download;
import apps.envision.mychurch.pojo.Media;
import apps.envision.mychurch.pojo.Playing_Audios;
import apps.envision.mychurch.pojo.Playing_Videos;
import apps.envision.mychurch.pojo.Playlist;
import apps.envision.mychurch.pojo.PlaylistMedias;
import apps.envision.mychurch.pojo.Radio;
import apps.envision.mychurch.pojo.Search;
import apps.envision.mychurch.pojo.Slider;

/**
 * Created by ray on 07/02/2018.
 * utility class to map pojo class to another pojo class
 */

public class ObjectMapper {


    public static List<Media> mapSearchMedia(List<Search> search) {
        List<Media> mediaList = new ArrayList<>();
        for (Search obj: search) {
            Media media = new Media();
            media.setId(obj.getId());
            media.setCategory(obj.getCategory());
            media.setTitle(obj.getTitle());
            media.setCover_photo(obj.getCover_photo());
            media.setDescription(obj.getDescription());
            media.setStream_url(obj.getStream_url());
            media.setDownload_url(obj.getDownload_url());
            media.setDuration(obj.getDuration());
            media.setCan_preview(obj.isCan_preview());
            media.setCan_download(obj.isCan_download());
            media.setPreview_duration(obj.getPreview_duration());
            media.setMedia_type(obj.getMedia_type());
            media.setVideo_type(obj.getVideo_type());
            media.setComments_count(obj.getComments_count());
            media.setLikes_count(obj.getLikes_count());
            media.setUserLiked(obj.isUserLiked());
            media.setViews_count(obj.getViews_count());
            media.setHttp(obj.isHttp());
            media.setIs_free(obj.isIs_free());
            mediaList.add(media);
        }
        return mediaList;
    }

    public static List<Search> mapSearchList(List<Media> mediaList) {
        List<Search> searchList = new ArrayList<>();
        for (Media obj: mediaList) {
            Search search = new Search();
            search.setId(obj.getId());
            search.setCategory(obj.getCategory());
            search.setTitle(obj.getTitle());
            search.setCover_photo(obj.getCover_photo());
            search.setDescription(obj.getDescription());
            search.setStream_url(obj.getStream_url());
            search.setDownload_url(obj.getDownload_url());
            search.setDuration(obj.getDuration());
            search.setCan_preview(obj.isCan_preview());
            search.setCan_download(obj.isCan_download());
            search.setPreview_duration(obj.getPreview_duration());
            search.setMedia_type(obj.getMedia_type());
            search.setVideo_type(obj.getVideo_type());
            search.setComments_count(obj.getComments_count());
            search.setLikes_count(obj.getLikes_count());
            search.setUserLiked(obj.isUserLiked());
            search.setViews_count(obj.getViews_count());
            search.setHttp(obj.isHttp());
            search.setIs_free(obj.isIs_free());
            searchList.add(search);
        }
        return searchList;
    }

    public static Bookmarks mapBoomarkFromMedia(Media media) {
        Bookmarks bookmarks = new Bookmarks();
        bookmarks.setId(media.getId());
        bookmarks.setCategory(media.getCategory());
        bookmarks.setTitle(media.getTitle());
        bookmarks.setCover_photo(media.getCover_photo());
        bookmarks.setDescription(media.getDescription());
        bookmarks.setStream_url(media.getStream_url());
        bookmarks.setDownload_url(media.getDownload_url());
        bookmarks.setDuration(media.getDuration());
        bookmarks.setCan_preview(media.isCan_preview());
        bookmarks.setCan_download(media.isCan_download());
        bookmarks.setPreview_duration(media.getPreview_duration());
        bookmarks.setMedia_type(media.getMedia_type());
        bookmarks.setVideo_type(media.getVideo_type());
        bookmarks.setComments_count(media.getComments_count());
        bookmarks.setLikes_count(media.getLikes_count());
        bookmarks.setViews_count(media.getViews_count());
        bookmarks.setUserLiked(media.isUserLiked());
        bookmarks.setHttp(media.isHttp());
        bookmarks.setIs_free(media.isIs_free());
        return bookmarks;

    }

    public static List<Media> mapBoomarkedMedia(List<Bookmarks> pins) {
        List<Media> mediaList = new ArrayList<>();
        for (Bookmarks obj: pins) {
            Media media = new Media();
            media.setId(obj.getId());
            media.setCategory(obj.getCategory());
            media.setTitle(obj.getTitle());
            media.setCover_photo(obj.getCover_photo());
            media.setDescription(obj.getDescription());
            media.setStream_url(obj.getStream_url());
            media.setDownload_url(obj.getDownload_url());
            media.setDuration(obj.getDuration());
            media.setCan_preview(obj.isCan_preview());
            media.setCan_download(obj.isCan_download());
            media.setPreview_duration(obj.getPreview_duration());
            media.setMedia_type(obj.getMedia_type());
            media.setVideo_type(obj.getVideo_type());
            media.setComments_count(obj.getComments_count());
            media.setLikes_count(obj.getLikes_count());
            media.setUserLiked(obj.isUserLiked());
            media.setViews_count(obj.getViews_count());
            media.setHttp(obj.isHttp());
            media.setIs_free(obj.isIs_free());
            mediaList.add(media);
        }
        return mediaList;
    }

    public static List<Media> mapMediaFromPlaylists(List<PlaylistMedias> playlistMedias) {
        List<Media> mediaList = new ArrayList<>();
        for (PlaylistMedias obj: playlistMedias) {
            Media media = new Media();
            media.setId(obj.getMedia_id());
            media.setCategory(obj.getCategory());
            media.setTitle(obj.getTitle());
            media.setCover_photo(obj.getCover_photo());
            media.setDescription(obj.getDescription());
            media.setStream_url(obj.getStream_url());
            media.setDownload_url(obj.getDownload_url());
            media.setDuration(obj.getDuration());
            media.setCan_preview(obj.isCan_preview());
            media.setCan_download(obj.isCan_download());
            media.setIs_free(obj.isIs_free());
            media.setPreview_duration(obj.getPreview_duration());
            media.setMedia_type(obj.getMedia_type());
            media.setVideo_type(obj.getVideo_type());
            media.setComments_count(obj.getComments_count());
            media.setLikes_count(obj.getLikes_count());
            media.setViews_count(obj.getViews_count());
            media.setUserLiked(obj.isUserLiked());
            media.setHttp(obj.isHttp());
            mediaList.add(media);
        }
        return mediaList;
    }


    public static PlaylistMedias mapMediaToPlaylist(Playlist playlist, Media media){
        PlaylistMedias playlistMedias = new PlaylistMedias();
        playlistMedias.setPlaylist_id(playlist.getId());
        playlistMedias.setMedia_id(media.getId());
        playlistMedias.setCategory(media.getCategory());
        playlistMedias.setTitle(media.getTitle());
        playlistMedias.setCover_photo(media.getCover_photo());
        playlistMedias.setDescription(media.getDescription());
        playlistMedias.setStream_url(media.getStream_url());
        playlistMedias.setDownload_url(media.getDownload_url());
        playlistMedias.setDuration(media.getDuration());
        playlistMedias.setCan_preview(media.isCan_preview());
        playlistMedias.setCan_download(media.isCan_download());
        playlistMedias.setIs_free(media.isIs_free());
        playlistMedias.setPreview_duration(media.getPreview_duration());
        playlistMedias.setMedia_type(media.getMedia_type());
        playlistMedias.setVideo_type(media.getVideo_type());
        playlistMedias.setComments_count(media.getComments_count());
        playlistMedias.setLikes_count(media.getLikes_count());
        playlistMedias.setViews_count(media.getViews_count());
        playlistMedias.setUserLiked(media.isUserLiked());
        playlistMedias.setHttp(media.isHttp());
        return playlistMedias;
    }

    public static PlaylistMedias mapPlaylistMedia(PlaylistMedias playlist, Media media){
        PlaylistMedias playlistMedias = new PlaylistMedias();
        playlistMedias.setId(playlist.getId());
        playlistMedias.setCategory(playlist.getCategory());
        playlistMedias.setPlaylist_id(playlist.getPlaylist_id());
        playlistMedias.setMedia_id(media.getId());
        playlistMedias.setTitle(media.getTitle());
        playlistMedias.setCover_photo(media.getCover_photo());
        playlistMedias.setDescription(media.getDescription());
        playlistMedias.setStream_url(media.getStream_url());
        playlistMedias.setDownload_url(media.getDownload_url());
        playlistMedias.setDuration(media.getDuration());
        playlistMedias.setCan_preview(media.isCan_preview());
        playlistMedias.setCan_download(media.isCan_download());
        playlistMedias.setIs_free(media.isIs_free());
        playlistMedias.setPreview_duration(media.getPreview_duration());
        playlistMedias.setMedia_type(media.getMedia_type());
        playlistMedias.setVideo_type(media.getVideo_type());
        playlistMedias.setComments_count(media.getComments_count());
        playlistMedias.setLikes_count(media.getLikes_count());
        playlistMedias.setViews_count(media.getViews_count());
        playlistMedias.setUserLiked(media.isUserLiked());
        playlistMedias.setHttp(media.isHttp());
        return playlistMedias;
    }

    public static Download mapCurrentDownloadFromMedia(Media media) {
        Download downloads = new Download();
        downloads.setId(media.getId());
        String ext = media.getMedia_type().equalsIgnoreCase(App.getContext().getString(R.string.audio))?".mp3":".mp4";
        if(media.getStream_url().contains(".")) {
            ext = media.getStream_url().substring(media.getStream_url().lastIndexOf("."));
        }
        String filename = media.getTitle() + ext;
        downloads.setTitle(media.getTitle());
        downloads.setCover_photo(media.getCover_photo());
        downloads.setMedia_type(media.getMedia_type());
        downloads.setDownload_path(media.getStream_url());
        downloads.setFile_path(String.valueOf(FileManager.getDestinationFolder(downloads.getMedia_type())));
        downloads.setTemp_dir(String.valueOf(FileManager.getTempFileStorage(downloads)));
        return downloads;
    }

    public static List<Playing_Videos> mapPlayingVideos(List<Media> mediaList) {
        List<Playing_Videos> playing_videos = new ArrayList<>();
        int count = 0;
        for (Media obj: mediaList) {
            if(obj.getMedia_type().equalsIgnoreCase(App.getContext().getString(R.string.video))) {
                Playing_Videos media = new Playing_Videos();
                media.setId(obj.getId());
                media.setCategory(obj.getCategory());
                media.setTitle(obj.getTitle());
                media.setCover_photo(obj.getCover_photo());
                media.setDescription(obj.getDescription());
                media.setStream_url(obj.getStream_url());
                media.setDownload_url(obj.getDownload_url());
                media.setDuration(obj.getDuration());
                media.setCan_preview(obj.isCan_preview());
                media.setCan_download(obj.isCan_download());
                media.setIs_free(obj.isIs_free());
                media.setPreview_duration(obj.getPreview_duration());
                media.setMedia_type(obj.getMedia_type());
                media.setVideo_type(obj.getVideo_type());
                media.setComments_count(obj.getComments_count());
                media.setLikes_count(obj.getLikes_count());
                media.setUserLiked(obj.isUserLiked());
                media.setViews_count(obj.getViews_count());
                media.setHttp(obj.isHttp());
                playing_videos.add(media);
                count++;
            }
            if(count>30){break;}
        }
        return playing_videos;
    }

    public static List<Playing_Audios> mapPlayingAudios(List<Media> mediaList) {
        List<Playing_Audios> playing_audios = new ArrayList<>();
        int count = 0;
        for (Media obj: mediaList) {
            if(obj.getMedia_type().equalsIgnoreCase(App.getContext().getString(R.string.audio))) {
                Playing_Audios media = new Playing_Audios();
                media.setId(obj.getId());
                media.setCategory(obj.getCategory());
                media.setTitle(obj.getTitle());
                media.setCover_photo(obj.getCover_photo());
                media.setDescription(obj.getDescription());
                media.setStream_url(obj.getStream_url());
                media.setDownload_url(obj.getDownload_url());
                media.setDuration(obj.getDuration());
                media.setCan_preview(obj.isCan_preview());
                media.setCan_download(obj.isCan_download());
                media.setIs_free(obj.isIs_free());
                media.setPreview_duration(obj.getPreview_duration());
                media.setMedia_type(obj.getMedia_type());
                media.setVideo_type(obj.getVideo_type());
                media.setComments_count(obj.getComments_count());
                media.setLikes_count(obj.getLikes_count());
                media.setViews_count(obj.getViews_count());
                media.setUserLiked(obj.isUserLiked());
                media.setHttp(obj.isHttp());
                playing_audios.add(media);
                count++;
            }
            if(count>30){break;}
        }
        return playing_audios;
    }

    public static List<Media> mapMediaFromPlayingVideosList(List<Playing_Videos> playing_videos) {
        List<Media> mediaList = new ArrayList<>();
        for (Playing_Videos obj: playing_videos) {
            Media media = new Media();
            media.setId(obj.getId());
            media.setCategory(obj.getCategory());
            media.setTitle(obj.getTitle());
            media.setCover_photo(obj.getCover_photo());
            media.setDescription(obj.getDescription());
            media.setStream_url(obj.getStream_url());
            media.setDownload_url(obj.getDownload_url());
            media.setDuration(obj.getDuration());
            media.setCan_preview(obj.isCan_preview());
            media.setCan_download(obj.isCan_download());
            media.setIs_free(obj.isIs_free());
            media.setPreview_duration(obj.getPreview_duration());
            media.setMedia_type(obj.getMedia_type());
            media.setVideo_type(obj.getVideo_type());
            media.setComments_count(obj.getComments_count());
            media.setLikes_count(obj.getLikes_count());
            media.setViews_count(obj.getViews_count());
            media.setUserLiked(obj.isUserLiked());
            media.setHttp(obj.isHttp());
            mediaList.add(media);
        }
        return mediaList;
    }

    public static List<Media> mapMediaFromPlayingAudiosList(List<Playing_Audios> playing_audios) {
        List<Media> mediaList = new ArrayList<>();
        for (Playing_Audios obj: playing_audios) {
            Media media = new Media();
            media.setId(obj.getId());
            media.setCategory(obj.getCategory());
            media.setTitle(obj.getTitle());
            media.setCover_photo(obj.getCover_photo());
            media.setDescription(obj.getDescription());
            media.setStream_url(obj.getStream_url());
            media.setDownload_url(obj.getDownload_url());
            media.setDuration(obj.getDuration());
            media.setCan_preview(obj.isCan_preview());
            media.setCan_download(obj.isCan_download());
            media.setIs_free(obj.isIs_free());
            media.setPreview_duration(obj.getPreview_duration());
            media.setMedia_type(obj.getMedia_type());
            media.setVideo_type(obj.getVideo_type());
            media.setComments_count(obj.getComments_count());
            media.setLikes_count(obj.getLikes_count());
            media.setViews_count(obj.getViews_count());
            media.setUserLiked(obj.isUserLiked());
            media.setHttp(obj.isHttp());
            mediaList.add(media);
        }
        return mediaList;
    }

    public static Media mapMediaFromRadio(Radio obj) {
        Media media = new Media();
        media.setId(obj.getId());
        media.setTitle(obj.getTitle());
        media.setCover_photo(obj.getCover_photo());
        media.setStream_url(obj.getStream_url());
        media.setCan_preview(true);
        media.setCan_download(false);
        media.setIs_free(true);
        return media;
    }

    public static List<Media> mapMediaFromSliderList(List<Slider> sliderList) {
        List<Media> mediaList = new ArrayList<>();
        for (Slider obj: sliderList) {
            Media media = new Media();
            media.setId(obj.getId());
            media.setCategory(obj.getCategory());
            media.setTitle(obj.getTitle());
            media.setCover_photo(obj.getCover_photo());
            media.setDescription(obj.getDescription());
            media.setStream_url(obj.getStream_url());
            media.setDownload_url(obj.getDownload_url());
            media.setDuration(obj.getDuration());
            media.setCan_preview(obj.isCan_preview());
            media.setCan_download(obj.isCan_download());
            media.setIs_free(obj.isIs_free());
            media.setPreview_duration(obj.getPreview_duration());
            media.setMedia_type(obj.getMedia_type());
            media.setVideo_type(obj.getVideo_type());
            media.setComments_count(obj.getComments_count());
            media.setLikes_count(obj.getLikes_count());
            media.setComments_count(obj.getComments_count());
            media.setLikes_count(obj.getLikes_count());
            media.setViews_count(obj.getViews_count());
            media.setUserLiked(obj.isUserLiked());
            media.setHttp(obj.isHttp());
            mediaList.add(media);
        }
        return mediaList;
    }

    public static List<Slider> mapSliderFromMediaList(List<Media> mediaList) {
        List<Slider> sliderList = new ArrayList<>();
        for (Media obj: mediaList) {
            Slider media = new Slider();
            media.setId(obj.getId());
            media.setCategory(obj.getCategory());
            media.setTitle(obj.getTitle());
            media.setCover_photo(obj.getCover_photo());
            media.setDescription(obj.getDescription());
            media.setStream_url(obj.getStream_url());
            media.setDownload_url(obj.getDownload_url());
            media.setDuration(obj.getDuration());
            media.setCan_preview(obj.isCan_preview());
            media.setCan_download(obj.isCan_download());
            media.setIs_free(obj.isIs_free());
            media.setPreview_duration(obj.getPreview_duration());
            media.setMedia_type(obj.getMedia_type());
            media.setVideo_type(obj.getVideo_type());
            media.setComments_count(obj.getComments_count());
            media.setLikes_count(obj.getLikes_count());
            media.setComments_count(obj.getComments_count());
            media.setLikes_count(obj.getLikes_count());
            media.setViews_count(obj.getViews_count());
            media.setUserLiked(obj.isUserLiked());
            media.setHttp(obj.isHttp());
            sliderList.add(media);
        }
        return sliderList;
    }
}
