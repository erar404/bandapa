---
name: bandapa-apk-patch
description: Build the bandapa debug APK and upload it to Supabase storage. Use when the user says "build and upload apk", "patch bandapa", "release apk", "deploy apk", "bandapa-apk-patch", or wants to build and push the Android app to storage.
disable-model-invocation: true
---

Build the debug APK for this KMM project and upload it to Supabase storage as `bandapa-latest.apk`.

## Step 1 — Build the APK

Run this via the PowerShell tool:

```powershell
$env:JAVA_HOME = "C:\Program Files (x86)\Android\openjdk\jdk-17.0.8.101-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
Set-Location "C:\Users\Arellano\ERA\Coding\bandapa"
.\gradlew.bat :composeApp:assembleDebug
```

The APK will be at:
`C:\Users\Arellano\ERA\Coding\bandapa\composeApp\build\outputs\apk\debug\composeApp-debug.apk`

If the build fails, show the full error output and stop.

## Step 2 — Read the Supabase anon key

Parse `local.properties` at the project root:

```powershell
$anonKey = ((Get-Content "C:\Users\Arellano\ERA\Coding\bandapa\local.properties") |
  Where-Object { $_ -match "^supabase\.anon_key=" }) -replace "^supabase\.anon_key=", ""
```

## Step 3 — Upload to Supabase Storage

Upload using `x-upsert: true` so it overwrites the existing file each time:

```powershell
$apkPath  = "C:\Users\Arellano\ERA\Coding\bandapa\composeApp\build\outputs\apk\debug\composeApp-debug.apk"
$uploadUrl = "https://rrfelwwoypouqcjbdzrb.supabase.co/storage/v1/object/releases/bandapa-latest.apk"

$response = Invoke-RestMethod `
  -Uri $uploadUrl `
  -Method Post `
  -Headers @{
    "Authorization" = "Bearer $anonKey"
    "x-upsert"      = "true"
  } `
  -ContentType "application/octet-stream" `
  -InFile $apkPath

$response | ConvertTo-Json
```

## Step 4 — Report result

On success, confirm the APK is live at:
`https://rrfelwwoypouqcjbdzrb.supabase.co/storage/v1/object/public/releases/bandapa-latest.apk`

Show the APK file size and the HTTP response from Supabase.
