package com.android.poetry_vocabulary.pojo;

public class Poem {
    private long poemId;
    private String poemName;
    private String writerName;
    private String content;
    private String dynasty;
    private String explanation;

    public Poem(long poemId, String poemName, String writerName, String content, String dynasty, String explanation) {
        this.poemId = poemId;
        this.poemName = poemName;
        this.writerName = writerName;
        this.content = content;
        this.dynasty = dynasty;
        this.explanation = explanation;
    }

    public long getPoemId() {
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
}