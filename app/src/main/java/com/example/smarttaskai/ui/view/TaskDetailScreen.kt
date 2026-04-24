package com.example.smarttaskai.ui.view

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smarttaskai.data.dto.Task
import com.example.smarttaskai.ui.theme.BackgroundLight
import com.example.smarttaskai.ui.theme.PrimaryBlue
import com.example.smarttaskai.ui.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    task: Task,
    viewModel: DashboardViewModel,
    tempPhotoUri: Uri? = null,
    onNavigateBack: () -> Unit,
    onOpenCamera: () -> Unit = {}
) {
    val baseUrl = "https://apivincent.teamlancer.space"
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val safeTitle = if (task.title.isNullOrBlank() || "${task.title}" == "null") "Untitled Task" else "${task.title}"
    val safeDesc = if (task.description.isNullOrBlank() || "${task.description}" == "null") "" else "${task.description}"

    val rawPrio = if (task.priority.isNullOrBlank() || "${task.priority}" == "null") "LOW" else "${task.priority}".uppercase()
    val safePrio = if (rawPrio == "MID") "MEDIUM" else rawPrio

    val rawStat = if (task.status.isNullOrBlank() || "${task.status}" == "null") "PENDING" else "${task.status}".uppercase()
    val safeStat = if (rawStat == "PROCESS") "IN PROGRESS" else rawStat

    val safeDate = if (task.dueDate.isNullOrBlank() || "${task.dueDate}" == "null") "" else "${task.dueDate}"
    val safeImg = if (task.imageUrl.isNullOrBlank() || "${task.imageUrl}" == "null") "" else "${task.imageUrl}"

    var editedTitle by remember(task) { mutableStateOf(safeTitle) }
    var editedDescription by remember(task) { mutableStateOf(safeDesc) }
    var editedPriority by remember(task) { mutableStateOf(safePrio) }
    var editedStatus by remember(task) { mutableStateOf(safeStat) }
    var editedDueDate by remember(task) { mutableStateOf(safeDate) }
    var editedImageUrl by remember(task) { mutableStateOf(safeImg) }

    LaunchedEffect(tempPhotoUri) {
        if (tempPhotoUri != null) {
            editedImageUrl = tempPhotoUri.toString()
            isEditing = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Task #${task.id}", fontSize = 12.sp, color = Color.Gray)
                        Text("SmartTask AI", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                navigationIcon = {
                    if (isEditing) {
                        IconButton(onClick = {
                            isEditing = false
                            editedTitle = safeTitle; editedDescription = safeDesc; editedPriority = safePrio
                            editedStatus = safeStat; editedDueDate = safeDate; editedImageUrl = safeImg
                        }) { Icon(Icons.Default.Close, contentDescription = "Cancel") }
                    } else {
                        IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = {
                            viewModel.updateTaskData(
                                taskId = task.id,
                                newTitle = editedTitle,
                                newDescription = editedDescription,
                                newPriority = editedPriority,
                                newStatus = editedStatus,
                                newDueDate = editedDueDate,
                                newImageUrl = editedImageUrl,
                                onSuccess = { isEditing = false }
                            )
                        }) { Icon(Icons.Default.Done, contentDescription = "Save", tint = PrimaryBlue) }
                    } else {
                        IconButton(onClick = { showDeleteDialog = true }) { Icon(Icons.Default.Delete, contentDescription = "Delete Task", tint = Color.Red) }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (isEditing) {
                if (editedImageUrl.isNotEmpty()) {
                    val displayUrl = if (editedImageUrl.startsWith("http") || editedImageUrl.startsWith("content://") || editedImageUrl.startsWith("file://")) {
                        editedImageUrl
                    } else {
                        "$baseUrl$editedImageUrl"
                    }

                    AsyncImage(
                        model = displayUrl, contentDescription = "Task Image", contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray)
                    )
                    TextButton(onClick = onOpenCamera, modifier = Modifier.align(Alignment.End)) {
                        Text("Retake Photo", color = PrimaryBlue)
                    }
                } else {
                    OutlinedButton(
                        onClick = onOpenCamera,
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Add Technical Photo", color = Color.Gray)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            } else if (safeImg.isNotEmpty()) {
                val viewDisplayUrl = if (safeImg.startsWith("http") || safeImg.startsWith("content://") || safeImg.startsWith("file://")) {
                    safeImg
                } else {
                    "$baseUrl$safeImg"
                }

                AsyncImage(
                    model = viewDisplayUrl, contentDescription = "Task Image", contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)).background(Color.LightGray)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (isEditing) {
                Text("TASK NOMENCLATURE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = editedTitle, onValueChange = { editedTitle = it },
                    modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("DUE DATE (YYYY-MM-DD)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = editedDueDate, onValueChange = {}, readOnly = true, placeholder = { Text("Select Date") },
                    trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, contentDescription = "Pick Date") } },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )
            } else {
                Text(text = safeTitle, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("UNIT ICT", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    if (safeDate.isNotEmpty()) { Text("•  Due: $safeDate", fontSize = 12.sp, color = Color.Gray) }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isEditing) {
                Text("PRIORITY MATRIX", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)).padding(4.dp)) {
                    listOf("HIGH", "MEDIUM", "LOW").forEach { level ->
                        val isSelected = editedPriority == level
                        val (containerColor, contentColor) = when {
                            !isSelected -> Color.Transparent to Color.Gray
                            level == "HIGH" -> Color(0xFFFFE4E6) to Color(0xFFDC2626)
                            level == "MEDIUM" -> Color(0xFFFEF3C7) to Color(0xFFD97706)
                            else -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
                        }

                        Button(
                            onClick = { editedPriority = level },
                            colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
                            modifier = Modifier.weight(1f),
                            elevation = if (isSelected) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) { Text(level, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (isEditing) {
                Text("STATUS MATRIX", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)).padding(4.dp)) {
                    listOf("PENDING", "IN PROGRESS", "COMPLETED").forEach { level ->
                        val isSelected = editedStatus == level
                        Button(
                            onClick = { editedStatus = level },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color.White else Color.Transparent,
                                contentColor = if (isSelected) PrimaryBlue else Color.Gray
                            ),
                            modifier = Modifier.weight(1f),
                            elevation = if (isSelected) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) { Text(level, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusBadge(text = safePrio, isPriority = true)
                    StatusBadge(text = editedStatus, isPriority = false)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isEditing) {
                Text("DETAILED TECHNICAL SPECIFICATIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = editedDescription, onValueChange = { editedDescription = it },
                    placeholder = { Text("Describe the operational requirements...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("DESCRIPTION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = if (safeDesc.isEmpty()) "No description" else safeDesc, fontSize = 15.sp, color = Color.DarkGray)
                    }
                }
            }

            if (!isEditing) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { isEditing = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) { Text("Edit Task", fontSize = 16.sp) }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        editedDueDate = formatter.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("CANCEL") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Hapus Laporan?", fontWeight = FontWeight.Bold) },
            text = { Text("Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteTask(task.id, onSuccess = onNavigateBack); showDeleteDialog = false }) {
                    Text("HAPUS", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("BATAL", color = PrimaryBlue) } }
        )
    }
}

@Composable
fun StatusBadge(text: String, isPriority: Boolean) {
    val upperText = text.uppercase()
    val color = if (isPriority) {
        when(upperText) { "HIGH" -> Color(0xFFEF4444); "MEDIUM" -> Color(0xFFD97706); "LOW" -> Color(0xFF16A34A); else -> Color.Gray }
    } else {
        when(upperText) { "PENDING" -> Color(0xFFF59E0B); "IN PROGRESS" -> PrimaryBlue; "COMPLETED" -> Color(0xFF10B981); else -> Color.Gray }
    }
    Surface(color = color.copy(alpha = 0.15f), shape = CircleShape) {
        Text(text = upperText, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
    }
}