package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ItemStatus
import com.example.data.model.ItemType
import com.example.data.model.UserItem
import com.example.ui.components.UserItemRow
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun LibraryScreen(
    libraryItems: List<UserItem>,
    onSelectItem: (UserItem) -> Unit,
    onQuickIncrement: (UserItem) -> Unit,
    onNavigateToDiscover: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStatus by remember { mutableStateOf(ItemStatus.WATCHING) }
    var selectedTypeFilter by remember { mutableStateOf("ALL") } // "ALL", "ANIME", "MANGA"
    var filterQuery by remember { mutableStateOf("") }

    val statuses = listOf(
        ItemStatus.WATCHING to "En cours",
        ItemStatus.COMPLETED to "Terminé",
        ItemStatus.ON_HOLD to "En pause",
        ItemStatus.PLAN_TO_WATCH to "À voir / lire"
    )

    val filteredItems = remember(libraryItems, selectedStatus, selectedTypeFilter, filterQuery) {
        libraryItems.filter { item ->
            val matchStatus = item.status == selectedStatus
            val matchType = when (selectedTypeFilter) {
                "ANIME" -> item.type == ItemType.ANIME
                "MANGA" -> item.type == ItemType.MANGA
                else -> true
            }
            val matchSearch = if (filterQuery.isBlank()) true else {
                item.title.contains(filterQuery, ignoreCase = true)
            }
            matchStatus && matchType && matchSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Screen Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Ma Bibliothèque",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${libraryItems.size} titres enregistrés et synchronisés",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search in personal library
        OutlinedTextField(
            value = filterQuery,
            onValueChange = { filterQuery = it },
            placeholder = { Text("Filtrer ma bibliothèque...", color = TextTertiary, fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = IndigoLight, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (filterQuery.isNotEmpty()) {
                    IconButton(onClick = { filterQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Effacer", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark,
                focusedBorderColor = IndigoPrimary,
                unfocusedBorderColor = SurfaceBorderDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("library_filter_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Type Switcher Row (Tous | Animes | Mangas)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val typeFilters = listOf("ALL" to "Tous", "ANIME" to "Animes", "MANGA" to "Mangas")
            typeFilters.forEach { (typeKey, label) ->
                val isSelected = selectedTypeFilter == typeKey
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) SurfaceElevatedDark else SurfaceDark,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedTypeFilter = typeKey }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Status Tabs
        val selectedTabIndex = statuses.indexOfFirst { it.first == selectedStatus }.coerceAtLeast(0)
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = BackgroundDark,
            contentColor = IndigoLight,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = IndigoPrimary
                    )
                }
            },
            divider = {}
        ) {
            statuses.forEachIndexed { index, (statusKey, statusLabel) ->
                val count = libraryItems.count { it.status == statusKey }
                Tab(
                    selected = selectedStatus == statusKey,
                    onClick = { selectedStatus = statusKey },
                    text = {
                        Text(
                            text = "$statusLabel ($count)",
                            color = if (selectedStatus == statusKey) TextPrimary else TextTertiary,
                            fontSize = 13.sp,
                            fontWeight = if (selectedStatus == statusKey) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_status_$statusKey")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(SurfaceElevatedDark, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Aucun titre dans cette catégorie",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Découvrez des animes et mangas populaires pour les ajouter à votre liste synchronisée.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onNavigateToDiscover,
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_empty_explore")
                    ) {
                        Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Explorer le catalogue")
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    UserItemRow(
                        item = item,
                        onClick = { onSelectItem(item) },
                        onQuickIncrement = { onQuickIncrement(item) }
                    )
                }
            }
        }
    }
}
