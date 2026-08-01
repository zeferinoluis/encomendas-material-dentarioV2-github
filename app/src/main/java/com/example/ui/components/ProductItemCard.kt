package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Product
import com.example.ui.theme.AssisdentBg
import com.example.ui.theme.AssisdentEmerald
import com.example.ui.theme.MontellanoBg
import com.example.ui.theme.MontellanoCyan
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

@Composable
fun ProductItemCard(
    product: Product,
    onQtyChange: (Int) -> Unit,
    onToggleSelect: (Boolean) -> Unit,
    onEditPrice: () -> Unit,
    onToggleFavorite: (Boolean) -> Unit,
    onOpenQuantityDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = product.qty > 0 || product.isSelected
    val subtotal = product.qty * product.minPrice

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) PrimaryBlue else Slate200,
                shape = RoundedCornerShape(12.dp)
            )
            .testTag("product_card_${product.code}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Top Row: Checkbox, Favorite Star, Company Badge, Code Badge, Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { checked -> onToggleSelect(checked) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PrimaryBlue,
                        uncheckedColor = Slate500
                    ),
                    modifier = Modifier.testTag("checkbox_${product.code}")
                )

                IconButton(
                    onClick = { onToggleFavorite(!product.isFavorite) },
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("favorite_star_${product.code}")
                ) {
                    Icon(
                        imageVector = if (product.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorito",
                        tint = if (product.isFavorite) Color(0xFFEAB308) else Slate500,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Company badge
                val isMontellano = product.company.equals("Montellano", ignoreCase = true)
                val badgeBg = if (isMontellano) MontellanoBg else AssisdentBg
                val badgeFg = if (isMontellano) MontellanoCyan else AssisdentEmerald

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = product.company,
                        color = badgeFg,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Code badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Slate100)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = product.code,
                        color = Slate700,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Price range with Edit button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = product.priceRange,
                            color = Slate900,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "(s/ IVA)",
                            color = Slate500,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    IconButton(
                        onClick = onEditPrice,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("edit_price_${product.code}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Preço",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Description
            Text(
                text = product.description,
                color = Slate900,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Row: Qty Stepper and Subtotal
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Stepper control
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                        .background(Color.White)
                ) {
                    IconButton(
                        onClick = { if (product.qty > 0) onQtyChange(product.qty - 1) },
                        enabled = product.qty > 0,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("qty_minus_${product.code}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Diminuir",
                            tint = if (product.qty > 0) Slate900 else Slate200,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(36.dp)
                            .clickable { onOpenQuantityDialog() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${product.qty}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (product.qty > 0) PrimaryBlue else Slate500,
                            modifier = Modifier.testTag("qty_text_${product.code}")
                        )
                    }

                    IconButton(
                        onClick = { onQtyChange(product.qty + 1) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("qty_plus_${product.code}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Aumentar",
                            tint = Slate900,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Subtotal display
                if (product.qty > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Subtotal:",
                            fontSize = 10.sp,
                            color = Slate500
                        )
                        Text(
                            text = String.format("%.2f €", subtotal),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            modifier = Modifier.testTag("subtotal_${product.code}")
                        )
                    }
                }
            }
        }
    }
}
