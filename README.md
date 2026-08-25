# kotlin-verification

Jetpack Compose / Kotlin の動作検証用アプリです。メイン画面から検証単位へ遷移し、各検証は Gradle feature モジュールに分かれています。

## 必要環境

- Android Studio (JBR 17+)
- Android SDK (`compileSdk` 35)
- JDK 17 以上（Android Studio 付属 JBR で可）

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:assembleDebug
```

`local.properties` に `sdk.dir` を書いてください（このファイルは git 管理しません）。

## モジュール構成

| モジュール | 役割 |
| --- | --- |
| `:app` | `NavHost` と検証一覧（メイン画面） |
| `:core:navigation` | Route 定数 |
| `:core:ui` | Theme と検証画面用 Scaffold |
| `:feature:liststability` | List 安定性 / MutableList 共有参照の検証 |

依存方向は `app` → `feature:*` → `core:*` です。

## 検証の足し方

1. `:feature:yourname` モジュールを作り、`settings.gradle.kts` に `include` する
2. `:core:navigation` の `Routes` に定数を足す
3. `:app` の `NavHost` に `composable` を足す
4. `HomeScreen` の一覧にボタン（カード）を 1 つ足す

## List 安定性検証の見方

メイン画面 → **List 安定性と MutableList 共有参照**。

要素型 `StableItem` は `@Immutable` で Stable です。それでもパラメータの `List<T>` はインタフェースのため Unstable です。実行時の実体が `MutableList` なら、呼び出し元が同じ参照を持っている限り、関数へ渡したあとも外部から中身を変えられます。

1. **同一インスタンス** — `identity` と `===` が一致する（コピーされない）
2. **通知なし変異** — 通常の `MutableList.add` では再 compose しない。`size を再読込` だけ別 state を更新すると、リスト表示は古いまま size だけ新しい値になる。親を再 compose すると `List` 引数の子は skippable ではないので中身が見える
3. **List 引数 vs @Immutable ラッパ** — 同じ `MutableList` を不安定な `List` と、誤って `@Immutable` にした holder の両方に渡す。親を再 compose すると左側は更新され、右側は skip されて古いままになり得る
4. **読み取り中の破壊的変更** — `forEach` 中の `add` で `ConcurrentModificationException`、index 参照中の `clear` で `IndexOutOfBoundsException`。例外は composition の外で捕捉して画面に出す。`LazyColumn` の composition 中に別スレッドから同じリストを変えると、同様の例外が Compose runtime まで届いてクラッシュすることがある
5. **正しい代替** — `mutableStateListOf`、`toList()` コピー、`kotlinx.collections.immutable` の `PersistentList`

## Compose compiler reports

`:feature:liststability` は debug ビルドでレポートを出します。

```bash
./gradlew :feature:liststability:compileDebugKotlin
```

出力先:

- `feature/liststability/build/compose_reports/liststability_debug-composables.txt`
- `feature/liststability/build/compose_reports/liststability_debug-classes.txt`

確認したいポイント:

- `StableItem` は `stable class`
- `UnstableListConsumer` は `unstable items: List<StableItem>` で restartable、**skippable ではない**
- `MisannotatedHolderConsumer` は `stable holder: ImmutableItemHolder` で **skippable**（`@Immutable` の誤用）
- `ImmutableListConsumer` は `stable items: ImmutableList<StableItem>` で **skippable**
