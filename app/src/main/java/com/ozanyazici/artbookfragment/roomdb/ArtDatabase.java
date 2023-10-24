package com.ozanyazici.artbookfragment.roomdb;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.ozanyazici.artbookfragment.model.Art;

@Database(entities = {Art.class},version = 1)
public abstract class ArtDatabase extends RoomDatabase {
    public abstract ArtDao artDao();
}
