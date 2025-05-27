
param (
    [string]$RepoName,
    [Parameter(Mandatory=$true)]
    [string]$DeviceDetection,
    [string]$DeviceDetectionUrl
)

$CxxCiDir = Join-Path $RepoName "ip-intelligence-cxx"
$CxxCiScript = Join-Path $pwd $CxxCiDir "ci" "fetch-assets.ps1"

& $CxxCiScript `
    -RepoName $CxxCiDir `
    -DeviceDetection $DeviceDetection `
    -DeviceDetectionUrl $DeviceDetectionUrl

exit $LASTEXITCODE
