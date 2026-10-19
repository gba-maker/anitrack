package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CatalogItem
import com.example.data.model.UserItem
import com.example.data.model.UserProfile
import com.example.data.repository.JikanRepository
import com.example.ui.components.CatalogItemCard
import com.example.ui.theme.AmberPremium
import com.example.ui.theme.AmberPremiumLight
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun PremiumScreen(
    userProfile: UserProfile?,
    libraryItems: List<UserItem>,
    jikanRepository: JikanRepository,
    onTogglePremium: (Boolean) -> Unit,
    onSelectCatalogItem: (CatalogItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPremium = userProfile?.isPremium ?: false
    val coroutineScope = rememberCoroutineScope()

    var recommendations by remember { mutableStateOf<List<CatalogItem>>(emptyList()) }
    var isLoadingRecomms by remember { mutableStateOf(false) }

    // Compute favorite genres from user's library
    val preferredGenres = remember(libraryItems) {
        libraryItems
            .filter { it.userScore >= 7 || it.status == "COMPLETED" }
            .flatMap { it.genres }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { it.key }
            .take(4)
    }

    val existingMalIds = remember(libraryItems) {
        libraryItems.map { it.malId }.toSet()
    }

    fun fetchRecommendations() {
        if (!isPremium) return
        isLoadingRecomms = true
        coroutineScope.launch {
            val res = jikanRepository.getPersonalizedRecommendations(preferredGenres, existingMalIds)
            isLoadingRecomms = false
            res.onSuccess {
                recommendations = it
            }
        }
    }

    LaunchedEffect(isPremium, libraryItems.size) {
        if (isPremium) {
            fetchRecommendations()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Premium Hero Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF312E81), Color(0xFF1E1B4B), Color(0xFF451A03))
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .border(1.5.dp, if (isPremium) AmberPremium else SurfaceBorderDark, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(AmberPremium.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = AmberPremium,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isPremium) "AniTrack VIP Actif" else "AniTrack Premium",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isPremium)
                            "Vous profitez de l'expérience sans publicité et de l'accès aux recommandations personnalisées en temps réel."
                        else
                            "Libérez le plein potentiel d'AniTrack avec zéro publicité et des recommandations basées sur vos animes préférés.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isPremium) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Abonnement VIP Actif", color = GreenSuccess, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { onTogglePremium(false) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            modifier = Modifier.testTag("btn_deactivate_premium")
                        ) {
                            Text("Désactiver l'abonnement VIP (Test)", fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = { onTogglePremium(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberPremium),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_subscribe_premium")
                        ) {
                            Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = Color(0xFF1E1B4B))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Activer AniTrack VIP (Simulé)",
                                color = Color(0xFF1E1B4B),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Perks List
        item {
            Text(
                text = "Avantages de la formule",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            PerkCard(
                icon = Icons.Default.Block,
                title = "Zéro Publicité",
                description = "Les bannières de sponsors sont définitivement masquées sur l'application."
            )
        }

        item {
            PerkCard(
                icon = Icons.Default.AutoAwesome,
                title = "Recommandations Intelligentes",
                description = "Moteur d'affinité analysant les genres de vos coups de cœur pour des suggestions ciblées."
            )
        }

        // Recommendations Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Recommandations Personnalisées",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isPremium && preferredGenres.isNotEmpty()) {
                        Text(
                            text = "Inspiré par vos goûts : ${preferredGenres.joinToString(", ")}",
                            color = CyanAccent,
                            fontSize = 11.sp
                        )
                    }
                }

                if (isPremium) {
                    IconButton(onClick = { fetchRecommendations() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualiser", tint = IndigoLight)
                    }
                }
            }
        }

        if (!isPremium) {
            item {
                Surface(
                    color = SurfaceDark,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SurfaceBorderDark, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AmberPremium, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Fonctionnalité réservée aux membres VIP",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Activez l'abonnement pour débloquer votre sélection sur-mesure basée sur les œuvres que vous avez notées.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else if (isLoadingRecomms) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AmberPremium)
                }
            }
        } else if (recommendations.isEmpty()) {
            item {
                Text(
                    text = "Ajoutez et notez des animes dans votre bibliothèque pour affiner vos recommandations.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(recommendations, key = { "rec_${it.malId}" }) { recItem ->
                CatalogItemCard(
                    item = recItem,
                    isInLibrary = recItem.malId in existingMalIds,
                    onClick = { onSelectCatalogItem(recItem) }
                )
            }
        }
    }
}

@Composable
private fun PerkCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorderDark, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(SurfaceElevatedDark, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = AmberPremium, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = description, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
            }
        }
    }
}
