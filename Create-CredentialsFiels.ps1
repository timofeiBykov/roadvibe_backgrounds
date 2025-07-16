param(
    [switch] $Overwrite
)

[Console]::OutputEncoding = [Text.Encoding]::UTF8

Write-Host " ~~ Begin create or recreate"

$roadvibeCredentials = "$PSScriptRoot\roadvibe.credentials.properties"
$roadvibeAppCredentials = "$PSScriptRoot\roadvibeapp.credentials.properties"

function New-RoadVibeCredentialsFile {
    Remove-Item -Path $roadvibeCredentials -Force -ErrorAction SilentlyContinue
    $content = @('queueUsername=examplename','queuePass=examplepass','queueHost=127.0.0.1','queuePort=0000','queueVHost=exp')
    Write-Host " ~~ Creating roadvibe app credentials with:`n`t $($content -join "`n`t")"
    foreach ($item in $content) {
        Add-Content -Path $roadvibeCredentials -Value $item
    }
}

function New-RoadVibeAppCredentialsFile {
    Remove-Item -Path $roadvibeAppCredentials -Force -ErrorAction SilentlyContinue
    New-Item -Path $roadvibeAppCredentials -ItemType File
    $content = @('appcenter=00000000-0000-0000-0000-000000000000')
    Write-Host " ~~ Creating roadvibe app credentials with:`n`t $($content -join "`n`t")"
    foreach ($item in $content) {
        Add-Content -Path $roadvibeAppCredentials -Value $item
    }
}

$isRoadvibeCredentialsExist = (Test-Path -Path $roadvibeCredentials)
$isRoadvibeAppCredentialsExist = (Test-Path -Path $roadvibeAppCredentials)

if ($isRoadvibeCredentialsExist -and $Overwrite) {
    New-RoadVibeCredentialsFile
}
elseif (!$isRoadvibeCredentialsExist) {
    New-RoadVibeCredentialsFile
}
else {
    Write-Error -Message "File is already exist -->> $roadvibeCredentials" -Category InvalidOperation
}

if ($isRoadvibeAppCredentialsExist -and $Overwrite) {
    New-RoadVibeAppCredentialsFile
}
elseif (!$isRoadvibeAppCredentialsExist) {
    New-RoadVibeAppCredentialsFile
}
else {
    Write-Error -Message "File is already exist -->> $roadvibeAppCredentials" -Category InvalidOperation
}

Write-Host " ~~ End of creation or recreation"

[Console]::OutputEncoding = [Text.Encoding]::UTF8