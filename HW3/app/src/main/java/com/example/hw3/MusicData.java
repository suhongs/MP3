package com.example.hw3;

import java.io.Serializable;

public class MusicData implements Serializable {
    private String id;
    private String albumId;
    private String title;
    private String duration;
    private String path;
    private String albumArt;

    public MusicData() {
    }

    public MusicData(String id, String albumId, String title, String duration, String path, String albumArt) {
        this.id = id;
        this.albumId = albumId;
        this.title = title;
        this.duration = duration;
        this.path = path;
        this.albumArt = albumArt;
    }

    public String getAlbumArt()
    {
        return albumArt;
    }

    public void setAlbumArt(String albumArt)
    {
        this.albumArt = albumArt;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath(){
        return path;
    }
    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "MusicDto{" +
                "id='" + id + '\'' +
                ", albumId='" + albumId + '\'' +
                ", title='" + title + '\'' +
                ", duration='" + duration + '\'' +
                ", path='" + path + '\'' +
                ", albumArt='"+albumArt+'\'';
    }
}