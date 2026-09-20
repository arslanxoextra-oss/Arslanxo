package com.example

import com.example.model.Post
import com.example.util.DateUtils
import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testDateUtilsParsing() {
    val millis = 1726000000000L
    assertEquals(millis, DateUtils.parseTimestampMillis(millis))
    assertEquals(millis, DateUtils.parseTimestampMillis(Date(millis)))

    val seconds = 1726000000L
    assertEquals(1726000000000L, DateUtils.parseTimestampMillis(seconds))
  }

  @Test
  fun testPostsSortingNewestFirst() {
    val postOld = Post(id = "old", title = "Old Post", createdAt = 1000L)
    val postToday = Post(id = "new", title = "Today Post", createdAt = 5000L)
    val postMiddle = Post(id = "mid", title = "Mid Post", createdAt = 3000L)

    val posts = listOf(postOld, postToday, postMiddle)
    val sorted = posts.sortedByDescending { it.creationTimestamp }

    assertEquals("new", sorted[0].id)
    assertEquals("mid", sorted[1].id)
    assertEquals("old", sorted[2].id)
  }
}

