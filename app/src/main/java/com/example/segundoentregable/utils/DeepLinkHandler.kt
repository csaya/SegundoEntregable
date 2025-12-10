package com.example.segundoentregable.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manejador centralizado de deep links desde notificaciones
 */
object DeepLinkHandler {
    private val _pendingAttractionId = MutableStateFlow<String?>(null)
    val pendingAttractionId: StateFlow<String?> = _pendingAttractionId.asStateFlow()

    fun setAttractionId(id: String?) {
        _pendingAttractionId.value = id
    }

    fun clearAttractionId() {
        _pendingAttractionId.value = null
    }
}
