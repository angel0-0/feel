# Feel — Aplicación Android de Microinteracciones

## Descripción del Proyecto

**Feel** es una aplicación Android desarrollada e## Funcionalidades Detalladas

### 1. Pantalla Principal (SplashFragment)

-  B## Criterios de Evaluación

### Implementación de Temas (80%)

-  **Fragmentos y Navegación (10%)**: Implementación correcta de Fragment, Navigation
-  **UI y UX (15%)**: Layouts, Views, estilos, accesibilidad
-  **Transiciones (10%)**: Animaciones fluidas y profesionales
-  **Servicios y Threading (15%)**: Background services, AsyncTask/ExecutorService
-  **Base de Datos (10%)**: Room implementation, CRUD operations
-  **Multimedia y Sensores (10%)**: Camera API, sensor integration
-  **Funcionalidades Externas (10%)**: Intents, permisos, mapas

### Calidad de Código (20%)

-  **Arquitectura**: Separación de responsabilidades, patrones MVC/MVP
-  **Documentación**: Comentarios, JavaDoc, README técnico
-  **Testing**: Unit tests, UI tests
-  **Performance**: Optimización de memoria y CPU

## Entregables

### 1. Código Fuente

-  Proyecto Android Studio completo
-  Control de versiones (Git) con commits descriptivos
-  Documentación técnica (JavaDoc)

### 2. APK Funcional

-  APK instalable y funcional
-  Pruebas en al menos 2 dispositivos diferentes
-  Screenshots de funcionalidades principales

### 3. Documentación

-  Manual de usuario
-  Documentación técnica de arquitectura
-  Video demo (3-5 minutos)

### 4. Presentación

-  Demostración en vivo de funcionalidades
-  Explicación de implementación de cada tema
-  Reflexión sobre aprendizajes obtenidos

## Instrucciones de Instalación

### Prerrequisitos

-  Android Studio Arctic Fox o superior
-  JDK 8 o superior
-  Android SDK API 28-34
-  Dispositivo Android o emulador

### Pasos de Instalación

1. Clonar el repositorio:

   ```bash
   git clone [URL_DEL_REPOSITORIO]
   cd feel
   ```

2. Abrir en Android Studio:

   -  File → Open → Seleccionar carpeta del proyecto

3. Sincronizar Gradle:

   -  Sync Project with Gradle Files

4. Ejecutar la aplicación:
   -  Run → Run 'app'

## Tecnologías Utilizadas

-  **Android SDK**: API 28-34
-  **Java**: JDK 8+
-  **Room**: Base de datos local
-  **Camera2 API**: Acceso a cámara
-  **Google Maps API**: Servicios de localización
-  **Material Design**: Componentes de UI
-  **Gradle**: Sistema de build

## Consideraciones de Privacidad

-  **Sin recolección de datos**: No se almacenan datos personales
-  **Uso local**: Toda la información permanece en el dispositivo
-  **Permisos mínimos**: Solo se solicitan permisos esenciales
-  **Transparencia**: Código abierto y auditable

## Licencia

Este proyecto es desarrollado con fines académicos para el curso de Desarrollo de Aplicaciones Móviles.

---

_Desarrollado por: Angel y Axel
\_Fecha: Octubre 2025_

-  Detección de sensor de proximidad para activación
-  Vibración ligera al tocar (accesibilidad)

### 2. Menú de Categorías (MenuFragment)

-  Tres opciones: "falling", "rising", "you"
-  Navigation Drawer o Bottom Navigation
-  Transiciones suaves entre opciones

### 3. Sección "falling" (FallingFragment)

-  Lista de frases introspectivas desde base de datos
-  Selección aleatoria sin repetición inmediata
-  Auto-cierre después de visualización
-  Animaciones de fade in/out

### 4. Sección "rising" (RisingFragment)

-  Lista de frases motivacionales
-  Algoritmo de selección inteligente
-  Integración con sensores (acelerómetro para siguiente frase)
-  Temporizador configurable

### 5. Sección "you" (YouFragment)

-  Preview de cámara en tiempo real
-  Overlay de texto personalizado
-  CRUD de frases personalizadas
-  Manejo de permisos de cámara

### 6. Funcionalidades Avanzadas

-  **Geolocalización**: Frases contextuales por ubicación
-  **Sensores**: Cambio de frases por movimiento/luz
-  **Servicios**: Background processing y notificaciones
-  **Multimedia**: Sonidos ambientales opcionalesne microinteracciones de menos de 20 segundos, con una interfaz mínima y una única intención por apertura. El proyecto integra todos los conocimientos del primer y segundo parcial del curso de desarrollo Android, implementando los temas 4-15 con un enfoque en simplicidad, desempeño y privacidad.

## Especificaciones Técnicas

-  **Plataforma**: Android
-  **Lenguaje**: Java
-  **API Mínima**: Android API 28 (Android 9.0)
-  **API Target**: Android API 34 (Android 14)
-  **IDE**: Android Studio
-  **Build System**: Gradle

## Requerimientos Académicos

Este proyecto debe implementar **obligatoriamente** los siguientes temas del curso:

### ✅ Tema 4: Fragmentos, flujo maestro-detalle y menú

-  **Fragmentos**: Implementación de `Fragment` para las diferentes secciones (falling, rising, you)
-  **Flujo maestro-detalle**: Lista de frases (maestro) → Visualización detallada (detalle)
-  **Menú**: Navigation Drawer o Bottom Navigation para navegación entre secciones
-  **Clases requeridas**: `Fragment`, `FragmentManager`, `FragmentTransaction`

### ✅ Tema 5: Elementos de interfaz gráfica

-  **Views básicos**: `TextView`, `Button`, `ImageView`, `RecyclerView`
-  **Layouts**: `LinearLayout`, `ConstraintLayout`, `RelativeLayout`
-  **Input controls**: `EditText` para frases personalizadas en sección "you"
-  **Event handling**: `OnClickListener`, `OnLongClickListener`

### ✅ Tema 6: Más sobre interfaz gráfica

-  **Estilos y temas**: Implementación de tema oscuro/monocromático
-  **Recursos**: Dimensiones, colores, strings en archivos de recursos
-  **Adaptadores**: `RecyclerView.Adapter` para listas de frases
-  **Custom Views**: Componentes personalizados para overlay de cámara

### ✅ Tema 7: Transiciones

-  **Animaciones**: `ObjectAnimator`, `AnimatorSet` para transiciones suaves
-  **Activity transitions**: Transiciones entre Activities/Fragments
-  **View animations**: Fade in/out, slide transitions
-  **Shared element transitions**: Para navegación fluida

### ✅ Tema 8: Uso de aplicaciones externas

-  **Intents implícitos**: Compartir frases (opcional, configurable)
-  **Camera Intent**: Acceso a cámara del sistema
-  **Permissions**: Manejo de permisos de cámara
-  **PackageManager**: Verificación de aplicaciones disponibles

### ✅ Tema 9: Emisiones, hilos y servicios

-  **AsyncTask/ExecutorService**: Carga de frases en background
-  **Handler y Looper**: Manejo de UI thread
-  **Broadcast Receivers**: Eventos del sistema (batería baja, etc.)
-  **Threading**: Operaciones pesadas fuera del hilo principal

### ✅ Tema 10: Servicios

-  **Foreground Services**: Temporizador para auto-cierre de frases
-  **Background Services**: Precarga de contenido
-  **Service binding**: Comunicación Activity-Service
-  **Notifications**: Notificaciones silenciosas para servicios

### ✅ Tema 11: Bases de datos

-  **SQLite**: Almacenamiento local de frases personalizadas
-  **Room Database**: ORM para manejo de datos
-  **DAO patterns**: Data Access Objects
-  **Migrations**: Versionado de base de datos

### ✅ Tema 12: Multimedia

-  **Camera API**: Preview de cámara para sección "you"
-  **MediaPlayer**: Sonidos sutiles (opcional)
-  **Bitmap handling**: Procesamiento de imágenes
-  **Audio permissions**: Manejo de permisos multimedia

### ✅ Tema 13: Mapeo

-  **Google Maps API**: Frases basadas en ubicación (opcional)
-  **Location Services**: Obtener ubicación actual
-  **Geocoding**: Conversión de coordenadas a direcciones
-  **Map markers**: Marcadores en mapa

### ✅ Tema 14: Sensores

-  **Accelerometer**: Detección de movimiento para cambio de frases
-  **Light sensor**: Ajuste automático de brillo
-  **Proximity sensor**: Pausar cuando el dispositivo está cerca del rostro
-  **SensorManager**: Gestión de sensores del dispositivo

### ✅ Tema 15: Publicación en Google Play

-  **App signing**: Configuración de certificados
-  **Play Console**: Preparación para publicación
-  **Metadata**: Descripción, capturas, iconos
-  **Release management**: APK/AAB generation

## Arquitectura y Estructura del Proyecto

### Estructura de Activities y Fragments

```
MainActivity (Container)
├── SplashFragment (Pantalla inicial)
├── MenuFragment (Selección de categorías)
├── FallingFragment (Frases introspectivas)
├── RisingFragment (Frases motivacionales)
└── YouFragment (Frases personalizadas + Cámara)
```

### Componentes Principales

#### 1. Data Layer

-  **Room Database**: Almacenamiento de frases personalizadas
-  **Shared Preferences**: Configuraciones de usuario
-  **Assets**: Frases predefinidas en JSON/XML

#### 2. Service Layer

-  **PhraseService**: Servicio para temporizador y auto-cierre
-  **CameraService**: Manejo de preview de cámara
-  **SensorService**: Procesamiento de datos de sensores

#### 3. UI Layer

-  **Custom Views**: Overlay de cámara, texto animado
-  **Adapters**: RecyclerView para listas de frases
-  **Animation Controllers**: Gestión de transiciones

### Permisos Requeridos

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

### 4.2 Contenido

Categorías y tono de frases  
• falling: introspectivas, melancólicas, de aceptación del descenso o pausa.  
• rising: alentadoras, de impulso, enfoque y recomienzo.  
• you: afirmaciones de identidad y presencia para el overlay.

Ejemplos iniciales  
• falling: “will you believe me when im covered in blood?”, “foxes are weird”, “my love runs deeper than words,”, “more than you´ll ever know,”, “more than i´ll ever tell you.”.  
• rising: “stay up late, wake up late.”, “actions, not words.”, “try again.”, “dare to act.”, “he who has a why,”, “can bear almost any how.”.  
• you: “it’s still you”.

Gestión del contenido  
• Frases locales en archivos de recursos; selección aleatoria sin repetición inmediata.  
• Algunas frases deben salir primero para que las siguientes partes puedan salir.  
• Soporte futuro opcional para traducción y listas personalizadas.

## Roadmap de Desarrollo

### Fase 1: Configuración y Estructura Base (Evidencia 2)

-  [x] Configuración del proyecto Android
-  [x] Implementación de `MainActivity` y estructura de `Fragment`
-  [x] Configuración de base de datos Room
-  [x] Diseño de layouts básicos

### Fase 2: Funcionalidades Core (Evidencia 2)

-  [ ] Implementación de fragmentos principales
-  [ ] Sistema de navegación entre secciones
-  [ ] CRUD de frases personalizadas
-  [ ] Animaciones y transiciones básicas

### Fase 3: Funcionalidades Avanzadas (Evidencia 3)

-  [ ] Integración de cámara y preview
-  [ ] Implementación de servicios y threading
-  [ ] Integración de sensores
-  [ ] Sistema de notificaciones

### Fase 4: Pulido y Testing (Evidencia 3)

-  [ ] Optimización de rendimiento
-  [ ] Testing en múltiples dispositivos
-  [ ] Preparación para publicación
-  [ ] Documentación final

## Alcance

Incluido  
• Pantalla inicial con botón “feel” y menú de tres opciones.  
• Visualización de frases a pantalla completa con temporizador y salida automática.  
• Módulo you con overlay y texto.  
• Contenido local en dos listas de frases y una de overlay.  
• Diseño accesible, oscuro y sin anuncios.

Excluido  
• Registro de usuario, perfiles, métricas, notificaciones, publicidad, compras, compartir contenido, almacenamiento en la nube, captura de fotos o vídeo.

## Beneficiarios

• Usuarios que buscan pausas breves de reflexión sin distracciones.  
• yo
