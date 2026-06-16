package com.miko.reader.ui.components

import android.graphics.Matrix
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.*
import kotlin.math.*

private data class PointNRound(
    val o: Offset,
    val r: CornerRounding = CornerRounding.Unrounded,
)

private fun Offset.rotateDegrees(angle: Float, center: Offset = Offset.Zero): Offset {
    val a = angle / 180f * PI.toFloat()
    val off = this - center
    return Offset(off.x * cos(a) - off.y * sin(a), off.x * sin(a) + off.y * cos(a)) + center
}

private fun Offset.angleDegrees() = atan2(y, x) * 180f / PI.toFloat()

private fun doRepeat(
    points: List<PointNRound>,
    reps: Int,
    center: Offset,
    mirroring: Boolean,
): List<PointNRound> {
    return if (mirroring) {
        buildList {
            val angles = points.map { (it.o - center).angleDegrees() }
            val distances = points.map { (it.o - center).getDistance() }
            val actualReps = reps * 2
            val sectionAngle = 360f / actualReps
            repeat(actualReps) { rep ->
                points.indices.forEach { index ->
                    val i = if (rep % 2 == 0) index else points.lastIndex - index
                    if (i > 0 || rep % 2 == 0) {
                        val a = (sectionAngle * rep +
                                if (rep % 2 == 0) angles[i]
                                else sectionAngle - angles[i] + 2 * angles[0]) / 180f * PI.toFloat()
                        val finalPoint = Offset(cos(a), sin(a)) * distances[i] + center
                        add(PointNRound(finalPoint, points[i].r))
                    }
                }
            }
        }
    } else {
        val np = points.size
        (0 until np * reps).map { idx ->
            val point = points[idx % np].o.rotateDegrees((idx / np) * 360f / reps, center)
            PointNRound(point, points[idx % np].r)
        }
    }
}

private fun customPolygon(
    pnr: List<PointNRound>,
    reps: Int,
    center: Offset = Offset(0.5f, 0.5f),
    mirroring: Boolean = false,
): RoundedPolygon {
    val actualPoints = doRepeat(pnr, reps, center, mirroring)
    return RoundedPolygon(
        vertices = FloatArray(actualPoints.size * 2) { ix ->
            actualPoints[ix / 2].o.let { if (ix % 2 == 0) it.x else it.y }
        },
        perVertexRounding = actualPoints.map { it.r },
        centerX = center.x,
        centerY = center.y,
    )
}

private fun oval(): RoundedPolygon {
    val m = Matrix().apply { setScale(1f, 0.64f) }
    m.postRotate(-45f)
    return RoundedPolygon.circle().transformed(m)
}

private fun pill(): RoundedPolygon {
    return customPolygon(
        listOf(
            PointNRound(Offset(0.961f, 0.039f), CornerRounding(0.426f)),
            PointNRound(Offset(1.001f, 0.428f)),
            PointNRound(Offset(1.000f, 0.609f), CornerRounding(1.000f)),
        ),
        reps = 2,
        mirroring = true,
    )
}

private fun pentagon(): RoundedPolygon {
    return customPolygon(
        listOf(
            PointNRound(Offset(0.500f, -0.009f), CornerRounding(0.172f)),
            PointNRound(Offset(1.030f, 0.365f), CornerRounding(0.164f)),
            PointNRound(Offset(0.828f, 0.970f), CornerRounding(0.169f)),
        ),
        reps = 1,
        mirroring = true,
    )
}

private fun sunny(): RoundedPolygon {
    return RoundedPolygon.star(
        numVerticesPerRadius = 8,
        innerRadius = 0.8f,
        rounding = CornerRounding(radius = 0.15f)
    )
}

private fun cookie4(): RoundedPolygon {
    return customPolygon(
        listOf(
            PointNRound(Offset(1.237f, 1.236f), CornerRounding(0.258f)),
            PointNRound(Offset(0.500f, 0.918f), CornerRounding(0.233f)),
        ),
        reps = 4,
    )
}

private fun cookie9(): RoundedPolygon {
    val m = Matrix().apply { postRotate(-90f) }
    return RoundedPolygon.star(
        numVerticesPerRadius = 9,
        innerRadius = 0.8f,
        rounding = CornerRounding(radius = 0.5f)
    ).transformed(m)
}

private fun softBurst(): RoundedPolygon {
    return customPolygon(
        listOf(
            PointNRound(Offset(0.193f, 0.277f), CornerRounding(0.053f)),
            PointNRound(Offset(0.176f, 0.055f), CornerRounding(0.053f)),
        ),
        reps = 10,
    )
}

@Composable
fun MikoLoadingScreen(message: String = "Fetching Manga...") {
    val shapes = remember {
        listOf(
            softBurst(),
            cookie9(),
            pentagon(),
            pill(),
            sunny(),
            cookie4(),
            oval()
        )
    }

    val morphs = remember(shapes) {
        listOf(
            Morph(shapes[0], shapes[1]),
            Morph(shapes[1], shapes[2]),
            Morph(shapes[2], shapes[3]),
            Morph(shapes[3], shapes[4]),
            Morph(shapes[4], shapes[5]),
            Morph(shapes[5], shapes[6]),
            Morph(shapes[6], shapes[0])
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "loading_morph")

    val morphProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "morph_progress"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val currentMorphIndex = morphProgress.toInt().coerceIn(0, 6)
    val progressInMorph = morphProgress - currentMorphIndex
    val activeMorph = morphs[currentMorphIndex]

    val indicatorColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(72.dp)
            ) {
                val rawPath = activeMorph.toPath(progress = progressInMorph).asComposePath()
                val bounds = rawPath.getBounds()
                
                // Avoid division by zero
                val w = if (bounds.width > 0) bounds.width else 1f
                val h = if (bounds.height > 0) bounds.height else 1f
                
                val scaleX = size.width / w * 0.72f * pulseScale
                val scaleY = size.height / h * 0.72f * pulseScale
                val scale = min(scaleX, scaleY)

                val matrix = androidx.compose.ui.graphics.Matrix()
                matrix.translate(size.width / 2f, size.height / 2f)
                matrix.scale(scale, scale)
                matrix.translate(-bounds.left - bounds.width / 2f, -bounds.top - bounds.height / 2f)

                rawPath.transform(matrix)

                rotate(rotation) {
                    drawPath(path = rawPath, color = indicatorColor)
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
        }
    }
}
