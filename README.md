# Coldstock

A native Android, fully offline **manual freezer inventory tracker**, built with
Kotlin and Jetpack Compose.

> **Positioning:** Keep a clear manual record of what is stored in your freezer
> and which items you planned to use first.

Coldstock is a manual freezer inventory tracker, a freezer drawer organizer, a
frozen-product date journal, a portion tracker, a local Use First list, and an
offline freezer history tool.

Coldstock is **not** a food safety application, a nutrition app, a calorie
counter, a diet planner, a medical application, an allergy advisor, a smart
freezer controller, an appliance monitor, a temperature monitor, a barcode
scanner, a receipt scanner, an online grocery app, a restaurant inventory
system, or a commercial warehouse system.

---

## Disclaimers

**Manual tracking disclaimer**

> Coldstock is a manual freezer inventory organizer. Product names, freezing
> dates, storage periods, quantities, drawers, statuses, and notes are entered
> by the user. The app does not inspect food, monitor freezer conditions,
> determine food safety, or provide medical, nutritional, dietary, or
> professional food-storage advice.

**Storage period disclaimer**

> Storage periods in Coldstock are entered by the user and are organizational
> reminders only. Always follow package instructions, storage guidance, and
> your own judgment.

Coldstock does not:

- provide any food safety guarantee;
- provide medical advice;
- provide nutritional advice;
- provide dietary advice;
- provide storage-duration recommendations;
- detect spoilage, freezer burn, or temperature;
- determine whether frozen food is safe to consume.

Neutral wording is used throughout: "Planned review date reached", "Storage
period entered by you has ended", "Review this item", and "Use First". Statuses
represent only the user-entered storage timeline.

---

## Main features

- **Freezer profiles** — one or more freezers (Upright, Chest, Fridge Freezer,
  Compact, Other), each with independent drawers, products, and history.
- **Freezer drawers and sections** — default layouts per freezer type, plus add,
  rename, reorder (Move Up / Move Down), disable, and restore-defaults. Products
  are never deleted silently; they can be reassigned or left Unassigned.
- **Freezer Drawer Map** — the Home screen renders a simplified freezer with
  stacked pull-out drawers, drawer handles, compact frozen-product tabs, per-drawer
  status indicators, a Use First rail, and an OK / Use Soon / Review Date Passed
  status strip.
- **Frozen products** — manual entry of name, category, freezing date,
  user-defined storage duration, portions, notes, Use First flag, and lifecycle
  state.
- **Manual freezing date** — stored as `YYYY-MM-DD`.
- **Manual storage duration** — value + unit (Days / Weeks / Months). The app
  never suggests durations by food type.
- **Planned review date calculation** — `plannedReviewDate = freezingDate +
  duration` (calendar-correct months via `plusMonths`).
- **Statuses** — OK, Use Soon, Review Date Passed, No Review Date, Used,
  Discarded (derived, never persisted). Configurable Soon threshold
  (1 / 3 / 7 / 14 / 30 days; default 7).
- **Portion tracking** — non-negative integer count with a free-text label; Use
  One, Add One, Set Quantity, Mark All Used.
- **Use First list** — Review Date Passed, Use Soon, Manually Prioritized, and
  No Review Date sections, ordered organizationally.
- **Mark Used / Mark Discarded / Restore** — explicit, confirmed workflows that
  write neutral history events.
- **Search and filters** — fully local search over name, category, drawer, and
  notes with freezer/drawer/category/status/Use First filters and multiple sort
  orders.
- **Product history** — explicit, reverse-chronological local events with
  filters; monthly Used/Discarded counts.
- **Statistics** — neutral inventory summaries with Compose-drawn bars (no chart
  library).
- **In-app reminders** — evaluated only while the app is open; no push, no
  background work.

---

## Privacy note

> Coldstock stores freezer profiles, drawer names, frozen product names,
> freezing dates, user-entered storage periods, quantities, statuses, notes, Use
> First preferences, history, and settings locally on this device. The app has
> no account, no cloud sync, no internet access, no ads, no analytics, no
> payments, no camera access, no scanner, no appliance connection, and no
> background monitoring.

**Explicit non-capabilities:** no push notifications, no background processing,
no camera, no barcode scanner, no receipt scanning, no smart freezer connection,
no temperature monitoring, no account, no backend, no cloud sync, no Firebase, no
ads, no analytics, no payments, no internet, no external API, and **no runtime
permissions**.

---

## Architecture & technology stack

- **Kotlin**, **Jetpack Compose**, **Material 3**
- **Navigation Compose**, **Android ViewModel**, **Kotlin Coroutines**, **Flow**
- **DataStore Preferences** with **Kotlinx Serialization** JSON strings
- **Gradle Kotlin DSL**

Simple MVVM: one local `ColdstockRepository`, a shared `ColdstockViewModel`
exposing immutable `AppData` as `StateFlow`, focused date/inventory utilities,
and Compose screens. No dependency-injection framework, no Room (DataStore JSON
is sufficient), no domain layer, no networking.

### Local storage

All data is persisted with DataStore Preferences as serialized JSON strings under
these keys:

- `freezer_profiles_json`
- `freezer_drawers_json`
- `frozen_products_json`
- `product_history_json`
- `settings_json`

The repository deserializes leniently (unknown keys ignored, defaults filled,
malformed items dropped item-by-item where practical), initializes default
drawers only when a freezer is created, never duplicates defaults on relaunch,
prevents negative portions, and never deletes products when drawer configuration
changes. It never logs full stored JSON or notes.

### Data model summary

`FreezerProfile`, `FreezerDrawer`, `FrozenProduct`, `ProductHistoryEvent`,
`ReminderSettings`, `AppSettings` — all `@Serializable` Kotlin data classes with
safe defaults for backward-compatible deserialization. `FrozenProductStatus` is
**derived** (never stored) from lifecycle state, freezing date, storage
duration, Soon threshold, and the current local date.

### Freezer layouts

- **Upright / Fridge Freezer / Compact / Other:** Top Drawer, Upper Middle
  Drawer, Lower Middle Drawer, Bottom Drawer.
- **Chest:** Left Basket, Right Basket, Main Compartment, Bottom Section.

### Visual concept & layout uniqueness

Visual concept: **Freezer Drawer Map** in a **Frost Drawer Inventory** style. The
Home screen is a drawer-based spatial interface — a large freezer cabinet of
vertically stacked drawer compartments with handles, compact product tabs, a Use
First rail, and a status strip — deliberately avoiding the generic
"mascot → title → stats card → stack of big buttons" layout and not reusing an
open-fridge shelf layout. All freezer visuals are drawn with Compose layouts,
shapes, and borders; no photographs or external illustration assets are used.

### App icon & splash

- **Adaptive icon** (`mipmap-anydpi-v26` + legacy PNGs): a deep-freezer-blue
  cabinet with three stacked drawers, the bottom drawer slightly open, and a
  single amber Use Soon tab. No text, barcode, camera, thermometer, or health
  symbol.
- **Splash**: a static ice-mist background with the centered freezer-drawer icon
  and a small Use First marker (no animation, no food photo).

---

## Screens

Onboarding, Freezer Setup, Freezer Drawer Map (Home), Drawer Detail, Add Product,
Edit Product, Product Detail, All Products, Search, Use First, Product History,
History Detail, Freezer Management, Drawer Management, Statistics, and Settings.
Navigation Compose handles missing freezer/drawer/product/history IDs with
friendly fallbacks ("Freezer not found", "Drawer not found", "Product not
found", "Date unavailable", "Unassigned Drawer") and never crashes.

---

## Building

### Requirements

- **JDK 17**
- **Android SDK Platform 36** and **Build Tools 36.0.0**
- `compileSdk = 36`, `targetSdk = 36`, `minSdk = 24`
- Android Gradle Plugin 8.9.1, Kotlin 1.9.24, Gradle 8.11.1

### Open in Android Studio

1. Open the project root in Android Studio (Koala/Ladybug or newer).
2. On first sync, Android Studio provisions the Gradle wrapper automatically.
   If you prefer the CLI and have Gradle installed, run once:
   `gradle wrapper --gradle-version 8.11.1`
   (the `gradle-wrapper.jar` is intentionally not committed).
3. Let Gradle sync, then Run.

### Debug build

```bash
./gradlew :app:assembleDebug
```

### Non-minified release build (verify this first)

The release build type ships with `isMinifyEnabled = false` and
`isShrinkResources = false` so you can validate a clean, unshrunk release before
enabling R8. Provide signing credentials (see below), then:

```bash
./gradlew :app:assembleRelease
./gradlew :app:bundleRelease
```

### Enabling R8 / resource shrinking (staged)

Only after the non-minified release is built, installed, launched, and tested,
edit `app/build.gradle.kts` and set both flags to `true` in `buildTypes.release`:

```kotlin
isMinifyEnabled = true
isShrinkResources = true
```

`proguard-rules.pro` already keeps the Kotlinx Serialization serializers and the
Coldstock model classes. Rebuild and reinstall, then re-test serialization,
DataStore, navigation, drawer rendering, date calculations, and history.

### 16 KB page-size compatibility

The project uses only Kotlin/Compose/DataStore with no third-party native
binaries, so Android 15+/16 KB page-size compatibility is straightforward. Still
verify the final release bundle before publishing.

---

## Signing

Release APK and AAB must be signed with a **real PKCS12 keystore** — never the
Android debug key. `app/build.gradle.kts` reads signing values from environment
variables first, then an optional local `keystore.properties`, and **fails
clearly** if a release build is requested without credentials (it never falls
back to the debug key).

### Generate a keystore

```bash
keytool -genkeypair -v -storetype PKCS12 \
  -keystore coldstock-release-key.p12 \
  -alias coldstock_key \
  -keyalg RSA -keysize 2048 -validity 10000
```

### Local signing setup

Create `keystore.properties` in the project root (do **not** commit it):

```properties
storeFile=/absolute/path/to/coldstock-release-key.p12
storePassword=your-store-password
keyAlias=coldstock_key
keyPassword=your-key-password
```

Or export environment variables instead:

```bash
export ANDROID_KEYSTORE_PATH=/absolute/path/to/coldstock-release-key.p12
export ANDROID_KEYSTORE_PASSWORD=your-store-password
export ANDROID_KEY_ALIAS=coldstock_key
export ANDROID_KEY_PASSWORD=your-key-password
```

The following files are git-ignored and must never be committed: the PKCS12
file, passwords, decoded keystores, and any secret signing properties.

### Required GitHub Secrets

- `ANDROID_KEYSTORE_BASE64` — base64 of the `.p12` file
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Create the base64 secret with: `base64 -w0 coldstock-release-key.p12`.

---

## GitHub Actions

`.github/workflows/android-build.yml` runs on push to `main` and via
`workflow_dispatch`. It checks out the repo, sets up JDK 17, installs Android SDK
Platform 36 and Build Tools 36.0.0, restores the Gradle cache, generates the
wrapper, decodes `ANDROID_KEYSTORE_BASE64` to a temporary PKCS12 file, exposes
signing secrets only as environment variables, builds the signed release **APK**
and **AAB**, locates the APK, runs `apksigner verify --print-certs`, **fails** if
verification fails or the certificate contains `CN=Android Debug`, and uploads
the signed APK (testing artifact) and signed AAB (Google Play artifact). No
passwords or base64 values are printed, and no emulator smoke test is required.

> CI proves compilation, signing, certificate verification, and artifact
> generation. It is **not** proof that the application launches — verify locally.

---

## Local release verification

```bash
# after building the signed release APK
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
adb install -r app/build/outputs/apk/release/app-release.apk
adb logcat
```

The signing certificate **must not** contain `CN=Android Debug`. Repeat the whole
verification after enabling R8 and resource shrinking. Only the `.aab` should be
uploaded to Google Play.

### Functional test checklist

Test with: empty storage; onboarding and skip; create Upright and Chest
freezers; switch/edit/delete freezer; verify default drawers; add/rename/reorder/
disable drawers; move products before disabling; restore default drawers; add a
product with days/weeks/months durations and one without; verify the calculated
review date; verify OK, Use Soon, and Review Date Passed statuses; change the
Soon threshold; add/use portions; reduce to zero and keep active; mark Used and
Discarded; restore; toggle Use First and verify automatic ordering; move products
between drawers and freezers; search by name/category and filter by
drawer/status/Use First; sort by review/freezing date; open Drawer Detail,
Product Detail, Use First, and History; filter history; trigger and dismiss
in-app reminders; disable reminders; delete history; delete active products;
reset all local data; relaunch; and run in airplane mode.

Confirm: all functionality works offline; no INTERNET permission; no camera
permission; no scanner; no runtime permission dialogs; and no recommended storage
periods appear. Inspect `adb logcat` for `ClassNotFoundException`,
`NoSuchMethodError`, serialization/DataStore/navigation/`LocalDate` crashes,
duplicate default drawers, portion or drawer-layout crashes, R8 issues, and
signing misconfiguration.

---

## Permissions

Coldstock requests **no runtime permissions**. The manifest declares no INTERNET,
CAMERA, POST_NOTIFICATIONS, location, microphone, storage/media, contacts,
calendar, Bluetooth, NFC, or alarm permissions, and no background services,
WorkManager, or scanner/appliance services.

---

## Data reset behaviour

Settings offers granular deletion (Used history, Discarded history, all active
products, the active freezer) and a full **Reset all local data** action, each
behind an explicit confirmation. Reset permanently removes every freezer, drawer,
frozen product, portion count, date, note, Use First record, history event, and
setting stored by Coldstock.

## Manual-entry limitations

Every value in Coldstock — product names, freezing dates, storage periods,
quantities, drawers, statuses, and notes — is entered by you. The app does not
inspect food, detect quantities, scan products, connect to a freezer, or provide
any food-safety, medical, nutritional, dietary, or professional food-storage
guidance.
