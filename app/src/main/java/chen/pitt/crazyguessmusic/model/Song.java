package chen.pitt.crazyguessmusic.model;

public class Song {
    private String mSongName; // 歌曲名称
    private String mSongFileName; // 歌曲文件名称
    private int mNameLength; // 歌曲名称长度

    public String getSongName() {
        return mSongName;
    }

    public void setSongName(String songName) {
        this.mSongName = songName;
        this.mNameLength = songName.length();
    }

    public String getSongFileName() {
        return mSongFileName;
    }

    public void setSongFileName(String songFileName) {
        this.mSongFileName = songFileName;
    }

    public int getNameLength() {
        return mNameLength;
    }


    public char[] getNameChars() {
        return mSongName.toCharArray();
    }

}
