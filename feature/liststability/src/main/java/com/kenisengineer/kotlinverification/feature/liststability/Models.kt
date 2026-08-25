package com.kenisengineer.kotlinverification.feature.liststability

import androidx.compose.runtime.Immutable

@Immutable
data class StableItem(
    val id: Int,
    val label: String
)

/**
 * 要素の List は実行時に MutableList であり得るのに @Immutable と宣言している。
 * Compose は equals でスキップ判定するため、同じ MutableList を包んだままだと
 * 中身が変わっても「変化なし」と見なされる。
 */
@Immutable
data class ImmutableItemHolder(
    val items: List<StableItem>
)
