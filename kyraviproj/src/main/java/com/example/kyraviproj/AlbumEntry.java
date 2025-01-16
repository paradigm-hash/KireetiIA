package com.example.kyraviproj;

public class AlbumEntry {
    public final String name;
    public final String artists;
    public int count;

    public AlbumEntry(String n, String a) {
        this.name = n;
        this.artists = a;
        this.count = 1;
    }
    public void incrementCount() {
        this.count++;
    }
}
