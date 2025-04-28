package com.fake.st10262898_budgetbunny_poepart2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class HomePageActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_page)


        //Initialise the Pie chart and the bar graph:
        val pieChart : PieChart = findViewById(R.id.pieChart)


        //Prepare data for the pie chart:
        val pieEntries = ArrayList<PieEntry>()
        pieEntries.add(PieEntry(25f, "Category 1"))
        pieEntries.add(PieEntry(25f, "Category 2"))
        pieEntries.add(PieEntry(25f, "Category 3"))
        pieEntries.add(PieEntry(25f, "Category 4"))

        //Create a dataset for the Pie chart:
        val pieDataSet = PieDataSet(pieEntries, "Categories")

        //Create Pie data object:
        val pieData = PieData(pieDataSet)
        pieChart.data = pieData





    }
}