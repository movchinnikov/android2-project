package com.artfinder.domain.gamification

import org.junit.Assert.assertEquals
import org.junit.Test

class BadgeTest {

    @Test
    fun `fromPoints returns EXPLORER for 0 points`() {
        assertEquals(Badge.EXPLORER, Badge.fromPoints(0))
    }

    @Test
    fun `fromPoints returns EXPLORER for 100 points`() {
        assertEquals(Badge.EXPLORER, Badge.fromPoints(100))
    }

    @Test
    fun `fromPoints returns CURATOR for 101 points`() {
        assertEquals(Badge.CURATOR, Badge.fromPoints(101))
    }

    @Test
    fun `fromPoints returns CURATOR for 250 points`() {
        assertEquals(Badge.CURATOR, Badge.fromPoints(250))
    }

    @Test
    fun `fromPoints returns ARCHIVIST for 251 points`() {
        assertEquals(Badge.ARCHIVIST, Badge.fromPoints(251))
    }

    @Test
    fun `fromPoints returns ARCHIVIST for 500 points`() {
        assertEquals(Badge.ARCHIVIST, Badge.fromPoints(500))
    }
}
