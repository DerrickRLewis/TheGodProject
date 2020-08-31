package apps.envision.mychurch.pojo;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bible_table")
public class Bible {

    @PrimaryKey
    @ColumnInfo(name = "id")
    private long id;

    @NonNull
    @ColumnInfo(name = "book")
    private String book = "";

    @NonNull
    @ColumnInfo(name = "chapter")
    private int chapter = 0;

    @NonNull
    @ColumnInfo(name = "verse")
    private int verse = 0;

    @NonNull
    @ColumnInfo(name = "AMP")
    private String AMP = "";

    @NonNull
    @ColumnInfo(name = "KJV")
    private String KJV = "";

    @NonNull
    @ColumnInfo(name = "MSG")
    private String MSG = "";

    @NonNull
    @ColumnInfo(name = "NIV")
    private String NIV = "";

    @NonNull
    @ColumnInfo(name = "NKJV")
    private String NKJV = "";

    @NonNull
    @ColumnInfo(name = "NLT")
    private String NLT = "";

    @NonNull
    @ColumnInfo(name = "NRSV")
    private String NRSV = "";



    public Bible() {

    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getBook() {
        return book;
    }

    public void setBook(@NonNull String book) {
        this.book = book;
    }

    @NonNull
    public String getAMP() {
        return AMP;
    }

    public void setAMP(@NonNull String AMP) {
        this.AMP = AMP;
    }

    @NonNull
    public String getKJV() {
        return KJV;
    }

    public void setKJV(@NonNull String KJV) {
        this.KJV = KJV;
    }

    @NonNull
    public String getMSG() {
        return MSG;
    }

    public void setMSG(@NonNull String MSG) {
        this.MSG = MSG;
    }

    @NonNull
    public String getNIV() {
        return NIV;
    }

    public void setNIV(@NonNull String NIV) {
        this.NIV = NIV;
    }

    @NonNull
    public String getNKJV() {
        return NKJV;
    }

    public void setNKJV(@NonNull String NKJV) {
        this.NKJV = NKJV;
    }

    @NonNull
    public String getNLT() {
        return NLT;
    }

    public void setNLT(@NonNull String NLT) {
        this.NLT = NLT;
    }

    @NonNull
    public String getNRSV() {
        return NRSV;
    }

    public void setNRSV(@NonNull String NRSV) {
        this.NRSV = NRSV;
    }

    public int getChapter() {
        return chapter;
    }

    public void setChapter(int chapter) {
        this.chapter = chapter;
    }

    public int getVerse() {
        return verse;
    }

    public void setVerse(int verse) {
        this.verse = verse;
    }
}
