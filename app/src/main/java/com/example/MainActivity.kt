package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Product
import com.example.ui.CompanyFilter
import com.example.ui.DentalViewModel
import com.example.ui.components.AddProductDialog
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.CatalogSyncDialog
import com.example.ui.components.CategoryFilterPillsRow
import com.example.ui.components.CategoryGridOverview
import com.example.ui.components.EditPriceDialog
import com.example.ui.components.HeaderBanner
import com.example.ui.components.OrderFooterBar
import com.example.ui.components.OrderSummarySheet
import com.example.ui.components.ProductItemCard
import com.example.ui.components.SavedOrdersSheet
import com.example.ui.components.SelectedCategoryHeaderBar
import com.example.ui.components.SetQuantityDialog
import com.example.ui.components.ToolbarSection
import com.example.ui.theme.EncomendasTheme
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900

class MainActivity : ComponentActivity() {
    private val viewModel: DentalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EncomendasTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(viewModel: DentalViewModel) {
    val allProducts by viewModel.products.collectAsStateWithLifecycle()
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val cartSummary by viewModel.cartSummary.collectAsStateWithLifecycle()
    val savedOrders by viewModel.savedOrders.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val selectedSortOption by viewModel.selectedSortOption.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val categoryStats by viewModel.categoryStats.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val syncUrl by viewModel.syncUrl.collectAsStateWithLifecycle()

    var showSummarySheet by remember { mutableStateOf(false) }
    var showSavedOrdersSheet by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var initialCodeForNewProduct by remember { mutableStateOf("") }
    var showBarcodeScannerDialog by remember { mutableStateOf(false) }
    var productToEditPrice by remember { mutableStateOf<Product?>(null) }
    var productToSetQuantity by remember { mutableStateOf<Product?>(null) }

    // Counts
    val totalCount = allProducts.size
    val montellanoCount = remember(allProducts) { allProducts.count { it.company.equals("Montellano", ignoreCase = true) } }
    val assisdentCount = remember(allProducts) { allProducts.count { it.company.equals("Assisdent", ignoreCase = true) } }

    val allVisibleSelected = remember(filteredProducts) {
        filteredProducts.isNotEmpty() && filteredProducts.all { it.qty > 0 || it.isSelected }
    }

    val selectedProductsList = remember(allProducts) {
        allProducts.filter { it.qty > 0 || it.isSelected }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_screen"),
        bottomBar = {
            OrderFooterBar(
                cartSummary = cartSummary,
                onViewOrder = { showSummarySheet = true }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("product_list"),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Header Banner item
            item(key = "header_banner") {
                HeaderBanner(
                    totalProductsCount = totalCount,
                    montellanoCount = montellanoCount,
                    assisdentCount = assisdentCount,
                    selectedCount = cartSummary.selectedCount,
                    onOpenSyncClick = { showSyncDialog = true }
                )
            }

            // Toolbar Section sticky item (keeps search field visible on scroll)
            stickyHeader(key = "toolbar_section") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ToolbarSection(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.searchQuery.value = it },
                            selectedFilter = selectedFilter,
                            onFilterSelect = { viewModel.selectedFilter.value = it },
                            selectedSortOption = selectedSortOption,
                            onSortSelect = { viewModel.selectedSortOption.value = it },
                            onOpenBarcodeScanner = { showBarcodeScannerDialog = true },
                            onClearOrder = { viewModel.clearOrder() },
                            onOpenHistory = { showSavedOrdersSheet = true },
                            onAddProductClick = {
                                initialCodeForNewProduct = ""
                                showAddProductDialog = true
                            }
                        )
                        CategoryFilterPillsRow(
                            selectedCategory = selectedCategory,
                            categoriesStats = categoryStats,
                            onCategorySelect = { viewModel.selectCategory(it) }
                        )
                    }
                }
            }

            // Category Overview Grid or Selected Category Header
            if (selectedCategory != null) {
                item(key = "selected_category_header") {
                    SelectedCategoryHeaderBar(
                        selectedCategoryName = selectedCategory!!,
                        totalCountInCategory = filteredProducts.size,
                        onBackToAllCategories = { viewModel.selectCategory(null) }
                    )
                }
            } else if (searchQuery.isBlank() && selectedFilter == CompanyFilter.ALL) {
                item(key = "category_grid_overview") {
                    CategoryGridOverview(
                        categoriesStats = categoryStats,
                        onCategoryClick = { categoryName ->
                            viewModel.selectCategory(categoryName)
                        }
                    )
                }
            }

            // Select All Bar item
            item(key = "select_all_bar") {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    color = Slate100,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = allVisibleSelected,
                            onCheckedChange = { checked ->
                                viewModel.toggleSelectAllVisible(checked)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = PrimaryBlue,
                                uncheckedColor = Slate500
                            ),
                            modifier = Modifier.testTag("select_all_checkbox")
                        )

                        Text(
                            text = if (selectedCategory != null) "Selecionar Tudo em $selectedCategory (${filteredProducts.size})"
                                   else "Selecionar Tudo (${filteredProducts.size} visíveis)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate900
                        )
                    }
                }
            }

            // Products List or Empty state
            if (filteredProducts.isEmpty()) {
                item(key = "empty_state") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "Nenhum produto encontrado para \"$searchQuery\"" else "Nenhum produto encontrado",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Text(
                                text = "Tente alterar os termos de pesquisa ou adicione este artigo ao catálogo.",
                                fontSize = 12.sp,
                                color = Slate500,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )
                            if (searchQuery.isNotBlank()) {
                                androidx.compose.material3.Button(
                                    onClick = {
                                        initialCodeForNewProduct = searchQuery.trim()
                                        showAddProductDialog = true
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                ) {
                                    Text("+ Adicionar Código ao Catálogo", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredProducts, key = { it.code }) { product ->
                    ProductItemCard(
                        product = product,
                        onQtyChange = { newQty -> viewModel.updateQuantity(product.code, newQty) },
                        onToggleSelect = { selected -> viewModel.toggleSelection(product.code, selected) },
                        onEditPrice = { productToEditPrice = product },
                        onToggleFavorite = { isFav -> viewModel.toggleFavorite(product.code, isFav) },
                        onOpenQuantityDialog = { productToSetQuantity = product }
                    )
                }
            }
        }
    }

    if (showSummarySheet) {
        OrderSummarySheet(
            selectedProducts = selectedProductsList,
            cartSummary = cartSummary,
            onDismiss = { showSummarySheet = false },
            onSaveOrder = { notes -> viewModel.saveCurrentOrder(notes) }
        )
    }

    if (showSavedOrdersSheet) {
        SavedOrdersSheet(
            savedOrders = savedOrders,
            onDeleteOrder = { id -> viewModel.deleteSavedOrder(id) },
            onRestoreOrder = { order -> viewModel.restoreSavedOrder(order.itemsSummary) },
            onDismiss = { showSavedOrdersSheet = false }
        )
    }

    if (showAddProductDialog) {
        AddProductDialog(
            initialCode = initialCodeForNewProduct,
            onDismiss = { showAddProductDialog = false },
            onAddProduct = { code, company, desc, priceRange, priceVal ->
                viewModel.addNewProduct(code, company, desc, priceRange, priceVal)
            }
        )
    }

    if (showBarcodeScannerDialog) {
        BarcodeScannerDialog(
            products = allProducts,
            onDismiss = { showBarcodeScannerDialog = false },
            onBarcodeScanned = { code ->
                viewModel.searchQuery.value = code
            }
        )
    }

    productToEditPrice?.let { product ->
        EditPriceDialog(
            product = product,
            onDismiss = { productToEditPrice = null },
            onConfirmPrice = { newPrice ->
                viewModel.updatePrice(product.code, newPrice)
            }
        )
    }

    productToSetQuantity?.let { product ->
        SetQuantityDialog(
            product = product,
            onDismiss = { productToSetQuantity = null },
            onConfirmQuantity = { newQty ->
                viewModel.updateQuantity(product.code, newQty)
            }
        )
    }

    if (showSyncDialog) {
        CatalogSyncDialog(
            initialUrl = syncUrl,
            syncStatus = syncStatus,
            onDismiss = { showSyncDialog = false },
            onSync = { url -> viewModel.syncCatalog(url) },
            onResetStatus = { viewModel.resetSyncStatus() }
        )
    }
}
