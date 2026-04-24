package com.example.smarttaskai.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarttaskai.data.UiState
import com.example.smarttaskai.data.dto.Task
import com.example.smarttaskai.ui.theme.*
import com.example.smarttaskai.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onTaskClick: (Long) -> Unit,
    onLogoutClick: () -> Unit,
    onCameraClick: () -> Unit,
    onAddTaskClick: () -> Unit
) {
    val tasksState by viewModel.tasksState.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

    var selectedFilter by remember { mutableStateOf("All Tasks") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Assignment, contentDescription = "App Icon", tint = PrimaryBlue, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SmartTask AI", color = PrimaryBlue, fontWeight = FontWeight.ExtraBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight),
                actions = {
                    IconButton(onClick = { viewModel.refreshTasks() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PrimaryBlue)
                    }
                    IconButton(onClick = { viewModel.logout(); onLogoutClick() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.Gray)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTaskClick,
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(icon = { Icon(Icons.Default.Home, "") }, label = { Text("DASHBOARD") }, selected = true, onClick = {})
                NavigationBarItem(icon = { Icon(Icons.Default.CameraAlt, "") }, label = { Text("CAMERA") }, selected = false, onClick = onCameraClick)
                NavigationBarItem(icon = { Icon(Icons.Default.Person, "") }, label = { Text("PROFILE") }, selected = false, onClick = {})
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(paddingValues)
        ) {
            LazyRow(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                val filters = listOf("All Tasks", "Pending", "In Progress", "Completed")
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PriorityMediumBg,
                            selectedLabelColor = PrimaryBlue
                        )
                    )
                }
            }

            when (tasksState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text((tasksState as UiState.Error).message, color = Color.Red, modifier = Modifier.padding(16.dp))
                    }
                }
                is UiState.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val allTasks = (tasksState as UiState.Success<List<Task>>).data

                    val filteredTasks = if (selectedFilter == "All Tasks") {
                        allTasks
                    } else {
                        allTasks.filter {
                            val rawStat = "${it.status}"
                            val safeStatus = if (rawStat == "null" || rawStat.isBlank()) "PENDING" else rawStat
                            safeStatus.equals(selectedFilter, ignoreCase = true)
                        }
                    }

                    val sortedAndFilteredTasks = filteredTasks.sortedWith(
                        compareBy<Task> {
                            val rawPrio = "${it.priority}"
                            val safePriority = if (rawPrio == "null" || rawPrio.isBlank()) "LOW" else rawPrio
                            when (safePriority.uppercase()) {
                                "HIGH" -> 0
                                "MEDIUM", "MID" -> 1
                                "LOW" -> 2
                                else -> 3
                            }
                        }.thenByDescending { it.id }
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isAnalyzing) {
                            item { AnalyzingTaskCard() }
                        }

                        items(sortedAndFilteredTasks) { task ->
                            TaskCard(task = task, onClick = { onTaskClick(task.id) })
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun TaskCard(task: Task, onClick: () -> Unit) {
    val rawPrio = "${task.priority}"
    val safePriority = if (rawPrio == "null" || rawPrio.isBlank()) "LOW" else rawPrio

    val rawStat = "${task.status}"
    val safeStatus = if (rawStat == "null" || rawStat.isBlank()) "PENDING" else rawStat

    val rawTitle = "${task.title}"
    val safeTitle = if (rawTitle == "null" || rawTitle.isBlank()) "Untitled Task" else rawTitle

    val rawDesc = "${task.description}"
    val safeDescription = if (rawDesc == "null" || rawDesc.isBlank()) "No description available" else rawDesc

    val rawImg = "${task.imageUrl}"
    val safeImg = if (rawImg == "null" || rawImg.isBlank()) "" else rawImg

    val isAiGenerated = safeImg.isNotEmpty() && safeDescription.length > 40

    val (textColor, bgColor, iconColor) = when (safePriority.uppercase()) {
        "HIGH" -> Triple(PriorityHighText, PriorityHighBg, PriorityHighText)
        "MEDIUM", "MID" -> Triple(PriorityMediumText, PriorityMediumBg, PrimaryBlue)
        else -> Triple(PriorityLowText, PriorityLowBg, Color.Gray)
    }

    val statusColor = when(safeStatus.uppercase()) {
        "PENDING" -> Color(0xFFF59E0B)
        "IN PROGRESS", "PROCESS" -> PrimaryBlue
        "COMPLETED" -> Color(0xFF10B981)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(textColor))

            Column(modifier = Modifier.padding(16.dp).weight(1f)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(color = bgColor, shape = CircleShape) {
                            Text(
                                text = if (safePriority.uppercase() == "MID") "MEDIUM" else safePriority.uppercase(),
                                color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(color = statusColor.copy(alpha = 0.15f), shape = CircleShape) {
                            Text(
                                text = if (safeStatus.uppercase() == "PROCESS") "IN PROGRESS" else safeStatus.uppercase(),
                                color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (isAiGenerated) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xFFF3E8FF), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = Color(0xFF9333EA), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Generated", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9333EA))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(text = safeTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = safeDescription, fontSize = 13.sp, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = task.dueDate ?: "No deadline", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun AnalyzingTaskCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PrimaryBlue, strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Gemini AI is analyzing...", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                Text("Identifying hardware issues...", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}