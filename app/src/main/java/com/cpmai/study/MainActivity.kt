package com.cpmai.study

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cpmai.study.data.ContentRepository
import com.cpmai.study.data.ProgressStore
import com.cpmai.study.data.QuizItem
import com.cpmai.study.data.StudyContent
import com.cpmai.study.data.Topic
import com.cpmai.study.ui.Background
import com.cpmai.study.ui.Border
import com.cpmai.study.ui.CpmaiColors
import com.cpmai.study.ui.Green
import com.cpmai.study.ui.Purple
import com.cpmai.study.ui.CardBg
import com.cpmai.study.ui.SurfaceAlt
import com.cpmai.study.ui.TextMain
import com.cpmai.study.ui.TextMuted
import com.cpmai.study.ui.categoryColor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val content = ContentRepository.load(this)
        val progress = ProgressStore(this)
        setContent {
            MaterialTheme(colorScheme = CpmaiColors, typography = Typography()) {
                Surface(color = Background, modifier = Modifier.fillMaxSize()) {
                    CpmaiApp(content, progress)
                }
            }
        }
    }
}

private enum class Tab { TOPICS, QUIZ, CHEAT, PROGRESS }

@Composable
private fun CpmaiApp(content: StudyContent, progress: ProgressStore) {
    var tab by rememberSaveable { mutableStateOf(Tab.TOPICS) }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var studiedTick by remember { mutableIntStateOf(0) }
    val studied = remember(studiedTick) { progress.studiedIds() }
    val selected = content.topics.firstOrNull { it.id == selectedId }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            if (selected == null) {
                NavigationBar(
                    containerColor = CardBg,
                    modifier = Modifier.navigationBarsPadding(),
                ) {
                    NavigationBarItem(
                        selected = tab == Tab.TOPICS,
                        onClick = { tab = Tab.TOPICS },
                        icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Topics") },
                        label = { Text("Topics") },
                        colors = navColors(),
                    )
                    NavigationBarItem(
                        selected = tab == Tab.QUIZ,
                        onClick = { tab = Tab.QUIZ },
                        icon = { Icon(Icons.Filled.Quiz, contentDescription = "Quiz") },
                        label = { Text("Quiz") },
                        colors = navColors(),
                    )
                    NavigationBarItem(
                        selected = tab == Tab.CHEAT,
                        onClick = { tab = Tab.CHEAT },
                        icon = { Icon(Icons.Filled.Bolt, contentDescription = "Cheat sheet") },
                        label = { Text("Cheat") },
                        colors = navColors(),
                    )
                    NavigationBarItem(
                        selected = tab == Tab.PROGRESS,
                        onClick = { tab = Tab.PROGRESS },
                        icon = { Icon(Icons.Outlined.Insights, contentDescription = "Progress") },
                        label = { Text("Progress") },
                        colors = navColors(),
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                selected != null -> TopicDetailScreen(
                    topic = selected,
                    studied = studied.contains(selected.id),
                    onBack = { selectedId = null },
                    onToggleStudied = {
                        progress.toggleStudied(selected.id)
                        studiedTick++
                    },
                )
                tab == Tab.TOPICS -> TopicListScreen(
                    topics = content.topics,
                    studied = studied,
                    onOpen = { selectedId = it },
                )
                tab == Tab.QUIZ -> QuizScreen(items = content.quiz, progress = progress)
                tab == Tab.CHEAT -> CheatSheetScreen(
                    topic = content.topics.first { it.id == "cheatsheet" },
                )
                tab == Tab.PROGRESS -> ProgressScreen(
                    topics = content.topics,
                    progress = progress,
                    studied = studied,
                    onOpen = { selectedId = it },
                    onResetQuiz = {
                        progress.resetQuizStats()
                        studiedTick++
                    },
                )
            }
        }
    }
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Purple,
    selectedTextColor = Purple,
    unselectedIconColor = TextMuted,
    unselectedTextColor = TextMuted,
    indicatorColor = SurfaceAlt,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TopicListScreen(
    topics: List<Topic>,
    studied: Set<String>,
    onOpen: (String) -> Unit,
) {
    val categories = remember(topics) {
        listOf("All") + topics.map { it.category }.distinct()
    }
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("All") }

    val filtered = topics.filter { topic ->
        val matchesCat = category == "All" || topic.category == category
        val q = query.trim().lowercase()
        val matchesQ = q.isEmpty() ||
            topic.title.lowercase().contains(q) ||
            topic.tags.contains(q) ||
            topic.analogies.any { it.lowercase().contains(q) } ||
            topic.keywords.any { it.lowercase().contains(q) }
        matchesCat && matchesQ
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Text("CPMAI Algorithms", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("in plain language · desi style · exam traps", color = TextMuted, fontSize = 14.sp)
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search fraud, recall, KNN, imbalanced…") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Purple,
                unfocusedBorderColor = Border,
                focusedContainerColor = SurfaceAlt,
                unfocusedContainerColor = SurfaceAlt,
                focusedTextColor = TextMain,
                unfocusedTextColor = TextMain,
                cursorColor = Purple,
            ),
        )
        Spacer(Modifier.height(10.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { cat ->
                val selected = category == cat
                val color = if (cat == "All") Purple else categoryColor(cat)
                Text(
                    text = cat,
                    color = if (selected) Color.White else color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selected) color.copy(alpha = 0.35f) else SurfaceAlt)
                        .border(1.dp, color.copy(alpha = if (selected) 1f else 0.5f), RoundedCornerShape(20.dp))
                        .clickable { category = cat }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("${filtered.size} topics", color = TextMuted, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(filtered, key = { it.id }) { topic ->
                TopicRow(topic, studied.contains(topic.id)) { onOpen(topic.id) }
            }
        }
    }
}

@Composable
private fun TopicRow(topic: Topic, studied: Boolean, onClick: () -> Unit) {
    val color = categoryColor(topic.category)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(topic.emoji, fontSize = 26.sp, modifier = Modifier.padding(end = 12.dp))
        Column(Modifier.weight(1f)) {
            Text(topic.title, color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 2)
            Spacer(Modifier.height(4.dp))
            Text(
                topic.tag,
                color = color,
                fontSize = 11.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
        if (studied) {
            Icon(Icons.Filled.Check, contentDescription = "Studied", tint = Green, modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TopicDetailScreen(
    topic: Topic,
    studied: Boolean,
    onBack: () -> Unit,
    onToggleStudied: () -> Unit,
) {
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextMain)
            }
            Text(topic.emoji, fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                topic.title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 28.dp),
        ) {
            Button(
                onClick = onToggleStudied,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (studied) Green else Purple,
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(if (studied) "Marked as studied" else "Mark as studied")
            }
            topic.analogies.forEach { analogy ->
                SectionCard(title = "Layman analogy", accent = Purple, body = analogy)
            }
            topic.howItWorks.forEach { how ->
                SectionCard(title = "How it actually works", accent = Purple, body = how)
            }
            topic.boxes.forEach { box ->
                SectionCard(title = box.label, accent = categoryColor(topic.category), body = box.value)
            }
            if (topic.uses.isNotEmpty()) {
                BulletCard("Use this when you see…", Green, topic.uses, "✓")
            }
            if (topic.traps.isNotEmpty()) {
                BulletCard("CPMAI traps", Color(0xFFFFB74D), topic.traps, "⚠")
            }
            topic.vs.forEach { vs ->
                SectionCard(title = "Don't mix these up", accent = Purple, body = vs)
            }
            topic.tables.forEach { table ->
                TableCard(table)
            }
            topic.examQs.forEach { exam ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0A1520))
                        .border(1.dp, Blue.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                ) {
                    MarkdownText(exam.q, Color(0xFFAAAAAA), 14.sp)
                    Spacer(Modifier.height(6.dp))
                    MarkdownText(exam.a, Green, 14.sp)
                }
            }
            if (topic.keywords.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    topic.keywords.forEach { kw ->
                        Text(
                            kw,
                            color = TextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceAlt)
                                .border(1.dp, Border, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, accent: Color, body: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceAlt)
            .border(1.dp, Border, RoundedCornerShape(12.dp))
            .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
            .border(width = 0.dp, color = Color.Transparent, shape = RoundedCornerShape(0.dp)),
    ) {
        Row {
            Box(
                Modifier
                    .width(3.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent),
            )
            Spacer(Modifier.width(8.dp))
            Text(title.uppercase(), color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
        }
        Spacer(Modifier.height(8.dp))
        MarkdownText(body, TextMain, 14.sp)
    }
}

@Composable
private fun BulletCard(title: String, accent: Color, items: List<String>, bullet: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .border(1.dp, Border, RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Text(title.uppercase(), color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
        Spacer(Modifier.height(8.dp))
        items.forEach { item ->
            Row(Modifier.padding(vertical = 4.dp)) {
                Text(bullet, modifier = Modifier.width(22.dp), color = accent)
                MarkdownText(item, Color(0xFFBBBBBB), 14.sp)
            }
        }
    }
}

@Composable
private fun TableCard(table: List<List<String>>) {
    if (table.isEmpty()) return
    val header = table.first()
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .border(1.dp, Border, RoundedCornerShape(12.dp))
            .horizontalScroll(rememberScrollState())
            .padding(8.dp),
    ) {
        table.forEachIndexed { index, row ->
            Row(Modifier.padding(vertical = 6.dp)) {
                row.forEachIndexed { col, cell ->
                    Text(
                        cell,
                        color = if (index == 0) TextMuted else TextMain,
                        fontSize = 12.sp,
                        fontWeight = if (index == 0 || col == 0) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.width(if (header.size <= 2) 180.dp else 140.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizScreen(items: List<QuizItem>, progress: ProgressStore) {
    val deck = remember { items.shuffled() }
    var index by rememberSaveable { mutableIntStateOf(0) }
    var revealed by rememberSaveable { mutableStateOf(false) }
    var sessionCorrect by rememberSaveable { mutableIntStateOf(0) }
    var sessionTried by rememberSaveable { mutableIntStateOf(0) }
    var done by rememberSaveable { mutableStateOf(false) }

    if (deck.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No quiz questions yet", color = TextMuted)
        }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
    ) {
        Text("Exam drill", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Tap to reveal the CPMAI answer, then mark yourself.", color = TextMuted, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        if (done) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBg)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Round complete", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("$sessionCorrect / $sessionTried this round", color = Green, fontSize = 18.sp)
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    index = 0
                    revealed = false
                    sessionCorrect = 0
                    sessionTried = 0
                    done = false
                }, colors = ButtonDefaults.buttonColors(containerColor = Purple)) {
                    Text("Drill again")
                }
            }
            return
        }

        val item = deck[index % deck.size]
        LinearProgressIndicator(
            progress = { (index.toFloat() / deck.size).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
            color = Purple,
            trackColor = SurfaceAlt,
        )
        Spacer(Modifier.height(6.dp))
        Text("${index + 1} of ${deck.size}  ·  this round $sessionCorrect/$sessionTried", color = TextMuted, fontSize = 12.sp)
        Spacer(Modifier.height(14.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(CardBg)
                .border(1.dp, Border, RoundedCornerShape(16.dp))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text("${item.emoji}  ${item.topicTitle}", color = Purple, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(10.dp))
            MarkdownText(item.question, TextMain, 16.sp)
            Spacer(Modifier.height(16.dp))
            AnimatedVisibility(revealed) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F2318))
                        .padding(12.dp),
                ) {
                    Text("ANSWER", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    MarkdownText(item.answer, Color(0xFFC8E6C9), 15.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        if (!revealed) {
            Button(
                onClick = { revealed = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                shape = RoundedCornerShape(14.dp),
            ) { Text("Show answer") }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        progress.recordQuiz(false)
                        sessionTried++
                        nextQuiz(deck.size, index) { i, finished ->
                            index = i
                            done = finished
                            revealed = false
                        }
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                ) { Text("Missed it") }
                Button(
                    onClick = {
                        progress.recordQuiz(true)
                        sessionTried++
                        sessionCorrect++
                        nextQuiz(deck.size, index) { i, finished ->
                            index = i
                            done = finished
                            revealed = false
                        }
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green),
                    shape = RoundedCornerShape(14.dp),
                ) { Text("Got it") }
            }
        }
    }
}

private fun nextQuiz(size: Int, index: Int, update: (Int, Boolean) -> Unit) {
    val next = index + 1
    if (next >= size) update(index, true) else update(next, false)
}

@Composable
private fun CheatSheetScreen(topic: Topic) {
    val table = topic.tables.firstOrNull().orEmpty()
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Text("⚡ Exam-day cheat sheet", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Keyword in the question → answer", color = TextMuted, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(table.drop(1)) { row ->
                if (row.size >= 2) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardBg)
                            .border(1.dp, Border, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                    ) {
                        Text(row[0], color = TextMuted, fontSize = 13.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(row[1], color = Green, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressScreen(
    topics: List<Topic>,
    progress: ProgressStore,
    studied: Set<String>,
    onOpen: (String) -> Unit,
    onResetQuiz: () -> Unit,
) {
    val total = topics.size
    val done = studied.size
    val attempted = progress.quizAttempted()
    val correct = progress.quizCorrect()
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text("Your progress", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        StatCard("Topics studied", "$done / $total", if (total == 0) 0f else done / total.toFloat())
        Spacer(Modifier.height(10.dp))
        StatCard(
            "Quiz lifetime",
            if (attempted == 0) "No attempts yet" else "$correct / $attempted correct",
            if (attempted == 0) 0f else correct / attempted.toFloat(),
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onResetQuiz) { Text("Reset quiz stats", color = TextMuted) }
        Spacer(Modifier.height(8.dp))
        Text("Topics", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        topics.forEach { topic ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onOpen(topic.id) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (studied.contains(topic.id)) Green else Border),
                )
                Spacer(Modifier.width(10.dp))
                Text("${topic.emoji}  ${topic.title}", color = TextMain, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, progress: Float) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .padding(16.dp),
    ) {
        Text(label, color = TextMuted, fontSize = 12.sp)
        Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
            color = Purple,
            trackColor = SurfaceAlt,
        )
    }
}

@Composable
private fun MarkdownText(text: String, color: Color, size: androidx.compose.ui.unit.TextUnit) {
    val annotated = remember(text, color) {
        buildAnnotatedString {
            val regex = Regex("\\*\\*(.+?)\\*\\*")
            var last = 0
            regex.findAll(text).forEach { match ->
                append(text.substring(last, match.range.first))
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = color)) {
                    append(match.groupValues[1])
                }
                last = match.range.last + 1
            }
            append(text.substring(last))
        }
    }
    Text(annotated, color = color, fontSize = size, fontFamily = FontFamily.SansSerif, lineHeight = size * 1.45f)
}

private val Blue = Color(0xFF2196F3)
