package apps.envision.mychurch.db;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.content.Context;

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

import static apps.envision.mychurch.utils.Constants.APP_DATABASE;

/**
 * Created by link on 08/09/2018.
 */

@Database(entities = {Note.class, Bible.class, Devotionals.class, Events.class, Livestreams.class, Radio.class, Categories.class, Media.class,
        Search.class, Bookmarks.class, Playlist.class, PlaylistMedias.class, Download.class, Hymns.class,
        Playing_Videos.class, Playing_Audios.class, Inbox.class}, version = 3, exportSchema = false)
public abstract class AppDb extends RoomDatabase {

    public abstract DataInterfaceDao dataDbInterface();

    private static AppDb INSTANCE;


    public static AppDb getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDb.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDb.class, APP_DATABASE)
                            .addMigrations(MIGRATIONV2)
                            .addMigrations(MIGRATIONV3)
                            //.addMigrations(MIGRATIONV4)
                            //.fallbackToDestructiveMigration()
                            .build();

                }
            }
        }
        return INSTANCE;
    }

    private static final Migration MIGRATIONV2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE devotionals_table "
                    + " ADD COLUMN thumbnail VARCHAR NOT NULL DEFAULT 'empty' ");
           }
    };

    private static final Migration MIGRATIONV3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
              database.execSQL("CREATE TABLE IF NOT EXISTS `hymns_table` (`id` INTEGER NOT NULL, `thumbnail` TEXT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `time` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        }
    };

    private static final Migration MIGRATIONV4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `livestreams_table` (`id` INTEGER NOT NULL, `title` TEXT NOT NULL, `cover_photo` TEXT NOT NULL, `description` TEXT NOT NULL, `type` TEXT NOT NULL, `stream_url` TEXT NOT NULL, `is_free` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        }
    };

}
