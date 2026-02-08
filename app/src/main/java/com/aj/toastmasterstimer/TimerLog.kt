package com.aj.toastmasterstimer

object TimerLog {
    data class Entry(
        val speechType: String,
        val speakerName: String,
        val speechName: String,
        val elapsedSeconds: Int,
        val color: Int
    )

    private val entries = mutableListOf<Entry>()

    fun addEntry(speechType: String, speakerName: String, speechName: String,
                 elapsed: Int, speech: SpeechType) {
        val color = when {
            elapsed < speech.greenTime -> 0
            elapsed < speech.yellowTime -> 1
            elapsed < speech.redTime -> 2
            else -> 3
        }
        entries.add(Entry(speechType, speakerName, speechName, elapsed, color))
    }

    fun getEntries() = entries.toList()
    fun clearEntries() = entries.clear()
}