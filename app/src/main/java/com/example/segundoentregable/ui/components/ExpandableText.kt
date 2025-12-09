package com.example.segundoentregable.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

/**
 * Texto expandible que muestra un número limitado de líneas con opción de "Ver más"
 */
@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    collapsedMaxLines: Int = 3,
    expandText: String = "Ver más",
    collapseText: String = "Ver menos",
    expandTextColor: Color = MaterialTheme.colorScheme.primary
) {
    var isExpanded by remember { mutableStateOf(false) }
    var hasOverflow by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.animateContentSize()
    ) {
        Text(
            text = text,
            style = style,
            maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult ->
                if (!isExpanded) {
                    hasOverflow = textLayoutResult.hasVisualOverflow
                }
            }
        )

        if (hasOverflow || isExpanded) {
            Text(
                text = if (isExpanded) collapseText else expandText,
                style = MaterialTheme.typography.bodyMedium,
                color = expandTextColor,
                modifier = Modifier.clickable { isExpanded = !isExpanded }
            )
        }
    }
}
