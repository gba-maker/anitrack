package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CatalogItem
import com.example.data.model.ItemStatus
import com.example.data.model.ItemType
import com.example.data.model.UserItem
import com.example.ui.theme.AmberPremium
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RedDanger
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemDetailSheet(
    catalogItem: CatalogItem?,
    existingUserItem: UserItem?,
    onDismiss: () -> Unit,
    onSaveItem: (UserItem) -> Unit,
    onDeleteItem: ((String) -> Unit)? = null
) {
    if (catalogItem == null && existingUserItem == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val title = existingUserItem?.title ?: catalogItem?.title ?: ""
    val imageUrl = existingUserItem?.imageUrl ?: catalogItem?.imageUrl ?: ""
    val itemType = existingUserItem?.type ?: catalogItem?.type ?: ItemType.ANIME
    val totalCount = (existingUserItem?.totalProgress ?: catalogItem?.totalCount?.toLong() ?: 0L).coerceAtLeast(0L)
    val synopsis = catalogItem?.synopsis ?: ""
    val genres = existingUserItem?.genres ?: catalogItem?.genres ?: emptyList()
    val malId = existingUserItem?.malId ?: catalogItem?.malId ?: 0L
    val isManga = itemType == ItemType.MANGA

    var status by remember { mutableStateOf(existingUserItem?.status ?: ItemStatus.WATCHING) }
    var currentProgress by remember { mutableLongStateOf(existingUserItem?.currentProgress ?: 0L) }
    var userScore by remember { mutableLongStateOf(existingUserItem?.userScore ?: 0L) }
    var notes by remember { mutableStateOf(existingUserItem?.personalNotes ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingUserItem != null) "Gérer dans ma liste" else "Détails & Suivi",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Info Row: Poster + Title + Stats
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(width = 90.dp, height = 130.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevatedDark)
                ) {
                    if (imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isManga) "Format : Manga" else "Format : Série Anime",
                        color = if (isManga) CyanAccent else IndigoLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (catalogItem != null && catalogItem.score > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = AmberPremium, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Score MAL : ${catalogItem.score}",
                                color = AmberPremium,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Genres chips
            if (genres.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    genres.forEach { genre ->
                        Box(
                            modifier = Modifier
                                .background(SurfaceElevatedDark, RoundedCornerShape(6.dp))
                                .border(1.dp, SurfaceBorderDark, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(text = genre, color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Status Selector
            Text(text = "Statut de lecture", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            val statusList = listOf(
                ItemStatus.WATCHING to (if (isManga) "En lecture" else "En cours"),
                ItemStatus.COMPLETED to "Terminé",
                ItemStatus.ON_HOLD to "En pause",
                ItemStatus.PLAN_TO_WATCH to (if (isManga) "À lire" else "À voir")
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                statusList.forEach { (statusKey, label) ->
                    val isSelected = status == statusKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { status = statusKey },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isManga) CyanAccent else IndigoPrimary,
                            selectedLabelColor = Color(0xFF0F172A),
                            containerColor = SurfaceElevatedDark,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) IndigoPrimary else SurfaceBorderDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Progression Counters
            val unitLabel = if (isManga) "Chapitres lus" else "Épisodes vus"
            Text(text = unitLabel, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceElevatedDark, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (currentProgress > 0) currentProgress-- },
                        modifier = Modifier
                            .size(34.dp)
                            .background(SurfaceDark, CircleShape)
                            .testTag("btn_decrement_progress")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "-1", tint = TextPrimary)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "$currentProgress ${if (totalCount > 0) "/ $totalCount" else ""}",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(
                        onClick = {
                            if (totalCount == 0L || currentProgress < totalCount) {
                                currentProgress++
                            }
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .background(IndigoPrimary, CircleShape)
                            .testTag("btn_increment_progress")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "+1", tint = Color.White)
                    }
                }

                if (totalCount > 0 && currentProgress < totalCount) {
                    Text(
                        text = "Terminer tout",
                        color = CyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                currentProgress = totalCount
                                status = ItemStatus.COMPLETED
                            }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // User Score (1 - 10)
            Text(text = "Ma Note Personnelle", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (star in 1..10) {
                    Icon(
                        imageVector = if (star <= userScore) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Note $star",
                        tint = if (star <= userScore) AmberPremium else TextTertiary,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { userScore = if (userScore == star.toLong()) 0L else star.toLong() }
                            .padding(2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (userScore > 0) "$userScore/10" else "Pas noté",
                    color = if (userScore > 0) AmberPremium else TextTertiary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Personal Notes
            Text(text = "Notes & Avis", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Écrivez vos impressions, théories ou favoris...", color = TextTertiary, fontSize = 13.sp) },
                maxLines = 4,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceElevatedDark,
                    unfocusedContainerColor = SurfaceElevatedDark,
                    focusedBorderColor = IndigoPrimary,
                    unfocusedBorderColor = SurfaceBorderDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("detail_notes_input")
            )

            // Synopsis Preview
            if (synopsis.isNotBlank()) {
                Spacer(modifier = Modifier.height(18.dp))
                Text(text = "Synopsis officiel", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = synopsis, color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save / Action Button
            Button(
                onClick = {
                    val updatedItem = UserItem(
                        id = existingUserItem?.id ?: "${itemType.lowercase()}_$malId",
                        userId = existingUserItem?.userId ?: "",
                        malId = malId,
                        title = title,
                        imageUrl = imageUrl,
                        type = itemType,
                        status = status,
                        currentProgress = currentProgress,
                        totalProgress = totalCount,
                        userScore = userScore,
                        personalNotes = notes,
                        genres = genres
                    )
                    onSaveItem(updatedItem)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_save_item")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (existingUserItem != null) "Enregistrer les modifications" else "Ajouter à ma bibliothèque",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Delete button if existing item
            if (existingUserItem != null && onDeleteItem != null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        onDeleteItem(existingUserItem.id)
                        onDismiss()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedDanger),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(RedDanger.copy(alpha = 0.5f))),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_delete_item")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Supprimer de ma liste", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
