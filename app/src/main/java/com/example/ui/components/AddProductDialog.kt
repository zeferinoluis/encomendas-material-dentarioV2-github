package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.TextStyle
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    initialCode: String = "",
    onDismiss: () -> Unit,
    onAddProduct: (code: String, company: String, description: String, priceRange: String, priceValue: Double) -> Unit
) {
    val context = LocalContext.current
    var code by remember(initialCode) { mutableStateOf(initialCode) }
    var company by remember { mutableStateOf("Montellano") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }

    var expandedCompanyDropdown by remember { mutableStateOf(false) }
    val companies = listOf("Montellano", "Assisdent", "Outro")

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Slate900,
        unfocusedTextColor = Slate900,
        focusedLabelColor = PrimaryBlue,
        unfocusedLabelColor = Slate500,
        focusedPlaceholderColor = Slate500,
        unfocusedPlaceholderColor = Slate500,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = PrimaryBlue,
        unfocusedBorderColor = Slate200,
        cursorColor = PrimaryBlue
    )
    val inputTextStyle = TextStyle(color = Slate900, fontSize = 14.sp)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_product_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Adicionar Novo Produto",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Insira os dados do produto para adicionar ao catálogo local.",
                    fontSize = 12.sp,
                    color = Slate500,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Fornecedor / Empresa Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedCompanyDropdown,
                    onExpandedChange = { expandedCompanyDropdown = !expandedCompanyDropdown }
                ) {
                    OutlinedTextField(
                        value = company,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fornecedor / Empresa") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCompanyDropdown) },
                        colors = textFieldColors,
                        textStyle = inputTextStyle,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCompanyDropdown,
                        onDismissRequest = { expandedCompanyDropdown = false }
                    ) {
                        companies.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item, color = Slate900) },
                                onClick = {
                                    company = item
                                    expandedCompanyDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Código do produto
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Código / Referência") },
                    placeholder = { Text("Ex: 123456") },
                    singleLine = true,
                    colors = textFieldColors,
                    textStyle = inputTextStyle,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Descrição
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição do Produto") },
                    placeholder = { Text("Ex: Agulhas curtas 30G box 100") },
                    colors = textFieldColors,
                    textStyle = inputTextStyle,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Preço
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Preço Estimado (s/ IVA em €)") },
                    placeholder = { Text("Ex: 12.50") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = textFieldColors,
                    textStyle = inputTextStyle,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (code.isBlank() || description.isBlank()) {
                                Toast.makeText(context, "Preencha o código e descrição", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val priceVal = priceText.replace(",", ".").toDoubleOrNull() ?: 0.0
                            val priceFormatted = if (priceVal > 0) String.format("%.2f €", priceVal) else "0.00 €"
                            onAddProduct(code, company, description, priceFormatted, priceVal)
                            Toast.makeText(context, "Produto adicionado!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Adicionar")
                    }
                }
            }
        }
    }
}
