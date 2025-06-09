package com.fake.st10262898_budgetbunny_poepart2.data

import com.google.firebase.firestore.Exclude

data class ExpenseFirebase(
    var expenseName: String = "",
    var expenseAmount: Double = 0.0,
    var username: String = "",
    var expenseCategory: String? = null,
    var expenseDate: Long = 0L,
    var expenseImageBase64: String? = null
) {
    @Exclude
    var id: String = "" //

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "expenseName" to expenseName,
            "expenseAmount" to expenseAmount,
            "username" to username,
            "expenseCategory" to expenseCategory,
            "expenseDate" to expenseDate,
            "expenseImageBase64" to expenseImageBase64
        )
    }

    companion object {
        fun fromDocument(documentId: String, data: Map<String, Any>): ExpenseFirebase {
            return ExpenseFirebase(
                expenseName = data["expenseName"] as? String ?: "",
                expenseAmount = data["expenseAmount"] as? Double ?: 0.0,
                username = data["username"] as? String ?: "",
                expenseCategory = data["expenseCategory"] as? String,
                expenseDate = data["expenseDate"] as? Long ?: 0L,
                expenseImageBase64 = data["expenseImageBase64"] as? String
            ).apply {
                id = documentId
            }
        }
    }
}