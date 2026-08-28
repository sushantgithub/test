package com.cpmai.study.data

import android.content.Context

class ProgressStore(context: Context) {
    private val prefs = context.getSharedPreferences("cpmai_progress", Context.MODE_PRIVATE)

    fun studiedIds(): Set<String> =
        prefs.getStringSet("studied", emptySet()) ?: emptySet()

    fun isStudied(id: String): Boolean = studiedIds().contains(id)

    fun toggleStudied(id: String) {
        val next = studiedIds().toMutableSet()
        if (!next.add(id)) next.remove(id)
        prefs.edit().putStringSet("studied", next).apply()
    }

    fun quizCorrect(): Int = prefs.getInt("quiz_correct", 0)
    fun quizAttempted(): Int = prefs.getInt("quiz_attempted", 0)

    fun recordQuiz(correct: Boolean) {
        prefs.edit()
            .putInt("quiz_attempted", quizAttempted() + 1)
            .putInt("quiz_correct", quizCorrect() + if (correct) 1 else 0)
            .apply()
    }

    fun resetQuizStats() {
        prefs.edit().putInt("quiz_attempted", 0).putInt("quiz_correct", 0).apply()
    }
}
