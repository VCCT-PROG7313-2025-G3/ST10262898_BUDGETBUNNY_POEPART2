package com.fake.st10262898_budgetbunny_poepart2

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class], version = 1, exportSchema = false )
abstract class BudgetBunnyDatabase : RoomDatabase()
{

    abstract fun userDao(): UserDao

    companion object{
        @Volatile
        private var INSTANCE: BudgetBunnyDatabase? = null

        fun getDatabase(context: Context): BudgetBunnyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BudgetBunnyDatabase::class.java,
                    "user_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }


}