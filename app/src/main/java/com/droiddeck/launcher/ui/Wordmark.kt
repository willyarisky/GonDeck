package com.droiddeck.launcher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Modern, custom GonDeck wordmark and brand badge.
 * Replaces old DroidDeck vector typography with a sleek, futuristic gaming deck identity.
 */
@Composable
internal fun Wordmark(height: Dp, modifier: Modifier = Modifier) {
    val ink = MaterialTheme.colorScheme.onBackground
    val pal = LocalPalette.current
    val accent = pal.signal // Cyan / Blue neon accent
    val purple = Color(0xFF8B5CF6)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(height * 0.28f),
        modifier = modifier.height(height)
    ) {
        // GonDeck Custom Emblem: Hexagonal Gaming Deck Badge with Stylized 'G' Core
        Canvas(modifier = Modifier.size(height * 1.1f)) {
            val w = size.width
            val h = size.height
            val strokeWidth = w * 0.08f

            // Outer rounded cyber shield
            val shield = Path().apply {
                moveTo(w * 0.2f, h * 0.05f)
                lineTo(w * 0.8f, h * 0.05f)
                lineTo(w * 0.95f, h * 0.5f)
                lineTo(w * 0.75f, h * 0.95f)
                lineTo(w * 0.25f, h * 0.95f)
                lineTo(w * 0.05f, h * 0.5f)
                close()
            }

            // Glowing background shield
            drawPath(
                path = shield,
                brush = Brush.linearGradient(
                    colors = listOf(accent.copy(alpha = 0.25f), purple.copy(alpha = 0.15f)),
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                )
            )

            // Shield border
            drawPath(
                path = shield,
                brush = Brush.linearGradient(
                    colors = listOf(accent, purple),
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                ),
                style = Stroke(width = strokeWidth)
            )

            // Futuristic 'G' monogram inner path
            val gPath = Path().apply {
                // Top-right start curving into 'G'
                moveTo(w * 0.72f, h * 0.32f)
                lineTo(w * 0.42f, h * 0.32f)
                lineTo(w * 0.30f, h * 0.50f)
                lineTo(w * 0.42f, h * 0.68f)
                lineTo(w * 0.70f, h * 0.68f)
                lineTo(w * 0.70f, h * 0.50f)
                lineTo(w * 0.52f, h * 0.50f)
            }

            drawPath(
                path = gPath,
                color = ink,
                style = Stroke(width = w * 0.12f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )

            // Central power core dot
            drawCircle(
                color = accent,
                radius = w * 0.07f,
                center = Offset(w * 0.52f, h * 0.50f)
            )
        }

        // GonDeck Brand Typography
        Column(verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "GON",
                    color = ink,
                    fontSize = (height.value * 0.68f).sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "DECK",
                    color = accent,
                    fontSize = (height.value * 0.68f).sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
