package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CategoryStats
import com.example.data.CategoryUtils
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

@Composable
fun CategoryGridOverview(
    categoriesStats: List<CategoryStats>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Section Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "Categorias",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Categorias do Catálogo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Selecione uma categoria para explorar os artigos",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
            }
        }

        // 2-Column Grid using paired rows
        val chunked = categoriesStats.chunked(2)
        chunked.forEach { rowCategories ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowCategories.forEach { catStat ->
                    CategoryCardItem(
                        categoryStats = catStat,
                        onClick = { onCategoryClick(catStat.categoryName) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // If odd number in last row, add dummy spacer
                if (rowCategories.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun CategoryCardItem(
    categoryStats: CategoryStats,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val info = categoryStats.categoryInfo
    val categoryColor = Color(info.colorHex)
    val hasSelections = categoryStats.selectedProductsCount > 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (hasSelections) 1.5.dp else 1.dp,
                color = if (hasSelections) categoryColor else Slate200,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .testTag("category_card_${categoryStats.categoryName}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasSelections) categoryColor.copy(alpha = 0.05f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Emoji icon container
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(categoryColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = info.emoji,
                        fontSize = 18.sp
                    )
                }

                // Count badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate100)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${categoryStats.totalProducts}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category Title
            Text(
                text = info.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Description
            Text(
                text = info.description,
                fontSize = 11.sp,
                color = Slate500,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Footer indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (hasSelections) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(categoryColor)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${categoryStats.selectedProductsCount} art. (${categoryStats.totalQuantity} un.)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Ver artigos",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = categoryColor
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = if (hasSelections) categoryColor else Slate500,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun SelectedCategoryHeaderBar(
    selectedCategoryName: String,
    totalCountInCategory: Int,
    onBackToAllCategories: () -> Unit,
    modifier: Modifier = Modifier
) {
    val info = CategoryUtils.getCategoryInfo(selectedCategoryName)
    val categoryColor = Color(info.colorHex)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = categoryColor.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, categoryColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(
                onClick = onBackToAllCategories,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("back_to_categories_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar para Categorias",
                    tint = categoryColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Emoji
            Text(
                text = info.emoji,
                fontSize = 20.sp,
                modifier = Modifier.padding(end = 6.dp)
            )

            // Category Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = info.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "$totalCountInCategory artigos disponíveis",
                    fontSize = 11.sp,
                    color = Slate500
                )
            }

            // Clear category filter button
            IconButton(
                onClick = onBackToAllCategories,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Fechar Categoria",
                    tint = Slate500,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryFilterPillsRow(
    selectedCategory: String?,
    categoriesStats: List<CategoryStats>,
    onCategorySelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "Todas / Início" Pill
        val isAllSelected = selectedCategory == null
        FilterChip(
            selected = isAllSelected,
            onClick = { onCategorySelect(null) },
            label = {
                Text(
                    text = "Todas as Categorias",
                    fontSize = 12.sp,
                    fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = PrimaryBlue,
                selectedLabelColor = Color.White,
                containerColor = Color.White,
                labelColor = Slate900
            ),
            border = FilterChipDefaults.filterChipBorder(
                borderColor = Slate200,
                selectedBorderColor = PrimaryBlue,
                enabled = true,
                selected = isAllSelected
            ),
            modifier = Modifier.testTag("category_chip_all")
        )

        categoriesStats.forEach { stat ->
            val isSelected = selectedCategory.equals(stat.categoryName, ignoreCase = true)
            val info = stat.categoryInfo
            val chipColor = Color(info.colorHex)

            FilterChip(
                selected = isSelected,
                onClick = {
                    if (isSelected) onCategorySelect(null) else onCategorySelect(stat.categoryName)
                },
                leadingIcon = {
                    Text(text = info.emoji, fontSize = 12.sp)
                },
                label = {
                    Text(
                        text = "${stat.categoryName} (${stat.totalProducts})",
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = chipColor,
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = Slate900
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = Slate200,
                    selectedBorderColor = chipColor,
                    enabled = true,
                    selected = isSelected
                ),
                modifier = Modifier.testTag("category_chip_${stat.categoryName}")
            )
        }
    }
}
