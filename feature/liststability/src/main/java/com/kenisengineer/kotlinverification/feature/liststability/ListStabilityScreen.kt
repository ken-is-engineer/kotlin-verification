package com.kenisengineer.kotlinverification.feature.liststability

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kenisengineer.kotlinverification.core.ui.VerificationScaffold
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun ListStabilityRoute(onBack: () -> Unit) {
    VerificationScaffold(title = "List 安定性", onBack = onBack) { padding ->
        ListStabilityScreen(modifier = Modifier.padding(padding))
    }
}

@Composable
internal fun ListStabilityScreen(modifier: Modifier = Modifier) {
    val sharedList = remember { seedItems() }
    val holder = remember { ImmutableItemHolder(sharedList) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "要素型が @Immutable でも、パラメータの List はインタフェースのため安定性を推論できません。" +
                "実行時の実体が MutableList なら、呼び出し元が同じ参照を持っている限り、" +
                "関数へ渡したあとも外部から中身を変更できます（コピーされません）。",
            style = MaterialTheme.typography.bodyMedium
        )
        IdentitySection(sharedList)
        StaleUiSection(sharedList)
        SkipCompareSection(sharedList, holder)
        MutationExceptionSection(sharedList)
        AlternativesSection(sharedList)
    }
}

@Composable
private fun IdentitySection(sharedList: MutableList<StableItem>) {
    ExperimentCard(
        title = "1. 同一インスタンス",
        body = "MutableList を List として渡しても新しいコレクションにはなりません。" +
            "identity と === が一致することを確認します。"
    ) {
        var peekedSize by remember { mutableIntStateOf(sharedList.size) }
        UnstableListConsumer(items = sharedList, source = sharedList)
        Text("メモリから読み直した size: $peekedSize")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    sharedList.add(nextItem(sharedList))
                    peekedSize = sharedList.size
                }
            ) {
                Text("元リストに追加")
            }
            OutlinedButton(onClick = { peekedSize = sharedList.size }) {
                Text("size を再読込")
            }
            OutlinedButton(
                onClick = {
                    sharedList.clear()
                    sharedList.addAll(seedItems())
                    peekedSize = sharedList.size
                }
            ) {
                Text("リセット")
            }
        }
    }
}

@Composable
private fun StaleUiSection(sharedList: MutableList<StableItem>) {
    ExperimentCard(
        title = "2. 通知なし変異（UI が古いまま）",
        body = "通常の MutableList は Snapshot に乗らないため、add しても Compose は再composeしません。" +
            "size の再読込は別の state なので、リスト表示は古いまま size だけ新しい値になります。"
    ) {
        var parentTick by remember { mutableIntStateOf(0) }
        Text("親の再compose tick: $parentTick")
        UnstableListConsumer(items = sharedList, source = sharedList)
        PeekedSizeLabel(sharedList)
        Button(onClick = { sharedList.add(nextItem(sharedList)) }) {
            Text("追加（通知なし）")
        }
        Button(onClick = { parentTick++ }) {
            Text("親を再 compose")
        }
        Text(
            "List 引数は skippable ではないので、親を再 compose すると子も走り、変異後の中身が見えます。",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun PeekedSizeLabel(sharedList: MutableList<StableItem>) {
    var peekedSize by remember { mutableIntStateOf(sharedList.size) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("メモリから読み直した size: $peekedSize")
        OutlinedButton(onClick = { peekedSize = sharedList.size }) {
            Text("size を再読込")
        }
    }
}

@Composable
private fun SkipCompareSection(
    sharedList: MutableList<StableItem>,
    holder: ImmutableItemHolder
) {
    var parentTick by remember { mutableIntStateOf(0) }
    ExperimentCard(
        title = "3. List 引数 vs @Immutable ラッパ",
        body = "同じ MutableList を、不安定な List 引数と誤って @Immutable にした holder の両方に渡します。" +
            "中身を変えてから親を再 compose すると、左側は更新され、右側は skip されて古いままになり得ます。"
    ) {
        Text("親の再compose tick: $parentTick")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UnstableListConsumer(
                items = sharedList,
                source = sharedList,
                modifier = Modifier.weight(1f)
            )
            MisannotatedHolderConsumer(
                    holder = holder,
                    modifier = Modifier.weight(1f)
                )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { sharedList.add(nextItem(sharedList)) }) {
                Text("追加（通知なし）")
            }
            Button(onClick = { parentTick++ }) {
                Text("親を再 compose")
            }
        }
    }
}

@Composable
private fun MutationExceptionSection(sharedList: MutableList<StableItem>) {
    var exceptionMessage by remember { mutableStateOf<String?>(null) }
    ExperimentCard(
        title = "4. 読み取り中の破壊的変更",
        body = "同じインスタンスを走査・index 参照している最中に add / clear すると、" +
            "ConcurrentModificationException や IndexOutOfBoundsException になります。" +
            "ここでのボタンは composition の外で再現し、例外を画面に出します。" +
            "LazyColumn の composition 中に別スレッドから変更すると、同様の例外が Compose runtime まで届いてクラッシュすることがあります。"
    ) {
        Text("現在の size: ${sharedList.size}")
        Text(sharedList.joinToString { it.label })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    if (sharedList.isEmpty()) {
                        sharedList.addAll(seedItems())
                    }
                    exceptionMessage = mutateWhileIterating(sharedList)
                }
            ) {
                Text("forEach 中に add（CME）")
            }
            Button(
                onClick = {
                    if (sharedList.isEmpty()) {
                        sharedList.addAll(seedItems())
                    }
                    exceptionMessage = indexAccessWhileShrinking(sharedList)
                }
            ) {
                Text("index 中に clear（IOBE）")
            }
        }
        exceptionMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun AlternativesSection(sharedList: MutableList<StableItem>) {
    val snapshotItems = remember {
        mutableStateListOf<StableItem>().apply { addAll(seedItems()) }
    }
    var copiedView by remember { mutableStateOf(sharedList.toList()) }
    var immutableItems by remember {
        mutableStateOf(
            persistentListOf(
                StableItem(1, "item-1"),
                StableItem(2, "item-2")
            )
        )
    }

    ExperimentCard(
        title = "5. 正しい代替",
        body = "SnapshotStateList は変更が再 compose を起こします。" +
            "toList() は共有参照を切ります。" +
            "ImmutableList は新しいインスタンスを返す更新になり、コンパイラも安定と推論しやすくなります。"
    ) {
        SnapshotStateListSection(snapshotItems)
        CopiedListSection(
            source = sharedList,
            copiedView = copiedView,
            onCopy = { copiedView = sharedList.toList() },
            onMutateSource = { sharedList.add(nextItem(sharedList)) }
        )
        ImmutableListSection(
            items = immutableItems,
            onAdd = { immutableItems = immutableItems.add(nextItem(immutableItems)) }
        )
    }
}

@Composable
private fun SnapshotStateListSection(items: SnapshotStateList<StableItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("mutableStateListOf", style = MaterialTheme.typography.titleSmall)
        Text("要素: ${items.joinToString { it.label }}")
        Button(onClick = { items.add(nextItem(items)) }) {
            Text("追加（UI がすぐ更新される）")
        }
    }
}

@Composable
private fun CopiedListSection(
    source: MutableList<StableItem>,
    copiedView: List<StableItem>,
    onCopy: () -> Unit,
    onMutateSource: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("toList() コピー", style = MaterialTheme.typography.titleSmall)
        CopiedListConsumer(items = copiedView, source = source)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onCopy) { Text("今の内容をコピー") }
            Button(onClick = onMutateSource) { Text("元リストだけ追加") }
        }
        Text(
            "コピー後に元を変えても、コピー側の identity と要素は変わりません。",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ImmutableListSection(
    items: PersistentList<StableItem>,
    onAdd: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("kotlinx ImmutableList", style = MaterialTheme.typography.titleSmall)
        ImmutableListConsumer(items = items)
        Button(onClick = onAdd) { Text("add して新しいリストを state に入れる") }
        Text(
            "persistentList の add は元を変更せず新しいインスタンスを返します。",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ExperimentCard(
    title: String,
    body: String,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodySmall)
            content()
        }
    }
}
