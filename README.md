# Nomad // Off-Grid Survival Node & Mesh Barter Courier

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![UI](https://img.shields.io/badge/Jetpack-Compose%20M3-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Network](https://img.shields.io/badge/Mesh-BLE%20Off--Grid-00E5FF?style=flat-square)](#ghost-relay-ble-mesh)
[![Persistence](https://img.shields.io/badge/Storage-Room%20Local-FF6F00?style=flat-square)](https://developer.android.com/training/data-storage/room)
[![Zero Cloud](https://img.shields.io/badge/Architecture-100%25%20Offline-green?style=flat-square)](#zero-infrastructure-philosophy)

> **Decentralized, air-gapped tactical communications and supply-barter network for grid-down scenarios.**  
> Zero cellular towers. Zero Wi-Fi routers. Zero cloud servers.

---

## Overview & Philosophy

**Nomad** is an Android tactical utility designed to maintain logistics, local intelligence, and peer-to-peer barter networks when standard civil telecommunications infrastructure collapses.

By leveraging **Bluetooth Low Energy (BLE)** opportunistic mesh synchronization, physical **NFC Digital Dead Drops**, and an **air-gapped APK self-replication beamer**, Nomad allows survivor nodes to coordinate, broadcast critical emergency hazards, and barter essential survival items without touching the internet.

To test this [download](https://drive.google.com/file/d/1QUEcO_3vergmakDcL_xCbeBrIm6K_9P_/view?usp=drive_link)
---

## Core Capabilities

### 1. 📡 Ghost Relay (BLE Mesh Gossip Protocol)
* **Opportunistic Carrier-Pigeon Relaying:** Packets hop passively between nodes as survivors pass each other in physical space.
* **Encrypted Short-Burst Broadcasts:** Transmit priority-tagged intel (Hostiles, Safe Haven, Biohazard, Resource Cache, Weather) over BLE advertising channels.
* **Node Telemetry & Peer Discovery:** Track signal strength (RSSI), hop count counters, relay tallies, and last-seen timestamps in real time.
* **Autonomous Packet De-duplication:** Prevents broadcast packet storms and circular loops through localized hash tracking.

### 2. ⚖️ Barter Ledger & Proximity Matchmaker
* **Zero-Trust Supply Exchange:** Register inventory items you have (**Offers**) and critical resources you lack (**Needs**).
* **Categorized Resource Tiers:** Classify goods by essential survival categories (Medical, Munitions, Rations, Water Purification, Fuel, Comms, Tools).
* **Automated Proximity Match Alert:** When your device passes within BLE range of another Nomad node whose offers fulfill your needs (or vice versa), the system triggers a silent tactical alert banner with matching item manifests.

### 3. 🏷️ Digital Dead Drops (NFC Intelligence Stashes)
* **Physical NFC Tag Stashing:** Write encrypted GPS coordinates, door passcodes, safety warnings, and cache inventories directly to physical NDEF-formatted NFC tags or stickers.
* **Offline Distance & Bearing Calculation:** Scan discovered dead drop tags to extract coordinates and compute straight-line distance and heading without cellular mapping services.
* **Anti-Tamper Digital Signatures:** Verify stash author callsigns and creation timestamps directly on-chip.

### 4. ⚡ Zero-State APK Beamer (Self-Propagating Node)
* **Air-Gapped App Extraction:** Automatically extracts the running Nomad `.apk` binary package directly from Android's local system storage (`ApplicationInfo.sourceDir`).
* **Offline Mesh Onboarding:** Beams the installer directly to unequipped devices via Bluetooth, Wi-Fi Direct, or nearby share, enabling rapid node expansion even in complete digital blackouts.
* **Zero Web Dependency:** New nodes can be provisioned in the field with zero internet access or app store availability.

### 5. 📖 Operations Manual (Survival Guide)
* **On-Device Offline Reference:** Comprehensive tactical manual outlining frequency protocols, OPSEC guidelines, battery management, and dead drop deployment tactics.

---

## System Architecture

Nomad follows modern Android **Clean Architecture / MVVM** patterns with strict local-first data isolation:

```
app/src/main/java/com/example/
├── MainActivity.kt               # Root Activity, Tab Navigation & Tactical Top Bar
├── data/
│   ├── local/                    # Room DB (NomadDatabase, BroadcastDao, BarterDao, DeadDropDao)
│   ├── model/                    # Data models (EmergencyBroadcast, BarterItem, DigitalDeadDrop, PeerNode)
│   └── repository/               # NomadRepository (Central reactive single source of truth)
├── mesh/
│   ├── GhostMeshManager.kt       # BLE Advertising, Scanning, Packet Encoding & Gossip Relay
│   ├── NfcDeadDropManager.kt     # Android NFC Adapter & NDEF Record read/write pipeline
│   └── ApkBeamerManager.kt       # System package extraction & offline sharing intent provider
└── ui/
    ├── screens/
    │   ├── GhostRelayScreen.kt   # Mesh broadcasts & peer discovery console
    │   ├── BarterLedgerScreen.kt # Trade ledger & matching inventory view
    │   ├── DeadDropsScreen.kt    # NFC stash reader & writer
    │   ├── ZeroStateBeamerScreen.kt # APK extraction & beaming dashboard
    │   └── SurvivalGuideScreen.kt   # Offline tactical operations manual
    └── theme/                    # High-contrast tactical HUD color palette & typography
```

---

## Technical Specifications

| Parameter | Specification |
| :--- | :--- |
| **Minimum SDK** | Android 7.0 (API Level 24) |
| **Target SDK** | Android 15 (API Level 36) |
| **Language** | Kotlin 2.0+ |
| **UI Framework** | Jetpack Compose with Material Design 3 |
| **Async & Streams** | Kotlin Coroutines & `StateFlow` / `SharedFlow` |
| **Local Database** | Room SQLite Persistence with KSP |
| **RF Technologies** | Bluetooth Low Energy (BLE), Near Field Communication (NFC) |
| **Theme / Aesthetic** | High-contrast Monospaced Tactical HUD (OLED black canvas with amber/cyan accents) |

---

## Permissions & Rationale

Nomad requests only hardware permissions strictly required for offline physical layer communication:

* `BLUETOOTH_SCAN` / `BLUETOOTH_ADVERTISE` / `BLUETOOTH_CONNECT`: Required to transmit and receive BLE mesh broadcast packets without pairing.
* `ACCESS_FINE_LOCATION`: Required by Android OS for BLE hardware scanning. Nomad never transmits GPS data off-device.
* `NFC`: Required to read and write physical Dead Drop tags.
* `FOREGROUND_SERVICE`: Keeps the Ghost Relay node actively monitoring and relaying packets when the device is locked in a pocket or pack.

---

## Building and Installation

### Prerequisites
* **Android Studio** (Ladybug / Meerkat or newer) or standalone Gradle build tools.
* **JDK 17** or **JDK 21**.
* Physical Android test device with **Bluetooth LE** and **NFC** enabled (BLE advertising requires physical silicon; emulators have limited RF emulation).

### Build APK via Gradle
```bash
# Clone the repository
git clone https://github.com/<your-username>/nomad.git
cd nomad

# Build Debug APK
gradle :app:assembleDebug

# The output APK will be located at:
# app/build/outputs/apk/debug/app-debug.apk
```

### Direct Device Installation
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Tactical OPSEC & Battery Conservation

1. **Passive Reconnaissance:** The BLE scanner runs low-latency discovery when active, then cycles duty states to minimize battery drain.
2. **RF Discipline:** When stationary or concealing location, toggle Ghost Sync **OFF** from the top HUD bar to halt all radio emissions.
3. **Physical Dead Drop Hiding:** Always inspect physical surroundings before tapping NFC tags. Waterproof stickers placed on non-conductive surfaces (wood, plastic, masonry) yield the best read distances.

---

## License

This project is licensed under the **MIT License** — free to use, modify, and distribute for personal, educational, or emergency resilience purposes.
