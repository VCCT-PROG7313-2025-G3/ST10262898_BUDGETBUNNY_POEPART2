package com.fake.st10262898_budgetbunny_poepart2

class UserRepository (private val userDao: UserDao)
{


    suspend fun insert(user: User) {
        userDao.insert(user)
    }

    suspend fun getUser(username: String, password: String): User? {
        return userDao.getUser(username, password)
    }

}