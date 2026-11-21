# Optimasi untuk Android lawas / low-end

Fork ini sudah disesuaikan supaya lebih ringan dan bisa jalan di perangkat lawas.
Perubahan utama:

## 1. Kompatibilitas
- `minSdk` diturunkan **27 → 23** (Android 6.0 Marshmallow).
- Semua flavor (`apiQ`, `apiR`, `apiS`) sekarang mewarisi `minSdk`/`targetSdk`
  dari `defaultConfig`, jadi tidak ada lagi override yang memaksa API 27.

## 2. Ukuran APK (build release)
- `isMinifyEnabled = true` + `isShrinkResources = true` (R8 + resource shrinking).
- ProGuard/R8 rules diperbarui di `app/proguard-rules.pro`
  (custom View dari XML, Parcelable, enum, Refine, dll).
- Resource RenderScript support dihapus: tidak ada kode RenderScript yang dipakai,
  hanya `@SuppressLint("DrawAllocation")` yang tersisa.
- Core library desugaring dimatikan. Semua pemakaian `Stream`/`parallelStream`
  di `LauncherActivity` dan `Migration` sudah diganti loop biasa.
- Lokalisasi bisa dibatasi lewat `local.properties`:
  ```properties
  resConfigs=en,id
  ```

## 3. Dependency yang dibuang
Library ini tidak dipakai di kode tapi ikut terpaket / menambah dex:
- `foundation.e.lib:telemetry` (+ inisialisasi Sentry di `BlissLauncher`)
- `org.greenrobot:eventbus`
- `foundation.e:elib`
- `org.apache.commons:commons-lang3` (diganti `java.text.Normalizer`)
- `com.github.ChickenHook:RestrictionBypass`
- `androidx.profileinstaller`
- `desugar_jdk_libs`

## 4. Runtime ringan di HP low-RAM
- `Utilities.isLowMemoryDevice()` mendeteksi `ActivityManager.isLowRamDevice()`
  atau heap maksimal < 96 MB.
- Saat low-RAM:
  - blur wallpaper otomatis mati (`BlurWallpaperProvider.getEnabledStatus()`),
  - placeholder blur memakai bitmap **1×1**, bukan bitmap seukuran layar,
  - `BlurViewDelegate` tidak membuat drawable/listener blur sama sekali.

Efeknya: konsumsi heap dan CPU saat scroll home screen turun drastis di perangkat
kelas bawah, sementara perangkat yang lebih kuat tetap mendapat tampilan blur iOS.

## Build
Butuh Android SDK + JDK 11 (AGP 7.3.1):

```bash
./gradlew assembleApiQRelease
```

> Catatan: environment tempat patch ini dibuat **tidak punya Android SDK**, jadi
> build belum bisa diverifikasi di sini. Jika R8 mengeluhkan class yang hilang,
> tambahkan rule `-keep` yang sesuai di `app/proguard-rules.pro`.
