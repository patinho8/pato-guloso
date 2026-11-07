param([ValidateSet('build','runClient','runServer')][string]$Task = 'build')
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$ProjectRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $ProjectRoot
$ToolRoot = Join-Path $ProjectRoot '.tools'
New-Item -ItemType Directory -Force -Path $ToolRoot | Out-Null
$LogPath = Join-Path $ProjectRoot 'COMPILACAO.log'
$BuildExitCode = 1

function Get-VerifiedFile([string]$Url, [string]$Destination, [string]$ExpectedHash) {
    if ($ExpectedHash -notmatch '^[a-fA-F0-9]{64}$') { throw 'Checksum SHA-256 invalido.' }
    if (Test-Path -LiteralPath $Destination) {
        if ((Get-FileHash -Algorithm SHA256 -LiteralPath $Destination).Hash -ieq $ExpectedHash) { return }
        Remove-Item -LiteralPath $Destination
    }
    $PartialPath = $Destination + '.partial'
    Invoke-WebRequest -UseBasicParsing -Uri $Url -OutFile $PartialPath -TimeoutSec 900
    if ((Get-FileHash -Algorithm SHA256 -LiteralPath $PartialPath).Hash -ine $ExpectedHash) {
        Remove-Item -LiteralPath $PartialPath
        throw 'Download corrompido: checksum diferente. Execute novamente.'
    }
    Move-Item -LiteralPath $PartialPath -Destination $Destination -Force
}

try {
    Start-Transcript -Path $LogPath -Force | Out-Null
    Write-Host 'MARE ELETRICA - NeoForge 1.21.1 - compilador rev. 3' -ForegroundColor Cyan
    Write-Host 'Primeira compilacao: precisa de internet e pode baixar mais de 1 GB.'
    Write-Host 'As ferramentas portateis ficam nesta pasta; nao e necessario administrador.'
    $JavaRoot = $null
    $Candidates = @()
    if ($env:JAVA_HOME) { $Candidates += $env:JAVA_HOME }
    $JavaOnPath = Get-Command javac.exe -ErrorAction SilentlyContinue
    if ($JavaOnPath) { $Candidates += (Split-Path -Parent (Split-Path -Parent $JavaOnPath.Source)) }
    $Candidates += @(Get-ChildItem -LiteralPath $ToolRoot -Directory | Where-Object { $_.Name -like 'jdk-*' } | ForEach-Object { $_.FullName })
    foreach ($Candidate in $Candidates) {
        $ReleaseFile = Join-Path $Candidate 'release'
        if ((Test-Path -LiteralPath (Join-Path $Candidate 'bin/javac.exe')) -and (Test-Path -LiteralPath $ReleaseFile)) {
            if ((Get-Content -LiteralPath $ReleaseFile -Raw) -match 'JAVA_VERSION="21[.\"]') { $JavaRoot = $Candidate; break }
        }
    }
    if (-not $JavaRoot) {
        Write-Host 'Baixando Java 21 (Eclipse Temurin, site oficial)...'
        $JavaAssets = Invoke-RestMethod -Uri 'https://api.adoptium.net/v3/assets/latest/21/hotspot?architecture=x64&image_type=jdk&os=windows&vendor=eclipse' -TimeoutSec 90
        $Package = $JavaAssets[0].binary.package
        if (-not $Package.link -or -not $Package.checksum) { throw 'Nao foi possivel localizar o Java 21 oficial.' }
        $JavaZip = Join-Path $ToolRoot 'temurin21-windows-x64.zip'
        Get-VerifiedFile $Package.link $JavaZip $Package.checksum
        Expand-Archive -LiteralPath $JavaZip -DestinationPath $ToolRoot -Force
        $JavaRoot = (Get-ChildItem -LiteralPath $ToolRoot -Directory | Where-Object { $_.Name -like 'jdk-21*' } | Select-Object -First 1).FullName
        if (-not $JavaRoot) { throw 'Java 21 nao foi extraido. Verifique espaco no disco.' }
    }
    $env:JAVA_HOME = $JavaRoot
    $env:PATH = (Join-Path $JavaRoot 'bin') + ';' + $env:PATH
    Write-Host ('Java: ' + $JavaRoot)
    $GradleVersion = '8.10.2'
    $GradleCmd = Join-Path $ToolRoot ('gradle-' + $GradleVersion + '/bin/gradle.bat')
    if (-not (Test-Path -LiteralPath $GradleCmd)) {
        Write-Host 'Baixando Gradle 8.10.2 (site oficial)...'
        $GradleUrl = 'https://services.gradle.org/distributions/gradle-8.10.2-bin.zip'
        # Gradle serves .sha256 as application/octet-stream. In Windows
        # PowerShell 5.1, .Content may be byte[], whose string cast is not
        # the checksum text. Save the bytes and decode the file explicitly.
        $GradleHashPath = Join-Path $ToolRoot 'gradle-8.10.2-bin.zip.sha256'
        Invoke-WebRequest -UseBasicParsing -Uri ($GradleUrl + '.sha256') -OutFile $GradleHashPath -TimeoutSec 90
        $GradleHash = [System.IO.File]::ReadAllText($GradleHashPath).Trim()
        if ($GradleHash -notmatch '\A[a-fA-F0-9]{64}\z') {
            throw 'O servidor nao retornou um SHA-256 valido do Gradle. Tente novamente e envie COMPILACAO.log se continuar.'
        }
        $GradleZip = Join-Path $ToolRoot 'gradle-8.10.2-bin.zip'
        Get-VerifiedFile $GradleUrl $GradleZip $GradleHash
        Expand-Archive -LiteralPath $GradleZip -DestinationPath $ToolRoot -Force
    }
    Write-Host 'Compilando o codigo e resolvendo dependencias...'
    & $GradleCmd --no-daemon --console=plain $Task
    $BuildExitCode = $LASTEXITCODE
    if ($BuildExitCode -ne 0) { throw ('Gradle retornou erro ' + $BuildExitCode + '. Envie o arquivo COMPILACAO.log para revisao.') }
    if ($Task -eq 'build') {
        $BuildFile = Get-Content -LiteralPath (Join-Path $ProjectRoot 'build.gradle') -Raw
        $VersionMatch = [regex]::Match($BuildFile, '(?m)^version\s*=\s*''([^'']+)''')
        if (-not $VersionMatch.Success) { throw 'Nao foi possivel ler a versao em build.gradle.' }
        $Jar = Join-Path $ProjectRoot ('build/libs/mare-eletrica-' + $VersionMatch.Groups[1].Value + '.jar')
        if (-not (Test-Path -LiteralPath $Jar)) { throw 'O Gradle terminou, mas o JAR nao foi encontrado.' }
        Write-Host ''
        Write-Host 'COMPILACAO CONCLUIDA' -ForegroundColor Green
        Write-Host 'Arquivo para a pasta mods:'
        Write-Host $Jar -ForegroundColor Cyan
        Write-Host 'Nao copie o arquivo -sources.jar. Teste primeiro em um mundo novo.'
        # Install straight into the TLauncher profile "hidroeletrica" (old Mare Eletrica JARs are moved to a backup folder, never deleted)
        $GameProfile = Join-Path $env:APPDATA '.minecraft\versions\hidroeletrica'
        $ModsDir = Join-Path $GameProfile 'mods'
        if (Test-Path -LiteralPath $ModsDir) {
            $Backup = Join-Path $GameProfile 'mods_antigos_mare_eletrica'
            New-Item -ItemType Directory -Force -Path $Backup | Out-Null
            Get-ChildItem -LiteralPath $ModsDir -Filter 'mare-eletrica-*.jar' | ForEach-Object {
                Move-Item -LiteralPath $_.FullName -Destination (Join-Path $Backup $_.Name) -Force
                Write-Host ('JAR antigo movido para o backup: ' + $_.Name)
            }
            Copy-Item -LiteralPath $Jar -Destination $ModsDir -Force
            Write-Host ''
            Write-Host ('INSTALADO no perfil hidroeletrica: ' + (Join-Path $ModsDir (Split-Path -Leaf $Jar))) -ForegroundColor Green
            Write-Host 'Abra o TLauncher no perfil hidroeletrica e jogue.'
        } else {
            Write-Host 'Perfil hidroeletrica nao encontrado; copie o JAR acima para a pasta mods manualmente.' -ForegroundColor Yellow
        }
    }
    $BuildExitCode = 0
} catch {
    $BuildExitCode = 1
    Write-Host ''
    Write-Host ('ERRO: ' + $_.Exception.Message) -ForegroundColor Red
    Write-Host 'O registro esta em COMPILACAO.log. Nenhum JAR valido foi confirmado nesta tentativa.'
} finally {
    try { Stop-Transcript | Out-Null } catch { }
}
exit $BuildExitCode
