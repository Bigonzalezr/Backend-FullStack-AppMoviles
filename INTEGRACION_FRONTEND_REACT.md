# 🚀 Integración Frontend React con Backend Spring Boot

## 📦 Archivos a Crear/Modificar en el Frontend

### 1. Configuración de API Base (`src/config/api.js`)

```javascript
// src/config/api.js
import axios from 'axios';

// URLs de los microservicios
export const API_URLS = {
  USUARIOS: 'http://localhost:8008/api/usuarios',
  AUTH: 'http://localhost:8008/api/auth',
  PRODUCTOS: 'http://localhost:8002/api/productos',
  CARRITO: 'http://localhost:8003/api/carrito',
  PEDIDOS: 'http://localhost:8004/api/pedidos',
  PAGOS: 'http://localhost:8005/api/pagos',
  RESENAS: 'http://localhost:8006/api/resenas',
  LOGS: 'http://localhost:8007/api/logs',
  ADMIN: 'http://localhost:8001/api/admin',
};

// Crear instancia de Axios con configuración base
const apiClient = axios.create({
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 segundos
});

// Interceptor para añadir token JWT a todas las peticiones
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor para manejar errores de respuesta
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token inválido o expirado
      localStorage.removeItem('token');
      localStorage.removeItem('levelupgamer_usuario');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
```

---

### 2. Servicio de Autenticación Actualizado (`src/Utils/Auth.js`)

**Reemplazar el archivo completo:**

```javascript
// src/Utils/Auth.js
import apiClient, { API_URLS } from '../config/api';

const USUARIO_KEY = 'levelupgamer_usuario';
const TOKEN_KEY = 'token';

// Función para determinar si un email es de admin (fallback)
const esEmailAdmin = (email) => {
  const emailsAdmin = [
    'cely.gamer@levelup.com',
    'maca.gamer@levelup.com',
    'benja.gamer@levelup.com'
  ];
  return emailsAdmin.includes(email);
};

export const authService = {
  // Registrar nuevo usuario
  registrar: async (email, password, nombre) => {
    try {
      const response = await apiClient.post(`${API_URLS.AUTH}/register`, {
        username: email,
        email: email,
        password: password,
        nombre: nombre
      });

      // Guardar token y usuario
      if (response.data.token) {
        localStorage.setItem(TOKEN_KEY, response.data.token);
        const usuario = {
          id: response.data.id,
          email: response.data.email,
          nombre: response.data.nombre,
          role: response.data.role || 'user'
        };
        localStorage.setItem(USUARIO_KEY, JSON.stringify(usuario));
        return { exito: true, usuario };
      }

      return { exito: true, usuario: response.data };
    } catch (error) {
      console.error('Error en registro:', error);
      return {
        exito: false,
        error: error.response?.data?.message || 'Error al registrar usuario'
      };
    }
  },

  // Login
  login: async (email, password) => {
    try {
      const response = await apiClient.post(`${API_URLS.AUTH}/login`, {
        username: email,
        password: password
      });

      // Guardar token
      if (response.data.token) {
        localStorage.setItem(TOKEN_KEY, response.data.token);
      }

      // Guardar usuario
      const usuario = {
        id: response.data.usuario?.id || response.data.id,
        email: response.data.usuario?.email || email,
        nombre: response.data.usuario?.nombre || response.data.nombre,
        role: response.data.usuario?.role || response.data.role || 'user'
      };

      localStorage.setItem(USUARIO_KEY, JSON.stringify(usuario));

      return { exito: true, usuario, token: response.data.token };
    } catch (error) {
      console.error('Error en login:', error);
      return {
        exito: false,
        error: error.response?.data?.message || 'Credenciales inválidas'
      };
    }
  },

  // Cerrar sesión
  logout: () => {
    localStorage.removeItem(USUARIO_KEY);
    localStorage.removeItem(TOKEN_KEY);
  },

  // Obtener usuario actual
  obtenerUsuarioActual: () => {
    try {
      const usuarioStr = localStorage.getItem(USUARIO_KEY);
      if (!usuarioStr) return null;
      
      const usuario = JSON.parse(usuarioStr);
      // Asegurar que tenga role
      if (!usuario.role) {
        usuario.role = esEmailAdmin(usuario.email) ? 'admin' : 'user';
      }
      return usuario;
    } catch {
      return null;
    }
  },

  // Verificar autenticación
  estaAutenticado: () => {
    return !!localStorage.getItem(TOKEN_KEY);
  },

  // Obtener perfil actual del backend
  obtenerPerfil: async () => {
    try {
      const response = await apiClient.get(`${API_URLS.AUTH}/me`);
      const usuario = response.data;
      localStorage.setItem(USUARIO_KEY, JSON.stringify(usuario));
      return usuario;
    } catch (error) {
      console.error('Error al obtener perfil:', error);
      return null;
    }
  }
};
```

---

### 3. Servicio de Productos Actualizado (`src/Data/productos.js`)

**Reemplazar las funciones CRUD con llamadas al backend:**

```javascript
// src/Data/productos.js
import apiClient, { API_URLS } from '../config/api';

// Mantener las categorías
export const categorias = [
  { id: 'todos', nombre: 'Todos los Productos' },
  { id: 'consolas', nombre: 'Consolas' },
  { id: 'perifericos', nombre: 'Periféricos' },
  { id: 'realidad-virtual', nombre: 'Realidad Virtual' },
  { id: 'monitores', nombre: 'Monitores' },
  { id: 'accesorios', nombre: 'Accesorios' }
];

// Obtener todos los productos
export const obtenerProductos = async () => {
  try {
    const response = await apiClient.get(API_URLS.PRODUCTOS);
    return response.data;
  } catch (error) {
    console.error('Error al obtener productos:', error);
    return [];
  }
};

// Obtener producto por ID
export const obtenerProductoPorId = async (id) => {
  try {
    const response = await apiClient.get(`${API_URLS.PRODUCTOS}/${id}`);
    return response.data;
  } catch (error) {
    console.error('Error al obtener producto:', error);
    return null;
  }
};

// Agregar producto (solo admin)
export const agregarProducto = async (producto) => {
  try {
    const response = await apiClient.post(API_URLS.PRODUCTOS, producto);
    return response.data;
  } catch (error) {
    console.error('Error al agregar producto:', error);
    throw error;
  }
};

// Actualizar producto (solo admin)
export const actualizarProducto = async (id, producto) => {
  try {
    const response = await apiClient.put(`${API_URLS.PRODUCTOS}/${id}`, producto);
    return response.data;
  } catch (error) {
    console.error('Error al actualizar producto:', error);
    throw error;
  }
};

// Eliminar producto (solo admin)
export const eliminarProducto = async (id) => {
  try {
    await apiClient.delete(`${API_URLS.PRODUCTOS}/${id}`);
    return true;
  } catch (error) {
    console.error('Error al eliminar producto:', error);
    throw error;
  }
};

// Buscar productos
export const buscarProductos = async (query) => {
  try {
    const response = await apiClient.get(`${API_URLS.PRODUCTOS}/buscar`, {
      params: { q: query }
    });
    return response.data;
  } catch (error) {
    console.error('Error al buscar productos:', error);
    return [];
  }
};
```

---

### 4. Actualizar Componentes para Usar Async/Await

#### Ejemplo: `src/Paginas/Productos.jsx`

```javascript
// src/Paginas/Productos.jsx
import React, { useState, useEffect } from 'react';
import { obtenerProductos, categorias } from '../Data/productos';
import ProductoCard from '../Componentes/ProductoCard';

const Productos = () => {
  const [productos, setProductos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [categoriaSeleccionada, setCategoriaSeleccionada] = useState('todos');

  useEffect(() => {
    cargarProductos();
  }, []);

  const cargarProductos = async () => {
    try {
      setLoading(true);
      const data = await obtenerProductos();
      setProductos(data);
    } catch (err) {
      setError('Error al cargar productos');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const productosFiltrados = categoriaSeleccionada === 'todos'
    ? productos
    : productos.filter(p => p.categoria === categoriaSeleccionada);

  if (loading) {
    return (
      <div className="container py-5 text-center">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Cargando...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container py-5">
        <div className="alert alert-danger">{error}</div>
      </div>
    );
  }

  return (
    <div className="container py-4">
      <h2 className="mb-4">Productos</h2>
      
      {/* Filtro de categorías */}
      <div className="mb-4">
        {categorias.map(cat => (
          <button
            key={cat.id}
            className={`btn btn-sm me-2 ${categoriaSeleccionada === cat.id ? 'btn-primary' : 'btn-outline-primary'}`}
            onClick={() => setCategoriaSeleccionada(cat.id)}
          >
            {cat.nombre}
          </button>
        ))}
      </div>

      {/* Grid de productos */}
      <div className="row g-4">
        {productosFiltrados.map(producto => (
          <div key={producto.id} className="col-md-4">
            <ProductoCard producto={producto} />
          </div>
        ))}
      </div>

      {productosFiltrados.length === 0 && (
        <div className="text-center py-5">
          <p>No se encontraron productos</p>
        </div>
      )}
    </div>
  );
};

export default Productos;
```

#### Ejemplo: `src/Paginas/AdminProducts.jsx`

```javascript
// src/Paginas/AdminProducts.jsx
import React, { useEffect, useState } from 'react';
import { obtenerProductos, agregarProducto, actualizarProducto, eliminarProducto } from '../Data/productos';

const AdminProducts = () => {
  const [productos, setProductos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(null);

  useEffect(() => {
    cargarProductos();
  }, []);

  const cargarProductos = async () => {
    try {
      setLoading(true);
      const data = await obtenerProductos();
      setProductos(data);
    } catch (error) {
      console.error('Error al cargar productos:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async (producto) => {
    try {
      if (producto.id) {
        // Actualizar
        await actualizarProducto(producto.id, producto);
      } else {
        // Crear
        await agregarProducto(producto);
      }
      await cargarProductos();
      setEditing(null);
    } catch (error) {
      alert('Error al guardar producto');
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('¿Eliminar este producto?')) return;
    
    try {
      await eliminarProducto(id);
      await cargarProductos();
    } catch (error) {
      alert('Error al eliminar producto');
    }
  };

  if (loading) {
    return <div className="container py-5 text-center">Cargando...</div>;
  }

  return (
    <div className="container py-4">
      <h2>Administrar Productos</h2>
      
      {/* Formulario para agregar/editar */}
      <ProductoForm onSave={handleSave} productoInicial={editing} />
      
      {/* Lista de productos */}
      <div className="row g-3 mt-3">
        {productos.map(producto => (
          <div key={producto.id} className="col-md-4">
            <div className="card">
              <div className="card-body">
                <h5>{producto.nombre}</h5>
                <p>${producto.precio}</p>
                <button 
                  className="btn btn-sm btn-primary me-2"
                  onClick={() => setEditing(producto)}
                >
                  Editar
                </button>
                <button 
                  className="btn btn-sm btn-danger"
                  onClick={() => handleDelete(producto.id)}
                >
                  Eliminar
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default AdminProducts;
```

---

### 5. Actualizar Login (`src/Paginas/Login.jsx`)

```javascript
// src/Paginas/Login.jsx
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FaUser, FaLock, FaArrowLeft } from 'react-icons/fa';
import { authService } from '../Utils/Auth';

const Login = () => {
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [cargando, setCargando] = useState(false);
  const navigate = useNavigate();

  const manejarCambio = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setError('');
  };

  const manejarEnvio = async (e) => {
    e.preventDefault();
    setCargando(true);
    setError('');

    const resultado = await authService.login(formData.email, formData.password);
    
    if (resultado.exito) {
      // Redirigir según el rol
      if (resultado.usuario.role === 'admin') {
        window.location.href = '/admin';
      } else {
        navigate('/');
        window.location.reload();
      }
    } else {
      setError(resultado.error);
    }
    
    setCargando(false);
  };

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-md-6 col-lg-4">
          <div className="card shadow">
            <div className="card-body p-4">
              <div className="text-center mb-4">
                <Link to="/" className="btn btn-outline-secondary btn-sm mb-3">
                  <FaArrowLeft className="me-1" />
                  Volver al Inicio
                </Link>
                <h3 className="card-title">Iniciar Sesión</h3>
                <p className="text-muted">Accede a tu cuenta</p>
              </div>

              {error && (
                <div className="alert alert-danger" role="alert">
                  {error}
                </div>
              )}

              <form onSubmit={manejarEnvio}>
                <div className="mb-3">
                  <label className="form-label">Email</label>
                  <div className="input-group">
                    <span className="input-group-text"><FaUser /></span>
                    <input
                      type="email"
                      name="email"
                      className="form-control"
                      value={formData.email}
                      onChange={manejarCambio}
                      required
                      disabled={cargando}
                    />
                  </div>
                </div>

                <div className="mb-3">
                  <label className="form-label">Contraseña</label>
                  <div className="input-group">
                    <span className="input-group-text"><FaLock /></span>
                    <input
                      type="password"
                      name="password"
                      className="form-control"
                      value={formData.password}
                      onChange={manejarCambio}
                      required
                      disabled={cargando}
                    />
                  </div>
                </div>

                <button 
                  type="submit" 
                  className="btn btn-primary w-100"
                  disabled={cargando}
                >
                  {cargando ? 'Iniciando sesión...' : 'Ingresar'}
                </button>
              </form>

              <div className="text-center mt-3">
                <p className="text-muted">
                  ¿No tienes cuenta? <Link to="/registro">Regístrate</Link>
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
```

---

## 📝 Variables de Entorno

### Crear archivo `.env` en la raíz del frontend:

```env
# .env
VITE_API_BASE_URL=http://localhost:8008
VITE_API_PRODUCTOS_URL=http://localhost:8002
VITE_API_CARRITO_URL=http://localhost:8003
VITE_API_PEDIDOS_URL=http://localhost:8004
VITE_API_PAGOS_URL=http://localhost:8005
```

### Usar en `api.js`:

```javascript
export const API_URLS = {
  USUARIOS: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8008' + '/api/usuarios',
  AUTH: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8008' + '/api/auth',
  PRODUCTOS: import.meta.env.VITE_API_PRODUCTOS_URL || 'http://localhost:8002' + '/api/productos',
  // ...
};
```

---

## 🚀 Pasos de Implementación

### 1. En el Frontend React

```bash
cd C:\Users\PC01\OneDrive\Desktop\ReactApp-Fullstack-II-1

# Instalar axios si no está instalado
npm install axios

# Crear estructura de carpetas
mkdir src\config
```

### 2. Copiar los archivos creados arriba:
- `src/config/api.js`
- Actualizar `src/Utils/Auth.js`
- Actualizar `src/Data/productos.js`
- Actualizar componentes como `Productos.jsx`, `Login.jsx`, etc.

### 3. Iniciar el Backend

```powershell
cd C:\Users\PC01\OneDrive\Desktop\Backend-FullStack-AppMoviles

# Iniciar microservicio de usuarios
cd msvc-usuarios
.\mvnw.cmd spring-boot:run

# En otra terminal, iniciar productos
cd msvc-productos
.\mvnw.cmd spring-boot:run
```

### 4. Iniciar el Frontend

```bash
cd C:\Users\PC01\OneDrive\Desktop\ReactApp-Fullstack-II-1
npm run dev
```

### 5. Verificar la Conexión

1. Abrir `http://localhost:5173`
2. Intentar registrarse con un nuevo usuario
3. Iniciar sesión
4. Ver que los productos se cargan desde el backend

---

## 🔍 Verificar que el Backend Está Corriendo

```powershell
# Verificar usuarios
curl http://localhost:8008/api/usuarios

# Verificar productos
curl http://localhost:8002/api/productos

# Verificar login
curl -X POST http://localhost:8008/api/auth/login -H "Content-Type: application/json" -d '{"username":"test@test.com","password":"123456"}'
```

---

## 🐛 Troubleshooting

### Error de CORS

Si ves errores como:
```
Access to XMLHttpRequest at 'http://localhost:8002' from origin 'http://localhost:5173' has been blocked by CORS
```

**Solución:** Ya está configurado en tu backend. Verifica que el puerto 5173 esté en la lista de orígenes permitidos.

### Error 401 Unauthorized

- Verifica que el token JWT se esté enviando correctamente
- Verifica que el token no haya expirado (duración: 24 horas)
- Revisa el interceptor de axios

### Productos no se cargan

- Verifica que el backend de productos esté corriendo en el puerto 8002
- Revisa la consola del navegador para ver errores
- Verifica que la URL sea correcta

---

## ✅ Checklist de Migración

- [ ] Backend corriendo (puertos 8001-8008)
- [ ] Axios instalado en el frontend
- [ ] Archivo `src/config/api.js` creado
- [ ] `src/Utils/Auth.js` actualizado
- [ ] `src/Data/productos.js` actualizado
- [ ] Componentes actualizados para async/await
- [ ] Variables de entorno configuradas
- [ ] Login funciona con el backend
- [ ] Productos se cargan desde el backend
- [ ] CRUD de productos funciona para admin
- [ ] Token JWT se maneja correctamente

---

## 📊 Mapeo de Endpoints

### Frontend (localStorage) → Backend (REST API)

| Funcionalidad Frontend | Método Anterior | Backend Endpoint | Método HTTP |
|----------------------|----------------|------------------|-------------|
| Login | localStorage | `/api/auth/login` | POST |
| Registro | localStorage | `/api/auth/register` | POST |
| Obtener productos | localStorage | `/api/productos` | GET |
| Crear producto | localStorage | `/api/productos` | POST |
| Actualizar producto | localStorage | `/api/productos/{id}` | PUT |
| Eliminar producto | localStorage | `/api/productos/{id}` | DELETE |
| Carrito | localStorage | `/api/carrito` | GET/POST |
| Pedidos | localStorage | `/api/pedidos` | GET/POST |

---

## 🎯 Próximos Pasos

1. **Migrar servicio de Carrito** - Conectar con `msvc-carrito` (puerto 8003)
2. **Migrar servicio de Pedidos** - Conectar con `msvc-pedidos` (puerto 8004)
3. **Migrar servicio de Pagos** - Conectar con `msvc-pagos` (puerto 8005)
4. **Implementar Reseñas** - Conectar con `msvc-resenas` (puerto 8006)
5. **Añadir Logging** - Conectar con `msvc-logs` (puerto 8007)

