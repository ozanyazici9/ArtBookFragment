package com.ozanyazici.artbookfragment.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Art {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "artistname")
    public String artistName;

    @ColumnInfo (name = "year")
    public String year;

    @ColumnInfo(name = "image")
    public byte[] image;

    public Art(String name,String artistName,String year,byte[] image) {
        this.name = name;
        this.artistName = artistName;
        this.year = year;
        this.image = image;
    }
}
