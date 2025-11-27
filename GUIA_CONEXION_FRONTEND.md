# 🔗 Guía de Conexión: ReactApp-Fullstack-II con Backend Spring Boot

## 📐 Arquitectura

```
Frontend React (Puerto 5173)
    ↓ HTTP/HTTPS (REST API con JWT)
Backend Spring Boot (Puertos 8001-8008)
    ↓ JDBC/JPA
Base de Datos H2 (Embebida)
```

**⚠️ IMPORTANTE:** El frontend **NUNCA** se conecta directamente a la base de datos. Siempre consume APIs REST del backend.

## 📋 Repositorio Frontend Analizado
- **GitHub:** https://github.com/effimrv/ReactApp-Fullstack-II
- **Framework:** React + Vite
- **Puerto:** 5173
- **Estado Actual:** Usa localStorage para persistencia
- **Objetivo:** Migrar a API REST del backend

---

## 🎯 Microservicios Disponibles

| Microservicio | Puerto | Base URL |
|--------------|--------|----------|
| Usuarios     | 8008   | `http://localhost:8008/api/usuarios` |
| Productos    | 8002   | `http://localhost:8002/api/productos` |
| Carrito      | 8003   | `http://localhost:8003/api/carrito` |
| Pedidos      | 8004   | `http://localhost:8004/api/pedidos` |
| Pagos        | 8005   | `http://localhost:8005/api/pagos` |
| Reseñas      | 8006   | `http://localhost:8006/api/resenas` |
| Logs         | 8007   | `http://localhost:8007/api/logs` |
| Admin        | 8001   | `http://localhost:8001/api/admin` |

---

## 🚀 Ejemplos de Integración

### **Opción 1: React con Axios**

#### 1. Instalar Axios
```bash
npm install axios
```

#### 2. Crear servicio de API (`src/services/api.js`)
```javascript
import axios from 'axios';

// Configuración base
const API_BASE_URL = 'http://localhost:8008';

// Crear instancia de Axios
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para añadir token JWT
api.interceptors.request.use(
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

export default api;
```

#### 3. Servicio de Autenticación (`src/services/authService.js`)
```javascript
import api from './api';

export const authService = {
  // Login
  login: async (username, password) => {
    try {
      const response = await api.post('/api/auth/login', {
        username,
        password,
      });
      
      // Guardar token
      if (response.data.token) {
        localStorage.setItem('token', response.data.token);
        localStorage.setItem('user', JSON.stringify(response.data.usuario));
      }
      
      return response.data;
    } catch (error) {
      throw error.response?.data || 'Error al iniciar sesión';
    }
  },

  // Registro
  register: async (userData) => {
    try {
      const response = await api.post('/api/auth/register', userData);
      return response.data;
    } catch (error) {
      throw error.response?.data || 'Error al registrar usuario';
    }
  },

  // Obtener usuario actual
  getCurrentUser: async () => {
    try {
      const response = await api.get('/api/auth/me');
      return response.data;
    } catch (error) {
      throw error.response?.data || 'Error al obtener usuario';
    }
  },

  // Logout
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },
};
```

#### 4. Servicio de Productos (`src/services/productService.js`)
```javascript
import axios from 'axios';

const API_URL = 'http://localhost:8002/api/productos';

// Configurar Axios con token
const getAuthHeader = () => {
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
};

export const productService = {
  // Obtener todos los productos
  getAllProducts: async () => {
    try {
      const response = await axios.get(API_URL, {
        headers: getAuthHeader(),
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || 'Error al obtener productos';
    }
  },

  // Obtener producto por ID
  getProductById: async (id) => {
    try {
      const response = await axios.get(`${API_URL}/${id}`, {
        headers: getAuthHeader(),
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || 'Error al obtener producto';
    }
  },

  // Crear producto
  createProduct: async (productData) => {
    try {
      const response = await axios.post(API_URL, productData, {
        headers: {
          ...getAuthHeader(),
          'Content-Type': 'application/json',
        },
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || 'Error al crear producto';
    }
  },

  // Actualizar producto
  updateProduct: async (id, productData) => {
    try {
      const response = await axios.put(`${API_URL}/${id}`, productData, {
        headers: {
          ...getAuthHeader(),
          'Content-Type': 'application/json',
        },
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || 'Error al actualizar producto';
    }
  },

  // Eliminar producto
  deleteProduct: async (id) => {
    try {
      await axios.delete(`${API_URL}/${id}`, {
        headers: getAuthHeader(),
      });
    } catch (error) {
      throw error.response?.data || 'Error al eliminar producto';
    }
  },
};
```

#### 5. Componente de Login (`src/components/Login.jsx`)
```jsx
import React, { useState } from 'react';
import { authService } from '../services/authService';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      const data = await authService.login(username, password);
      console.log('Login exitoso:', data);
      
      // Redirigir al dashboard
      window.location.href = '/dashboard';
    } catch (err) {
      setError(err.message || 'Error al iniciar sesión');
    }
  };

  return (
    <div className="login-container">
      <h2>Iniciar Sesión</h2>
      <form onSubmit={handleSubmit}>
        <div>
          <label>Usuario:</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
        <div>
          <label>Contraseña:</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        {error && <div className="error">{error}</div>}
        <button type="submit">Ingresar</button>
      </form>
    </div>
  );
};

export default Login;
```

#### 6. Componente de Productos (`src/components/Products.jsx`)
```jsx
import React, { useState, useEffect } from 'react';
import { productService } from '../services/productService';

const Products = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const data = await productService.getAllProducts();
      setProducts(data);
    } catch (err) {
      setError(err.message || 'Error al cargar productos');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Cargando productos...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div className="products-container">
      <h2>Productos</h2>
      <div className="products-grid">
        {products.map((product) => (
          <div key={product.id} className="product-card">
            <h3>{product.nombre}</h3>
            <p>{product.descripcion}</p>
            <p className="price">${product.precio}</p>
            <button onClick={() => addToCart(product.id)}>
              Agregar al Carrito
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Products;
```

---

### **Opción 2: React con Fetch API**

```javascript
// src/services/apiService.js
const API_BASE_URL = 'http://localhost:8008';

const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  return {
    'Content-Type': 'application/json',
    ...(token && { Authorization: `Bearer ${token}` }),
  };
};

export const apiService = {
  // Login
  login: async (username, password) => {
    const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    });

    if (!response.ok) {
      throw new Error('Error al iniciar sesión');
    }

    const data = await response.json();
    localStorage.setItem('token', data.token);
    return data;
  },

  // Obtener productos
  getProducts: async () => {
    const response = await fetch('http://localhost:8002/api/productos', {
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      throw new Error('Error al obtener productos');
    }

    return response.json();
  },

  // Crear pedido
  createOrder: async (orderData) => {
    const response = await fetch('http://localhost:8004/api/pedidos', {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(orderData),
    });

    if (!response.ok) {
      throw new Error('Error al crear pedido');
    }

    return response.json();
  },
};
```

---

### **Opción 3: Angular con HttpClient**

#### 1. Servicio de API (`src/app/services/api.service.ts`)
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'http://localhost:8008';

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` })
    });
  }

  login(username: string, password: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/auth/login`, {
      username,
      password
    });
  }

  getProducts(): Observable<any[]> {
    return this.http.get<any[]>('http://localhost:8002/api/productos', {
      headers: this.getAuthHeaders()
    });
  }

  createProduct(product: any): Observable<any> {
    return this.http.post('http://localhost:8002/api/productos', product, {
      headers: this.getAuthHeaders()
    });
  }
}
```

---

### **Opción 4: Vue.js con Axios**

#### 1. Servicio de API (`src/services/api.js`)
```javascript
import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8008',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para token
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default {
  // Autenticación
  login(credentials) {
    return apiClient.post('/api/auth/login', credentials);
  },
  
  // Productos
  getProducts() {
    return axios.get('http://localhost:8002/api/productos', {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('token')}`,
      },
    });
  },
  
  // Pedidos
  createOrder(orderData) {
    return axios.post('http://localhost:8004/api/pedidos', orderData, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('token')}`,
      },
    });
  },
};
```

#### 2. Componente Vue (`src/components/ProductList.vue`)
```vue
<template>
  <div class="product-list">
    <h2>Productos</h2>
    <div v-if="loading">Cargando...</div>
    <div v-else-if="error">{{ error }}</div>
    <div v-else class="products-grid">
      <div v-for="product in products" :key="product.id" class="product-card">
        <h3>{{ product.nombre }}</h3>
        <p>{{ product.descripcion }}</p>
        <p class="price">${{ product.precio }}</p>
        <button @click="addToCart(product.id)">Agregar</button>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';

export default {
  name: 'ProductList',
  data() {
    return {
      products: [],
      loading: false,
      error: null,
    };
  },
  mounted() {
    this.fetchProducts();
  },
  methods: {
    async fetchProducts() {
      this.loading = true;
      try {
        const response = await api.getProducts();
        this.products = response.data;
      } catch (err) {
        this.error = 'Error al cargar productos';
      } finally {
        this.loading = false;
      }
    },
    addToCart(productId) {
      // Implementar lógica del carrito
      console.log('Agregando producto:', productId);
    },
  },
};
</script>
```

---

## 🔒 Seguridad y Autenticación JWT

### Flujo de Autenticación

1. **Login:** El usuario envía credenciales al backend
2. **Token JWT:** El backend devuelve un token JWT
3. **Almacenar:** El frontend guarda el token en `localStorage`
4. **Requests:** Cada petición incluye el token en el header `Authorization: Bearer {token}`

### Ejemplo de Manejo de Token

```javascript
// Guardar token después del login
const handleLogin = async (username, password) => {
  const response = await authService.login(username, password);
  localStorage.setItem('token', response.token);
  localStorage.setItem('user', JSON.stringify(response.usuario));
};

// Verificar si el usuario está autenticado
const isAuthenticated = () => {
  return !!localStorage.getItem('token');
};

// Cerrar sesión
const handleLogout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  window.location.href = '/login';
};
```

---

## 🛠️ Pasos para Iniciar

### 1. Iniciar Backend
```bash
# En la raíz del proyecto Backend
mvn spring-boot:run

# O iniciar cada microservicio individualmente
cd msvc-usuarios
./mvnw spring-boot:run

cd msvc-productos
./mvnw spring-boot:run
```

### 2. Verificar que el backend esté corriendo
```bash
# Verificar usuarios
curl http://localhost:8008/api/usuarios

# Verificar productos
curl http://localhost:8002/api/productos
```

### 3. Configurar Frontend
```bash
# Crear proyecto React
npx create-react-app mi-frontend
cd mi-frontend

# Instalar Axios
npm install axios

# Copiar los servicios creados arriba
# Iniciar frontend
npm start
```

---

## 🌐 Variables de Entorno (Recomendado)

### React (`.env`)
```env
REACT_APP_API_USUARIOS=http://localhost:8008
REACT_APP_API_PRODUCTOS=http://localhost:8002
REACT_APP_API_PEDIDOS=http://localhost:8004
REACT_APP_API_PAGOS=http://localhost:8005
```

### Uso en código
```javascript
const API_USUARIOS = process.env.REACT_APP_API_USUARIOS;
const API_PRODUCTOS = process.env.REACT_APP_API_PRODUCTOS;
```

---

## 📊 Endpoints Principales

### Usuarios (Puerto 8008)
- `POST /api/auth/login` - Iniciar sesión
- `POST /api/auth/register` - Registrar usuario
- `GET /api/auth/me` - Obtener usuario actual
- `GET /api/usuarios` - Listar usuarios
- `GET /api/usuarios/{id}` - Obtener usuario por ID
- `PUT /api/usuarios/{id}` - Actualizar usuario
- `DELETE /api/usuarios/{id}` - Eliminar usuario

### Productos (Puerto 8002)
- `GET /api/productos` - Listar productos
- `GET /api/productos/{id}` - Obtener producto por ID
- `POST /api/productos` - Crear producto
- `PUT /api/productos/{id}` - Actualizar producto
- `DELETE /api/productos/{id}` - Eliminar producto

---

## 🐛 Troubleshooting

### Error de CORS
Si ves errores de CORS, verifica que el backend tenga configurado:
```java
configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
```

### Error 401 (Unauthorized)
- Verifica que el token JWT esté incluido en el header
- Verifica que el token no haya expirado

### Error de Conexión
- Verifica que el backend esté corriendo en el puerto correcto
- Verifica que la URL en el frontend sea correcta

---

## 📚 Recursos Adicionales

- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Axios Docs](https://axios-http.com/)
- [React Docs](https://react.dev/)
- [JWT.io](https://jwt.io/)

---

## ✅ Checklist

- [ ] Backend corriendo en puertos correctos
- [ ] CORS configurado en el backend
- [ ] Frontend instalado con dependencias (axios/fetch)
- [ ] Servicios de API creados
- [ ] Manejo de JWT implementado
- [ ] Interceptores configurados
- [ ] Componentes conectados a servicios
- [ ] Variables de entorno configuradas
- [ ] Pruebas de conexión exitosas

