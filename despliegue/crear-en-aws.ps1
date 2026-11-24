<#
  Crea SmartFlet en AWS: una instancia EC2 con Docker (app + MongoDB).

  Uso (desde la raiz del repo, con la sesion de AWS iniciada: aws login):
      .\despliegue\crear-en-aws.ps1 -Rama main -TipoInstancia t3.micro

  Que crea (todo con el prefijo "smartflet", para encontrarlo y borrarlo facil):
    1. Parametro cifrado /smartflet/admin-clave en SSM Parameter Store (la clave del admin).
    2. Rol IAM + perfil de instancia: permite administrar la instancia con Session Manager
       (sin abrir el puerto SSH) y leer SOLO los parametros /smartflet/*.
    3. Grupo de seguridad: solo el puerto 80 (HTTP) abierto a internet.
    4. La instancia EC2 (Amazon Linux 2023, disco cifrado, IMDSv2 obligatorio).

  Se puede ejecutar de nuevo: lo que ya existe se reutiliza.
#>
param(
    [string]$Rama = 'main',
    [string]$TipoInstancia = 't3.micro',
    [string]$Repo = 'https://github.com/GUERRREROALEXANDER/flota-de-taxis-SmartFlet.git'
)
$ErrorActionPreference = 'Stop'

$region = (aws configure get region)
$cuenta = (aws sts get-caller-identity --query Account --output text)
$tmp = New-Item -ItemType Directory -Force (Join-Path $env:TEMP 'smartflet-despliegue')
Write-Host "Region: $region  |  Cuenta: $cuenta  |  Rama: $Rama  |  Tipo: $TipoInstancia" -ForegroundColor Cyan

# ---------- 1. Clave del admin en Parameter Store ----------
$existe = aws ssm get-parameters --names /smartflet/admin-clave --query 'length(Parameters)' --output text
if ($existe -eq '0') {
    $bytes = New-Object byte[] 15; [Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
    $clave = [Convert]::ToBase64String($bytes).Replace('+', 'x').Replace('/', 'y')
    aws ssm put-parameter --name /smartflet/admin-clave --type SecureString --value $clave `
        --description 'Clave del administrador inicial de SmartFlet' | Out-Null
    Write-Host '1. Clave del admin creada en Parameter Store (/smartflet/admin-clave)' -ForegroundColor Green
} else {
    Write-Host '1. Clave del admin ya existia: se reutiliza' -ForegroundColor Yellow
}

# ---------- 2. Rol IAM y perfil de instancia ----------
$rol = 'smartflet-ec2-rol'
$confianza = @{ Version = '2012-10-17'; Statement = @(@{ Effect = 'Allow'; Principal = @{ Service = 'ec2.amazonaws.com' }; Action = 'sts:AssumeRole' }) } | ConvertTo-Json -Depth 5
$confianza | Set-Content "$tmp\confianza.json"
$permiso = @{ Version = '2012-10-17'; Statement = @(@{ Effect = 'Allow'; Action = @('ssm:GetParameter'); Resource = "arn:aws:ssm:${region}:${cuenta}:parameter/smartflet/*" }) } | ConvertTo-Json -Depth 5
$permiso | Set-Content "$tmp\permiso.json"

aws iam get-role --role-name $rol 2>$null | Out-Null
if ($LASTEXITCODE -ne 0) {
    aws iam create-role --role-name $rol --assume-role-policy-document "file://$tmp\confianza.json" `
        --description 'Instancia EC2 de SmartFlet' | Out-Null
    aws iam attach-role-policy --role-name $rol --policy-arn arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore
    aws iam put-role-policy --role-name $rol --policy-name leer-parametros-smartflet --policy-document "file://$tmp\permiso.json"
    aws iam create-instance-profile --instance-profile-name $rol | Out-Null
    aws iam add-role-to-instance-profile --instance-profile-name $rol --role-name $rol
    Write-Host '2. Rol IAM creado; esperando 15 s a que IAM lo propague...' -ForegroundColor Green
    Start-Sleep 15
} else {
    Write-Host '2. Rol IAM ya existia: se reutiliza' -ForegroundColor Yellow
}

# ---------- 3. Grupo de seguridad (solo puerto 80) ----------
$vpc = aws ec2 describe-vpcs --filters Name=is-default,Values=true --query 'Vpcs[0].VpcId' --output text
$sg = aws ec2 describe-security-groups --filters Name=group-name,Values=smartflet-web Name=vpc-id,Values=$vpc --query 'SecurityGroups[0].GroupId' --output text
if ($sg -eq 'None') {
    $sg = aws ec2 create-security-group --group-name smartflet-web --description 'SmartFlet: HTTP publico' --vpc-id $vpc --query GroupId --output text
    aws ec2 authorize-security-group-ingress --group-id $sg --protocol tcp --port 80 --cidr 0.0.0.0/0 | Out-Null
    Write-Host "3. Grupo de seguridad $sg creado (solo puerto 80)" -ForegroundColor Green
} else {
    Write-Host "3. Grupo de seguridad $sg ya existia" -ForegroundColor Yellow
}

# ---------- 4. Instancia EC2 ----------
$ami = aws ssm get-parameter --name /aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64 --query Parameter.Value --output text
$userData = (Get-Content "$PSScriptRoot\user-data.sh" -Raw).Replace('__RAMA__', $Rama).Replace('__REPO__', $Repo).Replace("`r`n", "`n")
[IO.File]::WriteAllText("$tmp\user-data.sh", $userData)
$disco = '[{"DeviceName":"/dev/xvda","Ebs":{"VolumeSize":20,"VolumeType":"gp3","Encrypted":true,"DeleteOnTermination":true}}]'
$disco | Set-Content "$tmp\disco.json"

$id = aws ec2 run-instances --image-id $ami --instance-type $TipoInstancia `
    --iam-instance-profile Name=$rol --security-group-ids $sg `
    --user-data "file://$tmp\user-data.sh" `
    --block-device-mappings "file://$tmp\disco.json" `
    --metadata-options HttpTokens=required,HttpEndpoint=enabled `
    --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=smartflet},{Key=proyecto,Value=smartflet}]' `
    --query 'Instances[0].InstanceId' --output text
Write-Host "4. Instancia $id creada; esperando a que arranque..." -ForegroundColor Green
aws ec2 wait instance-running --instance-ids $id
$ip = aws ec2 describe-instances --instance-ids $id --query 'Reservations[0].Instances[0].PublicIpAddress' --output text

Write-Host ''
Write-Host "Listo. La app tarda ~5-10 min en compilarse dentro de la instancia." -ForegroundColor Cyan
Write-Host "  URL:        http://$ip"
Write-Host "  Usuario:    admin"
Write-Host "  Clave:      aws ssm get-parameter --name /smartflet/admin-clave --with-decryption --query Parameter.Value --output text"
Write-Host "  Progreso:   .\despliegue\estado.ps1 -Instancia $id"
