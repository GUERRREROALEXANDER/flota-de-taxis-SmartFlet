# Pruebas con Postman

## Importar
En Postman: **Import** → arrastra los 3 archivos de esta carpeta:

| Archivo | Para qué |
|---|---|
| `SmartFlet.postman_collection.json` | 89 peticiones con pruebas automáticas |
| `SmartFlet-aws.postman_environment.json` | Apunta al servidor en AWS (`http://3.150.79.12/api`) |
| `SmartFlet-local.postman_environment.json` | Apunta a `http://localhost:8080/api` |

Arriba a la derecha elige el entorno (**SmartFlet AWS** o **SmartFlet local**) y en `adminClave`
escribe la clave del admin en la columna **Current value** (no se guarda en el archivo).

## Cómo funciona el login en Postman
1. La carpeta **00 · Sesion** hace `POST /auth/login`.
2. El servidor responde con la cookie `JSESSIONID`; Postman la guarda sola.
3. Todas las peticiones siguientes llevan esa cookie, así que ya no dan 401.

Si haces una petición suelta y te da **401**, ejecuta antes *Login como administrador*.
Si da **403**, iniciaste sesión con la cuenta demo (operador): vuelve a entrar como admin.

## Ejecutar todo
Clic derecho en la colección → **Run collection** → **Run**. Debe salir todo en verde.

- La colección completa **crea** vehículos, conductores, viajes, etc. con datos al azar.
  Contra AWS eso mete registros de prueba en la demo; para presentar, ejecuta solo
  las carpetas **00 · Sesion** y **10 · Roles y usuarios**, o usa el entorno local.
- Si fallas la clave 5 veces seguidas, el login se bloquea 5 minutos (respuesta 429).

Desde la terminal (opcional):
```powershell
npx newman run docs/postman/SmartFlet.postman_collection.json -e docs/postman/SmartFlet-aws.postman_environment.json --env-var adminClave=TU_CLAVE --folder "00 · Sesion (login)" --folder "10 · Roles y usuarios"
```
