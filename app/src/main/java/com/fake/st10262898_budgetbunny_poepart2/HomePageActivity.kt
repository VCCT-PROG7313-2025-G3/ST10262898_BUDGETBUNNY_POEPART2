package com.fake.st10262898_budgetbunny_poepart2

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



class HomePageActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_page)

        //Pie Chart code:

        //Initialise the Pie chart and the bar graph:
        val pieChart : PieChart = findViewById(R.id.pieChart)


        //This is the section where the  data is prepeared for the pie chart:
        val pieEntries = ArrayList<PieEntry>()
        pieEntries.add(PieEntry(40f,"Budget Goals")) //Leaving the labels open allows there to be more space on the pie chart (less cluttered)
        pieEntries.add(PieEntry(35f,"Expenses"))
        pieEntries.add(PieEntry(25f,"Savings"))


        //Create a dataset for the Pie chart:
        val pieDataSet = PieDataSet(pieEntries, "")
        pieDataSet.setColors(
            Color.rgb(0,100,0), //This makes budget Goals dark green
            Color.rgb(139,0,0), //This makes Expenses dark red
            Color.rgb(128,0,128) //This makes Savings dark purple
        )

        pieDataSet.sliceSpace = 5f
        pieDataSet.valueTextColor = Color.BLACK
        pieDataSet.valueTextSize = 14f
        pieDataSet.valueFormatter = NoDecimalPercentFormatter() //referring to the class that makes whole numbers



        //Create Pie data object:
        val pieData = PieData(pieDataSet)
        pieChart.data = pieData



        pieChart.description.isEnabled = false  // No description (No space for description)
        pieChart.setDrawEntryLabels(false)
        pieChart.setDrawCenterText(false)
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.setBackgroundColor(Color.TRANSPARENT)
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.animateY(1000)

        // The legend of the pie chart
        val legend = pieChart.legend
        legend.isEnabled = true
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
        legend.textColor = Color.WHITE
        legend.textSize = 8f //
        legend.form = Legend.LegendForm.CIRCLE
        legend.formSize = 8f //
        legend.yOffset = 4f //
        legend.xEntrySpace = 8f //
        legend.yEntrySpace = 0f //


        // Refresh
        pieChart.invalidate()



        //Bar Chart Work:

        //This is where the barchart in the xml
        val barChart: com.github.mikephil.charting.charts.BarChart = findViewById(R.id.barChart)

        //This is the static data which the bar graph will use:
        val entries = ArrayList<com.github.mikephil.charting.data.BarEntry>()
        entries.add(com.github.mikephil.charting.data.BarEntry(0f, 5000f)) // This is the bar for the Car
        entries.add(com.github.mikephil.charting.data.BarEntry(1f, 8000f)) // This is the bar for the House
        entries.add(com.github.mikephil.charting.data.BarEntry(2f, 3000f)) // This is the bar for the Food
        entries.add(com.github.mikephil.charting.data.BarEntry(3f, 1500f)) // This is the bar for the Hobbies
        entries.add(com.github.mikephil.charting.data.BarEntry(4f, 2000f)) // This is the bar for the Entertainment

        //These are the Labels for each bar
        val labels = arrayOf("Car", "House", "Food", "Hobbies", "Entertainment")

        //This is where all the colours of the graphs are set
        val barDataSet = com.github.mikephil.charting.data.BarDataSet(entries, "Expenses")
        barDataSet.setColors(
            Color.rgb(0, 100, 0),
            Color.rgb(139, 0, 0),
            Color.rgb(255, 165, 0),
            Color.rgb(75, 0, 130),
            Color.rgb(0, 191, 255)
        )
        barDataSet.valueTextColor = Color.BLACK
        barDataSet.valueTextSize = 12f


        val data = com.github.mikephil.charting.data.BarData(barDataSet)
        barChart.data = data

        // Styling the Bar Chart
        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        barChart.setBackgroundColor(Color.TRANSPARENT)
        barChart.animateY(1000)
        barChart.legend.isEnabled = false // Hide the default legend


        // Set the labels on X-axis off (So they will not appear, they clash when they appear, not enough space)
        val xAxis = barChart.xAxis
        xAxis.setDrawLabels(false)


        // Remove the left and right axis lines
        val leftAxis = barChart.axisLeft
        leftAxis.textColor = Color.WHITE
        leftAxis.setDrawGridLines(false)

        val rightAxis = barChart.axisRight
        rightAxis.isEnabled = false

        // Refresh the chart
        barChart.invalidate()








        //Find the text view on the xml here:
        val tvDateTime = findViewById<TextView>(R.id.tv_dateTime)

        //Specifying the format in which the date and time will be in:
        val currentDateTime = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date())
        tvDateTime.text = currentDateTime


    }

    //This class allows there to not be a decimal on the pie chart and its just whole numbers.
    class NoDecimalPercentFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return "${value.toInt()}%"
        }
    }
}