[Console]::OutputEncoding = [Text.Encoding]::UTF8
Write-Host " ~~ Begin of generation ~~ "

Write-Host " ~~ Generating ~~ "
./gradlew roadvibe:hideSecretFromPropertiesFile -PpropertiesFileName='roadvibe.credentials.properties' -Ppackage='com.spark.roadvibe.lib'
./gradlew app:hideSecretFromPropertiesFile -PpropertiesFileName='roadvibeapp.credentials.properties' -Ppackage='com.spark.roadvibe.app'
Write-Host " ~~ Generation done ~~ "

Start-Sleep -Milliseconds 500

Write-Host " ~~ Renaming generated files for lib ~~ "
$firstLocation = Get-Location
$targetLocation = "$firstLocation\roadvibe\src\main\"
Set-Location $targetLocation -PassThru

$currentLocation = Get-Location
(Get-Content -Path "$currentLocation\cpp\secrets.cpp").Replace('#include "secrets.hpp"', '#include "secretslib.hpp"') | Set-Content -Path "$currentLocation\cpp\secrets.cpp"
(Get-Content -Path "$currentLocation\cpp\CMakeLists.txt").Replace('secrets', 'secretslib') | Set-Content -Path "$currentLocation\cpp\CMakeLists.txt"

Remove-item -Path "$currentLocation\cpp\secretslib.cpp" -ErrorAction SilentlyContinue
Rename-Item -Path "$currentLocation\cpp\secrets.cpp" -NewName "secretslib.cpp"
Remove-Item -Path "$currentLocation\cpp\secretslib.hpp" -ErrorAction SilentlyContinue
Rename-Item -Path "$currentLocation\cpp\secrets.hpp" -NewName "secretslib.hpp"

$javaCurrentPath = "$currentLocation\java\com\spark\roadvibe\lib\Secrets.kt"
(Get-Content -Path $javaCurrentPath).Replace('secrets', 'secretslib') | Set-Content -Path $javaCurrentPath


Set-Location $firstLocation -PassThru
Write-Host " ~~ Renaming Done ~~ "
Write-Host " ~~ End of generation ~~"