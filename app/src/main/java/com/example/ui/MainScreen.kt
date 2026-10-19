package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CatalogItem
import com.example.data.model.ItemStatus
import com.example.data.model.ItemType
import com.example.data.model.UserItem
import com.example.data.repository.FirestoreRepository
import com.example.data.repository.JikanRepository
import com.example.ui.components.SponsorAdBanner
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.ItemDetailSheet
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PremiumScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.AmberPremium
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

enum class AppDestination(val label: String) {
    DISCOVER("Explorer"),
    LIBRARY("Ma Liste"),
    PREMIUM("VIP"),
    PROFILE("Profil")
}

@Composable
fun MainScreen(
    currentUserId: String,
    firestoreRepository: FirestoreRepository,
    onSignOut: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val jikanRepository = remember { JikanRepository() }

    var currentDestination by remember { mutableStateOf(AppDestination.LIBRARY) }

    // Realtime observed data from Firestore
    val libraryItems by firestoreRepository.observeUserItems(currentUserId)
        .collectAsState(initial = emptyList())

    val userProfile by firestoreRepository.observeUserProfile(currentUserId)
        .collectAsState(initial = null)

    // Ensure initial profile document exists
    LaunchedEffect(currentUserId) {
        val authUser = FirebaseAuth.getInstance().currentUser
        val email = authUser?.email ?: ""
        val name = authUser?.displayName ?: ""
        try {
            firestoreRepository.ensureUserProfile(email, name)
        } catch (e: Exception) {
            // Ignore error or log
        }
    }

    // Modal Sheet State
    var selectedCatalogItem by remember { mutableStateOf<CatalogItem?>(null) }
    var selectedUserItem by remember { mutableStateOf<UserItem?>(null) }
    var isSheetOpen by remember { mutableStateOf(false) }

    fun openDetail(catalog: CatalogItem?, userItem: UserItem?) {
        selectedCatalogItem = catalog
        selectedUserItem = userItem
        isSheetOpen = true
    }

    val isPremium = userProfile?.isPremium ?: false

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = BackgroundDark,
            bottomBar = {
                if (!isTablet) {
                    Column {
                        SponsorAdBanner(
                            isPremium = isPremium,
                            onUpgradeClicked = { currentDestination = AppDestination.PREMIUM }
                        )
                        NavigationBar(
                            containerColor = SurfaceDark,
                            contentColor = TextSecondary,
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentDestination == AppDestination.LIBRARY,
                                onClick = { currentDestination = AppDestination.LIBRARY },
                                icon = {
                                    Icon(
                                        if (currentDestination == AppDestination.LIBRARY) Icons.Filled.Book else Icons.Outlined.Book,
                                        contentDescription = "Ma Liste"
                                    )
                                },
                                label = { Text("Ma Liste", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = IndigoPrimary,
                                    selectedTextColor = TextPrimary,
                                    indicatorColor = Color(0xFF312E81)
                                ),
                                modifier = Modifier.testTag("nav_library")
                            )

                            NavigationBarItem(
                                selected = currentDestination == AppDestination.DISCOVER,
                                onClick = { currentDestination = AppDestination.DISCOVER },
                                icon = {
                                    Icon(
                                        if (currentDestination == AppDestination.DISCOVER) Icons.Filled.Explore else Icons.Outlined.Explore,
                                        contentDescription = "Explorer"
                                    )
                                },
                                label = { Text("Explorer", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = IndigoPrimary,
                                    selectedTextColor = TextPrimary,
                                    indicatorColor = Color(0xFF312E81)
                                ),
                                modifier = Modifier.testTag("nav_discover")
                            )

                            NavigationBarItem(
                                selected = currentDestination == AppDestination.PREMIUM,
                                onClick = { currentDestination = AppDestination.PREMIUM },
                                icon = {
                                    Icon(
                                        if (currentDestination == AppDestination.PREMIUM) Icons.Filled.WorkspacePremium else Icons.Outlined.WorkspacePremium,
                                        contentDescription = "VIP"
                                    )
                                },
                                label = { Text("VIP", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AmberPremium,
                                    selectedTextColor = AmberPremium,
                                    indicatorColor = Color(0xFF451A03)
                                ),
                                modifier = Modifier.testTag("nav_premium")
                            )

                            NavigationBarItem(
                                selected = currentDestination == AppDestination.PROFILE,
                                onClick = { currentDestination = AppDestination.PROFILE },
                                icon = {
                                    Icon(
                                        if (currentDestination == AppDestination.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                                        contentDescription = "Profil"
                                    )
                                },
                                label = { Text("Profil", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = IndigoPrimary,
                                    selectedTextColor = TextPrimary,
                                    indicatorColor = Color(0xFF312E81)
                                ),
                                modifier = Modifier.testTag("nav_profile")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Adaptive Navigation Rail for Tablets
                if (isTablet) {
                    NavigationRail(
                        containerColor = SurfaceDark,
                        contentColor = TextSecondary,
                        header = {
                            Text(
                                text = "AniTrack",
                                color = IndigoLight,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxHeight()
                            .testTag("navigation_rail")
                    ) {
                        NavigationRailItem(
                            selected = currentDestination == AppDestination.LIBRARY,
                            onClick = { currentDestination = AppDestination.LIBRARY },
                            icon = {
                                Icon(
                                    if (currentDestination == AppDestination.LIBRARY) Icons.Filled.Book else Icons.Outlined.Book,
                                    contentDescription = "Ma Liste"
                                )
                            },
                            label = { Text("Ma Liste", fontSize = 11.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = IndigoPrimary,
                                selectedTextColor = TextPrimary,
                                indicatorColor = Color(0xFF312E81)
                            )
                        )

                        NavigationRailItem(
                            selected = currentDestination == AppDestination.DISCOVER,
                            onClick = { currentDestination = AppDestination.DISCOVER },
                            icon = {
                                Icon(
                                    if (currentDestination == AppDestination.DISCOVER) Icons.Filled.Explore else Icons.Outlined.Explore,
                                    contentDescription = "Explorer"
                                )
                            },
                            label = { Text("Explorer", fontSize = 11.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = IndigoPrimary,
                                selectedTextColor = TextPrimary,
                                indicatorColor = Color(0xFF312E81)
                            )
                        )

                        NavigationRailItem(
                            selected = currentDestination == AppDestination.PREMIUM,
                            onClick = { currentDestination = AppDestination.PREMIUM },
                            icon = {
                                Icon(
                                    if (currentDestination == AppDestination.PREMIUM) Icons.Filled.WorkspacePremium else Icons.Outlined.WorkspacePremium,
                                    contentDescription = "VIP"
                                )
                            },
                            label = { Text("VIP", fontSize = 11.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = AmberPremium,
                                selectedTextColor = AmberPremium,
                                indicatorColor = Color(0xFF451A03)
                            )
                        )

                        NavigationRailItem(
                            selected = currentDestination == AppDestination.PROFILE,
                            onClick = { currentDestination = AppDestination.PROFILE },
                            icon = {
                                Icon(
                                    if (currentDestination == AppDestination.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                                    contentDescription = "Profil"
                                )
                            },
                            label = { Text("Profil", fontSize = 11.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = IndigoPrimary,
                                selectedTextColor = TextPrimary,
                                indicatorColor = Color(0xFF312E81)
                            )
                        )
                    }
                }

                // Main Content View
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Crossfade(targetState = currentDestination, label = "screen_transition") { destination ->
                        when (destination) {
                            AppDestination.LIBRARY -> {
                                LibraryScreen(
                                    libraryItems = libraryItems,
                                    onSelectItem = { item ->
                                        openDetail(catalog = null, userItem = item)
                                    },
                                    onQuickIncrement = { item ->
                                        coroutineScope.launch {
                                            val newProg = item.currentProgress + 1
                                            try {
                                                firestoreRepository.updateProgress(item.id, newProg)
                                                val unitName = if (item.type == ItemType.MANGA) "chapitre" else "épisode"
                                                snackbarHostState.showSnackbar("+1 $unitName validé !")
                                            } catch (e: Exception) {
                                                snackbarHostState.showSnackbar("Erreur lors de la mise à jour.")
                                            }
                                        }
                                    },
                                    onNavigateToDiscover = {
                                        currentDestination = AppDestination.DISCOVER
                                    }
                                )
                            }
                            AppDestination.DISCOVER -> {
                                DiscoverScreen(
                                    jikanRepository = jikanRepository,
                                    libraryItems = libraryItems,
                                    onSelectCatalogItem = { catalogItem ->
                                        val existing = libraryItems.find { it.malId == catalogItem.malId }
                                        openDetail(catalog = catalogItem, userItem = existing)
                                    }
                                )
                            }
                            AppDestination.PREMIUM -> {
                                PremiumScreen(
                                    userProfile = userProfile,
                                    libraryItems = libraryItems,
                                    jikanRepository = jikanRepository,
                                    onTogglePremium = { newStatus ->
                                        coroutineScope.launch {
                                            try {
                                                firestoreRepository.setPremiumStatus(newStatus)
                                                val msg = if (newStatus)
                                                    "Abonnement VIP activé ! Zéro pub & Recommandations débloquées."
                                                else
                                                    "Abonnement désactivé."
                                                snackbarHostState.showSnackbar(msg)
                                            } catch (e: Exception) {
                                                snackbarHostState.showSnackbar("Échec de mise à jour Premium.")
                                            }
                                        }
                                    },
                                    onSelectCatalogItem = { recItem ->
                                        val existing = libraryItems.find { it.malId == recItem.malId }
                                        openDetail(catalog = recItem, userItem = existing)
                                    }
                                )
                            }
                            AppDestination.PROFILE -> {
                                ProfileScreen(
                                    userProfile = userProfile,
                                    libraryItems = libraryItems,
                                    onSignOut = onSignOut
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for item details and editing
    if (isSheetOpen) {
        ItemDetailSheet(
            catalogItem = selectedCatalogItem,
            existingUserItem = selectedUserItem,
            onDismiss = { isSheetOpen = false },
            onSaveItem = { itemToSave ->
                coroutineScope.launch {
                    try {
                        firestoreRepository.saveOrUpdateItem(itemToSave)
                        snackbarHostState.showSnackbar("Titre enregistré avec succès !")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Erreur d'enregistrement dans le Cloud.")
                    }
                }
            },
            onDeleteItem = { itemId ->
                coroutineScope.launch {
                    try {
                        firestoreRepository.deleteItem(itemId)
                        snackbarHostState.showSnackbar("Titre retiré de votre bibliothèque.")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Erreur lors de la suppression.")
                    }
                }
            }
        )
    }
}
