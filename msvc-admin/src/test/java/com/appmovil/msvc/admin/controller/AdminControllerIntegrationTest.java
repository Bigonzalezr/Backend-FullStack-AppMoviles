package com.appmovil.msvc.admin.controller;

import com.appmovil.msvc.admin.client.PedidoClientRest;
import com.appmovil.msvc.admin.client.ProductoClientRest;
import com.appmovil.msvc.admin.models.Pedido;
import com.appmovil.msvc.admin.models.Producto;
import com.appmovil.msvc.admin.models.entities.Admin;
import com.appmovil.msvc.admin.repositories.AdminRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminRepository adminRepository;

    @MockBean
    private ProductoClientRest productoClientRest;

    @MockBean
    private PedidoClientRest pedidoClientRest;

    @BeforeEach
    void setUp() {
        adminRepository.deleteAll();
    }

    // ========== Tests de Admin CRUD ==========

    @Test
    @DisplayName("GET /api/v1/admin - Debe obtener todos los administradores")
    void testFindAllAdmins() throws Exception {
        // Given
        Admin admin1 = Admin.builder()
                .username("admin1")
                .email("admin1@test.com")
                .password("password123")
                .nombre("Admin")
                .apellido("One")
                .activo(true)
                .rol("ADMIN")
                .build();

        Admin admin2 = Admin.builder()
                .username("admin2")
                .email("admin2@test.com")
                .password("password456")
                .nombre("Admin")
                .apellido("Two")
                .activo(true)
                .rol("MODERADOR")
                .build();

        adminRepository.saveAll(List.of(admin1, admin2));

        // When & Then
        mockMvc.perform(get("/api/v1/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is(oneOf("admin1", "admin2"))))
                .andExpect(jsonPath("$[1].username", is(oneOf("admin1", "admin2"))));
    }

    @Test
    @DisplayName("GET /api/v1/admin/{id} - Debe obtener administrador por ID")
    void testFindAdminById() throws Exception {
        // Given
        Admin admin = Admin.builder()
                .username("admin1")
                .email("admin1@test.com")
                .password("password123")
                .nombre("Admin")
                .apellido("Test")
                .telefono("123456789")
                .activo(true)
                .rol("ADMIN")
                .build();

        Admin savedAdmin = adminRepository.save(admin);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/{id}", savedAdmin.getIdAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAdmin").value(savedAdmin.getIdAdmin()))
                .andExpect(jsonPath("$.username").value("admin1"))
                .andExpect(jsonPath("$.email").value("admin1@test.com"))
                .andExpect(jsonPath("$.nombre").value("Admin"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/{id} - Debe retornar 404 cuando administrador no existe")
    void testFindAdminByIdNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/admin - Debe crear un nuevo administrador")
    void testCreateAdmin() throws Exception {
        // Given
        Admin newAdmin = Admin.builder()
                .username("newadmin")
                .email("newadmin@test.com")
                .password("password123")
                .nombre("New")
                .apellido("Admin")
                .telefono("987654321")
                .activo(true)
                .rol("ADMIN")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAdmin)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAdmin").exists())
                .andExpect(jsonPath("$.username").value("newadmin"))
                .andExpect(jsonPath("$.email").value("newadmin@test.com"))
                .andExpect(jsonPath("$.nombre").value("New"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));

        // Verify in database
        List<Admin> admins = adminRepository.findAll();
        assertThat(admins).hasSize(1);
        assertThat(admins.get(0).getUsername()).isEqualTo("newadmin");
    }

    @Test
    @DisplayName("POST /api/v1/admin - Debe validar campos requeridos")
    void testCreateAdminValidacion() throws Exception {
        // Given - Admin sin username (campo requerido)
        Admin invalidAdmin = Admin.builder()
                .email("test@test.com")
                .password("password123")
                .nombre("Test")
                .apellido("Admin")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAdmin)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/admin - Debe validar email formato")
    void testCreateAdminEmailInvalido() throws Exception {
        // Given
        Admin adminEmailInvalido = Admin.builder()
                .username("testadmin")
                .email("invalid-email")
                .password("password123")
                .nombre("Test")
                .apellido("Admin")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminEmailInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/admin - Debe rechazar username duplicado")
    void testCreateAdminUsernameDuplicado() throws Exception {
        // Given
        Admin admin1 = Admin.builder()
                .username("admindup")
                .email("admin1@test.com")
                .password("password123")
                .nombre("Admin")
                .apellido("One")
                .activo(true)
                .rol("ADMIN")
                .build();

        adminRepository.save(admin1);

        Admin admin2 = Admin.builder()
                .username("admindup") // Username duplicado
                .email("admin2@test.com")
                .password("password456")
                .nombre("Admin")
                .apellido("Two")
                .activo(true)
                .rol("ADMIN")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(admin2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/admin - Debe rechazar email duplicado")
    void testCreateAdminEmailDuplicado() throws Exception {
        // Given
        Admin admin1 = Admin.builder()
                .username("admin1")
                .email("duplicate@test.com")
                .password("password123")
                .nombre("Admin")
                .apellido("One")
                .activo(true)
                .rol("ADMIN")
                .build();

        adminRepository.save(admin1);

        Admin admin2 = Admin.builder()
                .username("admin2")
                .email("duplicate@test.com") // Email duplicado
                .password("password456")
                .nombre("Admin")
                .apellido("Two")
                .activo(true)
                .rol("ADMIN")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(admin2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/admin/{id} - Debe actualizar administrador")
    void testUpdateAdmin() throws Exception {
        // Given
        Admin admin = Admin.builder()
                .username("adminold")
                .email("adminold@test.com")
                .password("password123")
                .nombre("AdminOld")
                .apellido("Test")
                .activo(true)
                .rol("ADMIN")
                .build();

        Admin savedAdmin = adminRepository.save(admin);

        Admin adminActualizado = Admin.builder()
                .username("adminnew")
                .email("adminnew@test.com")
                .nombre("AdminNew")
                .apellido("Updated")
                .telefono("111222333")
                .activo(false)
                .rol("MODERADOR")
                .build();

        // When & Then
        mockMvc.perform(put("/api/v1/admin/{id}", savedAdmin.getIdAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminActualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAdmin").value(savedAdmin.getIdAdmin()))
                .andExpect(jsonPath("$.username").value("adminnew"))
                .andExpect(jsonPath("$.email").value("adminnew@test.com"))
                .andExpect(jsonPath("$.nombre").value("AdminNew"));

        // Verify in database
        Admin updatedAdmin = adminRepository.findById(savedAdmin.getIdAdmin()).orElseThrow();
        assertThat(updatedAdmin.getUsername()).isEqualTo("adminnew");
        assertThat(updatedAdmin.getNombre()).isEqualTo("AdminNew");
    }

    @Test
    @DisplayName("PUT /api/v1/admin/{id} - Debe retornar 404 al actualizar admin inexistente")
    void testUpdateAdminNotFound() throws Exception {
        // Given
        Admin adminActualizado = Admin.builder()
                .username("admin")
                .email("admin@test.com")
                .nombre("Admin")
                .apellido("Test")
                .build();

        // When & Then
        mockMvc.perform(put("/api/v1/admin/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminActualizado)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/{id} - Debe eliminar administrador")
    void testDeleteAdmin() throws Exception {
        // Given
        Admin admin = Admin.builder()
                .username("admintodelete")
                .email("delete@test.com")
                .password("password123")
                .nombre("Admin")
                .apellido("Delete")
                .activo(true)
                .rol("ADMIN")
                .build();

        Admin savedAdmin = adminRepository.save(admin);

        // When & Then
        mockMvc.perform(delete("/api/v1/admin/{id}", savedAdmin.getIdAdmin()))
                .andExpect(status().isNoContent());

        // Verify deletion
        assertThat(adminRepository.findById(savedAdmin.getIdAdmin())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/{id} - Debe retornar 404 al eliminar admin inexistente")
    void testDeleteAdminNotFound() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/admin/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    // ========== Tests de Gestión de Productos (Feign) ==========

    @Test
    @DisplayName("GET /api/v1/admin/productos - Debe obtener todos los productos")
    void testGetAllProductos() throws Exception {
        // Given
        Producto producto1 = new Producto();
        producto1.setIdProducto(1L);
        producto1.setNombre("Producto 1");
        producto1.setPrecio(100.00);

        Producto producto2 = new Producto();
        producto2.setIdProducto(2L);
        producto2.setNombre("Producto 2");
        producto2.setPrecio(200.00);

        when(productoClientRest.findAll()).thenReturn(Arrays.asList(producto1, producto2));

        // When & Then
        mockMvc.perform(get("/api/v1/admin/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre").value("Producto 1"))
                .andExpect(jsonPath("$[1].nombre").value("Producto 2"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/productos/{id} - Debe obtener producto por ID")
    void testGetProductoById() throws Exception {
        // Given
        Producto producto = new Producto();
        producto.setIdProducto(1L);
        producto.setNombre("Producto Test");
        producto.setPrecio(150.00);

        when(productoClientRest.findById(1L)).thenReturn(producto);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/productos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProducto").value(1))
                .andExpect(jsonPath("$.nombre").value("Producto Test"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/productos - Debe crear nuevo producto")
    void testCreateProducto() throws Exception {
        // Given
        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombre("Nuevo Producto");
        nuevoProducto.setDescripcion("Descripción del producto");
        nuevoProducto.setPrecio(99.99);
        nuevoProducto.setStock(50);
        nuevoProducto.setCategoria("Electrónica");

        Producto productoCreado = new Producto();
        productoCreado.setIdProducto(1L);
        productoCreado.setNombre("Nuevo Producto");
        productoCreado.setPrecio(99.99);

        when(productoClientRest.save(any(Producto.class))).thenReturn(productoCreado);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoProducto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProducto").value(1))
                .andExpect(jsonPath("$.nombre").value("Nuevo Producto"));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/productos/{id} - Debe actualizar producto")
    void testUpdateProducto() throws Exception {
        // Given
        Producto productoActualizado = new Producto();
        productoActualizado.setNombre("Producto Actualizado");
        productoActualizado.setPrecio(120.00);

        when(productoClientRest.update(eq(1L), any(Producto.class))).thenReturn(productoActualizado);

        // When & Then
        mockMvc.perform(put("/api/v1/admin/productos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoActualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Producto Actualizado"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/productos/{id} - Debe eliminar producto")
    void testDeleteProducto() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/admin/productos/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/admin/productos/categoria/{categoria} - Debe filtrar por categoría")
    void testGetProductosByCategoria() throws Exception {
        // Given
        Producto producto1 = new Producto();
        producto1.setIdProducto(1L);
        producto1.setNombre("Laptop");
        producto1.setCategoria("Electrónica");

        when(productoClientRest.findByCategoria("Electrónica")).thenReturn(Arrays.asList(producto1));

        // When & Then
        mockMvc.perform(get("/api/v1/admin/productos/categoria/{categoria}", "Electrónica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].categoria").value("Electrónica"));
    }

    // ========== Tests de Gestión de Pedidos (Feign) ==========

    @Test
    @DisplayName("GET /api/v1/admin/pedidos - Debe obtener todos los pedidos")
    void testGetAllPedidos() throws Exception {
        // Given
        Pedido pedido1 = new Pedido();
        pedido1.setIdPedido(1L);
        pedido1.setIdUsuario(1L);
        pedido1.setTotal(500.00);

        Pedido pedido2 = new Pedido();
        pedido2.setIdPedido(2L);
        pedido2.setIdUsuario(2L);
        pedido2.setTotal(300.00);

        when(pedidoClientRest.findAll()).thenReturn(Arrays.asList(pedido1, pedido2));

        // When & Then
        mockMvc.perform(get("/api/v1/admin/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/v1/admin/pedidos/{id} - Debe obtener pedido por ID")
    void testGetPedidoById() throws Exception {
        // Given
        Pedido pedido = new Pedido();
        pedido.setIdPedido(1L);
        pedido.setIdUsuario(1L);
        pedido.setTotal(500.00);

        when(pedidoClientRest.findById(1L)).thenReturn(pedido);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/pedidos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPedido").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/admin/pedidos/usuario/{idUsuario} - Debe obtener pedidos por usuario")
    void testGetPedidosByUsuario() throws Exception {
        // Given
        Pedido pedido1 = new Pedido();
        pedido1.setIdPedido(1L);
        pedido1.setIdUsuario(1L);

        when(pedidoClientRest.findByUsuarioId(1L)).thenReturn(Arrays.asList(pedido1));

        // When & Then
        mockMvc.perform(get("/api/v1/admin/pedidos/usuario/{idUsuario}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/v1/admin/pedidos/estado/{estado} - Debe filtrar por estado")
    void testGetPedidosByEstado() throws Exception {
        // Given
        Pedido pedido1 = new Pedido();
        pedido1.setIdPedido(1L);
        pedido1.setEstado("PENDIENTE");

        when(pedidoClientRest.findByEstado("PENDIENTE")).thenReturn(Arrays.asList(pedido1));

        // When & Then
        mockMvc.perform(get("/api/v1/admin/pedidos/estado/{estado}", "PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/pedidos/{id}/estado - Debe actualizar estado del pedido")
    void testUpdateEstadoPedido() throws Exception {
        // Given
        Map<String, String> estadoUpdate = new HashMap<>();
        estadoUpdate.put("estado", "ENVIADO");

        Pedido pedidoActualizado = new Pedido();
        pedidoActualizado.setIdPedido(1L);
        pedidoActualizado.setEstado("ENVIADO");

        when(pedidoClientRest.updateEstado(eq(1L), any())).thenReturn(pedidoActualizado);

        // When & Then
        mockMvc.perform(patch("/api/v1/admin/pedidos/{id}/estado", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estadoUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENVIADO"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/pedidos/{id} - Debe eliminar pedido")
    void testDeletePedido() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/admin/pedidos/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Flujo completo - Crear admin, gestionar producto y pedido")
    void testFlujoCompletoAdmin() throws Exception {
        // Step 1: Crear admin
        Admin newAdmin = Admin.builder()
                .username("admintest")
                .email("admintest@test.com")
                .password("password123")
                .nombre("Admin")
                .apellido("Test")
                .activo(true)
                .rol("ADMIN")
                .build();

        String adminResponse = mockMvc.perform(post("/api/v1/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAdmin)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAdmin").exists())
                .andReturn().getResponse().getContentAsString();

        Admin createdAdmin = objectMapper.readValue(adminResponse, Admin.class);

        // Step 2: Verificar admin creado
        mockMvc.perform(get("/api/v1/admin/{id}", createdAdmin.getIdAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admintest"));

        // Step 3: Mock producto
        Producto producto = new Producto();
        producto.setIdProducto(1L);
        producto.setNombre("Producto Admin");
        producto.setPrecio(100.00);

        when(productoClientRest.findAll()).thenReturn(Arrays.asList(producto));

        // Step 4: Consultar productos
        mockMvc.perform(get("/api/v1/admin/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // Step 5: Mock pedido
        Pedido pedido = new Pedido();
        pedido.setIdPedido(1L);
        pedido.setEstado("PENDIENTE");

        when(pedidoClientRest.findByEstado("PENDIENTE")).thenReturn(Arrays.asList(pedido));

        // Step 6: Consultar pedidos pendientes
        mockMvc.perform(get("/api/v1/admin/pedidos/estado/{estado}", "PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // Step 7: Actualizar admin
        Admin adminActualizado = Admin.builder()
                .username("adminupdated")
                .email("adminupdated@test.com")
                .nombre("AdminUpdated")
                .apellido("Test")
                .activo(false)
                .rol("MODERADOR")
                .build();

        mockMvc.perform(put("/api/v1/admin/{id}", createdAdmin.getIdAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminActualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("adminupdated"));

        // Verify final state
        Admin finalAdmin = adminRepository.findById(createdAdmin.getIdAdmin()).orElseThrow();
        assertThat(finalAdmin.getUsername()).isEqualTo("adminupdated");
        assertThat(finalAdmin.getRol()).isEqualTo("MODERADOR");
        assertThat(finalAdmin.getActivo()).isFalse();
    }
}
