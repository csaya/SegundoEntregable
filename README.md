# 🏞️ Guía de Turismo Arequipa (Prototipo Funcional)

Este repositorio contiene el código fuente de un **prototipo funcional** para una aplicación móvil de guía turística de **Arequipa**, desarrollada como parte de un proyecto académico. La aplicación está construida **de forma nativa para Android** utilizando **Jetpack Compose** y una arquitectura **MVVM (Model-View-ViewModel)**.

---

## 📋 Resumen Ejecutivo

El objetivo de este proyecto es resolver la **dispersión de información turística** en Arequipa mediante una aplicación que centraliza datos sobre atractivos turísticos. Permite a los usuarios **descubrir lugares, ver su ubicación, filtrar por intereses y guardar sus favoritos**.

El prototipo cumple con dos criterios principales:

1. **Implementación de UI:** Interfaces desarrolladas con **Jetpack Compose**, basadas en los mockups de alta fidelidad del diseño propuesto.
2. **Lógica de Comportamiento Básico:** La app es completamente funcional gracias a un **FakeAttractionRepository**, que simula un backend local. Esto permite cargar datos, filtrar listas, guardar favoritos y manejar una sesión temporal de usuario sin conexión a internet.

La arquitectura está organizada bajo el principio de **“paquete por funcionalidad” (Package by Feature)**. Cada pantalla (por ejemplo, *Home*, *Map*, *Detail*) contiene su propia carpeta con la vista (*Screen*) y su respectivo *ViewModel*.

---

## 🧭 Interfaces Implementadas

Se desarrollaron **6 flujos principales** y un flujo adicional de **autenticación**, siguiendo buenas prácticas de componentización y reutilización de UI.

| Pantalla (Archivo) | Propósito | Comportamiento Principal |
|--------------------|------------|---------------------------|
| **HomeScreen.kt** | Punto de entrada principal. | Muestra listas de “Recomendaciones” y “Cercanos a ti”. La barra de búsqueda (simulada) navega a la pantalla de lista. |
| **MapScreen.kt** | Visualizar atractivos en un mapa. | Usa un *BottomSheetScaffold* para mostrar marcadores interactivos que abren detalles. |
| **AttractionListScreen.kt** | Buscar y filtrar atractivos. | Incluye barra de búsqueda funcional y filtros dinámicos. Actualiza resultados en tiempo real según el *ViewModel*. |
| **AttractionDetailScreen.kt** | Mostrar detalles de un atractivo. | Recibe un `attractionId` por navegación y carga información detallada. Permite guardar y quitar de favoritos. |
| **FavoritesScreen.kt** | Gestionar lugares guardados. | Muestra la lista de favoritos del usuario, permitiendo eliminarlos con actualización inmediata de UI. |
| **ProfileScreen.kt / LoginScreen.kt** | Manejar sesión y perfil del usuario. | Usa un *SessionViewModel* global. Si el usuario está invitado, muestra *LoginScreen*; si está logueado, muestra *ProfileScreen* con datos reales y botón de cerrar sesión. |

---

## 🚀 Instrucciones de Ejecución

### 🔧 Prerrequisitos
- **Android Studio** (versión Iguana o Jellyfish recomendada)
- **Emulador Android** (API 31 o superior)
- **Git** instalado

### ▶️ Pasos para Ejecutar

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/csaya/SegundoEntregable.git
   ```
2. **Abrir el proyecto:**
   - Abre Android Studio.
   - Selecciona **Open** y elige la carpeta del proyecto.
3. **Sincronizar dependencias:**
   - Espera que Gradle descargue las librerías necesarias (Compose, Navigation, etc.).
4. **Ejecutar la aplicación:**
   - Selecciona un emulador o conecta un dispositivo físico.
   - Pulsa **Run (▶)** para compilar y ejecutar.

---

## 🧪 Cómo Probar

### Flujo principal
La app inicia en **HomeScreen** con sesión de *invitado*. Puedes navegar por las pestañas **Inicio**, **Mapa** y **Lista** sin restricciones.

### Probar Favoritos
Entra al detalle de un atractivo (por ejemplo, *Cañón del Colca*) y presiona **Guardar**. Luego, visita la pestaña **Favoritos** para ver el cambio reflejado.

### Probar Autenticación
1. Navega a la pestaña **Perfil**.
2. Si estás como *invitado*, aparecerá la pantalla **Login**.
3. Puedes registrarte desde el enlace a **RegisterScreen**.
4. Inicia sesión con tus credenciales.
5. Al ingresar, verás tu **nombre y correo** en *ProfileScreen*.
6. Usa el botón **Cerrar Sesión** para volver al estado invitado.

---

## Integrantes

Proyecto desarrollado por:
   - Salhua Apfata Cristian Roberto
   - Saya Vargas Cristian Raul
