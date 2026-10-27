package com.architecturelab.taskapp.domain.model

import java.text.Normalizer

object TaskSorter {
    fun sort(
        tasks: List<Task>,
        sortOption: TaskSort,
    ): List<Task> {
        val comparator =
            when (sortOption) {
                TaskSort.DueDateAsc -> dueDateComparator(ascending = true)
                TaskSort.DueDateDesc -> dueDateComparator(ascending = false)
                TaskSort.PriorityAsc -> priorityComparator(ascending = true)
                TaskSort.PriorityDesc -> priorityComparator(ascending = false)
            }
        return tasks.sortedWith(comparator)
    }

    private fun dueDateComparator(ascending: Boolean): Comparator<Task> =
        Comparator { t1, t2 ->
            val date1 = t1.dueDate?.value
            val date2 = t2.dueDate?.value

            when {
                date1 == null && date2 == null -> secondaryTieBreaker(t1, t2)
                date1 == null -> 1
                date2 == null -> -1
                date1 != date2 -> if (ascending) date1.compareTo(date2) else date2.compareTo(date1)
                else -> secondaryTieBreaker(t1, t2)
            }
        }

    private fun priorityComparator(ascending: Boolean): Comparator<Task> =
        Comparator { t1, t2 ->
            val p1 = t1.priority
            val p2 = t2.priority

            if (p1 != p2) {
                if (ascending) p1.ordinal.compareTo(p2.ordinal) else p2.ordinal.compareTo(p1.ordinal)
            } else {
                secondaryTieBreakerPrioritySort(t1, t2)
            }
        }

    private fun secondaryTieBreaker(
        t1: Task,
        t2: Task,
    ): Int {
        if (t1.priority != t2.priority) {
            return t2.priority.ordinal.compareTo(t1.priority.ordinal)
        }
        return secondaryTieBreakerPrioritySort(t1, t2)
    }

    private fun secondaryTieBreakerPrioritySort(
        t1: Task,
        t2: Task,
    ): Int {
        val norm1 = Normalizer.normalize(t1.title.value, Normalizer.Form.NFC).lowercase()
        val norm2 = Normalizer.normalize(t2.title.value, Normalizer.Form.NFC).lowercase()

        val titleResult = norm1.compareTo(norm2)
        if (titleResult != 0) return titleResult

        return t1.id.value.compareTo(t2.id.value)
    }
}
