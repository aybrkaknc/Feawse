# FEAWSE (Fire Emblem: Awakening Save Editor) for Android 🛡️

**FEAWSE** is a modern, feature-packed save editor for **Fire Emblem: Awakening** (Nintendo 3DS), built natively for Android using Kotlin and Jetpack Compose.

Inspired by the classic desktop editor and Fire Emblem Awakening's signature aesthetic, FEAWSE allows you to inspect, modify, and customize your Awakening save files directly on your Android phone or tablet.

---

## ✨ Features

- 🎒 **Convoy & Inventory Editor**:
  - Full management of normal items, weapons, staves, and consumables.
  - Forged Weapons editor with custom weapon names, Mt, Hit, Crt, and Uses.
  - Fast search, category filtering, and item sorting.
- ⚔️ **Unit Editor**:
  - Detailed editing for all story, spotpass, and avatar characters.
  - Edit Class, Level, EXP, Current & Max HP, Stats (Str, Mag, Skl, Spd, Lck, Def, Res, Mov).
  - Weapon Ranks (Sword, Lance, Axe, Bow, Tome, Staff).
  - Equipped & Inactive Skills selector with icon/type categorization.
  - Battle modifiers, tonics, and support levels.
- 📜 **Progress & World Editor**:
  - Renown points and Story Chapter unlocks.
  - StreetPass / Outrealm team customization.
  - Double Duel & DLC map unlock flags.
- ⚡ **Cheats & Quick Actions**:
  - Max Gold, Max Renown, All Items x99, Max All Stats, etc.
- 🛡️ **Safety & Backups**:
  - Automated timestamped backups before every save write.
  - Easy one-tap restore from backup history.
- 🎨 **Awakening Aesthetic**:
  - Authentic Fire Emblem Awakening dark theme (Shield of Seals gold accents, Deep Navy backgrounds, custom crests and animations).

---

## 🚀 Download & Installation

Grab the latest signed release APK directly from GitHub Releases:

👉 **[Download Latest Release APK](https://github.com/aybrkaknc/Feawse/releases)**

1. Download `FEAWSE-Signed-*.apk` from the Releases page.
2. Install it on your Android device (Android 7.0 / API 24 or newer).
3. Open your Awakening save file (`Chapter1`, `Chapter2`, `Chapter3`, etc.) and enjoy!

---

## 🛠️ Building from Source

### Prerequisites
- JDK 17
- Android SDK (API 36, target 36, min 24)

### Build Commands
```bash
# Clone the repository
git clone https://github.com/aybrkaknc/Feawse.git
cd Feawse

# Build Release APK
./gradlew assembleRelease
```

---

## 📄 License

This project is licensed under the GNU General Public License v3 (GPLv3).
Fire Emblem and Fire Emblem: Awakening are registered trademarks of Nintendo / Intelligent Systems.
