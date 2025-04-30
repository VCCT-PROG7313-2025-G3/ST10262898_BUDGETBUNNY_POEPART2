package com.fake.st10262898_budgetbunny_poepart2.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao //Therefore this is the class where we defined the methods that will be used to access the database
interface UserDao
{

    @Insert //This will be the method to insert new users into the database
    suspend fun insert(user: User)

    @Query
        ("Select * FROM user_table WHERE username = :username AND password = :password LIMIT 1" )
        suspend fun getUser(username: String, password: String): User?


}