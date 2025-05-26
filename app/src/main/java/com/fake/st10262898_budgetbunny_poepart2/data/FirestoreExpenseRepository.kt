package com.fake.st10262898_budgetbunny_poepart2.data

import com.google.firebase.firestore.FirebaseFirestore

class FirestoreExpenseRepository {

    private val db = FirebaseFirestore.getInstance()

    fun addExpense(expense: ExpenseFirebase, onResult: (Boolean) -> Unit) {
        db.collection("expenses")
            .add(expense)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getExpenses(username: String, onResult: (List<ExpenseFirebase>) -> Unit) {
        db.collection("expenses")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { result ->
                val expenses = result.documents.map { doc ->
                    val expense = doc.toObject(ExpenseFirebase::class.java)!!
                    expense.id = doc.id  // Set Firestore document ID here
                    expense
                }
                onResult(expenses)
            }
    }

    fun deleteExpense(expenseId: String, onResult: (Boolean) -> Unit) {
        db.collection("expenses").document(expenseId)
            .delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getExpensesBetweenDates(username: String, startDate: Long, endDate: Long, onResult: (List<ExpenseFirebase>) -> Unit) {
        db.collection("expenses")
            .whereEqualTo("username", username)
            .whereGreaterThanOrEqualTo("expenseDate", startDate)
            .whereLessThanOrEqualTo("expenseDate", endDate)
            .get()
            .addOnSuccessListener { result ->
                val expenses = result.documents.map { doc ->
                    val expense = doc.toObject(ExpenseFirebase::class.java)!!
                    expense.id = doc.id  // Set Firestore document ID here
                    expense
                }
                onResult(expenses)
            }
    }
}
