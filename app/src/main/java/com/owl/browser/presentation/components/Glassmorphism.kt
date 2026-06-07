package com.owl.browser.presentation.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun GlassBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    opacity: Float = 0.6f,
    blurRadius: Float = 16f,
    neonBorderColor: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFF1E2538).copy(alpha = opacity))
            .let { m ->
                if (neonBorderColor != null) {
                    m.border(1.dp, neonBorderColor, shape)
                } else {
                    m.border(1.dp, Color.White.copy(alpha = 0.1f), shape)
                }
            }
            .let { m ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0f) {
                    m.graphicsLayer {
                        renderEffect = android.graphics.RenderEffect.createBlurEffect(
                            blurRadius,
                            blurRadius,
                            android.graphics.Shader.TileMode.DECAL
                        ).asComposeRenderEffect()
                    }
                } else {
                    m
                }
            },
        content = content
    )
}
