package com.artfinder.domain.gamification

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the RewardManager as per Milestone 4 requirements.
 */
class RewardManagerTest {

    @Test
    fun `calculatePointsForPhotos returns 0 for 0 photos`() {
        val points = RewardManager.calculatePointsForPhotos(0)
        assertEquals(0, points)
    }

    @Test
    fun `calculatePointsForPhotos returns 10 for 1 photo`() {
        val points = RewardManager.calculatePointsForPhotos(1)
        assertEquals(10, points)
    }

    @Test
    fun `calculatePointsForPhotos returns 10 for 5 photos`() {
        val points = RewardManager.calculatePointsForPhotos(5)
        assertEquals(10, points)
    }

    @Test
    fun `calculatePointsForPhotos returns 20 for 6 photos`() {
        val points = RewardManager.calculatePointsForPhotos(6)
        assertEquals(20, points)
    }

    @Test
    fun `getBadgeForPoints returns EXPLORER for 50 points`() {
        val badge = RewardManager.getBadgeForPoints(50)
        assertEquals(Badge.EXPLORER, badge)
    }

    @Test
    fun `getBadgeForPoints returns CURATOR for 150 points`() {
        val badge = RewardManager.getBadgeForPoints(150)
        assertEquals(Badge.CURATOR, badge)
    }

    @Test
    fun `getBadgeForPoints returns ARCHIVIST for 300 points`() {
        val badge = RewardManager.getBadgeForPoints(300)
        assertEquals(Badge.ARCHIVIST, badge)
    }
}
