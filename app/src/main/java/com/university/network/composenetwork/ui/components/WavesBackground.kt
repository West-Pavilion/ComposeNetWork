package com.university.network.composenetwork.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.sin

@Composable
fun WavesBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "waves")
    val animationProgress1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )

    val animationProgress2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 绘制第一条波浪
            drawWave(
                color = Color(0x080061A4),
                animationProgress = animationProgress1,
                waveAmplitude = height * 0.1f,
                waveFrequency = 1.5f,
                height = height * 0.8f
            )

            // 绘制第二条波浪
            drawWave(
                color = Color(0x10D1E4FF),
                animationProgress = animationProgress2,
                waveAmplitude = height * 0.08f,
                waveFrequency = 2f,
                height = height * 0.85f
            )
        }

        content()
    }
}

private fun DrawScope.drawWave(
    color: Color,
    animationProgress: Float,
    waveAmplitude: Float,
    waveFrequency: Float,
    height: Float
) {
    val width = size.width
    val path = Path()

    path.moveTo(0f, height)

    // 波浪路径
    val stepSize = 5f
    for (x in 0..width.toInt() step stepSize.toInt()) {
        val dx = x.toFloat()
        val dy = height - waveAmplitude * sin((dx / width * 2 * Math.PI * waveFrequency + animationProgress * 2 * Math.PI).toFloat())
        path.lineTo(dx, dy.toFloat())
    }

    // 完成路径，形成闭合区域
    path.lineTo(width, height)
    path.lineTo(width, size.height)
    path.lineTo(0f, size.height)
    path.close()

    drawPath(path, color)
}