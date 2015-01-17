package chen.pitt.crazyguessmusic.data;

import android.os.Environment;

import java.io.File;

public final class Const {

    public static final int TOTAL_COINS = 1000;
    public static final int PASS_COIN = 50;

    public static final String FILE_NAME_SAVE_DATA = "data.dat";
    public static final int INDEX_LOAD_DATA_STAGE = 0;
    public static final int INDEX_LOAD_DATA_COINS = 1;
    public static final int INDEX_LOAD_DATA_SONGNAME = 2;
    public static final int INDEX_LOAD_DATA_SONGPATH = 3;


    public static final File SD_CARD_DIR = Environment.getExternalStorageDirectory();
    public static final String[] EXT_NAMES = new String[] {".mp3", ".m4a", ".mp4"};

    public static final String APP_ID = "wx5b0aa80c64305aad";

}
