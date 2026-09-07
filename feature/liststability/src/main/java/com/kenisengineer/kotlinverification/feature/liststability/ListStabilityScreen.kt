package com.kenisengineer.kotlinverification.feature.liststability

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kenisengineer.kotlinverification.core.ui.VerificationScaffold
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.immutableListOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class Item(
    val id: Int,
    val label: String
)

@Composable
fun ListStabilityRoute(onBack: () -> Unit) {
    VerificationScaffold(title = "List 安定性", onBack = onBack) { padding ->
        Main(modifier = Modifier.padding(padding))
    }
}

@Composable
private fun Main(modifier: Modifier = Modifier) {
    val items: List<Item> = remember {
        listOf(
            Item(1, "A"),
            Item(2, "B"),
            Item(3, "C")
        )
    }
    var tick by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Item は Stable。渡しているのは List<Item> なので Unstable。")
        Text("中身は変えない。画面更新だけで親を再 compose する。")
        Text("tick: $tick")
        Button(onClick = { tick++ }) {
            Text("画面を更新")
        }
        ItemList(items = items, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ItemList(items: List<Item>, modifier: Modifier = Modifier) {
    val compositions = remember { intArrayOf(0) }
    compositions[0] += 1

    Column(modifier = modifier) {
        Text("ItemList の compose 回数: ${compositions[0]}")
        Text("↑ ここが増えれば、List 引数が skip されていない（主題）")
        LazyColumn {
            items(items, key = { it.id }) { item ->
                ItemRow(item = item)
            }
        }
    }
}

@Composable
private fun ItemRow(item: Item) {
    val compositions = remember(item.id) { intArrayOf(0) }
    compositions[0] += 1
    Text("${item.label}  行の compose 回数: ${compositions[0]}")
}
