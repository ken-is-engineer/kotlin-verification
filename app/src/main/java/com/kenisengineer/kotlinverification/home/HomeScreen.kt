package com.kenisengineer.kotlinverification.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kenisengineer.kotlinverification.core.navigation.Routes
import com.kenisengineer.kotlinverification.core.ui.VerificationScaffold

private data class VerificationEntry(
    val route: String,
    val title: String,
    val description: String
)

private val verifications = listOf(
    VerificationEntry(
        route = Routes.LIST_STABILITY,
        title = "List 安定性",
        description = "Stable な data model の List を渡す。中身は変えず、画面更新だけで ItemList が skip されないことを見る。"
    )
)

@Composable
fun HomeScreen(onOpenVerification: (String) -> Unit) {
    VerificationScaffold(title = "Kotlin 検証") { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "検証単位を選んでください。各画面は feature モジュールに分かれています。",
                style = MaterialTheme.typography.bodyMedium
            )
            verifications.forEach { entry ->
                Card(
                    onClick = { onOpenVerification(entry.route) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(entry.title, style = MaterialTheme.typography.titleMedium)
                        Text(entry.description, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
