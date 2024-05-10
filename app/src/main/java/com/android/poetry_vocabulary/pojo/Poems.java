package com.android.poetry_vocabulary.pojo;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "poems")
public class Poems {
    @ElementList(inline = true)
    private List<Poem> poemList;

    public List<Poem> getPoemList() {
        return poemList;
    }
}