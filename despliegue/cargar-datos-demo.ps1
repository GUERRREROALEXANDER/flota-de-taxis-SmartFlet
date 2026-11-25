<#
  Carga datos de ejemplo en SmartFlet y crea la cuenta demo (demo / demo2026, rol OPERADOR).

      .\despliegue\cargar-datos-demo.ps1 -Url http://3.150.79.12

  Como funciona:
    1. Inicia sesion como admin (la clave se lee de Parameter Store).
    2. Crea todo POR LA API, asi los datos pasan las mismas validaciones que en la app
       (placa unica, licencia vigente, vehiculo en turno para asignar, etc.).
    3. La API pone siempre "ahora" como fecha de los viajes; para que la grafica de 7 dias
       tenga forma, al final mueve hacia atras las fechas de los viajes cerrados directamente
       en MongoDB (via SSM, sin SSH).
  Si ya hay vehiculos cargados, no hace nada (para no duplicar).
#>
param(
    [Parameter(Mandatory)] [string]$Url,
    [string]$AdminClave
)
$ErrorActionPreference = 'Stop'
$api = "$($Url.TrimEnd('/'))/api"
if (-not $AdminClave) {
    $AdminClave = aws ssm get-parameter --name /smartflet/admin-clave --with-decryption --query Parameter.Value --output text
}

$sesion = New-Object Microsoft.PowerShell.Commands.WebRequestSession
function Llamar($ruta, $cuerpo = @{}, $metodo = 'Post') {
    try {
        Invoke-RestMethod "$api$ruta" -Method $metodo -ContentType 'application/json; charset=utf-8' `
            -Body ([Text.Encoding]::UTF8.GetBytes(($cuerpo | ConvertTo-Json -Depth 5))) -WebSession $sesion
    } catch {
        throw "Fallo $metodo $ruta → $($_.ErrorDetails.Message)"
    }
}

$null = Llamar '/auth/login' @{ usuario = 'admin'; clave = $AdminClave }
if ((Invoke-RestMethod "$api/vehiculos" -WebSession $sesion).Count -gt 0) {
    Write-Host 'Ya hay datos cargados: no se hace nada.' -ForegroundColor Yellow
    return
}
$hoy = Get-Date
$fmt = { param($d) $d.ToString('yyyy-MM-ddTHH:mm:ss') }

# ---------- Cuenta demo ----------
try { $null = Llamar '/usuarios' @{ nombreUsuario = 'demo'; nombre = 'Cuenta Demo'; clave = 'demo2026'; rol = 'OPERADOR' } }
catch { Write-Host 'La cuenta demo ya existia' -ForegroundColor Yellow }
$null = Llamar '/usuarios' @{ nombreUsuario = 'laura.ops'; nombre = 'Laura Martinez'; clave = 'operadora2026'; rol = 'OPERADOR' }
Write-Host '1. Usuarios demo y laura.ops creados'

# ---------- Tarifa ----------
$null = Llamar '/tarifas' @{ nombre = 'Tarifa urbana 2026'; valorBase = 4800; valorPorKm = 1350
    fechaInicioVigencia = $hoy.AddDays(-60).ToString('yyyy-MM-dd'); fechaFinVigencia = $hoy.AddDays(300).ToString('yyyy-MM-dd') }

# ---------- Flota ----------
$modelos = @(
    @('KLM482', 'Hyundai Grand i10', 'Sedan'), @('TXB915', 'Kia Picanto', 'Hatchback'),
    @('BOG301', 'Chevrolet Onix', 'Sedan'), @('SMF777', 'Renault Logan', 'Sedan'),
    @('PQR246', 'Toyota Corolla Hibrido', 'Sedan'), @('VAN120', 'Hyundai H1', 'Van'))
$veh = foreach ($m in $modelos) {
    Llamar '/vehiculos' @{ placa = $m[0]; modelo = $m[1]; capacidad = $(if ($m[2] -eq 'Van') { 8 } else { 4 }); tipo = $m[2]; estadoOperativo = $true }
}
$null = Llamar "/vehiculos/$($veh[5].id)/estado" @{ estadoOperativo = $false } 'Patch'   # la van en el taller

$nombresConductores = 'Carlos Perez', 'Marta Rios', 'Andres Gil', 'Paola Herrera', 'Jorge Castano'
$con = for ($i = 0; $i -lt 5; $i++) {
    # Paola tiene la licencia por vencer en 20 dias: el panel lo avisa.
    $vence = if ($i -eq 3) { $hoy.AddDays(20) } else { $hoy.AddYears(2) }
    Llamar '/conductores' @{ identificacion = "1020304$i$i"; nombre = $nombresConductores[$i]
        numeroLicencia = "LIC-2026-0$i"; fechaVencimientoLicencia = $vence.ToString('yyyy-MM-dd') }
}

$rutas = @(@('Terminal del Norte', 'Aeropuerto El Dorado', 18.4, 35), @('Centro Historico', 'Zona Rosa', 6.2, 20),
           @('Unicentro', 'Parque 93', 3.1, 12), @('Chapinero', 'Universidad Nacional', 4.8, 18), @('Suba', 'Centro Comercial Andino', 11.5, 32))
foreach ($r in $rutas) { $null = Llamar '/rutas' @{ origen = $r[0]; destino = $r[1]; distanciaKm = $r[2]; duracionEstimadaMinutos = $r[3] } }

$nombresPasajeros = 'Lucia Torres', 'Juan Mendez', 'Sofia Castro', 'Pedro Ruiz', 'Valentina Ospina', 'Mateo Vargas', 'Isabella Rojas', 'Samuel Diaz'
$pas = for ($i = 0; $i -lt $nombresPasajeros.Count; $i++) {
    Llamar '/pasajeros' @{ identificacion = "5060708$i"; nombre = $nombresPasajeros[$i]; telefono = "+57 310 555 01$i$i"
        correoElectronico = ($nombresPasajeros[$i].ToLower().Replace(' ', '.') + '@correo.com') }
}
Write-Host '2. Tarifa, 6 vehiculos, 5 conductores, 5 rutas y 8 pasajeros creados'

# ---------- Turnos: 4 en curso y 1 programado para manana ----------
for ($i = 0; $i -lt 4; $i++) {
    $t = Llamar '/turnos' @{ conductorId = $con[$i].id; vehiculoId = $veh[$i].id
        fechaInicio = (& $fmt $hoy.AddMinutes(-10)); fechaFin = (& $fmt $hoy.AddHours(10)) }
    $null = Llamar "/turnos/$($t.id)/iniciar" @{} 'Patch'
}
$manana = $hoy.Date.AddDays(1).AddHours(6)
$null = Llamar '/turnos' @{ conductorId = $con[4].id; vehiculoId = $veh[4].id; fechaInicio = (& $fmt $manana); fechaFin = (& $fmt $manana.AddHours(8)) }
Write-Host '3. Turnos creados (4 en curso, 1 programado)'

# ---------- Viajes ----------
$lugares = 'Terminal del Norte', 'Aeropuerto El Dorado', 'Centro Historico', 'Zona Rosa', 'Unicentro', 'Parque 93',
           'Chapinero', 'Universidad Nacional', 'Suba', 'Centro Comercial Andino', 'Usaquen', 'Salitre Plaza', 'Kennedy', 'Calle 80'
$metodos = 'EFECTIVO', 'TARJETA', 'TRANSFERENCIA', 'EFECTIVO', 'TARJETA'
$rnd = New-Object Random 2026
function NuevoViaje($n) {
    $o = $lugares[$rnd.Next($lugares.Count)]; do { $d = $lugares[$rnd.Next($lugares.Count)] } while ($d -eq $o)
    Llamar '/viajes' @{ pasajeroId = $pas[$n % $pas.Count].id; origen = $o; destino = $d }
}
# 20 finalizados (cada uno: solicitar → asignar → iniciar → finalizar con pago)
for ($n = 0; $n -lt 20; $n++) {
    $v = NuevoViaje $n
    $null = Llamar "/viajes/$($v.id)/asignar" @{ vehiculoId = $veh[$n % 4].id } 'Patch'
    $null = Llamar "/viajes/$($v.id)/iniciar" @{} 'Patch'
    $km = [Math]::Round(2.5 + $rnd.NextDouble() * 16, 1)
    $null = Llamar "/viajes/$($v.id)/finalizar" @{ distanciaKm = $km; metodoPago = $metodos[$n % $metodos.Count] } 'Patch'
}
# 2 cancelados
for ($n = 20; $n -lt 22; $n++) { $v = NuevoViaje $n; $null = Llamar "/viajes/$($v.id)/cancelar" @{} 'Patch' }
# Activos: 1 en curso, 1 asignado, 2 solicitados (esperando vehiculo)
$v = NuevoViaje 22; $null = Llamar "/viajes/$($v.id)/asignar" @{ vehiculoId = $veh[0].id } 'Patch'; $null = Llamar "/viajes/$($v.id)/iniciar" @{} 'Patch'
$v = NuevoViaje 23; $null = Llamar "/viajes/$($v.id)/asignar" @{ vehiculoId = $veh[1].id } 'Patch'
$null = NuevoViaje 24; $null = NuevoViaje 25
# Un pago anulado, para que se vea ese estado
$pagos = Invoke-RestMethod "$api/pagos" -WebSession $sesion
$null = Llamar "/pagos/$($pagos[0].id)/anular" @{} 'Patch'
Write-Host '4. 26 viajes creados (20 finalizados, 2 cancelados, 4 activos) y 1 pago anulado'

# ---------- Repartir los viajes cerrados en los ultimos 7 dias ----------
$id = aws ec2 describe-instances --filters 'Name=tag:Name,Values=smartflet' 'Name=instance-state-name,Values=running' `
    --query 'Reservations[0].Instances[0].InstanceId' --output text
$js = @'
let i = 0;
db.viajes.find({ estadoViaje: { $in: ['FINALIZADO', 'CANCELADO'] } }).sort({ fechaSolicitud: 1 }).forEach((v) => {
  const dias = [6, 6, 5, 5, 5, 4, 4, 3, 3, 3, 3, 2, 2, 1, 1, 1, 0, 0, 0, 0, 2, 4][i % 22];
  const ms = dias * 86400000 + ((i * 7919) % 36000000);
  const mover = (f) => (f ? new Date(f.getTime() - ms) : f);
  const cambios = { fechaSolicitud: mover(v.fechaSolicitud), fechaInicio: mover(v.fechaInicio), fechaFin: mover(v.fechaFin) };
  if (v.pago) cambios['pago.fechaPago'] = mover(v.pago.fechaPago);
  db.viajes.updateOne({ _id: v._id }, { $set: cambios });
  i++;
});
print('viajes movidos: ' + i);
'@
$b64 = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($js))
$parametros = @{ commands = @(
    'cd /opt/smartflet',
    "echo $b64 | base64 -d > /tmp/fechas.js",
    'docker compose exec -T mongo mongosh --quiet smartflet < /tmp/fechas.js'
) } | ConvertTo-Json -Compress
$cmd = aws ssm send-command --instance-ids $id --document-name AWS-RunShellScript --parameters $parametros --query Command.CommandId --output text
aws ssm wait command-executed --command-id $cmd --instance-id $id 2>$null
Write-Host ('5. ' + (aws ssm get-command-invocation --command-id $cmd --instance-id $id --query StandardOutputContent --output text).Trim())

Write-Host "`nListo. Entra en $Url con demo / demo2026 (operador) o con admin." -ForegroundColor Cyan
