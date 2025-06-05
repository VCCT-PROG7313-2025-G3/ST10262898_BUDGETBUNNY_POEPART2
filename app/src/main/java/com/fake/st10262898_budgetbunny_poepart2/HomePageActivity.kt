package com.fake.st10262898_budgetbunny_poepart2

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetFirestore
import com.fake.st10262898_budgetbunny_poepart2.data.ChatMessage
import com.fake.st10262898_budgetbunny_poepart2.data.ExpenseFirebase
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomePageActivity : AppCompatActivity() {

    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private val db = Firebase.firestore


    //This is for chatbot:
    private lateinit var fabChat: FloatingActionButton
    private lateinit var chatContainer: LinearLayout
    private lateinit var etChatInput: EditText
    private lateinit var btnChatSend: Button
    private var isChatExpanded = false
    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var btnCloseChat: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        // Initialize UI components
        initializeViews()
        loadData()
        setupBottomNavigation()

        findViewById<View>(R.id.bottomNavigationView).post {
            val navBar = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            val fab = findViewById<FloatingActionButton>(R.id.fabChat)
            val chatContainer = findViewById<LinearLayout>(R.id.chatContainer)

            // Calculate proper margin (nav height + 16dp padding)
            val marginBottom = navBar.height +
                    (16 * resources.displayMetrics.density).toInt()

            // Apply to both elements
            (fab.layoutParams as CoordinatorLayout.LayoutParams).bottomMargin = marginBottom
            (chatContainer.layoutParams as CoordinatorLayout.LayoutParams).bottomMargin = marginBottom
        }
    }

    private fun initializeViews() {
        pieChart = findViewById(R.id.pieChart)
        barChart = findViewById(R.id.barChart)
        val recyclerView = findViewById<RecyclerView>(R.id.rv_transactions)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<CardView>(R.id.cv_resources).setOnClickListener {
            startActivity(Intent(this, ResourcesActivity::class.java))
        }

        expenseAdapter = ExpenseAdapter(emptyList()) { selectedExpense ->
            Intent(this, EditTransactionsActivity::class.java).apply {
                putExtra("expenseId", selectedExpense.id)
                startActivity(this)
            }
        }
        recyclerView.adapter = expenseAdapter

        barChart.setOnClickListener {
            val intent = Intent(this, DetailedBarChartActivity::class.java)
            startActivity(intent)
        }


        // Chatbot setup
        fabChat = findViewById(R.id.fabChat)
        chatContainer = findViewById(R.id.chatContainer)
        etChatInput = findViewById(R.id.etChatInput)
        btnChatSend = findViewById(R.id.btnChatSend)

        fabChat.setOnClickListener { toggleChat() }
        btnChatSend.setOnClickListener { handleChatInput() }



        // Initialize chat RecyclerView
        chatAdapter = ChatAdapter(chatMessages)
        findViewById<RecyclerView>(R.id.rvChatMessages).apply {
            layoutManager = LinearLayoutManager(this@HomePageActivity)
            adapter = chatAdapter
        }
        // Welcome message
        addChatMessage("Welcome! Choose:\nA - Add Expense\nB - Set Budget\nC - Play Game", true)


        btnCloseChat = findViewById(R.id.btnCloseChat)
        btnCloseChat.setOnClickListener {
            chatContainer.visibility = View.GONE
            fabChat.visibility = View.VISIBLE // Show the FAB again
        }
    }

    private fun loadData() {
        val currentUserId = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            .getString("username", "") ?: ""

        // Load expenses
        db.collection("expenses")
            .whereEqualTo("username", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                val expenses = documents.map { doc ->
                    ExpenseFirebase(
                        expenseName = doc.getString("expenseName") ?: "",
                        expenseAmount = doc.getDouble("expenseAmount") ?: 0.0,
                        username = doc.getString("username") ?: "",
                        expenseCategory = doc.getString("expenseCategory"),
                        expenseDate = doc.getLong("expenseDate") ?: 0L,
                        expenseImageBase64 = doc.getString("expenseImageBase64")
                    ).apply { id = doc.id }
                }
                expenseAdapter.updateExpenses(expenses)
                setupPieChart(expenses)
            }

        // Load budgets
        lifecycleScope.launch {
            try {
                val budgets = db.collection("budgets")
                    .whereEqualTo("username", currentUserId)
                    .get()
                    .await()
                    .toObjects(BudgetFirestore::class.java)

                setupBudgetBarChart(budgets)
            } catch (e: Exception) {
                Log.e("HomePage", "Budget load error", e)
            }
        }
    }

    private fun setupBudgetBarChart(budgets: List<BudgetFirestore>) {
        val categoryData = budgets.groupBy { it.budgetCategory }
            .mapValues { it.value.sumOf { budget -> budget.budgetAmount } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        val entries = categoryData.mapIndexed { index, (category, amount) ->
            BarEntry(index.toFloat(), amount.toFloat())
        }

        val dataSet = BarDataSet(entries, "Budget Allocation").apply {
            colors = listOf(
                Color.rgb(0, 100, 0),
                Color.rgb(70, 130, 180),
                Color.rgb(255, 165, 0),
                Color.rgb(75, 0, 130),
                Color.rgb(220, 20, 60)
            )
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }

        barChart.apply {
            data = BarData(dataSet)
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(categoryData.map { it.first })
                granularity = 1f
                labelRotationAngle = -45f
                textColor = Color.WHITE
            }
            axisLeft.textColor = Color.WHITE
            axisRight.isEnabled = false
            description.isEnabled = false
            setBackgroundColor(Color.TRANSPARENT)
            animateY(1000)
            invalidate()
        }
    }

    private fun setupPieChart(expenses: List<ExpenseFirebase>) {
        val categoryData = expenses.groupBy { it.expenseCategory }
            .mapValues { it.value.sumOf { expense -> expense.expenseAmount } }
            .toList()
            .sortedByDescending { it.second }
            .take(4)

        val entries = categoryData.map { (category, amount) ->
            PieEntry(amount.toFloat(), category)
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.rgb(0, 100, 0),
                Color.rgb(139, 0, 0),
                Color.rgb(128, 0, 128),
                Color.rgb(255, 165, 0)
            )
            valueTextColor = Color.BLACK
            valueTextSize = 14f
            valueFormatter = NoDecimalPercentFormatter()
        }

        pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            setBackgroundColor(Color.TRANSPARENT)
            animateY(1000)
            invalidate()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.nav_home

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_transactions -> {
                    startActivity(Intent(this, TransactionsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_budgetGoal -> {
                    startActivity(Intent(this, BudgetGoalsOverviewActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, Settings::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    private fun navigateTo(activity: Class<*>) {
        startActivity(Intent(this, activity))
        overridePendingTransition(0, 0)
    }

    class NoDecimalPercentFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String = "${value.toInt()}%"
    }

    // Add this if you're getting IndexAxisValueFormatter errors
    class IndexAxisValueFormatter(private val labels: List<String>) : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return labels.getOrNull(value.toInt()) ?: ""
        }
    }

    //Chat Bot methods:
    private fun toggleChat() {
        isChatExpanded = !isChatExpanded
        chatContainer.visibility = if (isChatExpanded) View.VISIBLE else View.GONE
    }



    private fun addChatMessage(message: String, isBot: Boolean = true) {
        val chatMessage = ChatMessage(message, isBot)
        chatMessages.add(chatMessage)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)


        findViewById<RecyclerView>(R.id.rvChatMessages).smoothScrollToPosition(chatMessages.size - 1)
    }

    private fun handleChatInput() {
        val input = etChatInput.text.toString().trim().uppercase()
        etChatInput.text.clear()


        addChatMessage(input, false)

        when (input) {
            "A" -> {
                addChatMessage("Opening expenses...", true)
                startActivity(Intent(this, ExpenseEntry::class.java))
                collapseChat()
            }
            "B" -> {
                addChatMessage("Opening budgets...", true)
                startActivity(Intent(this, GoalEntry::class.java))
                collapseChat()
            }
            "C" -> {
                addChatMessage("Game coming soon! 🎮", true)
            }
            else -> {
                addChatMessage("Please enter A, B, or C", true)
            }
        }
    }

    private fun collapseChat() {
        isChatExpanded = false
        chatContainer.visibility = View.GONE
    }

}