package com.playapp.aiagents.ui.video

import android.content.Context
import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.playapp.aiagents.data.model.VideoTutorial

@Composable
fun VideoPlayerDialog(
    video: VideoTutorial,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            Column {
                // Video title
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )

                // WebView for video playback
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = WebViewClient()

                            // Load YouTube video in embed format
                            val videoId = extractYouTubeVideoId(video.url)
                            if (videoId != null) {
                                loadUrl("https://www.youtube.com/embed/$videoId")
                            } else {
                                // Fallback for non-YouTube URLs
                                loadUrl(video.url)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                // Close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

/**
 * Extract YouTube video ID from various YouTube URL formats
 */
private fun extractYouTubeVideoId(url: String): String? {
    val patterns = listOf(
        "youtube\\.com/watch\\?v=([^&]+)".toRegex(),
        "youtu\\.be/([^?]+)".toRegex(),
        "youtube\\.com/embed/([^?]+)".toRegex()
    )

    for (pattern in patterns) {
        val match = pattern.find(url)
        if (match != null) {
            return match.groupValues[1]
        }
    }

    return null
}

/**
 * Open video in external app/browser
 */
fun openVideoExternally(context: Context, videoUrl: String) {
    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(videoUrl))
    context.startActivity(intent)
}