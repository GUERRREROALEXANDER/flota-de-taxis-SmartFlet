# Despliegue de SmartFlet en AWS

Una instancia **EC2** con **Docker Compose**: un contenedor con la app (Spring Boot sirviendo también el frontend) y otro con **MongoDB**.

```
Navegador ──HTTP :80──► EC2 (Amazon Linux 2023)
                         ├─ contenedor app   (Spring Boot :8080 + frontend en /static)
                         └─ contenedor mongo (sin puerto público, volumen mongo-datos)
```

## Decisiones (y por qué)

| Decisión | Por qué | Alternativa |
|---|---|---|
| Una sola imagen (frontend dentro del jar) | Un solo puerto, sin CORS ni proxy extra | Frontend en S3 + CloudFront |
| MongoDB en la misma instancia | Cero costo extra y sin depender de otro servicio | MongoDB Atlas (cambiar `MONGODB_URI` en `.env`) |
| Sin SSH: **Session Manager** | No se abre el puerto 22; se entra con `aws ssm start-session` | Par de claves + puerto 22 |
| Clave del admin en **Parameter Store** (cifrada) | No queda en el repo ni en el script | Escribirla a mano en `.env` |
| **IP elastica** | La URL no cambia aunque apagues y enciendas la instancia | IP publica normal (cambia en cada encendido) |
| Swap de 2 GB | Java + Mongo + compilar en 1 GB de RAM no cabe | Usar `t3.small` (2 GB) |

> ⚠️ **HTTP sin cifrar**: la contraseña viaja en texto plano. Sirve para una demo de clase; para algo real
> pon **CloudFront** delante (da HTTPS gratis con un dominio `*.cloudfront.net`) y cambia `COOKIE_SEGURA=true`.

## Comandos (PowerShell, desde la raíz del repo)

```powershell
aws login                                                   # iniciar sesión en AWS
.\despliegue\crear-en-aws.ps1 -Rama main -TipoInstancia t3.micro
.\despliegue\estado.ps1                                     # ¿ya arrancó? (tarda 5-10 min)
aws ssm get-parameter --name /smartflet/admin-clave --with-decryption --query Parameter.Value --output text

# Después de hacer push de cambios:
.\despliegue\actualizar.ps1 -Rama main

# Entrar a la consola de la instancia (sin SSH):
aws ssm start-session --target <id-instancia>               # requiere el Session Manager plugin

# Borrar todo lo creado:
.\despliegue\eliminar-de-aws.ps1
```

## Si algo falla

| Síntoma | Revisa |
|---|---|
| La página no carga tras 10 min | `.\despliegue\estado.ps1` → mira el final de `cloud-init-output.log` |
| `Access Denied` de repente | ¿Tienes un límite de gasto (spend limit) en AWS Settings → Billing? |
| "Usuario o contraseña incorrectos" con la clave correcta | El admin se crea solo en el **primer** arranque; si cambiaste el parámetro después, la clave vieja sigue en Mongo |
| Contenedor `app` reiniciándose | `docker compose logs app` dentro de la instancia: casi siempre es memoria (sube a `t3.small`) |
