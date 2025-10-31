package com.example.segundoentregable.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.segundoentregable.data.model.AtractivoTuristico
import androidx.compose.material.icons.filled.Favorite

@Composable
fun RecomendacionCard(
    atractivo: AtractivoTuristico,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Placeholder para la Imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.Gray
                )
            }

            Column(Modifier.padding(12.dp)) {
                Text(atractivo.nombre, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(atractivo.descripcionCorta, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
    }
}

@Composable
fun CercanoItemRow(
    atractivo: AtractivoTuristico,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = Color.Gray
            )
        }

        Spacer(Modifier.width(16.dp))

        // Info
        Column(Modifier.weight(1f)) {
            Text(atractivo.nombre, style = MaterialTheme.typography.titleMedium)
            Text(atractivo.distanciaTexto, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}

@Composable
fun AttractionListItem(
    atractivo: AtractivoTuristico,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp), // Espacio vertical entre ítems
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Columna de Info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                atractivo.categoria,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                atractivo.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                atractivo.descripcionCorta,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Spacer(Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun FavoriteListItem(
    atractivo: AtractivoTuristico,
    onItemClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp) // Imagen cuadrada
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = Color.Gray
            )
        }

        Spacer(Modifier.width(16.dp))

        // Columna de Info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                atractivo.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                atractivo.categoria,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Spacer(Modifier.width(16.dp))

        IconButton(onClick = onFavoriteClick) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = "Quitar de favoritos",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}