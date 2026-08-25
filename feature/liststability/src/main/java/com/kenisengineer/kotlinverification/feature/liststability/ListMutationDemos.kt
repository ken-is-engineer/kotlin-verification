package com.kenisengineer.kotlinverification.feature.liststability

internal class IntRef(var value: Int = 0)

internal fun identity(value: Any?): String =
    "identity=${System.identityHashCode(value)} type=${value?.javaClass?.name}"

internal fun nextItem(items: List<StableItem>): StableItem {
    val nextId = (items.maxOfOrNull { it.id } ?: 0) + 1
    return StableItem(id = nextId, label = "item-$nextId")
}

internal fun seedItems(): MutableList<StableItem> = mutableListOf(
    StableItem(id = 1, label = "item-1"),
    StableItem(id = 2, label = "item-2")
)

internal fun mutateWhileIterating(items: MutableList<StableItem>): String {
    return try {
        items.forEach { _ ->
            items.add(nextItem(items))
        }
        "例外は出ませんでした（size=${items.size}）"
    } catch (error: ConcurrentModificationException) {
        "${error::class.qualifiedName}: ${error.message}"
    }
}

internal fun indexAccessWhileShrinking(items: MutableList<StableItem>): String {
    return try {
        val size = items.size
        for (index in 0 until size) {
            if (index == 0) {
                items.clear()
            }
            val item = items[index]
            item.label
        }
        "例外は出ませんでした（size=${items.size}）"
    } catch (error: IndexOutOfBoundsException) {
        "${error::class.qualifiedName}: ${error.message}"
    }
}
