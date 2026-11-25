<#
  Despliega los ultimos cambios de GitHub en la instancia, sin SSH (usa SSM Run Command).
  Antes: haz push de tu rama.
      .\despliegue\actualizar.ps1 -Rama main
#>
param([string]$Rama = 'main')
$ErrorActionPreference = 'Stop'
# AWS CLI usa Python: sin esto falla al imprimir caracteres como → o ✓ en la consola de Windows.
$env:PYTHONUTF8 = '1'

$id = aws ec2 describe-instances --filters 'Name=tag:Name,Values=smartflet' 'Name=instance-state-name,Values=running' `
    --query 'Reservations[0].Instances[0].InstanceId' --output text
if ($id -eq 'None') { throw 'No hay una instancia "smartflet" encendida. ¿La creaste con crear-en-aws.ps1?' }

$comandos = @(
    'cd /opt/smartflet',
    "git fetch --depth 1 origin $Rama",
    "git checkout -B $Rama FETCH_HEAD",
    'docker compose up -d --build',
    'docker image prune -f',
    'docker compose ps'
)
$parametros = @{ commands = $comandos } | ConvertTo-Json -Compress
$cmd = aws ssm send-command --instance-ids $id --document-name AWS-RunShellScript `
    --comment 'Actualizar SmartFlet' --parameters $parametros --query 'Command.CommandId' --output text
Write-Host "Actualizando $id (comando $cmd). Puede tardar unos minutos..." -ForegroundColor Cyan
aws ssm wait command-executed --command-id $cmd --instance-id $id 2>$null
aws ssm get-command-invocation --command-id $cmd --instance-id $id --query '[Status, StandardOutputContent, StandardErrorContent]' --output text
