package com.example.smarttaskai.ui.view

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smarttaskai.ui.theme.BackgroundLight
import com.example.smarttaskai.ui.theme.PrimaryBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    photoUri: Uri?,
    onCancel: () -> Unit,
    onOpenCamera: () -> Unit,
    onSubmit: (title: String, desc: String, priority: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("HIGH") }
    var dueDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Task", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
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
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri, contentDescription = "Captured Photo", contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray)
                )
                TextButton(onClick = onOpenCamera, modifier = Modifier.align(Alignment.End)) {
                    Text("Retake Photo", color = PrimaryBlue)
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                OutlinedButton(
                    onClick = onOpenCamera,
                    modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Add Technical Photo", color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text("TASK NOMENCLATURE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = title, onValueChange = { title = it }, placeholder = { Text("e.g., Calibration of sensor array") },
                modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("DUE DATE (Optional)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = dueDate, onValueChange = {}, readOnly = true, placeholder = { Text("Select Date (Auto defaults to today)") },
                trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, contentDescription = "Pick Date") } },
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("DETAILED TECHNICAL SPECIFICATIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description, onValueChange = { description = it }, placeholder = { Text("Describe the operational requirements...") },
                modifier = Modifier.fillMaxWidth().height(120.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("PRIORITY MATRIX", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)).padding(4.dp)) {
                listOf("HIGH", "MEDIUM", "LOW").forEach { level ->
                    val isSelected = priority == level
                    val (containerColor, contentColor) = when {
                        !isSelected -> Color.Transparent to Color.Gray
                        level == "HIGH" -> Color(0xFFFFE4E6) to Color(0xFFDC2626)
                        level == "MEDIUM" -> Color(0xFFFEF3C7) to Color(0xFFD97706)
                        else -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
                    }

                    Button(
                        onClick = { priority = level },
                        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
                        modifier = Modifier.weight(1f),
                        elevation = if (isSelected) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text(level, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("CANCEL", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { onSubmit(title, description, priority) },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue), shape = RoundedCornerShape(8.dp)
                ) { Text("CREATE TASK") }
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
                        dueDate = formatter.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("CANCEL") } }
        ) { DatePicker(state = datePickerState) }
    }
}