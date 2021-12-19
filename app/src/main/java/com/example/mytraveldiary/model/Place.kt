package com.example.mytraveldiary.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Place(
    @ColumnInfo(name ="name")
    var name: String,
    @ColumnInfo(name = "latitute")
    var latitute: Double,
    @ColumnInfo(name = "longitute")
    var longitute: Double
    ){

    @PrimaryKey(autoGenerate = true)
    var id = 0


}