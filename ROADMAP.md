# 🗺️ ROADMAP - Guía Turística Inteligente & Offline de Arequipa

## 📊 Estado Actual del Proyecto

### ✅ COMPLETADO (Lo que ya tenemos)

#### A. Arquitectura Base
- [x] Clean Architecture + MVVM implementado
- [x] Room Database con 6 entidades (User, Atractivo, Review, Favorito, GaleriaFoto, Actividad)
- [x] Repositorios (User, Attraction, Favorite)
- [x] ViewModels con StateFlow
- [x] Navegación con Jetpack Navigation Compose

#### B. Datos MINCETUR Integrados
- [x] **80 atractivos turísticos** reales de Arequipa
- [x] Coordenadas GPS precisas
- [x] Descripciones completas (corta y larga)
- [x] Horarios, precios, altitud, estado actual
- [x] Galerías de fotos (múltiples por atractivo)
- [x] Actividades disponibles por atractivo
- [x] Categorías: Cultural, Natural, etc.
- [x] DataImporter para carga automática desde JSON

#### C. Pantallas Implementadas
| Pantalla | Estado | Funcionalidad |
|----------|--------|---------------|
| HomeScreen | ✅ | Recomendaciones + Cercanos + Búsqueda |
| AttractionListScreen | ✅ | Lista filtrable por categoría |
| AttractionDetailScreen | ✅ | Galería, info detallada, actividades, reseñas |
| MapScreen | ✅ | Mapa con marcadores + BottomSheet |
| FavoritesScreen | ✅ | Lista de favoritos (requiere login) |
| LoginScreen | ✅ | Autenticación local |
| RegisterScreen | ✅ | Registro de usuarios |
| ProfileScreen | ✅ | Perfil de usuario |

#### D. Funcionalidades Core
- [x] Lazy Registration (Modo invitado completo)
- [x] Sistema de favoritos
- [x] Sistema de reseñas (rating + comentario)
- [x] Geolocalización (GPS real)
- [x] Cálculo de distancias
- [x] Persistencia de sesión (SharedPreferences)
- [x] Carga de imágenes con Coil (caché)
- [x] Google Maps integrado

---

## ✅ MEJORAS COMPLETADAS (Prioridad Alta)

### 1. Indicador "Abierto Ahora / Cerrado" ✅
**Estado:** Implementado
**Archivos:** `HorarioUtils.kt`, `CommonComponents.kt`, `AttractionDetailScreen.kt`
- Parsea horarios en formato "HH:MM a.m. - HH:MM p.m."
- Muestra badge verde "Abierto" o rojo "Cerrado"
- Indica hora de próximo cambio

### 2. Botón "Cómo Llegar" (Navegación Externa) ✅
**Estado:** Implementado
**Archivos:** `NavigationUtils.kt`, `AttractionDetailScreen.kt`
- Abre Google Maps con navegación turn-by-turn
- Fallback a navegador web si no hay Google Maps
- Soporta modos: driving, walking, bicycling, transit

### 3. Búsqueda Global Funcional ✅
**Estado:** Implementado
**Archivos:** `CommonComponents.kt`, `HomeScreen.kt`, `AttractionListScreen.kt`, `AppNavGraph.kt`
- SearchBar editable en HomeScreen
- Navega a lista con query como parámetro
- Filtrado en tiempo real por nombre/descripción

### 4. Clustering de Marcadores en Mapa ✅
**Estado:** Implementado
**Archivos:** `MapComponent.kt`, `build.gradle.kts`
- Agrupa marcadores cercanos automáticamente
- Al hacer zoom, clusters se expanden
- Click en cluster muestra primer atractivo

---

## 🚧 PENDIENTE DE MEJORA (Prioridad Media)

### Prioridad MEDIA 🟡

#### 5. Splash Screen con Misti
**Estado:** No existe
**Impacto:** Medio - Primera impresión del usuario

#### 6. Filtros Avanzados en Lista
**Estado:** Solo categoría
**Necesita:** Precio (Gratis/Pagado), Distancia, Rating

#### 7. Ordenamiento Inteligente
**Estado:** Lista estática
**Necesita:** Ordenar por distancia, rating, popularidad

#### 8. Pull-to-Refresh
**Estado:** No implementado
**Impacto:** UX esperada en listas

#### 9. Empty States Mejorados
**Estado:** Básico
**Necesita:** Ilustraciones y mensajes amigables

### Prioridad BAJA 🟢

#### 10. Animaciones y Transiciones
**Estado:** Sin animaciones
**Necesita:** Shared element transitions, fade in/out

#### 11. Dark Mode
**Estado:** No implementado

#### 12. Onboarding (Tutorial inicial)
**Estado:** No existe

---

## 🆕 FUNCIONALIDADES NUEVAS A IMPLEMENTAR

### FASE 1: Offline-First (El Diferenciador) ⭐
**Tiempo estimado:** 2-3 días

| Tarea | Descripción | Complejidad |
|-------|-------------|-------------|
| Caché de imágenes offline | Descargar imágenes principales al instalar | Media |
| Detector de conectividad | NetworkCallback para saber si hay internet | Baja |
| UI de modo offline | Banner "Sin conexión" + funcionalidad completa | Baja |
| Mapas offline | Tiles precargados de Arequipa (MapBox o similar) | Alta |

### FASE 2: Sincronización con Backend (Reseñas Compartidas)
**Tiempo estimado:** 3-4 días

| Tarea | Descripción | Complejidad |
|-------|-------------|-------------|
| API REST (Firebase/Supabase) | Backend para reseñas | Media |
| Sync bidireccional | Subir reseñas locales cuando hay internet | Alta |
| Conflictos de datos | Resolver cuando hay cambios offline y online | Alta |
| WorkManager | Sincronización en background | Media |

### FASE 3: Experiencia Premium
**Tiempo estimado:** 2-3 días

| Tarea | Descripción | Complejidad |
|-------|-------------|-------------|
| Rutas turísticas | "Ruta del Sillar", "Ruta Gastronómica" | Media |
| Notificaciones | "Estás cerca de X lugar" (Geofencing) | Alta |
| Compartir en redes | Compartir lugar con imagen | Baja |
| Widget de Android | Lugar del día en home screen | Media |

---

## 📋 PLAN DE IMPLEMENTACIÓN SUGERIDO

### Sprint 1 (Semana 1): Pulir Core
```
Día 1-2:
  □ Implementar "Abierto Ahora / Cerrado"
  □ Botón "Cómo Llegar" con Google Maps
  
Día 3-4:
  □ Búsqueda global funcional
  □ Filtros avanzados (precio, distancia)
  
Día 5:
  □ Clustering de marcadores
  □ Testing y bug fixes
```

### Sprint 2 (Semana 2): Offline-First
```
Día 1-2:
  □ Caché de imágenes offline
  □ Detector de conectividad
  
Día 3-4:
  □ UI de modo offline
  □ Splash Screen
  
Día 5:
  □ Pull-to-refresh
  □ Empty states mejorados
```

### Sprint 3 (Semana 3): Backend & Sync
```
Día 1-2:
  □ Configurar Firebase/Supabase
  □ API para reseñas
  
Día 3-4:
  □ Sincronización de reseñas
  □ WorkManager para sync en background
  
Día 5:
  □ Testing de sincronización
  □ Manejo de conflictos
```

### Sprint 4 (Semana 4): Polish & Launch
```
Día 1-2:
  □ Animaciones y transiciones
  □ Dark mode
  
Día 3-4:
  □ Onboarding
  □ Optimización de rendimiento
  
Día 5:
  □ Testing final
  □ Preparar para Play Store
```

---

## 🎯 MÉTRICAS DE ÉXITO

| Métrica | Objetivo | Actual |
|---------|----------|--------|
| Atractivos en BD | 50+ | ✅ 80 |
| Tiempo de carga inicial | < 2s | ⏳ Por medir |
| Funciona sin internet | 100% navegación | ❌ 0% |
| Crash rate | < 1% | ⏳ Por medir |
| Tamaño APK | < 50MB | ⏳ Por medir |

---

## 🛠️ STACK TECNOLÓGICO

### Actual
- **UI:** Jetpack Compose + Material 3
- **Arquitectura:** MVVM + Clean Architecture
- **BD Local:** Room (SQLite)
- **Imágenes:** Coil 2.7.0
- **Mapas:** Google Maps Compose 6.1.1
- **Async:** Kotlin Coroutines + Flow
- **DI:** Manual (sin Hilt/Koin por simplicidad)

### Propuesto para Backend
- **Opción A:** Firebase (Firestore + Auth + Storage)
  - Pros: Fácil, gratis hasta cierto punto, tiempo real
  - Cons: Vendor lock-in, costos escalan
  
- **Opción B:** Supabase (PostgreSQL + Auth + Storage)
  - Pros: Open source, SQL real, más control
  - Cons: Más configuración inicial

---

## 📝 NOTAS TÉCNICAS

### Estructura de Archivos Actual
```
app/src/main/
├── assets/
│   ├── atractivos.json (80 lugares)
│   ├── galerias.json (fotos)
│   └── actividades.json (actividades)
├── java/.../
│   ├── data/
│   │   ├── local/ (Room: entities, DAOs, database)
│   │   ├── model/ (Domain models)
│   │   └── repository/ (Data sources)
│   ├── navigation/ (NavGraph)
│   ├── ui/
│   │   ├── components/ (Reusables)
│   │   ├── home/, list/, detail/, map/, favorites/
│   │   └── login/, register/, profile/
│   └── utils/ (DataImporter)
└── res/ (resources)
```

### Decisiones de Diseño
1. **Sin Hilt/Koin:** Inyección manual para simplicidad académica
2. **Room como fuente única:** Todos los datos pasan por Room
3. **JSON Seeding:** Datos iniciales desde assets, no hardcodeados
4. **Lazy Registration:** El usuario explora sin cuenta

---

## ❓ PREGUNTAS PENDIENTES

1. **¿Backend propio o BaaS?**
   - Firebase es más rápido de implementar
   - Supabase da más control

2. **¿Mapas offline?**
   - Google Maps no soporta offline fácil
   - Alternativas: MapBox, OSMDroid

3. **¿Monetización futura?**
   - Ads, premium, partnerships con negocios locales

4. **¿Multi-idioma?**
   - Español base, ¿inglés para turistas extranjeros?

---

*Última actualización: Diciembre 2024*
*Versión del documento: 1.0*
