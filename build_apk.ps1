# Android Build Script for GangStar: Miami Vindication
$ErrorActionPreference = "Stop"

$SdkDir = "C:\Users\MT\AppData\Local\Android\Sdk"
$BuildToolsDir = "$SdkDir\build-tools\34.0.0"
$PlatformJar = "$SdkDir\platforms\android-34\android.jar"
$Javac = "C:\Program Files\Java\jdk-23\bin\javac.exe"
$Jar = "C:\Program Files\Java\jdk-23\bin\jar.exe"
$Keytool = "C:\Program Files\Java\jdk-23\bin\keytool.exe"

$ProjectDir = "c:\Users\MT\Desktop\game\android_app"
$BuildDir = "$ProjectDir\build_output"
$BinDir = "$BuildDir\bin"
$ClassesDir = "$BuildDir\classes"
$ResCompiledDir = "$BuildDir\res_compiled"
$ApkStagingDir = "$BuildDir\apk_staging"

Write-Host "=== 1. Preparing build directories ==="
if (Test-Path $BuildDir) {
    try {
        Remove-Item -Recurse -Force $BuildDir -ErrorAction SilentlyContinue
    } catch {}
}
New-Item -ItemType Directory -Force -Path $BinDir, $ClassesDir, $ResCompiledDir, $ApkStagingDir | Out-Null

Write-Host "=== 2. Compiling Java Source Files ==="
$JavaFiles = Get-ChildItem -Recurse "$ProjectDir\src\main\java\*.java" | ForEach-Object { $_.FullName }
& $Javac -source 8 -target 8 -bootclasspath $PlatformJar -cp "$PlatformJar" -d $ClassesDir $JavaFiles

Write-Host "=== 3. Copying Game .class Files to Classes Directory ==="
Copy-Item "c:\Users\MT\Desktop\game\*.class" -Destination $ClassesDir -Force

Write-Host "=== 4. Dexing Classes into classes.dex with d8 ==="
& $Jar cvf "$BuildDir\classes.jar" -C $ClassesDir .
& "$BuildToolsDir\d8.bat" --min-api 21 --lib $PlatformJar --output $BinDir "$BuildDir\classes.jar"

Write-Host "=== 5. Compiling Android Resources with aapt2 ==="
& "$BuildToolsDir\aapt2.exe" compile --dir "$ProjectDir\src\main\res" -o "$ResCompiledDir\resources.zip"

Write-Host "=== 6. Linking Resources & Generating Initial APK ==="
& "$BuildToolsDir\aapt2.exe" link -o "$BinDir\unaligned.apk" -I $PlatformJar --manifest "$ProjectDir\src\main\AndroidManifest.xml" -A "$ProjectDir\src\main\assets" "$ResCompiledDir\resources.zip" --auto-add-overlay

Write-Host "=== 7. Staging and Adding classes.dex + Assets in One Step ==="
Copy-Item "$BinDir\classes.dex" -Destination $ApkStagingDir -Force

$AssetNames = @("0", "1", "10", "11", "12", "13", "13.1", "13.2", "13.3", "13.4", "13.5", "14", "15", "15.1", "16", "17", "18", "19", "19.1", "2", "20", "21", "22", "23", "24", "24.1", "24.2", "25", "26", "3", "4", "5", "6", "6.1", "7", "8", "9", "999", "T", "dataIGP", "icon.png")

foreach ($item in $AssetNames) {
    if (Test-Path "c:\Users\MT\Desktop\game\$item") {
        Copy-Item "c:\Users\MT\Desktop\game\$item" -Destination "$ApkStagingDir\$item" -Force
    }
}

# Single atomic update into unaligned.apk
& $Jar uf "$BinDir\unaligned.apk" -C $ApkStagingDir .

Write-Host "=== 8. Zipaligning APK ==="
$AlignedApk = "$BinDir\aligned.apk"
& "$BuildToolsDir\zipalign.exe" -f -v 4 "$BinDir\unaligned.apk" $AlignedApk

Write-Host "=== 9. Creating Keystore and Signing APK ==="
$Keystore = "$ProjectDir\debug.keystore"
if (-not (Test-Path $Keystore)) {
    & $Keytool -genkeypair -v -keystore $Keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
}

$FinalApk = "c:\Users\MT\Desktop\game\Gangstar_Miami_Android.apk"
& "$BuildToolsDir\apksigner.bat" sign --ks $Keystore --ks-pass pass:android --ks-key-alias androiddebugkey --key-pass pass:android --out $FinalApk $AlignedApk

Write-Host "=== 10. Verifying Signed APK ==="
& "$BuildToolsDir\apksigner.bat" verify --verbose $FinalApk

Write-Host "SUCCESS! APK created at: $FinalApk"
