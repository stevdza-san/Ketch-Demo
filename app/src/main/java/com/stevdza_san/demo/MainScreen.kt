package com.stevdza_san.demo

import android.os.Environment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ketch.Ketch
import com.ketch.Status
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

const val FILE_TAG = "video"
const val FILE_NAME = "MyVideo.mp4"
const val DOWNLOAD_URL = "https://file-examples.com/storage/fe45dfa76e66c6232a111c9/2017/04/file_example_MP4_1920_18MG.mp4"

@Composable
fun MainScreen(ketch: Ketch) {
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf(Status.DEFAULT) }
    var progress by remember { mutableIntStateOf(0) }
    var total by remember { mutableLongStateOf(0L) }
    var isCollecting by remember { mutableStateOf(false) }

    LaunchedEffect(isCollecting) {
        if (isCollecting) {
            ketch.observeDownloadByTag(tag = FILE_TAG)
                .collect { downloadModel ->
                    for (model in downloadModel) {
                        status = model.status
                        progress = model.progress
                        total = model.total
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 48.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = status.name,
            textAlign = TextAlign.Center,
            fontSize = MaterialTheme.typography.titleLarge.fontSize,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(48.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = FILE_NAME)
            Text(text = "$progress%")
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            progress = { progress / 100f }
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "${
                calculateDownloadedMegabytes(
                    progress,
                    total
                )
            }MB / ${getTwoDecimals(value = total / (1024.0 * 1024.0))}MB",
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
            textAlign = TextAlign.End
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    isCollecting = true
                    ketch.download(
                        tag = FILE_TAG,
                        url = DOWNLOAD_URL,
                        fileName = FILE_NAME,
                        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
                    )
                }
            ) {
                Text(text = "Download")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { ketch.cancel(tag = FILE_TAG) }) {
                Text(text = "Cancel")
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = { ketch.pause(tag = FILE_TAG) }) {
                Text(text = "Pause")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { ketch.resume(tag = FILE_TAG) }) {
                Text(text = "Resume")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { ketch.retry(tag = FILE_TAG) }) {
                Text(text = "Retry")
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    ketch.clearDb(tag = FILE_TAG)
                    scope.launch {
                        delay(100)
                        status = Status.DEFAULT
                        progress = 0
                        total = 0L
                        isCollecting = false
                    }
                }
            ) {
                Text(text = "Delete")
            }
        }
    }
}

fun calculateDownloadedMegabytes(progress: Int, totalBytes: Long): String {
    val downloadedBytes = progress / 100.0 * totalBytes
    return getTwoDecimals(value = downloadedBytes / (1024.0 * 1024.0))
}

fun getTwoDecimals(value: Double): String {
    return String.format(Locale.ROOT, "%.2f", value)
}