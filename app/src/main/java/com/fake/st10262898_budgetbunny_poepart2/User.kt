package com.fake.st10262898_budgetbunny_poepart2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User (

    @PrimaryKey(autoGenerate = true) //This will ensure the database auto-generates a unique ID for each user
    val id: Int = 0, //The primary key will start from 0 and it will be called id
    val username: String,
    val password: String,

    )