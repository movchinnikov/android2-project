package com.artfinder.domain.gamification

/**
 * Logic for calculating points and badges based on the ArtFinder requirements.
 */
object RewardManager {

    /**
     * 1–5 фото к объекту = 10 баллов.
     * 6–10 фото = 20 баллов.
     * Максимум 20 за один объект.
     */
    fun calculatePointsForPhotos(photoCount: Int): Int {
        return when {
            photoCount in 1..5 -> 10
            photoCount >= 6 -> 20
            else -> 0
        }
    }

    /**
     * Explorer: 0–100 баллов.
     * Curator: 101–250 баллов.
     * Archivist: 251–500 баллов.
     */
    fun getBadgeForPoints(points: Int): Badge = Badge.fromPoints(points)
}
