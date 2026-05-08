# Technical Documentation — PulsePoint Android

## Architecture Overview
PulsePoint follows the **MVVM (Model-View-ViewModel)** architectural pattern, leveraging modern Android development practices with Jetpack Compose. The application is designed to be a thin client that interfaces with the PulsePoint server via a RESTful API.

### Data Layer
- **Retrofit & OkHttp**: Handles all network communication. Includes a custom `CookieJar` implementation to maintain session persistence for the Management Console.
- **Jetpack DataStore**: Used for persistent storage of user configuration (Server URL, API Key, and Management Password) instead of the legacy SharedPreferences.
- **Repository Pattern**: The `PulsePointRepository` acts as the single source of truth, coordinating data fetching from the API and local preference retrieval.

### UI Layer
- **Jetpack Compose**: A fully declarative UI built with Material 3 components.
- **State Management**: ViewModels expose `StateFlow` objects which the UI observes. This ensures a reactive UI that updates automatically when the underlying data changes.
- **Navigation**: Uses `Jetpack Navigation Compose` with a sealed class-based routing system for type-safety.

## Key Components

### 1. Networking (`data/api/`)
The API interface defines endpoints for:
- `getSummary()`: Dashboard statistics.
- `getHosts()`: List of all monitored machines.
- `getServices()`: Status of all service endpoints.
- `pingHost(hostname)`: Triggers a real-time ICMP/TCP check from the server.
- `management/`: Endpoints for CRUD operations on assets and services.

### 2. State Handling
The app uses a "Loading/Success/Error" pattern within the ViewModels:
```kotlin
sealed class UiState<T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error<T>(val message: String) : UiState<T>()
}
```

### 3. Security
- **API Key**: Required for all read-only requests, passed in the request headers.
- **Management Password**: Required for write operations. The app supports session-based authentication via the server's login endpoint.

## Data Flow
1. **User Action**: User triggers a "Pull-to-Refresh" on the Hosts screen.
2. **ViewModel**: Calls `repository.getHosts()`.
3. **Repository**: Fetches the Server URL and API Key from `DataStore`, then executes the Retrofit call.
4. **API**: Returns a JSON response mapped to Kotlin Data Classes.
5. **ViewModel**: Updates the `MutableStateFlow` with the new list of hosts.
6. **UI**: The Compose `LazyColumn` recomposes to display the updated status badges.

## Implementation Details

### Live Ping
The Host Detail screen allows for a "Live Ping". This is a non-blocking asynchronous call that provides immediate feedback on network latency between the PulsePoint server and the target host.

### Management Console
The management console utilizes a separate authentication flow. When a user logs in, the `OkHttp` client captures the session cookie. Subsequent management requests (like deleting a service) include this cookie automatically.

## Integrations

PulsePoint supports four optional hardware/network integrations, each with its own API routes, ViewModel, and dedicated screen.

### Unraid

Displays the state of a self-hosted Unraid NAS. Data is read from `GET /api/unraid` (with a force-refresh variant). The screen has four tabs:

- **Array**: Array state, aggregate storage usage, and per-disk details (device, status, temperature, size).
- **Docker**: Container list with state indicator and start/stop/restart controls.
- **VMs**: VM list with running status and start/stop/restart controls.
- **Shares**: Per-share storage utilization bars.

### iDRAC

Displays hardware telemetry from a Dell iDRAC BMC via `GET /api/idrac`. The screen has four tabs:

- **System**: Model, service tag, BIOS version, iDRAC firmware, power state, overall health, CPU and memory counts.
- **Thermal**: Temperature sensors with critical thresholds (color-coded), fan RPMs.
- **Power**: PSU cards showing output wattage, capacity, and status.
- **Storage**: Drive list with manufacturer, model, capacity, health, and state. Tapping a drive shows a detail modal.

### Omada SDN

Displays network data from a TP-Link Omada controller via `GET /api/omada`. The screen has two tabs:

- **Devices**: Access points and switches with online/offline status, MAC, model, client count, and throughput.
- **Clients**: Connected clients (wireless and wired) with signal strength, SSID, IP, and traffic stats.

Multi-site Omada deployments are supported: a site selector in the top bar lets the user switch context, and `PUT /api/omada/preferred-site/{siteId}` persists the preferred site server-side.

### Grow

Displays live sensor data from a self-hosted Grow monitoring device via `GET /api/grow/status`. The screen has three tabs:

- **Monitor**: Soil moisture, temperature, and humidity cards; pump status with start/stop controls; moisture threshold and pump duration settings; history clear button.
- **History**: Canvas-drawn line chart for moisture, temperature, or humidity over 12 h / 1 d / 1 w ranges, with min/avg/max stats.
- **Camera**: In-app HLS stream via ExoPlayer (`androidx.media3 1.5.1`). Falls back to showing the RTSP URL if no HLS URL is configured. The Camera tab requires a management session to load stream URLs.

### Integration Configuration

Integration credentials (Unraid host/API key, iDRAC host/username/password, Omada controller URL/client ID/secret, Grow device URL/RTSP URL/HLS URL) are managed through the Management Console under **Manage → Integrations**.

### Dashboard Appearance

`ManageAppearanceScreen` (reached via **Manage → Appearance**) exposes server-side appearance settings:

- Accent color, site name
- Hidden nav pages (per-integration toggles)
- Host card column count (`auto` or fixed 2–5)
- Hidden host card metrics (cpu, memory, disk, ping, uptime)
- Dashboard refresh interval and online threshold (seconds)
- Services widget visibility

## Error Handling
- **Network Failures**: Caught at the repository level and surfaced to the UI as user-friendly snackbars or error states.
- **Configuration Errors**: If the Server URL is missing or malformed, the app directs the user to the Settings screen via state-driven navigation.

## Future Technical Roadmap
- **WebSockets**: Implementation of real-time push updates for status changes.
- **WorkManager**: Background polling to provide Android system notifications when a host goes offline.
- **Charts**: Integration of a charting library (e.g., Vico) to visualize uptime history on the Host Detail screen.

## Build Requirements

- **Version**: 0.2.0 (versionCode 4)
- **Kotlin**: 1.9.0+
- **Compose Compiler**: Compatible with the chosen Kotlin version.
- **Minimum SDK**: 26 (Android 8.0).
- **Target SDK**: 35 (Android 15).
- **Signing**: Keystore credentials are read from `local.properties` (never committed). Required keys: `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
- **Media3**: `androidx.media3:media3-exoplayer`, `media3-exoplayer-hls`, and `media3-ui` at version 1.5.1 — required for HLS video playback on the Grow Camera tab.

---
*This documentation is maintained by the PulsePoint Android development team.*