# 🚀 Google OAuth - Guía Rápida de Implementación

## ✅ ARCHIVOS CREADOS

### Backend (Todos los archivos están listos en la rama `Feature/Google`)

**Modificados:**
- ✅ `pom.xml` - Dependencias de Google OAuth agregadas
- ✅ `User.java` - Campos `googleId`, `provider`, `picture` agregados
- ✅ `UserRepository.java` - Método `findByGoogleId()` agregado
- ✅ `AuthService.java` - Lógica para usuarios de Google
- ✅ `application.properties` - Client ID configurado

**Nuevos:**
- ✅ `GoogleAuthController.java` - Endpoint `/api/auth/google/login`
- ✅ `GoogleAuthService.java` - Lógica de verificación del token
- ✅ `GoogleAuthRequest.java` - DTO para request
- ✅ `GoogleUserInfo.java` - DTO con info de Google

---

## 🎯 CONFIGURACIÓN COMPLETADA

### Google Cloud Console
- ✅ Client ID: `298673093806-nnn4kq7860gtmqrh0pj775carg1nl6aj.apps.googleusercontent.com`
- ✅ Authorized JavaScript origins: `http://localhost:5173` (verifica que esté configurado)

### Backend
- ✅ `google.client-id` configurado en `application.properties`
- ✅ CORS configurado para `http://localhost:5173`

---

## 📝 PRÓXIMOS PASOS (EN ORDEN)

### 1️⃣ Instalar Dependencias de Maven

```bash
cd crudcloud-backend
mvn clean install
```

### 2️⃣ Levantar el Backend

```bash
mvn spring-boot:run
```

O desde IntelliJ:
- Abrir el proyecto
- Run `CrudcloudBackendApplication.java`

### 3️⃣ Verificar que el Backend Esté Corriendo

```bash
# Verificar que el server esté up
curl http://localhost:8080/api/auth/google/login

# Debería responder con 400 o 405 (significa que el endpoint existe)
```

---

## 🌐 INTEGRACIÓN CON EL FRONTEND

### Endpoint Disponible

**POST** `http://localhost:8080/api/auth/google/login`

**Request Body:**
```json
{
  "token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjU5M..."
}
```

**Response (Success):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "user@gmail.com",
  "role": "USER"
}
```

### Instalación en Frontend (React)

```bash
npm install @react-oauth/google
```

### Código para el Frontend

#### App.jsx (o main.jsx)
```jsx
import { GoogleOAuthProvider } from '@react-oauth/google';

const GOOGLE_CLIENT_ID = "298673093806-nnn4kq7860gtmqrh0pj775carg1nl6aj.apps.googleusercontent.com";

function App() {
  return (
    <GoogleOAuthProvider clientId={GOOGLE_CLIENT_ID}>
      {/* Tu app */}
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        {/* Otras rutas */}
      </Routes>
    </GoogleOAuthProvider>
  );
}
```

#### LoginPage.jsx
```jsx
import { GoogleLogin } from '@react-oauth/google';
import { useNavigate } from 'react-router-dom';

function LoginPage() {
  const navigate = useNavigate();

  const handleGoogleSuccess = async (credentialResponse) => {
    try {
      const response = await fetch('http://localhost:8080/api/auth/google/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          token: credentialResponse.credential
        })
      });

      if (!response.ok) {
        throw new Error('Authentication failed');
      }

      const data = await response.json();

      // Guardar el JWT token
      localStorage.setItem('token', data.token);
      localStorage.setItem('email', data.email);
      localStorage.setItem('role', data.role);

      console.log('Login exitoso:', data);

      // Redirigir al dashboard
      navigate('/dashboard');

    } catch (error) {
      console.error('Error al autenticar con Google:', error);
      alert('Error al iniciar sesión con Google');
    }
  };

  const handleGoogleError = () => {
    console.error('Google Login Error');
    alert('Error al iniciar sesión con Google');
  };

  return (
    <div className="login-container">
      <h2>Iniciar Sesión</h2>

      {/* Login tradicional si lo tienes */}
      <form>
        {/* ... */}
      </form>

      {/* Separador */}
      <div className="separator">
        <span>O</span>
      </div>

      {/* Botón de Google */}
      <GoogleLogin
        onSuccess={handleGoogleSuccess}
        onError={handleGoogleError}
        useOneTap
        theme="filled_blue"
        size="large"
        text="continue_with"
        shape="rectangular"
      />
    </div>
  );
}

export default LoginPage;
```

---

## 🧪 TESTING

### Test 1: Verificar Backend

1. Levantar backend: `mvn spring-boot:run`
2. Verificar logs que no haya errores
3. Ir a: http://localhost:8080/swagger-ui/index.html
4. Buscar el endpoint: `POST /api/auth/google/login`

### Test 2: Probar Login con Google

1. Levantar frontend: `npm run dev`
2. Ir a la página de login
3. Click en "Sign in with Google"
4. Seleccionar tu cuenta de Google
5. Deberías recibir el JWT token
6. Verificar en localStorage que se guardó el token

### Test 3: Verificar en Base de Datos

```bash
# Si estás usando H2 (por defecto en development)
# Ir a: http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:crudcloud
# User: sa
# Password: (dejar vacío)

# Ejecutar query:
SELECT * FROM users WHERE provider = 'GOOGLE';
```

---

## 🎯 FLUJO COMPLETO

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   Frontend  │         │   Google    │         │   Backend   │
└──────┬──────┘         └──────┬──────┘         └──────┬──────┘
       │                       │                       │
       │ 1. Click "Google"     │                       │
       ├──────────────────────>│                       │
       │                       │                       │
       │ 2. Login with Google  │                       │
       │<──────────────────────┤                       │
       │                       │                       │
       │ 3. ID Token           │                       │
       │<──────────────────────┤                       │
       │                       │                       │
       │ 4. POST /api/auth/google/login                │
       │       {token: "..."}                          │
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
       │ 9. {token, email, role}                       │
       │<─────────────────────────────────────────────┤
       │                       │                       │
       │ 10. Save JWT          │                       │
       │ 11. Redirect          │                       │
```

---

## 🔍 VERIFICACIÓN RÁPIDA

### ✅ Checklist de Backend

- [ ] Maven instaló las dependencias correctamente
- [ ] Backend se levanta sin errores
- [ ] El endpoint `/api/auth/google/login` aparece en Swagger
- [ ] No hay errores en los logs relacionados con Google

### ✅ Checklist de Frontend

- [ ] `@react-oauth/google` está instalado
- [ ] `GoogleOAuthProvider` envuelve la app con el Client ID correcto
- [ ] El botón de Google aparece en la página de login
- [ ] No hay errores en la consola del navegador

### ✅ Checklist de Google Cloud Console

- [ ] Proyecto creado
- [ ] OAuth consent screen configurado
- [ ] Client ID creado
- [ ] `http://localhost:5173` está en "Authorized JavaScript origins"
- [ ] Tu email está en "Test users" (si está en modo testing)

---

## ❓ TROUBLESHOOTING

| Problema | Solución |
|----------|----------|
| "mvn: command not found" | Instala Maven o usa IntelliJ para ejecutar el proyecto |
| "Invalid Google token" | Verifica que el Client ID en `application.properties` sea correcto |
| "Origin not allowed" en Google | Agrega tu URL en Google Cloud Console → Authorized JavaScript origins |
| Usuario no se crea en BD | Revisa los logs del backend para ver el error específico |
| Token expirado | Los tokens de Google expiran rápido, el usuario debe volver a autenticarse |
| CORS error | Verifica que `http://localhost:5173` esté en `app.cors.allowed-origins` |

---

## 📞 SIGUIENTE PASO

**AHORA MISMO DEBES:**

1. **Instalar Maven** (si no lo tienes):
   ```bash
   brew install maven  # macOS
   # o usa IntelliJ que ya tiene Maven integrado
   ```

2. **Levantar el backend**:
   ```bash
   cd crudcloud-backend
   mvn spring-boot:run
   ```

3. **Configurar el frontend** con el código que te proporcioné arriba

4. **Probar el login con Google** en tu navegador

---

## 🎉 ¿Todo Listo?

Si todo funciona correctamente:
- Deberías poder hacer login con Google
- El usuario se crea automáticamente en la BD
- Recibes un JWT token
- Puedes usar ese token para llamar a los otros endpoints (`/api/instances`, etc.)

**El JWT de Google funciona exactamente igual que el de login tradicional**, así que no necesitas cambiar nada más en tu frontend.

¡Listo para integrar! 🚀
