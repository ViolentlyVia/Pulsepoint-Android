# PulsePoint — Android

> **v0.1.0**

An Android companion app for a self-hosted [PulsePoint](https://github.com/ViolentlyVia) monitoring server. View the live status of your hosts and services, ping machines, manage assets, and monitor hardware integrations — all from your phone.

## Features

- **Dashboard** — at-a-glance host/service online counts, server uptime, and version info
- **Hosts** — browse all monitored hosts with live status badges
- **Host detail** — view host metadata and run a live ping
- **Services** — see the status of every monitored service endpoint
- **Integrations** — dedicated screens for Unraid, iDRAC, and Omada SDN
  - **Unraid** — array state, disk details, Docker container controls, VM controls, and share utilization
  - **iDRAC** — system info, thermal sensors, fan RPMs, PSU status, and storage drives
  - **Omada SDN** — multi-site support, device status, and connected client details
- **Management console** — add/delete services, rename/reorder/remove assets, and configure integration credentials
- **Settings** — configure the server URL, API key, and optional management password (auto-fills the login screen)
- Pull-to-refresh on all data screens

## Tech Stack

| Layer | Library |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Navigation | Jetpack Navigation Compose |
| Networking | Retrofit 2 + OkHttp + Gson |
| Persistence | Jetpack DataStore (Preferences) |
| Language | Kotlin |

## Requirements

- Android 8.0+ (API 26)
- A running PulsePoint server reachable from the device

## Getting Started

1. Clone the repo and open it in Android Studio.
2. Build and install on a device or emulator (`Run > Run 'app'`).
3. Open **Settings**, enter your server URL (e.g. `http://192.168.1.10:5000/`) and API key, then tap **Save Settings**.
4. Navigate to the Dashboard — your hosts and services will load automatically.

### Management Console

To access asset and service management, tap **Open Management Console** in Settings and enter your server's management password. Saving the password in Settings will auto-fill it on future visits.

## Project Structure

```
app/src/main/java/com/FMDAP/pulsepoint/
├── data/
│   ├── api/          # Retrofit interface + OkHttp client + cookie jar
│   ├── model/        # Data classes (Host, ServiceStatus, Summary, …)
│   ├── prefs/        # DataStore helpers (server URL, API key, password)
│   └── repository/   # Single repository wiring API + prefs together
├── ui/
│   ├── components/   # Shared composables (HostCard, ServiceCard, StatBar, …)
│   ├── navigation/   # NavGraph + Screen sealed class
│   ├── screens/      # One file per screen
│   └── theme/        # Color, Type, Theme
├── viewmodel/        # One ViewModel per screen
├── MainActivity.kt
└── PulsePointApp.kt
```
