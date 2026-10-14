package com.droiddeck.launcher.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@Composable
internal fun EmptyLibrary(avoid: Rect?, fade: @Composable () -> Unit) {
    val clock = rememberMarkClock(avoid)
    Box(Modifier.fillMaxSize().onSizeChanged { clock.page = Size(it.width.toFloat(), it.height.toFloat()) }) {
        EmptyWall(clock)
        fade()
        Mark(clock)
    }
}

private const val TRACE_MS = 1250f
private const val FILL_FROM = 450f
private const val POP_FROM = 900f
private const val IMPACT = 2200f
private const val RIPPLE_MS = 1250f
private const val HIT_MS = 900f
private const val END = 3600f
private const val DRIFT_MS = 75_000f

private class MarkClock {
    var intro by mutableFloatStateOf(0f)
    var ball by mutableFloatStateOf(0f)
    var wall by mutableFloatStateOf(0f)
    var at by mutableStateOf(Offset.Unspecified)
    var slam by mutableStateOf(Offset.Unspecified)
    var page = Size.Zero
    var poked = false
    fun poke() { if (ball >= IMPACT + RIPPLE_MS) poked = true }
}

@Composable
private fun rememberMarkClock(avoid: Rect?): MarkClock {
    val clock = remember { MarkClock() }
    val haptics = LocalHapticFeedback.current
    val keepOff by rememberUpdatedState(avoid)
    val speed = with(LocalDensity.current) { 64.dp.toPx() }
    val inset = with(LocalDensity.current) { 10.dp.toPx() }
    val clearance = with(LocalDensity.current) { 16.dp.toPx() }
    LaunchedEffect(clock) {
        var last = -1L
        var vx = speed * 0.8f
        var vy = -speed * 0.6f
        while (true) {
            if (clock.page == Size.Zero) { withFrameMillis { }; continue }
            if (clock.at == Offset.Unspecified) clock.at = Offset(clock.page.width * 0.68f, clock.page.height * 0.42f)
            if (Motion.scale == 0f) {
                clock.intro = END
                clock.ball = END
                clock.wall = 0f
                return@LaunchedEffect
            }
            val now = withFrameMillis { it }
            if (last < 0L) { last = now; continue }
            val dt = (now - last).toFloat()
            last = now
            clock.wall += dt
            if (clock.intro < END) {
                val next = clock.intro + dt
                if (clock.intro < IMPACT && next >= IMPACT) {
                    clock.slam = clock.at
                    haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                }
                clock.intro = next.coerceAtMost(END)
            }
            if (clock.intro >= POP_FROM) clock.ball = (clock.ball + dt).coerceAtMost(END)
            if (clock.intro >= END) {
                val box = Rect(
                    inset, inset,
                    clock.page.width - inset,
                    clock.page.height - inset
                )
                var x = clock.at.x + vx * (dt / 1000f)
                var y = clock.at.y + vy * (dt / 1000f)
                if (x < box.left) { x = box.left; vx = -vx }
                if (x > box.right) { x = box.right; vx = -vx }
                if (y < box.top) { y = box.top; vy = -vy }
                if (y > box.bottom) { y = box.bottom; vy = -vy }
                keepOff?.let { k ->
                    val pad = Rect(k.left - clearance, k.top - clearance, k.right + clearance, k.bottom + clearance)
                    if (pad.contains(Offset(x, y))) {
                        val dxLeft = abs(x - pad.left)
                        val dxRight = abs(x - pad.right)
                        val dyTop = abs(y - pad.top)
                        val dyBottom = abs(y - pad.bottom)
                        val m = min(min(dxLeft, dxRight), min(dyTop, dyBottom))
                        when (m) {
                            dxLeft -> { x = pad.left; vx = -abs(vx) }
                            dxRight -> { x = pad.right; vx = abs(vx) }
                            dyTop -> { y = pad.top; vy = -abs(vy) }
                            else -> { y = pad.bottom; vy = abs(vy) }
                        }
                    }
                }
                clock.at = Offset(x, y)
            }
        }
    }
    return clock
}

private fun abs(f: Float): Float = if (f < 0f) -f else f

@Composable
private fun EmptyWall(clock: MarkClock) {
    val pal = LocalPalette.current
    val bg = pal.background
    val hit = pal.signal
    val density = LocalDensity.current
    Canvas(Modifier.fillMaxSize()) {
        val capW = 112.dp.toPx()
        val capH = 168.dp.toPx()
        val gap = 14.dp.toPx()
        val corner = CornerRadius(10.dp.toPx())
        val line = 1.dp.toPx()
        val rad = Math.toRadians(13.0)
        val cs = cos(rad).toFloat()
        val sn = sin(rad).toFloat()
        val wallW = (size.width * cs + size.height * sn) * 1.1f
        val wallH = max(size.height * 2.4f, (size.width * sn + size.height * cs) * 1.1f)
        val columns = ((wallW + gap) / (capW + gap)).toInt() + 1
        val perRun = ((wallH + gap) / (capH + gap)).toInt() + 1
        val run = (capH + gap) * perRun
        val shift = run * ((clock.wall % DRIFT_MS) / DRIFT_MS)
        val pivot = center
        val left = pivot.x - wallW / 2
        val top = pivot.y - wallH / 2
        rotate(-13f, pivot) {
            for (c in 0 until columns) {
                val x = left + c * (capW + gap)
                val dy = if (c % 2 == 0) -shift else shift - run
                for (i in 0 until perRun * 2) {
                    val y = top + i * (capH + gap) + dy
                    val sx = pivot.x + (x + capW / 2 - pivot.x) * cs + (y + capH / 2 - pivot.y) * sn
                    val sy = pivot.y - (x + capW / 2 - pivot.x) * sn + (y + capH / 2 - pivot.y) * cs
                    if (sx < -capH || sx > size.width + capH || sy < -capH || sy > size.height + capH) continue
                    drawRoundRect(pal.surface, Offset(x, y), Size(capW, capH), corner)
                    drawRoundRect(pal.line, Offset(x, y), Size(capW, capH), corner, style = Stroke(line))
                }
            }
        }
        drawRect(
            Brush.radialGradient(
                0f to bg, 0.6f to bg, 0.85f to bg.copy(alpha = 0.7f), 1f to Color.Transparent,
                center = if (clock.at != Offset.Unspecified) clock.at else center, radius = 460.dp.toPx(),
            ),
        )
    }
}

/**
 * Modern GonDeck Hero Visual:
 * Glowing Cybernetic Gaming Deck Monogram & Concentric Neon Energy Pulse.
 * Replaces the old DroidDeck D mark completely.
 */
@Composable
private fun Mark(clock: MarkClock) {
    val pal = LocalPalette.current
    val ink = MaterialTheme.colorScheme.onBackground
    val accent = pal.signal // Neon Cyan
    val violet = Color(0xFF8B5CF6) // Neon Violet
    Canvas(
        Modifier.fillMaxSize().pointerInput(clock) {
            detectTapGestures { clock.poke() }
        },
    ) {
        if (clock.at == Offset.Unspecified) return@Canvas
        val c = clock.at
        val t = clock.ball
        val intro = clock.intro
        val baseRadius = min(size.height * 0.22f, 130.dp.toPx())

        // Ripple pulse when tapped / slam
        if (clock.slam != Offset.Unspecified && t in IMPACT..IMPACT + RIPPLE_MS) {
            val p = (t - IMPACT) / RIPPLE_MS
            val alpha = (1f - p) * 0.8f
            drawCircle(
                brush = Brush.radialGradient(listOf(accent.copy(alpha = alpha), Color.Transparent), center = clock.slam, radius = baseRadius * (1f + p * 2.2f)),
                radius = baseRadius * (1f + p * 2.2f),
                center = clock.slam,
            )
            drawCircle(
                color = accent.copy(alpha = alpha),
                radius = baseRadius * (1f + p * 1.8f),
                center = clock.slam,
                style = Stroke(3.dp.toPx() * (1f - p)),
            )
        }

        // Ambient pulsating glow circles
        val breathe = 1f + 0.05f * sin((clock.wall / 1200f) * PI.toFloat())
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.28f), violet.copy(alpha = 0.12f), Color.Transparent),
                center = c,
                radius = baseRadius * 1.8f * breathe
            ),
            radius = baseRadius * 1.8f * breathe,
            center = c,
        )

        // Outer concentric cyber deck orbit rings
        val spinDeg = (clock.wall / 35f) % 360f
        rotate(spinDeg, c) {
            drawCircle(
                brush = Brush.sweepGradient(listOf(accent, violet, Color.Transparent, accent), center = c),
                radius = baseRadius * 1.25f,
                center = c,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        rotate(-spinDeg * 0.7f, c) {
            drawCircle(
                color = violet.copy(alpha = 0.45f),
                radius = baseRadius * 1.45f,
                center = c,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // GonDeck Central Hex Shield Badge
        val shieldW = baseRadius * 1.5f
        val shieldH = baseRadius * 1.6f
        val top = c.y - shieldH / 2f
        val left = c.x - shieldW / 2f

        val shieldPath = Path().apply {
            moveTo(left + shieldW * 0.22f, top)
            lineTo(left + shieldW * 0.78f, top)
            lineTo(left + shieldW, top + shieldH * 0.48f)
            lineTo(left + shieldW * 0.75f, top + shieldH)
            lineTo(left + shieldW * 0.25f, top + shieldH)
            lineTo(left, top + shieldH * 0.48f)
            close()
        }

        // 3D Shadow / Underlay
        translate(0f, 6.dp.toPx()) {
            drawPath(shieldPath, Color.Black.copy(alpha = 0.45f))
        }

        // Shield Surface
        drawPath(
            path = shieldPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A)),
                start = Offset(left, top),
                end = Offset(left + shieldW, top + shieldH)
            )
        )

        // Shield Neon Border
        drawPath(
            path = shieldPath,
            brush = Brush.linearGradient(
                colors = listOf(accent, violet),
                start = Offset(left, top),
                end = Offset(left + shieldW, top + shieldH)
            ),
            style = Stroke(width = 3.5.dp.toPx())
        )

        // Stylized Futuristic 'G' Monogram inside the shield
        val gw = shieldW * 0.55f
        val gh = shieldH * 0.55f
        val gx = c.x - gw / 2f
        val gy = c.y - gh / 2f

        val gMonogram = Path().apply {
            moveTo(gx + gw * 0.85f, gy + gh * 0.25f)
            lineTo(gx + gw * 0.35f, gy + gh * 0.25f)
            lineTo(gx + gw * 0.15f, gy + gh * 0.50f)
            lineTo(gx + gw * 0.35f, gy + gh * 0.75f)
            lineTo(gx + gw * 0.82f, gy + gh * 0.75f)
            lineTo(gx + gw * 0.82f, gy + gh * 0.50f)
            lineTo(gx + gw * 0.50f, gy + gh * 0.50f)
        }

        drawPath(
            path = gMonogram,
            color = ink,
            style = Stroke(width = shieldW * 0.10f, cap = StrokeCap.Round)
        )

        // Glowing Core Reactor Dot
        drawCircle(
            brush = Brush.radialGradient(listOf(Color.White, accent), center = Offset(gx + gw * 0.50f, gy + gh * 0.50f), radius = shieldW * 0.08f),
            radius = shieldW * 0.065f,
            center = Offset(gx + gw * 0.50f, gy + gh * 0.50f),
        )
    }
}
