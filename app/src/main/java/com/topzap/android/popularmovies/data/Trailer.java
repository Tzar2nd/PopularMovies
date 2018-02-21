package com.topzap.android.popularmovies.data;

public class Trailer {

    private String name;
    private String size;
    private String source;
    private String type;

    public Trailer(String name, String size, String source, String type) {
        this.name = name;
        this.size = size;
        this.source = source;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getSize() {
        return size;
    }

    public String getSource() {
        return source;
    }

    public String getType() {
        return type;
    }
}
