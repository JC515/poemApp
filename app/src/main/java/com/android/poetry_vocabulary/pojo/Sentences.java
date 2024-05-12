package com.android.poetry_vocabulary.pojo;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "sentences")
public class Sentences {
    @ElementList(inline = true)
    private List<Sentence> sentenceList;

    public List<Sentence> getContentList() {
        return sentenceList;
    }

}
