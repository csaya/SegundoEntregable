package com.example.segundoentregable.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.segundoentregable.data.model.Review
import com.example.segundoentregable.utils.HorarioUtils

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClicked: () -> Unit,
    placeholder: String = "Buscar..."
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar") },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onSearchClicked) {
                    Icon(Icons.Filled.Search, contentDescription = "Buscar", tint = MaterialTheme.colorScheme.primary)
                }
            }
        },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
fun RatingBar(
    modifier: Modifier = Modifier,
    rating: Float,
    starSize: Dp = 16.dp,
    starColor: Color = Color(0xFFFFC107)
) {
    Row(modifier = modifier) {
        (1..5).forEach { index ->
            val icon = when {
                rating >= index -> Icons.Filled.Star
                rating >= (index - 0.5f) -> Icons.AutoMirrored.Filled.StarHalf
                else -> Icons.Filled.StarBorder
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = starColor,
                modifier = Modifier.size(starSize)
            )
        }
    }
}

@Composable
fun ReviewCard(
    review: Review,
    currentUserEmail: String? = null,
    onLike: ((String) -> Unit)? = null,
    onDislike: ((String) -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isOwnReview = currentUserEmail != null && review.userEmail == currentUserEmail
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header: Avatar, nombre, tiempo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar con inicial
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = review.userName.firstOrNull()?.uppercase() ?: "U",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            review.userName, 
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isOwnReview) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "(Tú)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        review.getRelativeTime(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                // Rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB800),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        String.format("%.1f", review.rating),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Comentario
            if (review.comment.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    review.comment, 
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Acciones: Útil / No útil / Editar / Eliminar
            Spacer(Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Calificación de utilidad
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "¿Útil?",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    
                    // Botón Útil
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(enabled = onLike != null && !isOwnReview) { onLike?.invoke(review.id) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Filled.ThumbUp,
                            contentDescription = "Útil",
                            modifier = Modifier.size(16.dp),
                            tint = if (review.likes > 0) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            review.likes.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (review.likes > 0) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                    
                    // Botón No útil
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(enabled = onDislike != null && !isOwnReview) { onDislike?.invoke(review.id) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Filled.ThumbDown,
                            contentDescription = "No útil",
                            modifier = Modifier.size(16.dp),
                            tint = if (review.dislikes > 0) MaterialTheme.colorScheme.error else Color.Gray
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            review.dislikes.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (review.dislikes > 0) MaterialTheme.colorScheme.error else Color.Gray
                        )
                    }
                }
                
                // Botones de edición/eliminación (solo para propia reseña)
                if (isOwnReview && (onEdit != null || onDelete != null)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        onEdit?.let {
                            IconButton(
                                onClick = it,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Editar",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        onDelete?.let {
                            IconButton(
                                onClick = it,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Eliminar",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente que muestra el estado de apertura de un lugar.
 * Muestra "Abierto" en verde o "Cerrado" en rojo, con información adicional.
 */
@Composable
fun OpenStatusBadge(
    horario: String,
    modifier: Modifier = Modifier,
    showDetails: Boolean = true
) {
    val estado = HorarioUtils.getEstadoActual(horario)
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (estado.isOpen) Color(0xFF4CAF50) else Color(0xFFF44336))
        )
        
        Text(
            text = estado.mensaje,
            color = if (estado.isOpen) Color(0xFF4CAF50) else Color(0xFFF44336),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        
        if (showDetails && estado.proximoCambio != null) {
            Text(
                text = "· ${estado.proximoCambio}",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Versión compacta del badge de estado (solo el indicador y texto corto).
 */
@Composable
fun OpenStatusChip(
    horario: String,
    modifier: Modifier = Modifier
) {
    val estado = HorarioUtils.getEstadoActual(horario)
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (estado.isOpen) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFF44336).copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (estado.isOpen) Color(0xFF4CAF50) else Color(0xFFF44336))
            )
            Text(
                text = if (estado.isOpen) "Abierto" else "Cerrado",
                color = if (estado.isOpen) Color(0xFF4CAF50) else Color(0xFFF44336),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        }
    }
}