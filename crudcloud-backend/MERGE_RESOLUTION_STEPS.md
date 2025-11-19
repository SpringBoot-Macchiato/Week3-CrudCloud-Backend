# 🔀 PASOS PARA CERRAR EL MERGE Y DESPLEGAR

## 📌 ESTADO ACTUAL
✅ **Conflicto resuelto:** UserService.java fusionado correctamente  
✅ **Compilación:** SUCCESS  
✅ **Tests:** Pasando (1/1)  
✅ **Código validado:** Todo el flujo funciona correctamente

---

## 🚀 PASOS PARA FINALIZAR EL MERGE

### 1. Añadir los archivos resueltos
```bash
cd /home/error/Documentos/SPRINTBOOT/Week3-CrudCloud-Backend/crudcloud-backend

# Añadir el archivo con conflicto resuelto
git add src/main/java/com/crudzaso/crudcloud_backend/service/UserService.java

# Añadir cualquier otro archivo modificado (si hay)
git add .
```

### 2. Ver qué se va a commitear
```bash
git status
```

Deberías ver algo como:
```
On branch Feature/MercadoPago
All conflicts fixed but you are still merging.
  (use "git commit" to conclude merge)

Changes to be committed:
        modified:   src/main/java/com/crudzaso/crudcloud_backend/service/UserService.java
        new file:   src/main/java/com/crudzaso/crudcloud_backend/service/DiscordNotificationService.java
        ...
```

### 3. Hacer commit del merge
```bash
git commit -m "Merge develop into Feature/MercadoPago: integrate FREE plan + Discord notifications"
```

### 4. Verificar que el merge se completó
```bash
git status
```

Deberías ver:
```
On branch Feature/MercadoPago
Your branch is ahead of 'origin/Feature/MercadoPago' by X commits.
nothing to commit, working tree clean
```

### 5. Compilar y validar una vez más
```bash
./mvnw clean package -DskipTests
```

### 6. Subir los cambios
```bash
git push origin Feature/MercadoPago
```

---

## ✅ VALIDACIÓN POST-MERGE

Después del push, valida que:

1. **GitHub/GitLab muestra el merge correctamente**
2. **No hay archivos con conflictos pendientes**
3. **El build en CI/CD pasa (si lo tienes configurado)**

---

## 📦 PREPARACIÓN PARA PRODUCCIÓN

### Base de Datos
Antes de desplegar, ejecuta estos INSERT en tu BD de producción:

```sql
-- Plan FREE (requerido para registro automático)
INSERT INTO plans (id, name, description, price_amount, max_instances, state, created_at, updated_at) 
VALUES (3, 'FREE', 'Plan gratuito con 2 instancias', 0.00, 2, 'ACTIVE', NOW(), NOW());

-- Plan Standard
INSERT INTO plans (id, name, description, price_amount, max_instances, state, created_at, updated_at) 
VALUES (1, 'STANDARD', 'Plan estándar con 5 instancias', 1000.00, 5, 'ACTIVE', NOW(), NOW());

-- Plan Premium
INSERT INTO plans (id, name, description, price_amount, max_instances, state, created_at, updated_at) 
VALUES (2, 'PREMIUM', 'Plan premium con 10 instancias', 2000.00, 10, 'ACTIVE', NOW(), NOW());
```

### Variables de Entorno para VPS

Crea un archivo `.env` o configura en tu sistema:

```bash
# Base de datos principal
DB_URL=jdbc:mysql://localhost:3306/crudcloud_db?useSSL=false&serverTimezone=UTC
DB_USER=root
DB_PASSWORD=tu_password_seguro

# Mercado Pago (PRODUCCIÓN)
MP_ACCESS_TOKEN=tu_token_de_produccion
MP_PUBLIC_KEY=tu_public_key_de_produccion

# Email
MAIL_USERNAME=tu_email@gmail.com
MAIL_PASSWORD=tu_app_password

# Crypto
CRYPTO_KEY=UnaClaveMuySeguraYLargaDe32CaracteresOMas

# URLs (PRODUCCIÓN)
APP_BASE_URL=https://api.macchiato.crudzaso.com
APP_FRONT_BASE_URL=https://macchiato.crudzaso.com

# Discord (opcional)
DISCORD_WEBHOOK_URL=tu_webhook_url
DISCORD_NOTIFICATIONS_ENABLED=true
```

### Actualizar application.properties para producción

```properties
# Usar variables de entorno
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}

mercadopago.access.token=${MP_ACCESS_TOKEN}
mercadopago.public.key=${MP_PUBLIC_KEY}

spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}

app.crypto.key=${CRYPTO_KEY}

app.base-url=${APP_BASE_URL}
app.front-base-url=${APP_FRONT_BASE_URL}
app.payments.success-path=/success.html
app.payments.failure-path=/failure.html
app.payments.pending-path=/pending.html

discord.webhook.url=${DISCORD_WEBHOOK_URL:}
discord.notifications.enabled=${DISCORD_NOTIFICATIONS_ENABLED:false}
```

---

## 🏗️ DESPLIEGUE EN VPS

### Opción A: JAR directo

```bash
# 1. Empaquetar
./mvnw clean package -DskipTests

# 2. Copiar a VPS
scp target/crudcloud-backend-0.0.1-SNAPSHOT.jar user@tu-vps:/opt/crudcloud/

# 3. En la VPS, crear servicio systemd
sudo nano /etc/systemd/system/crudcloud.service
```

Contenido del servicio:
```ini
[Unit]
Description=CrudCloud Backend
After=network.target

[Service]
User=tu_usuario
WorkingDirectory=/opt/crudcloud
ExecStart=/usr/bin/java -jar /opt/crudcloud/crudcloud-backend-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143
TimeoutStopSec=10
Restart=on-failure
RestartSec=5
Environment="JAVA_OPTS=-Xmx512m -Xms256m"

# Cargar variables de entorno desde archivo
EnvironmentFile=/opt/crudcloud/.env

[Install]
WantedBy=multi-user.target
```

```bash
# 4. Iniciar servicio
sudo systemctl daemon-reload
sudo systemctl enable crudcloud
sudo systemctl start crudcloud
sudo systemctl status crudcloud

# 5. Ver logs
sudo journalctl -u crudcloud -f
```

### Opción B: Docker (si tienes Dockerfile)

```bash
# 1. Build
docker build -t crudcloud-backend:latest .

# 2. Run con .env
docker run -d \
  --name crudcloud-backend \
  --env-file .env \
  -p 8080:8080 \
  --restart unless-stopped \
  crudcloud-backend:latest

# 3. Ver logs
docker logs -f crudcloud-backend
```

### Configurar Nginx (reverse proxy)

```nginx
server {
    listen 80;
    server_name api.macchiato.crudzaso.com;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket support (si lo necesitas)
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

```bash
# Activar sitio y obtener SSL
sudo ln -s /etc/nginx/sites-available/crudcloud /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
sudo certbot --nginx -d api.macchiato.crudzaso.com
```

---

## 🧪 TESTING POST-DEPLOY

### 1. Health check
```bash
curl https://api.macchiato.crudzaso.com/actuator/health
```

### 2. Registro de usuario
```bash
curl -X POST https://api.macchiato.crudzaso.com/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!",
    "fullName": "Test User"
  }'
```

### 3. Login
```bash
curl -X POST https://api.macchiato.crudzaso.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!"
  }'
```

### 4. Crear instancia (con token del paso 3)
```bash
curl -X POST https://api.macchiato.crudzaso.com/api/instances \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TU_TOKEN_JWT" \
  -d '{
    "engineId": 1,
    "dbName": "mi_base_datos",
    "userDb": "mi_usuario"
  }'
```

### 5. Test de pago (desde el front)
Usa el HTML de botones de pago con las URLs de producción.

---

## 📊 MONITOREO

### Logs en tiempo real
```bash
# Systemd
sudo journalctl -u crudcloud -f

# Docker
docker logs -f crudcloud-backend

# Archivo directo (si configuras logging)
tail -f /var/log/crudcloud/app.log
```

### Métricas importantes
- ✅ CPU y memoria del proceso Java
- ✅ Conexiones activas a BD
- ✅ Tiempo de respuesta de endpoints
- ✅ Errores en logs (grep ERROR)
- ✅ Webhooks de MercadoPago recibidos

---

## 🆘 TROUBLESHOOTING

### El servicio no inicia
```bash
# Ver logs completos
sudo journalctl -u crudcloud --no-pager

# Verificar variables de entorno
sudo systemctl show crudcloud | grep Environment

# Test manual
cd /opt/crudcloud
java -jar crudcloud-backend-0.0.1-SNAPSHOT.jar
```

### Error de conexión a BD
```bash
# Verificar MySQL corriendo
sudo systemctl status mysql

# Probar conexión
mysql -h localhost -u root -p
```

### Mercado Pago no recibe webhooks
1. Verificar que la URL esté pública y accesible
2. Revisar logs de Nginx: `sudo tail -f /var/log/nginx/access.log`
3. Probar manualmente: `curl https://api.macchiato.crudzaso.com/api/payments/notifications`

### No llegan emails
1. Verificar credenciales SMTP en `.env`
2. Revisar logs de la app: buscar "Failed to send email"
3. Validar App Password de Gmail activo

---

## ✅ CHECKLIST FINAL

- [ ] Merge completado y pusheado
- [ ] JAR compilado sin errores
- [ ] Variables de entorno configuradas en VPS
- [ ] Base de datos creada y planes insertados
- [ ] Servicio systemd/Docker configurado
- [ ] Nginx configurado con SSL
- [ ] Health check funcionando
- [ ] Registro de usuario funciona
- [ ] Login genera JWT válido
- [ ] Creación de instancia funciona
- [ ] Pago de MercadoPago funciona (sandbox primero)
- [ ] Webhook de MP recibe notificaciones
- [ ] Emails se envían correctamente
- [ ] Logs monitoreándose

---

**¡Éxito en tu despliegue! 🚀**

Si tienes dudas o errores, revisa los logs y consulta la sección de troubleshooting.

