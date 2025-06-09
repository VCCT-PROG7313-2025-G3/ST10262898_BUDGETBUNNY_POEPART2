package com.fake.st10262898_budgetbunny_poepart2.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//This is a data class to help the implementation of the chat bot used in the Home Page
data class ChatMessage(
    val message: String,
    val isBot: Boolean,
    val time: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
)