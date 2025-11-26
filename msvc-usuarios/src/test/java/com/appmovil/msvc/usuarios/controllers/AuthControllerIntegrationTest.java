package com.appmovil.msvc.usuarios.controllers;

import com.appmovil.msvc.usuarios.dtos.AuthResponse;
import com.appmovil.msvc.usuarios.dtos.LoginRequest;
import com.appmovil.msvc.usuarios.dtos.UsuarioCreationDTO;
import com.appmovil.msvc.usuarios.dtos.UsuarioDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController - Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UsuarioCreationDTO nuevoUsuario;
    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        nuevoUsuario = new UsuarioCreationDTO();
        nuevoUsuario.setUsername("testintegration");
        nuevoUsuario.setEmail("testintegration@email.com");
        nuevoUsuario.setPassword("password123");
        nuevoUsuario.setNombre("Integration");
        nuevoUsuario.setApellido("Test");
        nuevoUsuario.setTelefono("123456789");
        nuevoUsuario.setDireccion("Test Address");
        nuevoUsuario.setRol("USER");
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Debe registrar nuevo usuario exitosamente")
    void testRegister_DebeCrearUsuarioExitosamente() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoUsuario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testintegration"))
                .andExpect(jsonPath("$.email").value("testintegration@email.com"))
                .andExpect(jsonPath("$.nombre").value("Integration"))
                .andExpect(jsonPath("$.apellido").value("Test"))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.rol").value("USER"))
                .andExpect(jsonPath("$.idUsuario").exists())
                .andExpect(jsonPath("$.fechaRegistro").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Debe fallar con username duplicado")
    void testRegister_ConUsernameDuplicado_DebeRetornarConflict() throws Exception {
        // Given - Primer registro
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoUsuario)))
                .andExpect(status().isCreated());

        // When & Then - Intento de duplicar
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoUsuario)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value(containsString("username")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Debe fallar con email duplicado")
    void testRegister_ConEmailDuplicado_DebeRetornarConflict() throws Exception {
        // Given - Primer registro
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoUsuario)))
                .andExpect(status().isCreated());

        // When - Cambiar username pero mantener email
        nuevoUsuario.setUsername("otrousername");

        // Then
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoUsuario)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value(containsString("email")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Debe validar campos obligatorios")
    void testRegister_ConCamposInvalidos_DebeRetornarBadRequest() throws Exception {
        // Given
        UsuarioCreationDTO usuarioInvalido = new UsuarioCreationDTO();
        usuarioInvalido.setUsername("ab"); // Muy corto
        usuarioInvalido.setEmail("emailinvalido"); // Email inválido
        usuarioInvalido.setPassword(""); // Vacío

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuarioInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Debe autenticar usuario y retornar JWT")
    void testLogin_ConCredencialesCorrectas_DebeRetornarJWT() throws Exception {
        // Given - Registrar usuario primero
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoUsuario)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testintegration");
        loginRequest.setPassword("password123");

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("testintegration"))
                .andExpect(jsonPath("$.email").value("testintegration@email.com"))
                .andExpect(jsonPath("$.rol").value("USER"))
                .andExpect(jsonPath("$.usuarioId").exists())
                .andReturn();

        // Verificar que el token no sea null
        String responseBody = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseBody, AuthResponse.class);
        assertThat(authResponse.getToken()).isNotNull();
        assertThat(authResponse.getToken().length()).isGreaterThan(20);
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Debe fallar con credenciales incorrectas")
    void testLogin_ConCredencialesIncorrectas_DebeRetornarUnauthorized() throws Exception {
        // Given - Registrar usuario
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoUsuario)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testintegration");
        loginRequest.setPassword("passwordIncorrecto");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Debe fallar con usuario inexistente")
    void testLogin_ConUsuarioInexistente_DebeRetornarUnauthorized() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("usuarioquenoe xiste");
        loginRequest.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/auth/me - Debe retornar usuario autenticado con JWT válido")
    void testGetCurrentUser_ConJWTValido_DebeRetornarUsuario() throws Exception {
        // Given - Registrar y autenticar usuario
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoUsuario)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testintegration");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseBody, AuthResponse.class);
        String token = authResponse.getToken();

        // When & Then
        mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testintegration"))
                .andExpect(jsonPath("$.email").value("testintegration@email.com"))
                .andExpect(jsonPath("$.nombre").value("Integration"))
                .andExpect(jsonPath("$.apellido").value("Test"))
                .andExpect(jsonPath("$.rol").value("USER"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me - Debe fallar sin JWT")
    void testGetCurrentUser_SinJWT_DebeRetornarUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/auth/me - Debe fallar con JWT inválido")
    void testGetCurrentUser_ConJWTInvalido_DebeRetornarUnauthorized() throws Exception {
        // Given
        String tokenInvalido = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalidtoken.invalidtoken";

        // When & Then
        mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + tokenInvalido))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Flujo completo: Registro → Login → Obtener perfil")
    void testFlujoCompletoAutenticacion() throws Exception {
        // 1. Registro
        MvcResult registroResult = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoUsuario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testintegration"))
                .andReturn();

        String registroBody = registroResult.getResponse().getContentAsString();
        UsuarioDTO usuarioRegistrado = objectMapper.readValue(registroBody, UsuarioDTO.class);
        assertThat(usuarioRegistrado.getIdUsuario()).isNotNull();

        // 2. Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testintegration");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.usuarioId").value(usuarioRegistrado.getIdUsuario()))
                .andReturn();

        String loginBody = loginResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(loginBody, AuthResponse.class);

        // 3. Obtener perfil con JWT
        mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + authResponse.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(usuarioRegistrado.getIdUsuario()))
                .andExpect(jsonPath("$.username").value("testintegration"))
                .andExpect(jsonPath("$.email").value("testintegration@email.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Debe validar campos de login")
    void testLogin_ConCamposVacios_DebeRetornarBadRequest() throws Exception {
        // Given
        LoginRequest loginInvalido = new LoginRequest();
        loginInvalido.setUsername("");
        loginInvalido.setPassword("");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Password debe ser encriptado")
    void testRegister_PasswordDebeSerEncriptado() throws Exception {
        // When
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoUsuario)))
                .andExpect(status().isCreated())
                .andReturn();

        // Then - Verificar que puede hacer login (password fue encriptado correctamente)
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testintegration");
        loginRequest.setPassword("password123"); // Password original

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}
