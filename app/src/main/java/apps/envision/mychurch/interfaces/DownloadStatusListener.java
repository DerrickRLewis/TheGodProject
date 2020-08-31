package apps.envision.mychurch.interfaces;

import apps.envision.mychurch.pojo.Download;

public interface DownloadStatusListener {
    void progress(Download downloads);
    void startDownload(Download downloads, int status);
    void downloadError(Download downloads);
}
