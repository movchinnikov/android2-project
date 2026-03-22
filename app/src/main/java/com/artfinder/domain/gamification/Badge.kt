package com.artfinder.domain.gamification

enum class Badge(val displayName: String, val icon: String, val minPoints: Int) {
    EXPLORER("Explorer", "🧭", 0),
    CURATOR("Curator", "🎨", 101),
    ARCHIVIST("Archivist", "📚", 251);

    companion object {
        fun fromPoints(points: Int): Badge {
            return when {
                points >= 251 -> ARCHIVIST
                points >= 101 -> CURATOR
                else -> EXPLORER
            }
        }
    }
}
