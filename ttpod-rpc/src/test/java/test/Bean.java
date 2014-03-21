package test;

public class Bean{
    String id = "id" ;
    String desc = "desc" ;
    String song_ids = "song_ids" ;
    String index_field = "index_field" ;
    String publish_time = "publish_time" ;
    String song_total = "song_total" ;
    String name = "name" ;
    String singer_name = "singer_name" ;
    String lang = "lang" ;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getSong_ids() {
        return song_ids;
    }

    public void setSong_ids(String song_ids) {
        this.song_ids = song_ids;
    }

    public String getIndex_field() {
        return index_field;
    }

    public void setIndex_field(String index_field) {
        this.index_field = index_field;
    }

    public String getPublish_time() {
        return publish_time;
    }

    public void setPublish_time(String publish_time) {
        this.publish_time = publish_time;
    }

    public String getSong_total() {
        return song_total;
    }

    public void setSong_total(String song_total) {
        this.song_total = song_total;
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

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    @Override
    public String toString() {
        return "Bean{" +
                "id='" + id + '\'' +
                ", desc='" + desc + '\'' +
                ", song_ids='" + song_ids + '\'' +
                ", index_field='" + index_field + '\'' +
                ", publish_time='" + publish_time + '\'' +
                ", song_total='" + song_total + '\'' +
                ", name='" + name + '\'' +
                ", singer_name='" + singer_name + '\'' +
                ", lang='" + lang + '\'' +
                '}';
    }

    public static void main(String[] args) {
        int j = -2323;
        int UNSIGN_SHORT_OVER_FLOW = 0xFFFF;
        for(int i = 0;i<10;++i)
        System.out.println(((i+j)&UNSIGN_SHORT_OVER_FLOW )%5);
    }
}