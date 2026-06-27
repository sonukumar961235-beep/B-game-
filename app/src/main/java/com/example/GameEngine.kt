package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class GameState { MENU, PLAYING, GAME_OVER, SHOP }

data class PlayerState(
    val lane: Int = 0, // -1 (Left), 0 (Center), 1 (Right)
    val isJumping: Boolean = false,
    val isSliding: Boolean = false,
    val actionProgress: Float = 0f, // 0f to 1f for animation
    val currentBikeIndex: Int = 0,
    val isAccelerating: Boolean = false,
    val isBraking: Boolean = false
)

enum class EntityType { COIN, BARRIER_HIGH, BARRIER_LOW, CAR, BUS, TRUCK, ONCOMING_BIKE }

data class Entity(
    val id: Int,
    val type: EntityType,
    val lane: Int,
    var z: Float, // 1.0 (Horizon) -> 0.0 (Camera/Bottom)
    val speedMultiplier: Float = 1.0f
)

data class ShopItem(val id: Int, val name: String, val cost: Int, val colorHex: Long)

class TrafficManager {
    private var trafficTypes = mutableListOf(EntityType.CAR, EntityType.BUS, EntityType.TRUCK, EntityType.ONCOMING_BIKE)
    private var index = 0

    init {
        trafficTypes.shuffle()
    }

    fun getNextOncomingTraffic(): Pair<EntityType, Float> {
        val type = trafficTypes[index]
        index = (index + 1) % trafficTypes.size
        if (index == 0) trafficTypes.shuffle()
        
        // Oncoming traffic approaches faster (speed multiplier > 1.0)
        val speedMultiplier = when (type) {
            EntityType.CAR -> 1.6f
            EntityType.BUS -> 1.4f
            EntityType.TRUCK -> 1.3f
            EntityType.ONCOMING_BIKE -> 1.8f
            else -> 1.0f
        }
        return Pair(type, speedMultiplier)
    }
}

val BIKES = listOf(
    ShopItem(0, "Starter Bike", 0, 0xFF00E5FFL),
    ShopItem(1, "Pro BMX", 100, 0xFFD500F9L),
    ShopItem(2, "Neon Rider", 250, 0xFF00FF00L),
    ShopItem(3, "Golden Glider", 500, 0xFFFFD700L)
)

class BikeRushViewModel : ViewModel() {
    private val _gameState = MutableStateFlow(GameState.MENU)
    val gameState = _gameState.asStateFlow()

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState = _playerState.asStateFlow()

    private val _entities = MutableStateFlow<List<Entity>>(emptyList())
    val entities = _entities.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score = _score.asStateFlow()

    private val _coins = MutableStateFlow(0)
    val coins = _coins.asStateFlow()
    
    private val _speed = MutableStateFlow(0.015f) // Z units per frame

    private var gameLoopJob: Job? = null
    private var entityIdCounter = 0
    private var framesSinceLastSpawn = 0

    // Unlock states
    private val unlockedBikes = mutableSetOf(0)

    private var trafficManager = TrafficManager()

    fun startGame() {
        _score.value = 0
        _entities.value = emptyList()
        _playerState.update { it.copy(lane = 0, isJumping = false, isSliding = false) }
        _speed.value = 0.012f
        _gameState.value = GameState.PLAYING
        
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (_gameState.value == GameState.PLAYING) {
                updateGameLogic()
                delay(16) // ~60 FPS
            }
        }
    }

    private fun updateGameLogic() {
        val baseSpeed = _speed.value
        val currentSpeed = when {
            _playerState.value.isBraking -> baseSpeed * 0.5f
            _playerState.value.isAccelerating -> baseSpeed * 1.5f
            else -> baseSpeed
        }
        
        // Update Action Progress
        _playerState.update {
            if (it.isJumping || it.isSliding) {
                val newProgress = it.actionProgress + 0.03f
                if (newProgress >= 1f) {
                    it.copy(isJumping = false, isSliding = false, actionProgress = 0f)
                } else {
                    it.copy(actionProgress = newProgress)
                }
            } else {
                it
            }
        }

        // Move entities and check collisions
        val playerZ = 0.1f
        val playerLane = _playerState.value.lane
        val isJumping = _playerState.value.isJumping
        val isSliding = _playerState.value.isSliding

        var collisionOccurred = false
        var coinsCollected = 0

        val remainingEntities = _entities.value.mapNotNull { entity ->
            val newZ = entity.z - (currentSpeed * entity.speedMultiplier)
            if (newZ < 0f) return@mapNotNull null // Entity passed camera

            // Collision check (Z window 0.05 to 0.15)
            if (newZ in 0.05f..0.15f && entity.lane == playerLane) {
                when (entity.type) {
                    EntityType.COIN -> {
                        if (!isJumping) coinsCollected++ // Assuming coins are on ground
                        return@mapNotNull null // Collect coin
                    }
                    EntityType.BARRIER_HIGH -> {
                        if (!isSliding) collisionOccurred = true
                    }
                    EntityType.BARRIER_LOW -> {
                        if (!isJumping) collisionOccurred = true
                    }
                    EntityType.CAR, EntityType.BUS, EntityType.TRUCK, EntityType.ONCOMING_BIKE -> {
                        collisionOccurred = true // Must dodge
                    }
                }
            }
            entity.copy(z = newZ)
        }

        if (collisionOccurred) {
            _gameState.value = GameState.GAME_OVER
            return
        }

        if (coinsCollected > 0) {
            _coins.value += coinsCollected
        }

        _score.value += if (_playerState.value.isAccelerating) 2 else 1
        
        // Speed up slightly
        if (_score.value % 500 == 0) {
            _speed.value += 0.001f
        }

        // Spawn logic
        framesSinceLastSpawn++
        val spawnThreshold = (0.5f / currentSpeed).toInt() // Spawn rate based on speed
        if (framesSinceLastSpawn > spawnThreshold) {
            if (Random.nextFloat() < 0.6f) { // 60% chance to spawn something
                val lane = Random.nextInt(-1, 2)
                
                var spawnType = EntityType.COIN
                var speedMultiplier = 1.0f
                
                if (Random.nextFloat() >= 0.3f) {
                    val r = Random.nextFloat()
                    if (r < 0.4f) {
                        spawnType = if (Random.nextBoolean()) EntityType.BARRIER_HIGH else EntityType.BARRIER_LOW
                    } else {
                        val traffic = trafficManager.getNextOncomingTraffic()
                        spawnType = traffic.first
                        speedMultiplier = traffic.second
                    }
                }
                
                val newEntity = Entity(entityIdCounter++, spawnType, lane, 1.0f, speedMultiplier)
                _entities.value = remainingEntities + newEntity
            } else {
                _entities.value = remainingEntities
            }
            framesSinceLastSpawn = 0
        } else {
            _entities.value = remainingEntities
        }
    }

    // Input handling
    fun swipeLeft() {
        if (_gameState.value != GameState.PLAYING) return
        _playerState.update {
            if (it.lane > -1) it.copy(lane = it.lane - 1) else it
        }
    }

    fun swipeRight() {
        if (_gameState.value != GameState.PLAYING) return
        _playerState.update {
            if (it.lane < 1) it.copy(lane = it.lane + 1) else it
        }
    }

    fun swipeUp() {
        if (_gameState.value != GameState.PLAYING) return
        _playerState.update {
            if (!it.isJumping && !it.isSliding) it.copy(isJumping = true, actionProgress = 0f) else it
        }
    }

    fun swipeDown() {
        if (_gameState.value != GameState.PLAYING) return
        _playerState.update {
            if (!it.isJumping && !it.isSliding) it.copy(isSliding = true, actionProgress = 0f) else it
        }
    }
    
    fun setAccelerating(isAccelerating: Boolean) {
        if (_gameState.value != GameState.PLAYING) return
        _playerState.update { it.copy(isAccelerating = isAccelerating) }
    }

    fun setBraking(isBraking: Boolean) {
        if (_gameState.value != GameState.PLAYING) return
        _playerState.update { it.copy(isBraking = isBraking) }
    }

    fun goHome() {
        _gameState.value = GameState.MENU
    }

    fun openShop() {
        _gameState.value = GameState.SHOP
    }
    
    fun isUnlocked(bikeId: Int) = unlockedBikes.contains(bikeId)
    
    fun buyOrSelectBike(bikeId: Int) {
        val bike = BIKES.find { it.id == bikeId } ?: return
        if (isUnlocked(bikeId)) {
            _playerState.update { it.copy(currentBikeIndex = bikeId) }
        } else {
            if (_coins.value >= bike.cost) {
                _coins.value -= bike.cost
                unlockedBikes.add(bikeId)
                _playerState.update { it.copy(currentBikeIndex = bikeId) }
            }
        }
    }
}
