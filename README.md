# Insta Downloader - Android Studio Project

## Setup Steps:

### 1. local.properties fix karo
`local.properties` file mein apna SDK path daalo:
```
sdk.dir=C:\Users\APKA_NAME\AppData\Local\Android\Sdk
```

### 2. App Icon add karo
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png` - 192x192
- `app/src/main/res/mipmap-xxhdpi/ic_launcher.png`  - 144x144
- `app/src/main/res/mipmap-xhdpi/ic_launcher.png`   - 96x96
- `app/src/main/res/mipmap-hdpi/ic_launcher.png`    - 72x72
- `app/src/main/res/mipmap-mdpi/ic_launcher.png`    - 48x48
- Round icons bhi same folders mein: `ic_launcher_round.png`

### 3. Android Studio mein open karo
- File → Open → InstaDownloader folder select karo
- Gradle sync hone do

### 4. Build karo
- Build → Generate Signed Bundle/APK → APK
- Ya seedha: Build → Build Bundle(s)/APK(s) → Build APK(s)

## AdMob IDs:
- App ID:       ca-app-pub-4589986408147092~3560636946
- Banner:       ca-app-pub-4589986408147092/7739748620
- Interstitial: ca-app-pub-4589986408147092/6589328803
- Rewarded:     ca-app-pub-4589986408147092/5746570718

## Features:
✅ Instagram Video/Image Download
✅ AdMob Banner (bottom)
✅ Interstitial Ad (download ke baad)
✅ Rewarded Ad (HD Fast Download button)
✅ Direct Gallery/Downloads mein save
✅ Download Manager se proper download
✅ History feature
✅ No Adsterra ads
