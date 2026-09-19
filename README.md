# Dashboard GOTA — Documentación de Cambios

Este documento describe las mejoras implementadas en el dashboard principal del sistema GOTA, incluyendo el módulo de clima en tiempo real y el sistema de tema claro/oscuro.

**Cambios incluidos:**

1. Clima actual para el lector del campo
2. Modo claro / oscuro

---

## 🆕 Cambios implementados

### 1. 🌤️ Tarjeta de clima actual (Open-Meteo)

Se agregó una tarjeta de clima en tiempo real en la parte superior del dashboard que muestra:

- **Temperatura actual** (en °C)
- **Condición climática** (Despejado, Nublado, Lluvia, Tormenta, etc.)
- **Velocidad del viento** (km/h)
- **Humedad relativa** (%)
- **Ubicación actual** (nombre del lugar, ciudad y país)

#### 🔧 Tecnologías usadas

| Servicio | Uso | API Key |
|---|---|---|
| **Open-Meteo** | Datos meteorológicos en tiempo real | ❌ No |
| **BigDataCloud** | Geocodificación inversa (lat/lon → nombre) | ❌ No |
| **Nominatim (OSM)** | Fallback si BigDataCloud falla | ❌ No |

Todas las APIs son **gratuitas** y **no requieren registro ni API key**.

#### ⚙️ Funcionamiento

1. Al cargar el dashboard, el navegador solicita la **ubicación del usuario** mediante `navigator.geolocation`.
2. Si el usuario acepta, se usan sus coordenadas reales.
3. Si el usuario **niega** la geolocalización o hay un error, se usa como **ubicación por defecto Guatemala** (`14.6349, -90.5069`).
4. Con las coordenadas se consulta **Open-Meteo** para obtener el clima actual (temperatura, viento, humedad y código WMO).
5. En paralelo se consulta **BigDataCloud** para traducir las coordenadas a un nombre legible (ej. *"Mixco, Guatemala, Guatemala"*).
6. Si BigDataCloud no responde, se intenta **Nominatim** como respaldo.
7. Si ambos fallan, se muestran las coordenadas o el texto por defecto.

#### 🗺️ Mapeo de códigos WMO

Los códigos de clima que devuelve Open-Meteo se traducen a texto + icono (FontAwesome) usando un diccionario interno:

```javascript
const WMO_MAP = {
    0:  { text: 'Despejado',            icon: 'fa-sun' },
    1:  { text: 'Mayormente despejado', icon: 'fa-cloud-sun' },
    2:  { text: 'Parcialmente nublado', icon: 'fa-cloud-sun' },
    3:  { text: 'Nublado',              icon: 'fa-cloud' },
    45: { text: 'Niebla',               icon: 'fa-smog' },
    48: { text: 'Niebla con escarcha',  icon: 'fa-smog' },
    51: { text: 'Llovizna ligera',      icon: 'fa-cloud-rain' },
    53: { text: 'Llovizna',             icon: 'fa-cloud-rain' },
    55: { text: 'Llovizna intensa',     icon: 'fa-cloud-rain' },
    61: { text: 'Lluvia ligera',        icon: 'fa-cloud-rain' },
    63: { text: 'Lluvia',               icon: 'fa-cloud-showers-heavy' },
    65: { text: 'Lluvia intensa',       icon: 'fa-cloud-showers-heavy' },
    71: { text: 'Nieve ligera',         icon: 'fa-snowflake' },
    73: { text: 'Nieve',                icon: 'fa-snowflake' },
    75: { text: 'Nieve intensa',        icon: 'fa-snowflake' },
    80: { text: 'Chubascos ligeros',    icon: 'fa-cloud-rain' },
    81: { text: 'Chubascos',            icon: 'fa-cloud-showers-heavy' },
    82: { text: 'Chubascos fuertes',    icon: 'fa-cloud-showers-heavy' },
    95: { text: 'Tormenta',             icon: 'fa-bolt' },
    96: { text: 'Tormenta con granizo', icon: 'fa-bolt' },
    99: { text: 'Tormenta fuerte',      icon: 'fa-bolt' }
};

📍 Ejemplo de respuesta de Open-Meteo
json
{
  "current": {
    "time": "2026-09-19T15:00",
    "temperature_2m": 24.3,
    "relative_humidity_2m": 72,
    "weather_code": 2,
    "wind_speed_10m": 11.9
  }
}
🛡️ Manejo de errores:

Escenario	Comportamiento:

- Usuario niega geolocalización	Se usa Guatemala como ubicación por defecto
- Navegador sin geolocation	Se usa Guatemala como ubicación por defecto
- Open-Meteo no responde	Muestra --°C y "Clima no disponible"
- BigDataCloud falla	Se intenta Nominatim
- Nominatim también falla	Se muestra "Mi ubicación" o coordenadas crudas

⚠️ Consideraciones
Nominatim (OpenStreetMap) tiene una política de máximo 1 request por segundo. Como solo se llama 1 vez por carga del dashboard, está dentro de los límites aceptables.

Para uso en producción de alta concurrencia, se recomienda cachear el resultado de la geocodificación en el backend o mover la lógica a un WeatherController de CodeIgniter 4.

2. 🌗 Sistema de tema Claro / Oscuro:

Se agregó un botón flotante en el header del dashboard que permite alternar entre Modo Claro (por defecto) y Modo Oscuro, con persistencia entre sesiones.

🎨 Comportamiento:

- Estado actual	Icono	Texto
- Modo Claro	bi bi-lightbulb-fill 💡	"Modo Claro On"
- Modo Oscuro	bi bi-lightbulb ○	"Modo Oscuro On"
- El botón se inyecta dinámicamente vía JavaScript dentro de .app-header .header-actions, justo antes del avatar del usuario.

La preferencia se guarda en localStorage con la clave gota-theme.

Al recargar la página, el tema se aplica antes del renderizado para evitar el flash blanco (FOUC).

🔧 Implementación técnica:

1. Script anti-FOUC (en <head>, antes del <style>):

html
<script>
(function() {
    try {
        if (localStorage.getItem('gota-theme') === 'dark') {
            document.documentElement.setAttribute('data-theme', 'dark');
        }
    } catch(e) {}
})();
</script>

2. Iconos de Bootstrap Icons (CDN):

html
<link rel="stylesheet"
      href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

3. Atributo global data-theme aplicado a <html>:

html
<html data-theme="dark">

4. Variables CSS en modo oscuro:

css
[data-theme="dark"] {
    --dm-bg: #0f1115;
    --dm-card: #1a1d24;
    --dm-elevated: #232730;
    --dm-border: rgba(255,255,255,0.07);
    --dm-text: #e8eaed;
    --dm-text-secondary: #9aa0a6;
}

🎯 Componentes cubiertos por el modo oscuro

Header superior (.app-header)

Tarjetas de estadísticas (.stat-card)

Tarjetas de analíticas (.analytics-card)

Contenedor de tabla (.table-container)

Tabla de lecturas (thead, tbody, celdas)

Cards de lecturas en móvil (.lectura-card)

Navegación inferior (.bottom-nav)

Tarjeta de clima (.weather-card)

Paginación

🔄 Ciclo de vida del tema

1. Navegador carga el HTML
2. Script anti-FOUC lee localStorage
3. Si 'dark' → aplica data-theme="dark"
4. Se renderiza la página SIN parpadeo
5. DOMContentLoaded → inyecta botón
6. Click usuario → alterna tema + guarda

📂 Archivos modificados:

Archivo	Cambios:

- app/Views/dashboard/index.php	Se agregó sección styles con CSS de clima, tema y modo oscuro. Se agregó tarjeta de clima en content. 
- Se agregó <script> con lógica de tema y clima dentro de la sección content.


📚 Referencias

Open-Meteo API Docs:

- BigDataCloud Reverse Geocoding

- Nominatim (OpenStreetMap)

Bootstrap Icons:

- CodeIgniter 4 — View Layouts