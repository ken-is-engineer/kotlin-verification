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
| `:feature:liststability` | [安定性の問題を修正する](https://developer.android.com/develop/ui/compose/performance/stability/fix?hl=ja) の検証 |

依存方向は `app` → `feature:*` → `core:*` です。

## 検証の足し方

1. `:feature:yourname` モジュールを作り、`settings.gradle.kts` に `include` する
2. `:core:navigation` の `Routes` に定数を足す
3. `:app` の `NavHost` に `composable` を足す
4. `HomeScreen` の一覧にボタン（カード）を 1 つ足す

## 初学者向けの図解

- [docs/list-stability.html](docs/list-stability.html)

## List 安定性の見方

メイン画面 → **List 安定性**。

`Item` は Stable でも、渡している `List<Item>` は Unstable です。中身は変えず **画面を更新** するだけで親を再 compose します。

- **ItemList の compose 回数**が増える → `List` 引数が skip されていない（主題）
- **各行の回数**は増えないことがある → `Item` が Stable なので行は skip できる

```bash
./gradlew :app:assembleDebug -Pcompose.strongSkipping=true
```

Strong Skipping をオンにすると、同じ `List` インスタンスなら `ItemList` も skip されます。

## Compose compiler reports

```bash
./gradlew :feature:liststability:compileDebugKotlin
```

- `feature/liststability/build/compose_reports/liststability_debug-composables.txt`
- `feature/liststability/build/compose_reports/liststability_debug-classes.txt`

Strong Skipping OFF では `ItemList` が `unstable items: List<Item>` で skippable ではありません。
