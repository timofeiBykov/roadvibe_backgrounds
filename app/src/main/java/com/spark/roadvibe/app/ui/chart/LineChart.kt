package com.spark.roadvibe.app.ui.chart

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LiveData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.spark.roadvibe.app.ui.chart.model.DataSetConfiguration

@Composable
internal fun LineChart(
    initialConfiguration: List<DataSetConfiguration>,
    visibleCount: Float,
    sensorValue: LiveData<List<Entry>>,
    desc: String
) {
    val liveEntries by sensorValue.observeAsState()
    AndroidView(modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val lineChart = LineChart(ctx)

            val dataSets = initialConfiguration.map {
                LineDataSet(null, it.name).apply {
                    color = it.color
                    axisDependency = YAxis.AxisDependency.LEFT
                    lineWidth = 1F
                    isHighlightEnabled = false
                    setDrawValues(false)
                    setDrawCircles(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    cubicIntensity = 0.2F
                }
            }

            val lineData = LineData()
            dataSets.onEach {
                lineData.addDataSet(it)
            }

            lineChart.xAxis.apply {
                setAvoidFirstLastClipping(true)
                isEnabled = true
            }

            lineChart.axisLeft.apply {
                setDrawGridLines(true)
            }

            lineChart.axisRight.apply {
                isEnabled = false
            }

            lineChart.data = lineData
            lineChart.invalidate()

            lineChart.description.text = desc

            lineChart
        }) {
        if (liveEntries != null) {
            liveEntries!!.onEachIndexed { index, entry ->
                    val dataSet = it.data.dataSets[index]
                    dataSet.addEntry(entry)
                    if (dataSet.entryCount >= visibleCount) {
                        dataSet.removeFirst()
                    }

            }

            it.data.notifyDataChanged()
            it.notifyDataSetChanged()
            it.moveViewToX(it.data.entryCount - visibleCount)
            it.xAxis.axisMinimum = liveEntries!!.minBy { e -> e.x }.x - visibleCount

        }
    }
}
