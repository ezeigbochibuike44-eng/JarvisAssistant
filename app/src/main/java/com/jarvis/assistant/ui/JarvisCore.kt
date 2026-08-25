package com.jarvis.assistant.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.ai.AssistantState
import com.jarvis.assistant.ui.theme.JarvisAmber
import com.jarvis.assistant.ui.theme.JarvisCyan
import com.jarvis.assistant.ui.theme.JarvisCyanDim
import com.jarvis.assistant.ui.theme.JarvisError

@Composable
fun JarvisCore(state: AssistantState, sizeDp: Int = 220, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "jarvis-core")

    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == AssistantState.THINKING) 1200 else 6000, easing = LinearEasing),
        ),
        label = "rotation",
    )

    val pulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == AssistantState.LISTENING) 500 else 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    val accentColor = when (state) {
        AssistantState.ERROR -> JarvisError
        AssistantState.LISTENING, AssistantState.RESPONDING -> JarvisCyan
        AssistantState.THINKING, AssistantState.EXECUTING -> JarvisAmber
        AssistantState.IDLE -> JarvisCyanDim
    }

    Canvas(modifier = modifier.size(sizeDp.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = size.minDimension / 2f * 0.92f
        val coreRadius = size.minDimension / 2f * 0.4f * pulse

        // Outer ring
        drawCircle(color = JarvisCyanDim.copy(alpha = 0.4f), radius = outerRadius, center = center, style = Stroke(width = 2f))

        // Rotating segmented arc
        rotate(degrees = rotation) {
            drawArc(
                color = accentColor,
                startAngle = 0f,
                sweepAngle = 120f,
                useCenter = false,
                style = Stroke(width = 6f, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                topLeft = Offset(center.x - outerRadius * 0.8f, center.y - outerRadius * 0.8f),
                size = androidx.compose.ui.geometry.Size(outerRadius * 1.6f, outerRadius * 1.6f),
            )
        }

        // Glowing core
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accentColor.copy(alpha = 0.9f), accentColor.copy(alpha = 0.05f)),
                center = center,
                radius = coreRadius * 1.6f,
            ),
            radius = coreRadius * 1.6f,
            center = center,
        )
        drawCircle(color = accentColor, radius = coreRadius * 0.35f, center = center)
    }
}
