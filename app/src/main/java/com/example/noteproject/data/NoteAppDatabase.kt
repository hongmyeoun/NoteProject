package com.example.noteproject.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Note::class], version = 1)
abstract class NoteAppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    companion object{
        private var INSTANCE: NoteAppDatabase? = null

        fun getDatabase(context: Context): NoteAppDatabase {
            return INSTANCE?: synchronized(this){
                val db = Room.databaseBuilder(
                    context,
                    NoteAppDatabase::class.java, "my.db"
                ).build()
                db
            }
        }
    }
}