# 🍽️ Calorite - Smart Calorie & Protein Tracker

Calorite adalah aplikasi Android *native* yang dirancang untuk mempermudah pelacakan nutrisi harian (kalori dan protein). Menggunakan kombinasi kecerdasan buatan (AI) untuk pengenalan gambar makanan dan opsi input manual yang presisi, Calorite adalah pendamping terbaik untuk menjaga target *bulking* atau diet harian tetap *on-track*—sangat praktis untuk menakar porsi makanan harian yang sulit diestimasi.

## ✨ Fitur Utama

*   📸 **AI Food Snapshot:** Hitung estimasi kalori dan protein secara instan hanya dengan memfoto makanan. AI akan menganalisis gambar dan menyimpan datanya secara otomatis.
*   ✍️ **Manual Entry Fallback:** Fitur input manual presisi (dengan *smart placeholder* gambar) ketika AI kurang akurat atau saat mengonsumsi cemilan kecil, memastikan data nutrisi harian tetap valid 100%.
*   📊 **Dynamic Progress Visualizer:** *Dashboard* interaktif dengan *progress bar* visual (menggunakan manipulasi `ClipDrawable`) yang terisi secara dinamis sesuai persentase target kalori dan protein harian (misal: 2000 kcal & 160gr protein).
*   🗒️ **Recipe Notes:** Capek bolak balik ke browser atau media sosial untuk mencari resep favorit? Tinggal tambah kedalam Recipe Journal dalam aplikasi aja! Calorite juga berfungsi sebagai catatan resep untuk recook makanan-makanan favorit kamu!
*   🗓️ **Comprehensive Daily History:**
    *   Tampilan riwayat makanan harian dengan *horizontal scrolling* (`RecyclerView`).
    *   Pop-up *fullscreen* untuk melihat kembali foto makanan yang telah di-*capture*.
    *   Fitur **Daily Journal** untuk mencatat evaluasi diet di setiap akhir hari.
*   🔔 **Smart Meal Reminders:** Sistem *push notification* lokal berbasis `AlarmManager` dan `BroadcastReceiver` yang efisien daya, mengingatkan jadwal makan (07:00, 12:00, 16:00, 20:00) secara otomatis walau aplikasi sedang ditutup.
*   💾 **Optimized Local Database:** Menggunakan *Room Database* (SQLite) dengan optimasi manajemen memori (menghindari `SQLiteBlobTooBigException` dengan tidak menyimpan file *placeholder* ke dalam *database*).

## 🛠️ Teknologi yang Digunakan

*   **Platform:** Android (Minimum SDK 24+)
*   **Bahasa Pemrograman:** Java
*   **Database:** Room Persistence Library (SQLite)
*   **UI Components:** XML, Custom Dialogs, RecyclerView, ClipDrawable untuk *custom progress bar*, Auto-Size Text.
*   **Background Tasks:** AlarmManager, BroadcastReceiver, PendingIntent.
*   **Arsitektur:** Native MVC / MVVM Architecture.

## 🚀 Cara Instalasi & Menjalankan Aplikasi (Development)

Jika kamu ingin menjalankan kode sumber Calorite di Android Studio lokalmu, ikuti langkah-langkah berikut:

1.  **Clone Repositori:**
    ```bash
    git clone [https://github.com/username-kamu/Calorite.git](https://github.com/username-kamu/Calorite.git)
    ```
2.  **Buka Proyek:**
    Buka Android Studio, pilih **File > Open**, lalu arahkan ke folder repositori `Calorite` yang baru saja di-*clone*.
3.  **Sinkronisasi Gradle:**
    Tunggu hingga Android Studio selesai melakukan sinkronisasi Gradle dan mengunduh semua *dependencies* yang dibutuhkan.
4.  **Siapkan API Key (Jika Ada):**
    Masukkan API Key untuk fitur pengenal makanan AI di dalam file *config* atau kelas yang relevan (sesuai implementasi).
5.  **Jalankan Aplikasi:**
    Pilih *emulator* atau hubungkan *smartphone* Android fisik (pastikan *Developer Options* & USB *Debugging* menyala), lalu klik tombol **Run** (`Shift + F10`).

> **Catatan untuk Pengguna Xiaomi/POCO/Redmi/Oppo/Vivo:**
> Agar fitur **Smart Meal Reminders** berjalan optimal, pastikan untuk mematikan *Battery Saver* (Tidak ada pembatasan) dan mengaktifkan *Autostart* pada menu **App Info** Calorite di pengaturan HP Anda.

## 📥 Unduh Versi Rilis

Tidak ingin kompilasi kode? Unduh langsung file APK terbaru untuk langsung dipasang di HP Android kamu!
*   [Download Calorite v1.0.0 APK](https://github.com/axizorr/Calorite/releases/tag/v1.0.0) *

## 📄 Lisensi

Proyek ini dibuat untuk keperluan edukasi dan pelacakan nutrisi pribadi.
