package com.example.segundoentregable.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier as M

@Composable
fun ImagePlaceholderCircle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(Color.LightGray)
    )
}

@Composable
fun SimpleCard(title: String, subtitle: String = "", onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .background(MaterialTheme.colorScheme.surface),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(36.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp), textAlign = TextAlign.Center)
            if (subtitle.isNotBlank()) Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
