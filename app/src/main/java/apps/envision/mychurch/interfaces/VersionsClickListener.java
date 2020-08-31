package apps.envision.mychurch.interfaces;

import java.util.UUID;

import apps.envision.mychurch.pojo.BibleVersions;
import apps.envision.mychurch.pojo.Events;

/**
 * Created by Ray on 6/25/2018.
 */

public interface VersionsClickListener {
    void downloadVersion(BibleVersions bibleVersions);
    void cancelDownload(UUID uuid, String book);
}
