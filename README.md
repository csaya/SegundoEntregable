# Guía de Turismo Arequipa (Prototipo Funcional)

Este repositorio contiene el código fuente de una aplicación móvil de guía turística de **Arequipa**, desarrollada como parte de un proyecto académico. La aplicación está construida **de forma nativa para Android** utilizando **Jetpack Compose** y una arquitectura **MVVM (Model-View-ViewModel)**. La idea es centralizar la información turística de Arequipa y ofrecer una experiencia fluida para los usuarios.

---

## Resumen Ejecutivo

La aplicación resuelve el problema de la dispersión de información turística en Arequipa. Centraliza datos de atractivos, permite filtrar por intereses, guardar favoritos y sincronizar la información entre dispositivos. Además, la app evolucionó desde un prototipo funcional hasta una versión robusta, con sesiones de usuario reales, sincronización offline/online y una navegación intuitiva.

La arquitectura sigue el principio de **"paquete por funcionalidad"**, donde cada pantalla (por ejemplo, Home, Map, Detail) tiene su propia carpeta con la vista (`Screen`) y su `ViewModel`. Esto facilita la organización y mantenimiento del código.

---

## Interfaces Implementadas

| Pantalla                              | Propósito                             | Comportamiento Principal                                                                                                         |
| ------------------------------------- | ------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------- |
| **HomeScreen.kt**                     | Punto de entrada principal.           | Muestra recomendaciones y lugares cercanos. La barra de búsqueda lleva a la lista de atractivos.                                 |
| **MapScreen.kt**                      | Visualizar atractivos en un mapa.     | Usa un `BottomSheetScaffold` para mostrar marcadores interactivos que abren detalles.                                            |
| **AttractionListScreen.kt**           | Buscar y filtrar atractivos.          | Incluye barra de búsqueda y filtros dinámicos. Actualiza resultados en tiempo real.                                              |
| **AttractionDetailScreen.kt**         | Mostrar detalles de un atractivo.     | Carga información detallada, permite guardar/quitar favoritos y dejar reseñas.                                                   |
| **FavoritesScreen.kt**                | Gestionar lugares guardados.          | Muestra la lista de favoritos del usuario y permite eliminarlos con actualización inmediata.                                     |
| **ProfileScreen.kt / LoginScreen.kt** | Manejar sesión y perfil del usuario.  | Usa un `SessionViewModel` global; si es invitado, muestra Login; si está logueado, muestra datos reales y permite cerrar sesión. |
| **RegisterScreen.kt**                 | Registro de nuevos usuarios.          | Permite crear una cuenta nueva y guardar los datos del usuario.                                                                  |
| **RutasScreen.kt**                    | Listar rutas turísticas predefinidas. | Muestra una lista de rutas recomendadas.                                                                                         |
| **RutaDetalleScreen.kt**              | Detalle de una ruta turística.        | Muestra información detallada y permite navegar a sus atractivos.                                                                |
| **PlannerScreen.kt**                  | Planificador de rutas personales.     | Permite crear, editar y guardar rutas personalizadas.                                                                            |
| **MisRutasScreen.kt**                 | CRUD de rutas guardadas.              | Permite ver, editar y eliminar rutas guardadas.                                                                                  |

---

## Instrucciones de Ejecución

### Prerrequisitos

* **Android Studio** (versión Iguana o Jellyfish recomendada)
* **Emulador Android** (API 31 o superior)
* **Git** instalado

### Pasos para Ejecutar

1. **Clonar el repositorio:**

   ```bash
   git clone https://github.com/csaya/SegundoEntregable.git
   ```
2. **Abrir el proyecto:**

   * Abre Android Studio.
   * Selecciona **Open** y elige la carpeta del proyecto.
3. **Sincronizar dependencias:**

   * Espera que Gradle descargue las librerías necesarias (Compose, Navigation, etc.).
4. **Ejecutar la aplicación:**

   * Selecciona un emulador o conecta un dispositivo físico.
   * Pulsa **Run (▶)** para compilar y ejecutar.

---

## Cómo Probar

### Flujo Principal

La app inicia en **HomeScreen** con sesión de invitado.

* Navega por Inicio, Mapa y Lista.
* Usa la barra de búsqueda para filtrar.
* Explora recomendaciones y lugares cercanos.

### Probar Favoritos

* Entra al detalle de un atractivo y pulsa **Guardar**.
* Ve a **Favoritos** para ver la lista.
* Puedes eliminar favoritos con un toque.

### Probar Autenticación

* Ve a **Perfil**.
* Como invitado verás **Login**; puedes registrarte.
* Inicia sesión para ver tus datos en **ProfileScreen**.
* Puedes cerrar sesión.

### Probar Rutas

* Revisa las rutas en **Rutas**.
* Entra al detalle de una ruta.
* En **Mis Rutas**, edita o elimina rutas guardadas.

### Probar Planificador de Rutas

* En **PlannerScreen**, selecciona atractivos.
* Optimiza el orden.
* Guarda la ruta personalizada.

### Probar Reseñas

* En el detalle de un atractivo, deja una reseña.
* Edita o elimina tus reseñas.
* Vota por reseñas de otros usuarios.

### Notificaciones y Deep Links

* La app maneja deep links hacia detalles de atractivos.
* Las notificaciones de proximidad pueden probarse desde **Perfil**.

---

## Integrantes

Proyecto desarrollado por:

* **Salhua Apfata Cristian Roberto**
* **Saya Vargas Cristian Raúl**
