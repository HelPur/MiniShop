$ErrorActionPreference = "Stop"

$ProjectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$MavenVersion = "3.9.9"
$MavenDir = Join-Path $ProjectDir "apache-maven-$MavenVersion"
$MavenZip = Join-Path $ProjectDir "apache-maven.zip"
$MavenUrl = "https://archive.apache.org/dist/maven/maven-3/$MavenVersion/binaries/apache-maven-$MavenVersion-bin.zip"

Set-Location $ProjectDir

if (-not (Test-Path (Join-Path $MavenDir "bin\mvn.cmd"))) {
    Write-Host "Downloading Maven $MavenVersion ..."
    Invoke-WebRequest -Uri $MavenUrl -OutFile $MavenZip
    Expand-Archive -Path $MavenZip -DestinationPath $ProjectDir -Force
}

$Mvn = Join-Path $MavenDir "bin\mvn.cmd"
Write-Host "Starting Mini Shop at http://localhost:8080 ..."
& $Mvn spring-boot:run
