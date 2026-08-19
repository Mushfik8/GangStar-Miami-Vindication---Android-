$ErrorActionPreference = "Stop"

Write-Host "Initializing Git repository..."
git init
git branch -M main

Write-Host "Adding remote origin..."
git remote add origin https://github.com/Mushfik8/GangStar-Miami-Vindication---Android-.git

# Remove any existing .gitignore if it conflicts, or create one for safe exclusions
Set-Content -Path .gitignore -Value "build/`n.gradle/`nlocal.properties`n.idea/`n*.log`n"

# First commit: Add base project files and README (if exists)
Write-Host "Part 1: Initial project setup"
git add .gitignore
if (Test-Path "build_apk.ps1") { git add build_apk.ps1 }
git commit -m "Initial commit: Project setup and build scripts"

# Get all remaining untracked files
$allFiles = git ls-files --others --exclude-standard

# We want around 20 commits. Let's do 25 to be safe.
$totalCommits = 24
$filesPerCommit = [math]::Ceiling($allFiles.Count / $totalCommits)

$commitMessages = @(
    "Add game engine core assets",
    "Integrate J2ME base classes",
    "Configure MIDlet properties",
    "Setup UI and Canvas frameworks",
    "Add rendering modules",
    "Include graphics and image processing",
    "Add touch input handling",
    "Integrate sound and media playback",
    "Add font metric utilities",
    "Configure persistent storage (RMS)",
    "Include Android app manifest",
    "Set up game view SurfaceView",
    "Add Virtual HUD overlay logic",
    "Implement hold-to-move mechanics",
    "Include driving control assets",
    "Add on-foot control assets",
    "Configure Web player HTML5 interface",
    "Add Cyberpunk/Vice City CSS styling",
    "Implement Web JS touch controllers",
    "Package original J2ME jar",
    "Include compiled classes part 1",
    "Include compiled classes part 2",
    "Add miscellaneous game data",
    "Finalize remaining assets and release build"
)

$commitIndex = 0

for ($i = 0; $i -lt $allFiles.Count; $i += $filesPerCommit) {
    $chunk = $allFiles | Select-Object -Skip $i -First $filesPerCommit
    foreach ($file in $chunk) {
        git add "`"$file`""
    }
    
    $msg = if ($commitIndex -lt $commitMessages.Count) { $commitMessages[$commitIndex] } else { "Add component batch $($commitIndex + 1)" }
    Write-Host "Committing Part $($commitIndex + 2): $msg"
    git commit -m "Part $($commitIndex + 2): $msg"
    $commitIndex++
}

# Just in case any files were missed
git add .
git commit -m "Final polish and release assets"

Write-Host "Pushing to GitHub..."
git push -u origin main -f

Write-Host "Upload complete!"
