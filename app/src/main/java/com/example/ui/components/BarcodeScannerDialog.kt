package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.data.Product
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@Composable
fun BarcodeScannerDialog(
    products: List<Product>,
    onDismiss: () -> Unit,
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    var manualBarcode by remember { mutableStateOf("") }
    var isFlashOn by remember { mutableStateOf(false) }
    var zoomLevel by remember { mutableFloatStateOf(0.35f) } // Default 2x zoom for small QR codes!

    // Camera permission check
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasCameraPermission = isGranted
            if (!isGranted) {
                Toast.makeText(context, "Permissão de câmara necessária para ler códigos", Toast.LENGTH_LONG).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    // Laser animation overlay
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 190f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("barcode_scanner_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Leitor QR & Códigos Pequenos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar", tint = Slate500)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Camera Viewfinder Box with Flash & Zoom controls
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0F172A))
                        .border(2.dp, PrimaryBlue, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasCameraPermission) {
                        // Real CameraX live feed + ML Kit scanning
                        CameraBarcodeScannerView(
                            isFlashOn = isFlashOn,
                            zoomLinear = zoomLevel,
                            onBarcodeScanned = { rawCode ->
                                val processedCode = parseScannedQrCode(rawCode, products)
                                Toast.makeText(context, "Código lido: $processedCode", Toast.LENGTH_SHORT).show()
                                onBarcodeScanned(processedCode)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Laser Line Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(2.dp)
                                .padding(top = laserY.dp)
                                .background(Color(0xFFEF4444))
                        )

                        // Flashlight Button (Top Right Overlay)
                        IconButton(
                            onClick = { isFlashOn = !isFlashOn },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(36.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Lanterna",
                                tint = if (isFlashOn) Color(0xFFFFD700) else Color.White
                            )
                        }

                        // Zoom Controls Bar (Bottom Overlay inside camera)
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Zoom",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            ZoomPresetChip(label = "1x", isSelected = zoomLevel == 0.0f) { zoomLevel = 0.0f }
                            ZoomPresetChip(label = "2x", isSelected = zoomLevel == 0.35f) { zoomLevel = 0.35f }
                            ZoomPresetChip(label = "3x", isSelected = zoomLevel == 0.65f) { zoomLevel = 0.65f }
                            ZoomPresetChip(label = "5x", isSelected = zoomLevel == 1.0f) { zoomLevel = 1.0f }
                        }
                    } else {
                        // Permission request fallback
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Permissão da câmara desativada",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { launcher.launch(Manifest.permission.CAMERA) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Text("Ativar Câmara", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Helpful tip for tiny QR codes
                Text(
                    text = "💡 Dica: Afaste a câmara 20cm e use Zoom (2x/3x) + Lanterna se a embalagem for pequena ou desfocar.",
                    fontSize = 11.sp,
                    color = Slate500,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Test Catalog Codes (Quick Tap)
                Text(
                    text = "Atalhos rápidos para testar no catálogo:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate900,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products.take(8)) { product ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            modifier = Modifier.clickable {
                                onBarcodeScanned(product.code)
                                Toast.makeText(context, "Código lido: ${product.code}", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(text = product.code, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                Text(text = product.description.take(14) + "...", fontSize = 10.sp, color = Slate500)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Manual input or hardware scanner gun
                OutlinedTextField(
                    value = manualBarcode,
                    onValueChange = { manualBarcode = it },
                    label = { Text("Digitar / Pistola de Leitura") },
                    placeholder = { Text("Ex: 1001, 2002...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (manualBarcode.isNotBlank()) {
                                onBarcodeScanned(manualBarcode.trim())
                                onDismiss()
                            }
                        }
                    ),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate500)
                    },
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900,
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = Slate500,
                        focusedPlaceholderColor = Slate500,
                        unfocusedPlaceholderColor = Slate500,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = com.example.ui.theme.Slate200,
                        cursorColor = PrimaryBlue
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Slate900, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                            if (manualBarcode.isBlank()) {
                                Toast.makeText(context, "Insira um código de barras", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onBarcodeScanned(manualBarcode.trim())
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Pesquisar Código")
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoomPresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isSelected) PrimaryBlue else Color.White.copy(alpha = 0.25f),
        contentColor = Color.White,
        modifier = Modifier.size(28.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraBarcodeScannerView(
    isFlashOn: Boolean,
    zoomLinear: Float,
    onBarcodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var camera by remember { mutableStateOf<Camera?>(null) }
    var hasScanned by remember { mutableStateOf(false) }

    // Update Torch state when toggled
    LaunchedEffect(isFlashOn, camera) {
        camera?.cameraControl?.enableTorch(isFlashOn)
    }

    // Update Zoom level when changed
    LaunchedEffect(zoomLinear, camera) {
        camera?.cameraControl?.setLinearZoom(zoomLinear)
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)

            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    // Enable all barcode formats including DataMatrix & QR
                    val options = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                            Barcode.FORMAT_QR_CODE,
                            Barcode.FORMAT_DATA_MATRIX,
                            Barcode.FORMAT_EAN_13,
                            Barcode.FORMAT_EAN_8,
                            Barcode.FORMAT_CODE_128,
                            Barcode.FORMAT_CODE_39,
                            Barcode.FORMAT_UPC_A,
                            Barcode.FORMAT_UPC_E,
                            Barcode.FORMAT_AZTEC,
                            Barcode.FORMAT_PDF417
                        )
                        .build()
                    val scanner = BarcodeScanning.getClient(options)

                    // Request High Resolution (1920x1080) for tiny packaging matrix codes
                    @Suppress("DEPRECATION")
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size(1920, 1080))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(executor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null && !hasScanned) {
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        val rawValue = barcode.rawValue
                                        if (!rawValue.isNullOrBlank() && !hasScanned) {
                                            hasScanned = true
                                            onBarcodeScanned(rawValue)
                                            break
                                        }
                                    }
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    cameraProvider.unbindAll()
                    val boundCamera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    camera = boundCamera

                    // Initial zoom and torch setup
                    boundCamera.cameraControl.setLinearZoom(zoomLinear)
                    boundCamera.cameraControl.enableTorch(isFlashOn)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, executor)

            previewView
        },
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                camera?.cameraControl?.let { control ->
                    val factory = SurfaceOrientedMeteringPointFactory(size.width.toFloat(), size.height.toFloat())
                    val point = factory.createPoint(offset.x, offset.y)
                    val action = FocusMeteringAction.Builder(point).build()
                    control.startFocusAndMetering(action)
                }
            }
        }
    )
}

/**
 * Intelligently extracts product codes or clean references from HIBC DataMatrix, GS1, URLs, or barcodes.
 */
fun parseScannedQrCode(rawScanned: String, products: List<Product>): String {
    val cleanRaw = rawScanned.trim()
    if (cleanRaw.isBlank()) return ""

    // 1. Direct match with product code
    val exactProduct = products.firstOrNull { it.code.equals(cleanRaw, ignoreCase = true) }
    if (exactProduct != null) return exactProduct.code

    // 2. Check parenthesized manufacturer references in product descriptions (e.g., "(25950)", "(36588)")
    val parenthesizedRefRegex = Regex("""\(([a-zA-Z0-9\-\.\/]+)\)""")
    val refMatchProduct = products.firstOrNull { prod ->
        val refs = parenthesizedRefRegex.findAll(prod.description).map { it.groupValues[1] }
        refs.any { ref ->
            ref.length >= 3 && (cleanRaw.contains(ref, ignoreCase = true) || ref.equals(cleanRaw, ignoreCase = true))
        }
    }
    if (refMatchProduct != null) return refMatchProduct.code

    // 3. Check if raw text contains any known product code as a word or token
    val matchedProduct = products.firstOrNull { prod ->
        prod.code.isNotBlank() && prod.code.length >= 3 && cleanRaw.contains(prod.code, ignoreCase = true)
    }
    if (matchedProduct != null) return matchedProduct.code

    // 4. HIBC Standard format (e.g., "*+D9334350421/$$329103120250073781*")
    if (cleanRaw.contains("+")) {
        val hibcPrimary = cleanRaw.substringAfter("+").substringBefore("/").substringBefore("*").trim()
        if (hibcPrimary.isNotBlank()) {
            val hibcProduct = products.firstOrNull { prod ->
                prod.code.equals(hibcPrimary, ignoreCase = true) ||
                prod.description.contains(hibcPrimary, ignoreCase = true) ||
                hibcPrimary.contains(prod.code, ignoreCase = true) ||
                parenthesizedRefRegex.findAll(prod.description).any { m ->
                    val ref = m.groupValues[1]
                    ref.length >= 3 && hibcPrimary.contains(ref, ignoreCase = true)
                }
            }
            if (hibcProduct != null) return hibcProduct.code
            // If primary ID extracted clean, return primary HIBC segment e.g. D9334350421
            return hibcPrimary
        }
    }

    // 5. GS1 or DataMatrix parsing: strip common GS1 application identifiers like (01), (10), (21)
    val strippedGs1 = cleanRaw.replace(Regex("""\(\d+\)"""), " ").trim()
    val gs1Tokens = strippedGs1.split(Regex("""\s+""")).filter { it.length >= 3 }
    for (token in gs1Tokens) {
        val gs1Match = products.firstOrNull { prod ->
            prod.code.contains(token, ignoreCase = true) ||
            prod.description.contains(token, ignoreCase = true)
        }
        if (gs1Match != null) return gs1Match.code
    }

    // 6. URL or web link segment
    if (cleanRaw.startsWith("http://", ignoreCase = true) || cleanRaw.startsWith("https://", ignoreCase = true)) {
        val lastSegment = cleanRaw.substringAfterLast("/").substringBefore("?").substringBefore("#")
        if (lastSegment.isNotBlank()) return lastSegment
    }

    return cleanRaw
}
