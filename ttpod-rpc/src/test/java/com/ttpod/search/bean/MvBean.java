package com.ttpod.search.bean;

public class MvBean extends StringIntegerOnly {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5661332931579909771L;

	private Integer _id; //
	private Integer pick_count; // 红心数
    private Integer song_id; //对应歌曲id

	private String name; // 歌曲名
	private String singer_name; //歌手名
	private String down_list; // 歌曲下载列表


    public Integer get_id() {
        return _id;
    }

    public void set_id(Integer _id) {
        this._id = _id;
    }

    public Integer getPick_count() {
        return pick_count;
    }

    public void setPick_count(Integer pick_count) {
        this.pick_count = pick_count;
    }

    public Integer getSong_id() {
        return song_id;
    }

    public void setSong_id(Integer song_id) {
        this.song_id = song_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSinger_name() {
        return singer_name;
    }

    public void setSinger_name(String singer_name) {
        this.singer_name = singer_name;
    }

    public String getDown_list() {
        return down_list;
    }

    public void setDown_list(String down_list) {
        this.down_list = down_list;
    }

    @Override
    public String toString() {
        return "MvBean{" +
                "_id=" + _id +
                ", pick_count=" + pick_count +
                ", song_id=" + song_id +
                ", name='" + name + '\'' +
                ", singer_name='" + singer_name + '\'' +
                ", down_list='" + down_list + '\'' +
                '}';
    }
}
