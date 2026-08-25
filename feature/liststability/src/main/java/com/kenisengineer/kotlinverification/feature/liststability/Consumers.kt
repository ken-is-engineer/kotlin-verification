package com.kenisengineer.kotlinverification.feature.liststability

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList

/**
 * [items] は [List] インタフェースのため Unstable。要素 [StableItem] が Stable でも
 * このパラメータの安定性は推論されない（restartable / 非 skippable）。
 */
@Composable
internal fun UnstableListConsumer(
    items: List<StableItem>,
    source: MutableList<StableItem>,
    modifier: Modifier = Modifier
) {
    val compositions = remember { IntRef() }
    compositions.value += 1

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("UnstableListConsumer", style = MaterialTheme.typography.titleSmall)
        Text("再compose回数: ${compositions.value}")
        Text("引数 List: ${identity(items)}")
        Text("呼び出し元 MutableList: ${identity(source)}")
        Text("同一インスタンス: ${items === source}")
        Text("表示中の要素: ${items.joinToString { it.label }}")
        Text("表示中の size: ${items.size}")
    }
}

/**
 * [@Immutable] な holder は skippable。同じ MutableList を包んでいると、
 * 中身の破壊的変更が equals に現れず、親が再composeしてもスキップされうる。
 *
 * MutableList を引数に残すとパラメータが Unstable になり skippable にならないため、
 * ここでは holder だけを受け取る。
 */
@Composable
internal fun MisannotatedHolderConsumer(
    holder: ImmutableItemHolder,
    modifier: Modifier = Modifier
) {
    val compositions = remember { IntRef() }
    compositions.value += 1

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("MisannotatedHolderConsumer", style = MaterialTheme.typography.titleSmall)
        Text("再compose回数: ${compositions.value}")
        Text("holder: ${identity(holder)}")
        Text("holder.items: ${identity(holder.items)}")
        Text("表示中の要素: ${holder.items.joinToString { it.label }}")
        Text("表示中の size: ${holder.items.size}")
    }
}

@Composable
internal fun ImmutableListConsumer(
    items: ImmutableList<StableItem>,
    modifier: Modifier = Modifier
) {
    val compositions = remember { IntRef() }
    compositions.value += 1

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("ImmutableListConsumer", style = MaterialTheme.typography.titleSmall)
        Text("再compose回数: ${compositions.value}")
        Text("引数 ImmutableList: ${identity(items)}")
        Text("表示中の要素: ${items.joinToString { it.label }}")
        Text("表示中の size: ${items.size}")
    }
}

@Composable
internal fun CopiedListConsumer(
    items: List<StableItem>,
    source: MutableList<StableItem>,
    modifier: Modifier = Modifier
) {
    val compositions = remember { IntRef() }
    compositions.value += 1

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("CopiedListConsumer (toList)", style = MaterialTheme.typography.titleSmall)
        Text("再compose回数: ${compositions.value}")
        Text("コピー: ${identity(items)}")
        Text("元の MutableList: ${identity(source)}")
        Text("同一インスタンス: ${items === source}")
        Text("表示中の要素: ${items.joinToString { it.label }}")
    }
}
