package apps.envision.mychurch.interfaces;

import apps.envision.mychurch.pojo.BibleDownload;

public interface BibleDownloadStatusListener {
    void progress(BibleDownload downloads);
    void startDownload(BibleDownload downloads, int status);
    void downloadError(BibleDownload downloads);
}
