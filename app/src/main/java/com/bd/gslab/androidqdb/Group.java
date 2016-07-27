package com.bd.gslab.androidqdb;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 7/27/16.
 */
public class Group {

    public String string;
    public final List<String> children = new ArrayList<String>();

    public Group(String string) {
        this.string = string;
    }

}

