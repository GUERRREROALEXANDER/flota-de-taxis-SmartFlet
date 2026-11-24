<#
  Borra TODO lo que creo crear-en-aws.ps1 (instancia, grupo de seguridad, rol y parametro).
  Los datos de MongoDB se pierden con el disco de la instancia.
      .\despliegue\eliminar-de-aws.ps1
#>
$ErrorActionPreference = 'Continue'
$ids = aws ec2 describe-instances --filters 'Name=tag:Name,Values=smartflet' 'Name=instance-state-name,Values=pending,running,stopping,stopped' `
    --query 'Reservations[].Instances[].InstanceId' --output text
if ($ids) {
    aws ec2 terminate-instances --instance-ids $ids.Split() | Out-Null
    Write-Host "Terminando $ids..."; aws ec2 wait instance-terminated --instance-ids $ids.Split()
}
$sg = aws ec2 describe-security-groups --filters 'Name=group-name,Values=smartflet-web' --query 'SecurityGroups[0].GroupId' --output text
if ($sg -and $sg -ne 'None') { aws ec2 delete-security-group --group-id $sg }
$rol = 'smartflet-ec2-rol'
aws iam remove-role-from-instance-profile --instance-profile-name $rol --role-name $rol 2>$null
aws iam delete-instance-profile --instance-profile-name $rol 2>$null
aws iam detach-role-policy --role-name $rol --policy-arn arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore 2>$null
aws iam delete-role-policy --role-name $rol --policy-name leer-parametros-smartflet 2>$null
aws iam delete-role --role-name $rol 2>$null
aws ssm delete-parameter --name /smartflet/admin-clave 2>$null
Write-Host 'Listo: recursos de SmartFlet eliminados.' -ForegroundColor Green
