package com.architecturelab.taskapp.domain.model

import com.architecturelab.taskapp.domain.exception.TaskDomainException
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskTitleTest {
    @Test
    fun `create title with valid text trims surrounding spaces`() {
        val title = TaskTitle.create("   Comprar leche   ")
        assertEquals("Comprar leche", title.value)
    }

    @Test(expected = TaskDomainException.InvalidTitle.EmptyTitle::class)
    fun `create title with empty text throws EmptyTitle`() {
        TaskTitle.create("")
    }

    @Test(expected = TaskDomainException.InvalidTitle.EmptyTitle::class)
    fun `create title with blank spaces throws EmptyTitle`() {
        TaskTitle.create("    ")
    }

    @Test
    fun `create title with exactly 36 Unicode code points succeeds`() {
        val raw = "a".repeat(36)
        val title = TaskTitle.create(raw)
        assertEquals(36, title.value.codePointCount(0, title.value.length))
    }

    @Test(expected = TaskDomainException.InvalidTitle.ExceedsMaxLength::class)
    fun `create title exceeding 36 Unicode code points throws ExceedsMaxLength`() {
        val raw = "a".repeat(37)
        TaskTitle.create(raw)
    }

    @Test
    fun `create title with multi-byte Unicode characters handles code points correctly`() {
        val emojis = "😀".repeat(36) // 36 Unicode code points
        val title = TaskTitle.create(emojis)
        assertEquals(36, title.value.codePointCount(0, title.value.length))
    }
}
