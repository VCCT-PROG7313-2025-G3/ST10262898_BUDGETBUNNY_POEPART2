package com.fake.st10262898_budgetbunny_poepart2.data

import com.google.firebase.firestore.FirebaseFirestore

class FirestoreExpenseRepository {

    //This gets an instance of firestore so it can be used in this class.
    private val db = FirebaseFirestore.getInstance()

    //Allows the application to add expenses to the database
    fun addExpense(expense: ExpenseFirebase, onResult: (Boolean) -> Unit) {
        db.collection("expenses")
            .add(expense)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }


    //This gets teh expenses which are in the database
    fun getExpenses(username: String, onResult: (List<ExpenseFirebase>) -> Unit) {
        db.collection("expenses")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { result ->
                val expenses = result.documents.map { doc ->
                    val expense = doc.toObject(ExpenseFirebase::class.java)!!
                    expense.id = doc.id
                    expense
                }
                onResult(expenses)
            }
    }

    //Allows user to be able to delete an expense entry
    fun deleteExpense(expenseId: String, onResult: (Boolean) -> Unit) {
        db.collection("expenses").document(expenseId)
            .delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    //This allows user to be able to pick a date and see all expenses between teh date.
    fun getExpensesBetweenDates(username: String, startDate: Long, endDate: Long, onResult: (List<ExpenseFirebase>) -> Unit) {
        db.collection("expenses")
            .whereEqualTo("username", username)
            .whereGreaterThanOrEqualTo("expenseDate", startDate)
            .whereLessThanOrEqualTo("expenseDate", endDate)
            .get()
            .addOnSuccessListener { result ->
                val expenses = result.documents.map { doc ->
                    val expense = doc.toObject(ExpenseFirebase::class.java)!!
                    expense.id = doc.id
                    expense
                }
                onResult(expenses)
            }
    }
}
