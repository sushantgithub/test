package com.cpmai.study.data

import android.content.Context
import com.google.gson.Gson
import java.nio.charset.Charset

object ContentRepository {
    @Volatile
    private var cached: StudyContent? = null

    fun load(context: Context): StudyContent {
        cached?.let { return it }
        val json = context.assets.open("content.json").use { stream ->
            stream.readBytes().toString(Charset.forName("UTF-8"))
        }
        val content = Gson().fromJson(json, StudyContent::class.java)
        cached = content
        return content
    }
}
