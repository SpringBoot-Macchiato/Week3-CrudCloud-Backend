# 🔐 Configuración de Google OAuth 2.0 para CrudCloud

## 📋 Índice
1. [Configuración en Google Cloud Console](#1-configuración-en-google-cloud-console)
2. [Configuración del Backend](#2-configuración-del-backend)
3. [Integración con el Frontend](#3-integración-con-el-frontend)
4. [Testing](#4-testing)

---

## 1️⃣ Configuración en Google Cloud Console

### Paso 1: Crear un Proyecto en Google Cloud

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuevo proyecto o selecciona uno existente
3. Nombre sugerido: `CrudCloud Auth`

### Paso 2: Habilitar Google+ API

1. En el menú lateral, ve a **APIs & Services** → **Library**
2. Busca "Google+ API" o "Google Identity"
3. Haz clic en **Enable**

### Paso 3: Crear Credenciales OAuth 2.0

1. Ve a **APIs & Services** → **Credentials**
2. Haz clic en **Create Credentials** → **OAuth client ID**
3. Si es la primera vez, configura la **OAuth consent screen**:
   - User Type: **External** (para desarrollo)
   - App name: `CrudCloud`
   - User support email: tu email
   - Developer contact: tu email
   - Scopes: Agrega `email`, `profile`, `openid`
   - Test users: Agrega los emails que usarás para probar

4. Crear OAuth 2.0 Client ID:
   - Application type: **Web application**
   - Name: `CrudCloud Web Client`
   - Authorized JavaScript origins:
     - `http://localhost:5173` (desarrollo)
     - `http://localhost:3000` (si usas React en puerto 3000)
     - Tu URL de producción cuando la tengas
   - Authorized redirect URIs:
     - `http://localhost:5173` (o tu puerto del frontend)
   - Haz clic en **Create**

5. **¡IMPORTANTE!** Copia y guarda:
   - **Client ID**: `1234567890-abcdefghijklmnop.apps.googleusercontent.com`
   - **Client Secret**: (no lo necesitas en el frontend, pero guárdalo)

---

## 2️⃣ Configuración del Backend

### Paso 1: Agregar el Client ID a application.properties

Crea o edita `crudcloud-backend/src/main/resources/application.properties`:

```properties
# Google OAuth2 Configuration
google.client-id=TU_CLIENT_ID_AQUI.apps.googleusercontent.com

# Ejemplo:
# google.client-id=1234567890-abcdefghijklmnopqrstuvwxyz123456.apps.googleusercontent.com
```

### Paso 2: Instalar Dependencias

Las dependencias ya están agregadas en `pom.xml`. Solo ejecuta:

```bash
cd crudcloud-backend
mvn clean install
```

### Paso 3: Migración de Base de Datos

Cuando levantes el backend por primera vez con los cambios, Hibernate actualizará automáticamente la tabla `users` agregando las columnas:
- `google_id`
- `provider`
- `picture`

**IMPORTANTE:** El campo `password` ahora es nullable para permitir usuarios que solo se autentican con Google.

---

## 3️⃣ Integración con el Frontend

### Paso 1: Instalar Google Identity Services

En tu proyecto frontend (React/Vue/etc):

```bash
npm install @react-oauth/google
# o si usas vanilla JS, incluye el script en tu HTML
```

### Paso 2: Configurar Google OAuth en el Frontend

#### Opción A: React con @react-oauth/google

```jsx
// App.jsx o main.jsx
import { GoogleOAuthProvider } from '@react-oauth/google';

const CLIENT_ID = "TU_CLIENT_ID_AQUI.apps.googleusercontent.com";

function App() {
  return (
    <GoogleOAuthProvider clientId={CLIENT_ID}>
      {/* Tu aplicación */}
    </GoogleOAuthProvider>
  );
}
```

```jsx
// LoginPage.jsx
import { GoogleLogin } from '@react-oauth/google';

function LoginPage() {
  const handleGoogleSuccess = async (credentialResponse) => {
    try {
      // Enviar el token al backend
      const response = await fetch('http://localhost:8080/api/auth/google/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          token: credentialResponse.credential
        })
      });

      const data = await response.json();

      // Guardar el JWT token
      localStorage.setItem('token', data.token);
      localStorage.setItem('email', data.email);
      localStorage.setItem('role', data.role);

      // Redirigir al dashboard
      navigate('/dashboard');

    } catch (error) {
      console.error('Error al autenticar con Google:', error);
    }
  };

  const handleGoogleError = () => {
    console.error('Error en Google Login');
  };

  return (
    <div>
      <h1>Login</h1>
      <GoogleLogin
        onSuccess={handleGoogleSuccess}
        onError={handleGoogleError}
        useOneTap
      />
    </div>
  );
}
```

#### Opción B: Vanilla JavaScript

```html
<!-- index.html -->
<script src="https://accounts.google.com/gsi/client" async defer></script>

<div id="g_id_onload"
     data-client_id="TU_CLIENT_ID_AQUI.apps.googleusercontent.com"
     data-callback="handleCredentialResponse">
</div>
<div class="g_id_signin" data-type="standard"></div>

<script>
async function handleCredentialResponse(response) {
  try {
    // Enviar el token al backend
    const res = await fetch('http://localhost:8080/api/auth/google/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        token: response.credential
      })
    });

    const data = await res.json();

    // Guardar el JWT token
    localStorage.setItem('token', data.token);
    localStorage.setItem('email', data.email);
    localStorage.setItem('role', data.role);

    // Redirigir
    window.location.href = '/dashboard.html';

  } catch (error) {
    console.error('Error:', error);
  }
}
</script>
```

### Paso 3: Usar el JWT Token en Requests

Una vez autenticado, usa el token en todas las peticiones protegidas:

```javascript
const token = localStorage.getItem('token');

fetch('http://localhost:8080/api/instances', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
})
```

---

## 4️⃣ Testing

### Test Manual

1. **Levantar el Backend:**
   ```bash
   cd crudcloud-backend
   mvn spring-boot:run
   ```

2. **Levantar el Frontend:**
   ```bash
   npm run dev
   # o el comando que uses
   ```

3. **Probar el Login con Google:**
   - Haz clic en el botón de Google
   - Selecciona tu cuenta de Google
   - El backend debe crear el usuario automáticamente
   - Debes recibir un JWT token

### Test con cURL (simular el frontend)

Primero obtén un token de Google desde el frontend, luego:

```bash
curl -X POST http://localhost:8080/api/auth/google/login \
  -H "Content-Type: application/json" \
  -d '{
    "token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjU5M..."
  }'
```

Respuesta esperada:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "tu-email@gmail.com",
  "role": "USER"
}
```

### Verificar en la Base de Datos

```sql
SELECT id, email, full_name, google_id, provider, picture, role
FROM users
WHERE provider = 'GOOGLE';
```

Deberías ver algo como:
```
id | email              | full_name    | google_id        | provider | picture                | role
---+--------------------+--------------+------------------+----------+------------------------+------
1  | user@gmail.com     | John Doe     | 123456789012345  | GOOGLE   | https://lh3.google... | USER
```

---

## 🔒 Seguridad

### Mejores Prácticas

1. **NUNCA** expongas tu Client Secret en el frontend
2. El Client ID SÍ puede estar en el frontend (es público)
3. Usa HTTPS en producción
4. Configura correctamente los **Authorized Origins** en Google Cloud
5. Valida siempre el token en el backend (ya implementado)

### Manejo de Errores

El backend maneja automáticamente:
- ✅ Tokens inválidos o expirados
- ✅ Creación automática de usuarios nuevos
- ✅ Vinculación de cuentas existentes (si un usuario se registró primero con email/password y luego usa Google)
- ✅ Actualización de información del usuario desde Google

---

## 🚀 Flujo Completo

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   Frontend  │         │   Google    │         │   Backend   │
└──────┬──────┘         └──────┬──────┘         └──────┬──────┘
       │                       │                       │
       │ 1. Click "Login"      │                       │
       ├──────────────────────>│                       │
       │                       │                       │
       │ 2. Google Auth        │                       │
       │<──────────────────────┤                       │
       │                       │                       │
       │ 3. ID Token           │                       │
       │<──────────────────────┤                       │
       │                       │                       │
       │ 4. POST /api/auth/google/login                │
       │       { token: "..." }                        │
       ├─────────────────────────────────────────────>│
       │                       │                       │
       │                       │  5. Verify Token      │
       │                       │<──────────────────────┤
       │                       │                       │
       │                       │  6. Token Valid       │
       │                       ├──────────────────────>│
       │                       │                       │
       │                       │  7. Find/Create User  │
       │                       │  8. Generate JWT      │
       │                       │                       │
       │ 9. { token, email, role }                     │
       │<─────────────────────────────────────────────┤
       │                       │                       │
       │ 10. Save JWT          │                       │
       │ 11. Redirect          │                       │
```

---

## 📚 Endpoints Disponibles

### POST `/api/auth/google/login`

**Request:**
```json
{
  "token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjU5M2Y0OWY3YTBhODM..."
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGdtYWlsLmNvbSIsInJvbGUiOiJVU0VSIiwiaWF0IjoxNzA5ODU...",
  "email": "user@gmail.com",
  "role": "USER"
}
```

**Response (400 Bad Request):**
```json
{
  "error": "Invalid Google token"
}
```

---

## ❓ Troubleshooting

| Problema | Solución |
|----------|----------|
| "Invalid Google token" | Verifica que el Client ID en application.properties sea correcto |
| "Origin not allowed" en Google | Agrega tu origen en Google Cloud Console → Authorized JavaScript origins |
| Usuario no se crea | Verifica logs del backend, puede ser problema de BD |
| Token expirado | Los tokens de Google expiran, el usuario debe volver a autenticarse |
| "Email is required" en BD | Asegúrate de que Hibernate haya actualizado la tabla (password ahora es nullable) |

---

## 🎯 Próximos Pasos

Una vez configurado:

1. ✅ El usuario puede autenticarse con Google
2. ✅ Se crea automáticamente en la BD
3. ✅ Recibe un JWT token
4. ✅ Puede usar todas las funcionalidades de la app

El JWT token funciona exactamente igual que el de login tradicional, así que no necesitas cambiar nada más en tu frontend.

---

## 📞 Soporte

Si tienes problemas:
1. Revisa los logs del backend
2. Verifica la configuración en Google Cloud Console
3. Asegúrate de que el Client ID esté correcto
4. Prueba con un usuario agregado en "Test users" en Google Cloud

¡Listo para integrar! 🚀
