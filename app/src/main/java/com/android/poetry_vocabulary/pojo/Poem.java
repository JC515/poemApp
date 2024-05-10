package com.android.poetry_vocabulary.pojo;

public class Poem {
    private long poemId;//诗词id,主键,自增长,数据库中不用设置
    private String poemName;//诗词名
    private String writerName;//作者名
    private String content;//诗词内容
    private String dynasty;//朝代
    private String explanation;//注释


    public Poem() {}

    public Poem(String poemName, String writerName, String content, String dynasty, String explanation) {
        poemId = -1;//默认值
        this.poemName = poemName;
        this.writerName = writerName;
        this.content = content;
        this.dynasty = dynasty;
        this.explanation = explanation;
    }

    public Long getPoemId() {
        return poemId;
    }

    public String getPoemName() {
        return poemName;
    }

    public String getWriterName() {
        return writerName;
    }

    public String getContent() {
        return content;
    }

    public String getDynasty() {
        return dynasty;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setPoemId(long poemId) {
        this.poemId = poemId;
    }

    public void setPoemName(String poemName) {
        this.poemName = poemName;
    }

    public void setWriterName(String writerName) {
        this.writerName = writerName;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setDynasty(String dynasty) {
        this.dynasty = dynasty;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

}