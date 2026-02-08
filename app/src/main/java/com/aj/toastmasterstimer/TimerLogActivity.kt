package com.aj.toastmasterstimer

import android.os.Bundle
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.aj.toastmasterstimer.R

class TimerLogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer_log)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Timer Log"

        val tableLayout = findViewById<TableLayout>(R.id.logTable)

        for (entry in TimerLog.getEntries()) {
            val row = TableRow(this)

            val color = when (entry.color) {
                1 -> ContextCompat.getColor(this, android.R.color.holo_green_light)
                2 -> ContextCompat.getColor(this, android.R.color.holo_orange_light)
                3 -> ContextCompat.getColor(this, android.R.color.holo_red_light)
                else -> ContextCompat.getColor(this, android.R.color.white)
            }
            row.setBackgroundColor(color)

            addCell(row, entry.speakerName)
            addCell(row, entry.speechType)
            addCell(row, formatTime(entry.elapsedSeconds))

            tableLayout.addView(row)
        }
    }

    private fun addCell(row: TableRow, text: String) {
        val cell = TextView(this)
        cell.text = text
        cell.setPadding(16, 16, 16, 16)
        row.addView(cell)
    }

    private fun formatTime(seconds: Int) = String.format("%02d:%02d", seconds / 60, seconds % 60)

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}