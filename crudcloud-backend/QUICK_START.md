# 🚀 Guía Rápida - Desarrollo Local

## Setup inicial (una sola vez)

1. **Instala ngrok** (si no lo tienes):
   ```bash
   sudo snap install ngrok
   ngrok config add-authtoken TU_AUTHTOKEN
   ```

2. **Instala Live Server en VS Code** (extensión)

## Flujo de trabajo diario

### 1️⃣ Inicia los túneles ngrok

**Terminal 1 - Backend:**
```bash
ngrok http 8080
```
📝 Copia la URL: `https://abc-xxx.ngrok-free.dev`

**Terminal 2 - Frontend:**
```bash
ngrok http 5500
```
📝 Copia la URL: `https://xyz-yyy.ngrok-free.dev`

### 2️⃣ Actualiza application.properties

```properties
app.base-url=https://abc-xxx.ngrok-free.dev
app.front-base-url=https://xyz-yyy.ngrok-free.dev
```

### 3️⃣ Inicia el backend

**Terminal 3 (o IntelliJ):**
```bash
cd /home/error/Documentos/SPRINTBOOT/Week3-CrudCloud-Backend/crudcloud-backend
mvn spring-boot:run
```

### 4️⃣ Abre Live Server

1. Abre `src/main/resources/static/buttonTest.html` en VS Code
2. Click derecho → **Open with Live Server**
3. Se abrirá en `http://127.0.0.1:5500/buttonTest.html`

### 5️⃣ Obtén un JWT fresco

**Postman o curl:**
```bash
curl -X POST https://abc-xxx.ngrok-free.dev/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"adrian@gmail.com","password":"tu_password"}'
```

📋 Copia el `token` de la respuesta.

### 6️⃣ Actualiza el JWT en buttonTest.html

```javascript
const API = "https://abc-xxx.ngrok-free.dev";
const AUTH = "Bearer eyJhbGci..."; // Pega tu token aquí
```

Live Server recarga automáticamente. ✨

### 7️⃣ Prueba desde ngrok

Abre en el navegador:
```
https://xyz-yyy.ngrok-free.dev/buttonTest.html
```

¡Click en un plan y paga! 💳

---

## 🔄 Cuando el JWT expire

1. Genera nuevo JWT (paso 5)
2. Pégalo en `buttonTest.html` (línea 37)
3. Live Server recarga automáticamente
4. ¡Sigue probando! 🎉

---

## 📊 Monitoreo

- **Backend ngrok:** http://127.0.0.1:4040
- **Frontend ngrok:** http://127.0.0.1:4041
- **Logs backend:** Terminal 3

---

## ⚠️ Problemas comunes

| Problema | Solución |
|----------|----------|
| Error 401 | JWT expirado, genera uno nuevo |
| Error 400 "Plan no encontrado" | Verifica que existan planes con id=1 y 2 en BD |
| Modal no abre | Public Key incorrecta en buttonTest.html |
| Webhook no llega | Verifica que ngrok del backend esté corriendo |
| CORS error | Añade URL de ngrok a app.cors.allowed-origins |

---

## 🎯 URLs de referencia

- Tarjetas de prueba MP: https://www.mercadopago.com.co/developers/es/docs/checkout-pro/additional-content/test-cards
- Dashboard ngrok: https://dashboard.ngrok.com
- Panel MP: https://www.mercadopago.com/developers/panel

---

✅ **Tip Pro:** Deja los dos túneles ngrok corriendo todo el día. Solo reinicia el backend cuando cambies código Java.

