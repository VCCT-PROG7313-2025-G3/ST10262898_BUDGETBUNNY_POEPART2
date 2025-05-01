package com.fake.st10262898_budgetbunny_poepart2.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class, Expense::class, Budget::class ], version = 4, exportSchema = false )
abstract class BudgetBunnyDatabase : RoomDatabase()
{

    abstract fun userDao(): UserDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao

    companion object{
        @Volatile
        private var INSTANCE: BudgetBunnyDatabase? = null

        fun getDatabase(context: Context): BudgetBunnyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BudgetBunnyDatabase::class.java,
                    "user_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    INSTANCE = instance
                    instance
            }
        }
    }


}