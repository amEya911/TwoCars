package eu.tutorials.twocars.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F1B)) // Deep dark space background
    ) {
        // Dynamic "Mesh" Background
        DynamicMeshBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Top Progress Bar
            OnboardingProgressBar(
                currentPage = pagerState.currentPage,
                totalPages = 2
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Premium Title
            Text(
                text = "Formula Two Cars",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (pagerState.currentPage == 0) "MASTER THE TRACK" else "LEVEL UP YOUR GAME",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF1E00).copy(alpha = 0.8f),
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Content Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> SlideHowToPlay()
                    1 -> SlideModesAndPowerUps()
                }
            }

            // Glassmorphic Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 32.dp)
            ) {
                val isLastPage = pagerState.currentPage == 1
                
                Button(
                    onClick = {
                        if (isLastPage) {
                            onFinished()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .graphicsLayer {
                            shadowElevation = 8.dp.toPx()
                            shape = RoundedCornerShape(20.dp)
                            clip = true
                        },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLastPage) Color(0xFFFF1E00) else Color.White.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = if (!isLastPage) BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)) else null
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isLastPage) "START RACING" else "LEARN MORE",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        if (!isLastPage) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("→", fontSize = 20.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DynamicMeshBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    
    val animOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Reverse),
        label = "offset1"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Purple glow
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-100).dp, y = 100.dp)
                .blur(100.dp)
                .background(Color(0xFF6D28D9).copy(alpha = 0.15f), CircleShape)
        )
        // Red glow
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 50.dp, y = 50.dp)
                .blur(80.dp)
                .background(Color(0xFFFF1E00).copy(alpha = 0.1f), CircleShape)
        )
    }
}

@Composable
private fun OnboardingProgressBar(currentPage: Int, totalPages: Int) {
    Row(
        modifier = Modifier
            .padding(horizontal = 48.dp)
            .fillMaxWidth()
            .height(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalPages) { index ->
            val isSelected = index <= currentPage
            val width by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0f,
                animationSpec = tween(500),
                label = "progress"
            )
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(width)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFF1E00), Color(0xFFFFD700))
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun SlideHowToPlay() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        // Main Control Card
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FloatingEmoji(emoji = "🏎️", size = 40.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Split Decision",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Control two cars simultaneously. Tap left for the red car, right for the blue car.",
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 22.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MiniGlassCard(
                emoji = "🔴",
                title = "COLLECT",
                description = "Hit every circle.",
                accentColor = Color(0xFFFF1E00),
                modifier = Modifier.weight(1f)
            )
            MiniGlassCard(
                emoji = "🟦",
                title = "DODGE",
                description = "Avoid all squares.",
                accentColor = Color(0xFF00D2FF),
                modifier = Modifier.weight(1f)
            )
        }

        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFFFD700).copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔥", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Combo Multiplier",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Chain collections to boost your score up to 5x!",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SlideModesAndPowerUps() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "EQUIPMENT & MODES",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.5f),
            letterSpacing = 2.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModeCard(
                emoji = "♾️",
                title = "Endless",
                modifier = Modifier.weight(1f)
            )
            ModeCard(
                emoji = "⏱️",
                title = "Timed",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Power-ups list with glassmorphism
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(8.dp)) {
                PowerUpItem("🛡️", "Shield", "Survive one collision", Color(0xFF4FC3F7))
                PowerUpItem("🧲", "Magnet", "Auto-pulls circles", Color(0xFFFFD700))
                PowerUpItem("🐢", "Slow-Mo", "Temporal shift", Color(0xFFA5D6A7))
                PowerUpItem("💰", "Bonus", "Double points active", Color(0xFFFFD54F))
            }
        }

        // F1 Team Unlock Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF1E1E2C), Color(0xFF2D2D44))
                    )
                )
                .border(
                    BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🏁", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Collect points to unlock 11 official F1-inspired team liveries!",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        content()
    }
}

@Composable
private fun MiniGlassCard(
    emoji: String,
    title: String,
    description: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ModeCard(emoji: String, title: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun PowerUpItem(emoji: String, name: String, desc: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(desc, color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun FloatingEmoji(emoji: String, size: androidx.compose.ui.unit.TextUnit) {
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val yOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "yOffset"
    )
    
    Text(
        text = emoji,
        fontSize = size,
        modifier = Modifier.offset(y = yOffset.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview(modifier: Modifier = Modifier) {
    OnboardingScreen {  }
}
