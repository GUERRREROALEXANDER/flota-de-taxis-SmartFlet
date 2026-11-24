<#
  Muestra si SmartFlet ya arranco en la instancia y las ultimas lineas del log.
      .\despliegue\estado.ps1
#>
param([string]$Instancia)
$ErrorActionPreference = 'Stop'
if (-not $Instancia) {
    $Instancia = aws ec2 describe-instances --filters Name=tag:Name,Values=smartflet Name=instance-state-name,Values=running `
        --query 'Reservations[0].Instances[0].InstanceId' --output text
}
$ip = aws ec2 describe-instances --instance-ids $Instancia --query 'Reservations[0].Instances[0].PublicIpAddress' --output text

$comandos = @(
    'tail -n 15 /var/log/cloud-init-output.log',
    'echo ---- contenedores',
    'cd /opt/smartflet 2>/dev/null && docker compose ps || echo "todavia instalando"',
    'echo ---- log app',
    'cd /opt/smartflet 2>/dev/null && docker compose logs --tail 10 app || true'
)
$parametros = @{ commands = $comandos } | ConvertTo-Json -Compress
$cmd = aws ssm send-command --instance-ids $Instancia --document-name AWS-RunShellScript --parameters $parametros --query 'Command.CommandId' --output text
aws ssm wait command-executed --command-id $cmd --instance-id $Instancia 2>$null
aws ssm get-command-invocation --command-id $cmd --instance-id $Instancia --query StandardOutputContent --output text

try {
    $r = Invoke-WebRequest "http://$ip/" -TimeoutSec 5 -SkipHttpErrorCheck
    Write-Host "`nhttp://$ip responde $($r.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "`nhttp://$ip aun no responde (normal durante los primeros minutos)" -ForegroundColor Yellow
}
