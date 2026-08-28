package com.cpmai.study.data

data class StudyContent(
    val topics: List<Topic>,
    val quiz: List<QuizItem>,
)

data class Topic(
    val id: String,
    val emoji: String,
    val title: String,
    val tag: String,
    val category: String,
    val tags: String,
    val analogies: List<String> = emptyList(),
    val howItWorks: List<String> = emptyList(),
    val uses: List<String> = emptyList(),
    val traps: List<String> = emptyList(),
    val examQs: List<ExamQ> = emptyList(),
    val keywords: List<String> = emptyList(),
    val vs: List<String> = emptyList(),
    val tables: List<List<List<String>>> = emptyList(),
    val boxes: List<InfoBox> = emptyList(),
)

data class ExamQ(
    val q: String,
    val a: String,
)

data class QuizItem(
    val topicId: String,
    val topicTitle: String,
    val emoji: String,
    val question: String,
    val answer: String,
)

data class InfoBox(
    val label: String,
    val value: String,
)
