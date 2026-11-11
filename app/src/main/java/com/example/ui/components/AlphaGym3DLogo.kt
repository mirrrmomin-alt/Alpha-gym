package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.AlphaRed
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyberCyan

/**
 * Multi-layered 3D extruded startup logo with animated 3D perspective rotation (rotationX, rotationY,
 * cameraDistance, depth extrusion plates, and metallic rim lighting).
 */
@Composable
fun AlphaGym3DStartupLogo(
    logoSize: Dp = 148.dp,
    modifier: Modifier = Modifier
) {
    var boost3DSpin by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "alpha_3d_startup_logo")

    val rotationY by infiniteTransition.animateFloat(
        initialValue = if (boost3DSpin) -32f else -18f,
        targetValue = if (boost3DSpin) 32f else 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (boost3DSpin) 1800 else 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_3d_rot_y"
    )

    val rotationX by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_3d_rot_x"
    )

    val floatOffsetY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_3d_float_y"
    )

    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_3d_aura"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.testTag("alpha_gym_3d_startup_logo")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(logoSize + 44.dp)
                .clickable { boost3DSpin = !boost3DSpin }
        ) {
            // 3D Floor Shadow Pedestal
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(logoSize * 0.85f)
                    .height(22.dp)
                    .graphicsLayer {
                        scaleX = auraPulse
                        alpha = 0.55f
                    }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AlphaRed.copy(alpha = 0.65f),
                                AmberGold.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Outer 3D Orbiting Metallic Ring
            Box(
                modifier = Modifier
                    .size(logoSize + 24.dp)
                    .graphicsLayer {
                        this.rotationX = rotationX * 1.3f
                        this.rotationY = -rotationY * 1.2f
                        this.cameraDistance = 14f * density
                        this.scaleX = auraPulse
                        this.scaleY = auraPulse
                    }
                    .border(
                        width = 2.dp,
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                AlphaRed,
                                AmberGold,
                                Color.White.copy(alpha = 0.85f),
                                CyberCyan,
                                AlphaRed
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // 5 Stacked 3D Extrusion Depth Plates behind the main emblem
            for (depthLayer in 5 downTo 1) {
                val shiftX = (-rotationY * 0.22f * depthLayer).dp
                val shiftY = (floatOffsetY + depthLayer * 2.2f).dp
                Box(
                    modifier = Modifier
                        .offset(x = shiftX, y = shiftY)
                        .size(logoSize)
                        .graphicsLayer {
                            this.rotationY = rotationY
                            this.rotationX = rotationX
                            this.cameraDistance = 16f * density
                        }
                        .clip(CircleShape)
                        .background(
                            if (depthLayer % 2 == 0) {
                                Color(0xFF2B120C)
                            } else {
                                Color(0xFF471A10)
                            }
                        )
                        .border(
                            width = 1.5.dp,
                            color = AmberGold.copy(alpha = 0.35f),
                            shape = CircleShape
                        )
                )
            }

            // Primary Front 3D Emblem Face
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(y = floatOffsetY.dp)
                    .size(logoSize)
                    .graphicsLayer {
                        this.rotationY = rotationY
                        this.rotationX = rotationX
                        this.cameraDistance = 16f * density
                    }
                    .shadow(
                        elevation = 24.dp,
                        shape = CircleShape,
                        ambientColor = AlphaRed,
                        spotColor = AmberGold
                    )
                    .clip(CircleShape)
                    .background(Color(0xFF14181C))
                    .border(
                        width = 3.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White,
                                AmberGold,
                                AlphaRed,
                                Color(0xFF8C96A4)
                            )
                        ),
                        shape = CircleShape
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.alpha_gym_logo),
                    contentDescription = stringResource(R.string.cd_alpha_gym_logo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Specular 3D Light Glint Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.22f),
                                    Color.Transparent,
                                    AlphaRed.copy(alpha = 0.14f)
                                )
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            color = Color(0xFF14181C).copy(alpha = 0.88f),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(listOf(AlphaRed, AmberGold, AlphaRed)),
                    shape = RoundedCornerShape(50)
                )
                .clickable { boost3DSpin = !boost3DSpin }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = AmberGold,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = if (boost3DSpin) "3D STARTUP LOGO • TURBO SPIN" else "3D STARTUP LOGO • A UNISEX GYM",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = null,
                    tint = AlphaRed,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * Full-screen 3D perspective background watermark of the Alpha Gym logo.
 */
@Composable
fun AlphaGym3DBackgroundWatermark(
    modifier: Modifier = Modifier,
    watermarkAlpha: Float = 0.11f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_3d_watermark")
    val slowTiltY by infiniteTransition.animateFloat(
        initialValue = -22f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_rot_y"
    )
    val slowTiltX by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_rot_x"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        // Depth shadow copy
        Image(
            painter = painterResource(id = R.drawable.alpha_gym_logo),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(340.dp)
                .offset(x = 10.dp, y = 14.dp)
                .alpha(watermarkAlpha * 0.6f)
                .graphicsLayer {
                    rotationY = slowTiltY
                    rotationX = slowTiltX
                    cameraDistance = 18f * density
                }
                .clip(CircleShape)
        )
        // Primary 3D background logo
        Image(
            painter = painterResource(id = R.drawable.alpha_gym_logo),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(340.dp)
                .alpha(watermarkAlpha)
                .graphicsLayer {
                    rotationY = slowTiltY
                    rotationX = slowTiltX
                    cameraDistance = 18f * density
                }
                .clip(CircleShape)
                .border(2.dp, AlphaRed.copy(alpha = watermarkAlpha * 2f), CircleShape)
        )
    }
}

/**
 * Circular 3D-rimmed Alpha Gym logo badge for Top App Bars and headers.
 */
@Composable
fun AlphaGymLogoBadge(
    size: Dp = 42.dp,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .shadow(6.dp, CircleShape, spotColor = AlphaRed)
            .clip(CircleShape)
            .background(Color(0xFF14181C))
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(listOf(Color.White, AmberGold, AlphaRed)),
                shape = CircleShape
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.alpha_gym_logo),
            contentDescription = stringResource(R.string.cd_alpha_gym_logo),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
