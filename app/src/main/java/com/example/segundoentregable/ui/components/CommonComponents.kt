package com.example.segundoentregable.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    query: String, // Recibe el estado
    onQueryChange: (String) -> Unit, // Recibe el callback
    onSearchClicked: () -> Unit,
    placeholder: String = "Buscar..."
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange, // Llama al callback
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar") },
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .clickable { onSearchClicked() },
        enabled = false, // Sigue deshabilitado para actuar como botón
        colors = TextFieldDefaults.colors(
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledIndicatorColor = Color.Transparent
        )
    )
}