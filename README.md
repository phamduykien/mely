# 🌙 Mêly Sleep - Ứng Dụng Âm Thanh Hỗ Trợ Giấc Ngủ (Android)

Ứng dụng Android native hiện đại viết bằng **Kotlin** và **Jetpack Compose**, phát các dải âm thanh thiên nhiên & tiếng ồn trắng (white/pink noise) giúp thư giãn, dễ đi vào giấc ngủ và tập trung sâu.

---

## ✨ Tính Năng Nổi Bật

1. **4 Kênh Âm Thanh Độc Lập Với Thanh Trượt Slider**:
   - 🌧️ **Tiếng Mưa (Rain)**: Âm thanh mưa rào dịu mát kết hợp tiếng giọt rải rác, tạo cảm giác thư thái.
   - 🍃 **Tiếng Gió (Wind)**: Luồng gió đêm vi vu rì rào qua vòm lá.
   - 🦗 **Tiếng Côn Trùng Đồng Quê / Dế (Crickets)**: Tiếng dế kêu rả rích đêm hè thân thuộc, đưa tâm trí về chốn đồng nội bình yên.
   - 📻 **Âm Thanh Noise (Pink/White Noise)**: Dải tần số êm mượt giúp triệt tiêu tiếng ồn tạp âm từ môi trường phòng ngủ.

2. **Khả Năng Phối Âm (Sound Mixer)**:
   - Từng kênh có thanh trượt Slider điều chỉnh độ lớn độc lập (0% - 100%).
   - Nút bật/tắt tiếng (Mute/Unmute) tức thì cho từng kênh.
   - Badge hiển thị phần trăm âm lượng và hiệu ứng viền phát sáng tương tác theo trạng thái phát.

3. **Bộ Phối Âm Gợi Ý Sẵn (Presets)**:
   - 🌧️ *Đêm Mưa Rào*: Tập trung tiếng mưa lớn kết hợp gió nhẹ và chút noise.
   - 🦗 *Đồng Quê Yên Ả*: Tiếng dế hòa cùng gió đêm vi vu.
   - 💤 *Giấc Ngủ Sâu*: Dải tần Noise êm ái lấn át tạp âm kết hợp mưa phùn.
   - 🍃 *Gió Núi Vi Vu*: Gió lộng thoang thoảng tiếng dế xa xa.
   - 🔇 *Tắt Tất Cả*: Đặt lại toàn bộ slider về 0.

4. **Hẹn Giờ Tắt Thông Minh (Sleep Timer)**:
   - Các mốc lựa chọn nhanh: **15p, 30p, 45p, 60p, 90p, 120p**.
   - Hiển thị đồng hồ đếm ngược từng giây trực quan.
   - Tự động **Fade Out** từ từ trong 15 giây cuối cùng trước khi dừng hẳn để không làm giật mình người dùng khi đã chìm vào giấc ngủ.

5. **Phát Nền Hoàn Hảo (Foreground Service & Media Notification)**:
   - Ứng dụng chạy bằng `ForegroundService` với `foregroundServiceType="mediaPlayback"`.
   - Vẫn phát mượt mà khi tắt màn hình, khóa máy hoặc chuyển sang ứng dụng khác.
   - Thanh điều khiển trên Notification: Tạm dừng / Tiếp tục / Dừng hẳn và hiển thị thời gian hẹn giờ còn lại.
   - Tự động xử lý Audio Focus (giảm âm lượng hoặc tạm dừng khi có cuộc gọi đến).

---

## 🛠️ Cấu Trúc Dự Án

```
mely/
├── app/
│   ├── build.gradle                 # Cấu hình Gradle & Compose
│   └── src/main/
│       ├── AndroidManifest.xml      # Khai báo quyền Foreground Service & Notifications
│       ├── res/
│       │   ├── raw/                 # Các file âm thanh chất lượng cao chuẩn loop seamless:
│       │   │   ├── rain.ogg
│       │   │   ├── wind.ogg
│       │   │   ├── crickets.ogg
│       │   │   └── noise.ogg
│       │   ├── values/              # Strings, Colors, Themes
│       │   └── ...
│       └── java/com/mely/sleep/
│           ├── MainActivity.kt      # Activity chính, kết nối Service và render giao diện
│           ├── model/
│           │   └── SoundType.kt     # Định nghĩa các kênh âm thanh, preset
│           ├── audio/
│           │   └── AudioMixer.kt    # Quản lý phát đa luồng MediaPlayer, volume & focus
│           ├── service/
│           │   └── SleepAudioService.kt # Foreground Service chạy nền & đếm giờ ngủ
│           └── ui/
│               ├── SleepScreen.kt   # Màn hình chính Compose với giao diện đêm tĩnh lặng
│               ├── components/
│               │   ├── SoundSliderCard.kt   # Card slider từng âm thanh với glow & mute
│               │   ├── PresetChips.kt       # Danh sách gợi ý phối âm
│               │   └── SleepTimerSection.kt # Bộ hẹn giờ tắt
│               └── theme/           # Night Theme (Material 3)
├── build.sh                         # Script build APK tự động 1 lệnh
└── build.gradle
```

---

## 🚀 Hướng Dẫn Biên Dịch & Cài Đặt

### 1. Build file APK trực tiếp:
Chạy script:
```bash
cd /app/mely
./build.sh
```

File APK sẽ được tạo tại:
```
/app/mely/app/build/outputs/apk/debug/app-debug.apk
```

### 2. Cài đặt vào điện thoại Android qua ADB:
Kết nối điện thoại bật USB Debugging và chạy:
```bash
adb install -r /app/mely/app/build/outputs/apk/debug/app-debug.apk
```
Hoặc mở thư mục `/app/mely` trực tiếp bằng **Android Studio** để chạy và debug.
