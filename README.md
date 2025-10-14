# Feel — Propuesta de proyecto

## Contexto

La mayoría de las apps móviles compiten por retener atención y tiempo de pantalla. Feel propone lo contrario: microinteracciones de menos de 20 segundos, con una interfaz mínima y una única intención por apertura. El proyecto se desarrollará en Android con Java y , dentro del curso del semestre, priorizando simplicidad, desempeño y privacidad.

## Problema

Usuarios saturados por notificaciones, feeds infinitos y pantallas llenas de opciones buscan pausas breves, sin fricción ni distracciones. Las apps de frases existentes suelen tener anuncios, navegación compleja o funciones accesorias (compartir, perfiles, monetización) que rompen el momento. Se requiere una experiencia efímera, clara y silenciosa, que funcione 100% offline y no almacene datos.

## Justificación

• Pedagógica: permite aplicar y demostrar competencias de desarrollo Android con Java, diseño de interfaces con XML y buenas prácticas de UX minimalista.  
• Social y de bienestar digital: ofrece un espacio puntual de auto-observación y regulación emocional en contextos de sobrestimulación.  
• Ética y privacidad: no recolecta datos, no muestra anuncios ni requiere registro; uso de cámara solo para vista previa, sin capturas ni almacenamiento.  
• Técnica: alcance acotado y entregable en el semestre, con componentes básicos de Android fáciles de probar y mantener.  

## Propuesta de solución

### 4.1 Concepto y flujo de usuario

Pantalla inicial negra con un botón centrado: "feel". Al tocarlo, aparecen tres opciones (en un diálogo o menú simple):  
• falling: muestra al azar una frase de la lista A a pantalla completa por unos segundos y cierra la app al finalizar.  
• rising: igual que la anterior, pero con lista B.  
• you: indica “coloca el teléfono a la altura de tu rostro”, abre cámara frontal con un overlay estilo espejo y texto tipo “it’s still you” o variaciones; no toma fotos; tras unos segundos, cierra la app.  

Comportamientos clave  
• Sin navegación adicional, menús ni historial. • Sin compartir ni guardar. Un solo gesto lo inicia; una sola lectura lo termina. • Si el usuario niega el permiso de cámara, la opción you muestra un mensaje breve y regresa al menú de opciones.  

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

### 4.3 Diseño y accesibilidad  
• Interfaz monocromática, alto contraste, tipografía legible y tamaños escalables.   
• Animaciones simples para mostrar opciones y frases.   
• Texto accesible para lectores de pantalla; vibración ligera al interactuar.  


/// 
## 4.4 Arquitectura y tecnología
• Lenguaje y UI: Java, layouts XML. 
• Estructura: Activities y/o Fragments con controladores sencillos.   
• Lógica: manejo directo en clases Java con métodos de selección aleatoria de frases.   
• Cámara: integración con CameraX o API estándar de cámara para preview frontal; overlay dibujado sobre un SurfaceView/TextureView.   
• Permisos: gestionados con el sistema de Android (Runtime Permissions).   
• Cierre controlado: se programa finalización de la Activity tras mostrar contenido.   
• Dependencias mínimas para reducir tamaño y complejidad.  
///


### 4.5 Privacidad y seguridad  
• Sin red, sin analytics, sin almacenamiento de imágenes o datos personales.   
• Cámara en preview únicamente; no se persisten frames. Permisos solicitados solo al entrar en you.  

### 4.6 Riesgos y mitigaciones  
• Permisos de cámara denegados: ruta alternativa informativa.   
• Variaciones de dispositivos: probar en al menos 3 modelos y versiones Android (API 29+).   
• Auto-cierre percibido como brusco: se usará desvanecido (o pantalla de despedida rápida) para suavizar salida.   

## Alcance

Incluido  
• Pantalla inicial con botón “feel” y menú de tres opciones.   
• Visualización de frases a pantalla completa con temporizador y salida automática.   
• Módulo you con cámara frontal, overlay y texto.   
• Contenido local en dos listas de frases y una de overlay.   
• Diseño accesible, oscuro y sin anuncios.  

Excluido  
• Registro de usuario, perfiles, métricas, notificaciones, publicidad, compras, compartir contenido, almacenamiento en la nube, captura de fotos o vídeo.    

## Beneficiarios

• Usuarios que buscan pausas breves de reflexión sin distracciones.   
• yo  
