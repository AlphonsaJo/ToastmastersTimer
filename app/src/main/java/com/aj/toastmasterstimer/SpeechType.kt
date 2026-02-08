package com.aj.toastmasterstimer

enum class SpeechType(
    val displayName: String,
    val greenTime: Int,
    val yellowTime: Int,
    val redTime: Int
) {
    ICEBREAKER("Icebreaker (4-6 min)", 240, 300, 360),
    NORMAL_SPEECH("Normal Speech (5-7 min)", 300, 360, 420),
    TABLE_TOPICS("Table Topics (1-2 min)", 60, 90, 120),
    EVALUATION("Individual Evaluation (2-3 min)", 120, 150, 180),
    CUSTOM("Custom", 300, 360, 420)
}