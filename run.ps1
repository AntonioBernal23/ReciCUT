# =====================================================================
#  ReciCUT - arranca un microservicio con las variables de .env.local
#
#  USO:   .\run.ps1 config-server
#         .\run.ps1 discovery-server
#         .\run.ps1 report-service
#
#  Spring Boot NO lee archivos .env por si solo. Este script carga
#  .env.local en las variables de entorno del proceso y luego lanza el jar,
#  que es exactamente lo que hace el plugin EnvFile de IntelliJ.
# =====================================================================

param(
    [Parameter(Mandatory = $true, Position = 0)]
    [ValidateSet('config-server','discovery-server','auth-service','user-service',
                 'api-gateway','report-service','collection-service')]
    [string]$Servicio
)

$ErrorActionPreference = 'Stop'
$raiz = $PSScriptRoot

# --- 1. Cargar .env.local en variables de entorno --------------------
$envFile = Join-Path $raiz '.env.local'
if (-not (Test-Path $envFile)) {
    Write-Host "No existe .env.local. Copialo de .env.example y llenalo." -ForegroundColor Red
    exit 1
}

$cargadas = 0
Get-Content $envFile | ForEach-Object {
    if ($_ -match '^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*)$') {
        $nombre = $matches[1]
        $valor  = $matches[2].Trim()
        [Environment]::SetEnvironmentVariable($nombre, $valor, 'Process')
        $cargadas++
    }
}
Write-Host "$cargadas variables cargadas desde .env.local" -ForegroundColor DarkGray

# --- 2. Localizar el jar ---------------------------------------------
$jar = Join-Path $raiz "$Servicio\target\$Servicio-0.0.1-SNAPSHOT.jar"
if (-not (Test-Path $jar)) {
    Write-Host "No existe $jar" -ForegroundColor Red
    Write-Host "Compila primero:  .\report-service\mvnw.cmd -f pom.xml clean install -DskipTests"
    exit 1
}

# --- 3. Arrancar ------------------------------------------------------
Write-Host ""
Write-Host "Arrancando $Servicio ..." -ForegroundColor Cyan
Write-Host "(Ctrl+C para detenerlo)" -ForegroundColor DarkGray
Write-Host ""
& java -jar $jar
