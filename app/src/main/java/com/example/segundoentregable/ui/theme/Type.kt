package com.example.segundoentregable.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

val AppTypography: Typography = Typography().let { base ->
    base.copy(
        headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
        titleLarge    = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium   = base.titleMedium.copy(fontWeight = FontWeight.SemiBold)
    )
}
