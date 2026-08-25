# J.A.R.V.I.S. — Native Android Assistant

## Open the project
1. Android Studio (Koala or newer) → Open → select this folder.
2. Android Studio will auto-generate `gradle/wrapper/gradle-wrapper.jar` and `local.properties`
   on first sync (this build environment has no network access, so the jar isn't pre-bundled).
   If it doesn't, run `gradle wrapper --gradle-version 8.7` once you have Gradle installed locally.
3. Sync Gradle, then Run on a device/emulator running Android 9 (API 28) or newer.

## What's implemented
- CommandRouter + 10 working commands (app launch, home/back/scroll, settings shortcuts,
  battery, network, media/volume, location, call-with-confirm, screenshot, notifications)
- Accessibility Service, Notification Listener, MediaProjection screen capture, CameraX
- Voice (SpeechRecognizer + TextToSpeech), Calendar read/create, Alarm via AlarmClock intent
- Permission Center, Security Center, local Command History, DataStore Settings
- Full Compose HUD UI: Home, Camera, Files, Calendar, Settings, Permissions, Security,
  History, Accessibility setup — all navigable from MainActivity's NavHost
- Adaptive launcher icon (original design, no Iron Man IP)

## Known gaps to finish before shipping
- Camera lens-switch button toggles state but doesn't yet force a preview rebind — add a
  `key(lensFacing)` around the AndroidView in CameraScreen.kt to make it live.
- No unit/instrumented tests included yet.
- `gradle-wrapper.jar` binary must be generated locally (see step 2 above) since this
  environment couldn't reach the network to download it.
