package com.example

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun BikeRushApp(viewModel: BikeRushViewModel) {
    val gameState by viewModel.gameState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBg
    ) {
        when (gameState) {
            GameState.MENU -> MainMenu(viewModel)
            GameState.PLAYING -> GameScreen(viewModel)
            GameState.GAME_OVER -> GameOverScreen(viewModel)
            GameState.SHOP -> ShopScreen(viewModel)
        }
    }
}

@Composable
fun MainMenu(viewModel: BikeRushViewModel) {
    val coins by viewModel.coins.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "BIKE RUSH",
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            color = NeonBlue
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Endless 3D-Style Runner", color = GrayText)
        Spacer(modifier = Modifier.height(48.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.MonetizationOn, contentDescription = "Coins", tint = CoinColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "$coins", color = LightText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = { viewModel.startGame() },
            colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(60.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = DarkBg)
            Spacer(modifier = Modifier.width(8.dp))
            Text("START RACE", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkBg)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = { viewModel.openShop() },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPurple),
            border = androidx.compose.foundation.BorderStroke(2.dp, NeonPurple),
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(60.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.ShoppingCart, contentDescription = "Shop", tint = NeonPurple)
            Spacer(modifier = Modifier.width(8.dp))
            Text("GARAGE", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NeonPurple)
        }
    }
}

@Composable
fun GameOverScreen(viewModel: BikeRushViewModel) {
    val score by viewModel.score.collectAsState()
    val coins by viewModel.coins.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "CRASHED!",
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            color = WarningColor
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "SCORE", color = GrayText, fontSize = 20.sp)
        Text(text = "$score", color = LightText, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.MonetizationOn, contentDescription = "Coins", tint = CoinColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "$coins", color = LightText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = { viewModel.startGame() },
            colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(60.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("TRY AGAIN", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkBg)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = { viewModel.goHome() }) {
            Text("MAIN MENU", color = GrayText)
        }
    }
}

@Composable
fun ShopScreen(viewModel: BikeRushViewModel) {
    val coins by viewModel.coins.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.goHome() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = LightText)
            }
            Text("GARAGE", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = LightText, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.MonetizationOn, contentDescription = "Coins", tint = CoinColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "$coins", color = LightText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        
        BIKES.forEach { bike ->
            val isUnlocked = viewModel.isUnlocked(bike.id)
            val isSelected = playerState.currentBikeIndex == bike.id
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier
                        .size(48.dp)
                        .background(Color(bike.colorHex), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.DirectionsBike, contentDescription = "Bike", tint = DarkBg)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(bike.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LightText)
                        if (!isUnlocked) {
                            Text("${bike.cost} Coins", color = CoinColor, fontSize = 14.sp)
                        } else if (isSelected) {
                            Text("Equipped", color = NeonBlue, fontSize = 14.sp)
                        } else {
                            Text("Unlocked", color = GrayText, fontSize = 14.sp)
                        }
                    }
                    
                    Button(
                        onClick = { viewModel.buyOrSelectBike(bike.id) },
                        enabled = isUnlocked || coins >= bike.cost,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) DarkBg else if (isUnlocked) NeonBlue else NeonPurple,
                            contentColor = if (isSelected) NeonBlue else DarkBg
                        )
                    ) {
                        Text(if (isSelected) "EQUIPPED" else if (isUnlocked) "SELECT" else "BUY")
                    }
                }
            }
        }
    }
}

@Composable
fun GameScreen(viewModel: BikeRushViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val entities by viewModel.entities.collectAsState()
    val score by viewModel.score.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var dragStart = Offset.Zero
                detectDragGestures(
                    onDragStart = { dragStart = it },
                    onDragEnd = { dragStart = Offset.Zero },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val diffX = change.position.x - dragStart.x
                        val diffY = change.position.y - dragStart.y
                        if (abs(diffX) > abs(diffY)) {
                            // Horizontal Swipe
                            if (abs(diffX) > 50f) {
                                if (diffX > 0) viewModel.swipeRight() else viewModel.swipeLeft()
                                dragStart = change.position // reset to prevent multiple triggers
                            }
                        } else {
                            // Vertical Swipe
                            if (abs(diffY) > 50f) {
                                if (diffY > 0) viewModel.swipeDown() else viewModel.swipeUp()
                                dragStart = change.position
                            }
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val horizonY = height * 0.2f
            
            // Draw Sky and Ground (HDR Sky Gradient)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFFB3E5FC)),
                    startY = 0f,
                    endY = horizonY
                ),
                size = Size(width, horizonY)
            )
            
            // Draw Sun with Bloom
            val sunCenter = Offset(width * 0.8f, horizonY * 0.5f)
            // Outer soft glow (Bloom)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFEB3B).copy(alpha = 0.6f), Color.Transparent),
                    center = sunCenter,
                    radius = 150f
                ),
                radius = 150f,
                center = sunCenter
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color(0xFFFFEB3B)),
                    center = sunCenter,
                    radius = 50f
                ),
                radius = 50f,
                center = sunCenter
            )
            
            // Draw Grass Ground (Shaded)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF388E3C), Color(0xFF1B5E20)),
                    startY = horizonY,
                    endY = height
                ),
                topLeft = Offset(0f, horizonY),
                size = Size(width, height - horizonY)
            )
            
            // Draw Lanes (Pseudo 3D)
            val laneWidthBottom = width / 3f
            val laneWidthTop = width * 0.1f // Width at horizon
            
            // Draw Road Base
            val roadPath = Path().apply {
                moveTo(width / 2f - laneWidthTop * 1.5f, horizonY)
                lineTo(width / 2f + laneWidthTop * 1.5f, horizonY)
                lineTo(width / 2f + laneWidthBottom * 1.5f, height)
                lineTo(width / 2f - laneWidthBottom * 1.5f, height)
                close()
            }
            drawPath(roadPath, color = Color(0xFF606060))
            
            val drawLaneLine = { laneBoundary: Float, isEdge: Boolean ->
                val startXTop = (width / 2f) + (laneBoundary * laneWidthTop)
                val startXBottom = (width / 2f) + (laneBoundary * laneWidthBottom)
                drawLine(
                    color = if (isEdge) Color.White else Color.White.copy(alpha = 0.5f),
                    start = Offset(startXTop, horizonY),
                    end = Offset(startXBottom, height),
                    strokeWidth = if (isEdge) 8f else 4f
                )
            }
            
            drawLaneLine(-1.5f, true) // Left Edge
            drawLaneLine(-0.5f, false) // Left-Center Divider
            drawLaneLine(0.5f, false)  // Center-Right Divider
            drawLaneLine(1.5f, true)  // Right Edge

            
            // Helper to project 3D coords to 2D Screen
            // z: 1.0 (Horizon) to 0.0 (Bottom)
            fun projectZToY(z: Float): Float {
                // simple linear mapping for now, but a perspective mapping is better:
                // y = horizonY + (1 - z) * (height - horizonY)
                val normalizedZ = 1f - z 
                // exponential curve for depth feeling
                val depthCurve = normalizedZ * normalizedZ
                return horizonY + depthCurve * (height - horizonY)
            }
            
            fun projectScale(z: Float): Float {
                val normalizedZ = 1f - z
                return 0.1f + normalizedZ * 0.9f
            }
            
            fun projectX(lane: Int, z: Float): Float {
                val pY = projectZToY(z)
                // interpolate lane width based on Y
                val progress = (pY - horizonY) / (height - horizonY)
                val currentLaneWidth = laneWidthTop + progress * (laneWidthBottom - laneWidthTop)
                return (width / 2f) + (lane * currentLaneWidth)
            }
            
            // Draw Entities (sorted by Z for proper depth overlap)
            val sortedEntities = entities.sortedByDescending { it.z }
            
            fun draw3DBox(ex: Float, ey: Float, eScale: Float, w: Float, h: Float, depth: Float, color: Color, yOffset: Float = 0f) {
                val pw = w * eScale
                val ph = h * eScale
                val pd = depth * eScale
                val py = ey - yOffset * eScale
                
                // Soft Shadow
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent),
                        center = Offset(ex, ey),
                        radius = (pw * 0.8f).coerceAtLeast(1f)
                    ),
                    topLeft = Offset(ex - pw*0.8f, ey - pd*0.5f),
                    size = Size(pw*1.6f, pd)
                )
                
                // Back/Side depth (simple perspective trick towards center)
                val isLeft = ex < width / 2
                val sideOffset = if (isLeft) pd * 0.5f else -pd * 0.5f
                
                // Top face (Lightest, light from above)
                val topPath = Path().apply {
                    moveTo(ex - pw/2, py - ph)
                    lineTo(ex + pw/2, py - ph)
                    lineTo(ex + pw/2 + sideOffset, py - ph - pd * 0.3f)
                    lineTo(ex - pw/2 + sideOffset, py - ph - pd * 0.3f)
                    close()
                }
                drawPath(
                    path = topPath,
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent),
                        start = Offset(ex, py - ph - pd * 0.3f),
                        end = Offset(ex, py - ph)
                    )
                )
                drawPath(topPath, color = color)
                
                // Side face (Darker, shadow)
                val sidePath = Path().apply {
                    if (isLeft) {
                        moveTo(ex + pw/2, py - ph)
                        lineTo(ex + pw/2 + sideOffset, py - ph - pd * 0.3f)
                        lineTo(ex + pw/2 + sideOffset, py - pd * 0.3f)
                        lineTo(ex + pw/2, py)
                    } else {
                        moveTo(ex - pw/2, py - ph)
                        lineTo(ex - pw/2 + sideOffset, py - ph - pd * 0.3f)
                        lineTo(ex - pw/2 + sideOffset, py - pd * 0.3f)
                        lineTo(ex - pw/2, py)
                    }
                    close()
                }
                drawPath(sidePath, color = color)
                drawPath(
                    path = sidePath,
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.6f)),
                        start = Offset(ex, py - ph),
                        end = Offset(ex, py)
                    )
                )
                
                // Front face (Slightly shaded gradient)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(color, Color.Black.copy(alpha = 0.3f)),
                        startY = py - ph,
                        endY = py
                    ),
                    topLeft = Offset(ex - pw/2, py - ph), size = Size(pw, ph)
                )
                drawRect(color = color.copy(alpha = 0.8f), topLeft = Offset(ex - pw/2, py - ph), size = Size(pw, ph))
            }
            
            sortedEntities.forEach { entity ->
                val ex = projectX(entity.lane, entity.z)
                val ey = projectZToY(entity.z)
                val eScale = projectScale(entity.z)
                
                when (entity.type) {
                    EntityType.COIN -> {
                        // Soft Shadow
                        drawOval(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent),
                                center = Offset(ex, ey),
                                radius = (25f * eScale).coerceAtLeast(1f)
                            ),
                            topLeft = Offset(ex - 25f * eScale, ey - 10f * eScale),
                            size = Size(50f * eScale, 20f * eScale)
                        )
                        // Coin body with bloom
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFD700), Color(0xFFFF8F00)),
                                center = Offset(ex, ey - 20f * eScale),
                                radius = (22f * eScale).coerceAtLeast(1f)
                            ),
                            radius = 22f * eScale, center = Offset(ex, ey - 20f * eScale)
                        )
                        // Inner ring
                        drawCircle(color = Color(0xFFFBC02D), radius = 16f * eScale, center = Offset(ex, ey - 20f * eScale))
                        // Highlight glow
                        drawCircle(color = Color.White.copy(alpha = 0.6f), radius = 8f * eScale, center = Offset(ex - 6f * eScale, ey - 26f * eScale))
                    }
                    EntityType.BARRIER_HIGH -> { // e.g. floating barrier, requires slide
                        // floating box
                        draw3DBox(ex, ey, eScale, 80f, 30f, 30f, WarningColor, yOffset = 70f)
                        // poles
                        drawRect(color = Color.DarkGray, topLeft = Offset(ex - 35f * eScale, ey - 70f * eScale), size = Size(10f * eScale, 70f * eScale))
                        drawRect(color = Color.DarkGray, topLeft = Offset(ex + 25f * eScale, ey - 70f * eScale), size = Size(10f * eScale, 70f * eScale))
                    }
                    EntityType.BARRIER_LOW -> { // e.g. ground barrier, requires jump
                        draw3DBox(ex, ey, eScale, 80f, 40f, 40f, Color(0xFFFF9800))
                    }
                    EntityType.CAR -> { 
                        draw3DBox(ex, ey, eScale, 80f, 50f, 100f, Color.Blue)
                        // Windshield
                        drawRect(color = Color.Cyan.copy(alpha=0.8f), topLeft = Offset(ex - 30f*eScale, ey - 45f*eScale), size = Size(60f*eScale, 20f*eScale))
                        // Headlights
                        drawCircle(color = Color.Yellow, radius = 8f*eScale, center = Offset(ex - 25f*eScale, ey - 15f*eScale))
                        drawCircle(color = Color.Yellow, radius = 8f*eScale, center = Offset(ex + 25f*eScale, ey - 15f*eScale))
                    }
                    EntityType.BUS -> { 
                        draw3DBox(ex, ey, eScale, 90f, 100f, 150f, Color(0xFFFFC107))
                        // Windshield
                        drawRect(color = Color.Cyan.copy(alpha=0.8f), topLeft = Offset(ex - 35f*eScale, ey - 90f*eScale), size = Size(70f*eScale, 40f*eScale))
                        // Headlights
                        drawCircle(color = Color.Yellow, radius = 10f*eScale, center = Offset(ex - 30f*eScale, ey - 20f*eScale))
                        drawCircle(color = Color.Yellow, radius = 10f*eScale, center = Offset(ex + 30f*eScale, ey - 20f*eScale))
                    }
                    EntityType.TRUCK -> { 
                        draw3DBox(ex, ey, eScale, 90f, 120f, 200f, Color.Red)
                        // Windshield
                        drawRect(color = Color.Cyan.copy(alpha=0.8f), topLeft = Offset(ex - 35f*eScale, ey - 110f*eScale), size = Size(70f*eScale, 30f*eScale))
                        // Grille
                        drawRect(color = Color.Gray, topLeft = Offset(ex - 20f*eScale, ey - 50f*eScale), size = Size(40f*eScale, 30f*eScale))
                        // Headlights
                        drawCircle(color = Color.White, radius = 10f*eScale, center = Offset(ex - 35f*eScale, ey - 20f*eScale))
                        drawCircle(color = Color.White, radius = 10f*eScale, center = Offset(ex + 35f*eScale, ey - 20f*eScale))
                    }
                    EntityType.ONCOMING_BIKE -> { 
                        draw3DBox(ex, ey, eScale, 40f, 60f, 80f, Color.Green)
                        // Headlight
                        drawCircle(color = Color.Yellow, radius = 12f*eScale, center = Offset(ex, ey - 30f*eScale))
                    }
                }
            }
            
            // Draw Player (3D-like Bike)
            val playerZ = 0.1f
            val px = projectX(playerState.lane, playerZ)
            val py = projectZToY(playerZ)
            val pScale = projectScale(playerZ)
            
            val bikeColor = Color(BIKES[playerState.currentBikeIndex].colorHex)
            
            var pHeight = 80f * pScale
            var yOffset = 0f
            
            if (playerState.isJumping) {
                // Sine wave jump
                val jumpArc = sin(playerState.actionProgress * Math.PI).toFloat()
                yOffset = -jumpArc * 150f * pScale
            }
            
            if (playerState.isSliding) {
                pHeight = 40f * pScale // flatten
            }
            
            // Bike Shadow
            drawOval(color = Color.Black.copy(alpha = 0.4f), topLeft = Offset(px - 30f * pScale, py - 10f * pScale), size = Size(60f * pScale, 20f * pScale))
            
            // Wheels
            drawCircle(color = Color.DarkGray, radius = 25f * pScale, center = Offset(px, py - 25f * pScale + yOffset))
            drawCircle(color = Color.LightGray, radius = 15f * pScale, center = Offset(px, py - 25f * pScale + yOffset))
            
            // Bike Frame (simplified body)
            val framePath = Path().apply {
                moveTo(px - 20f * pScale, py - 25f * pScale + yOffset)
                lineTo(px + 20f * pScale, py - 25f * pScale + yOffset)
                lineTo(px + 10f * pScale, py - pHeight + yOffset)
                lineTo(px - 10f * pScale, py - pHeight + yOffset)
                close()
            }
            drawPath(framePath, color = bikeColor)
            
            // Rider (simplified)
            if (!playerState.isSliding) {
                drawCircle(color = NeonBlue, radius = 20f * pScale, center = Offset(px, py - pHeight - 20f * pScale + yOffset)) // Head
                drawRect(color = NeonPurple, topLeft = Offset(px - 15f * pScale, py - pHeight + yOffset), size = Size(30f * pScale, 30f * pScale)) // Body
            } else {
                drawCircle(color = NeonBlue, radius = 15f * pScale, center = Offset(px + 20f * pScale, py - pHeight - 10f * pScale + yOffset)) // Head sliding
                drawRect(color = NeonPurple, topLeft = Offset(px - 25f * pScale, py - pHeight + yOffset), size = Size(50f * pScale, 20f * pScale)) // Body sliding
            }
        }
        
        // HUD
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SCORE", color = GrayText, fontSize = 14.sp)
                    Text("$score", color = LightText, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.MonetizationOn, contentDescription = "Coins", tint = CoinColor)
                    Spacer(modifier = Modifier.width(4.dp))
                    val coins by viewModel.coins.collectAsState()
                    Text("$coins", color = LightText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brake Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.Red.copy(alpha = 0.5f), RoundedCornerShape(40.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { viewModel.setBraking(true) },
                                onDragEnd = { viewModel.setBraking(false) },
                                onDragCancel = { viewModel.setBraking(false) },
                                onDrag = { change, _ -> change.consume() }
                            )
                        }
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (event.changes.any { it.pressed }) {
                                        viewModel.setBraking(true)
                                    } else {
                                        viewModel.setBraking(false)
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("BRAKE", color = Color.White, fontWeight = FontWeight.Bold)
                }

                // Race Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.Green.copy(alpha = 0.5f), RoundedCornerShape(40.dp))
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (event.changes.any { it.pressed }) {
                                        viewModel.setAccelerating(true)
                                    } else {
                                        viewModel.setAccelerating(false)
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("RACE", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
