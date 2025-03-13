package com.cordierlaurent.paymybuddy.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


// Ne charge que la configuration Spring Security.
@WebMvcTest(SpringSecurityConfiguration.class)
public class SpringSecurityConfigurationTest {

    // because MockMvc is not automatically configured in a @WebMvcTest unit test.
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @MockitoBean // replaces deprecated @MockBean
    private CustomUserDetailsService customUserDetailsService;
    
    
    // Ces tests doivent s'exécuter sans nécessiter d'authentification préalable.
    // --------------------------------------------------------------------------
    @Test
    @DisplayName("The login path and registration path are accessible without authentication")
    void pathForLoginAndRegisterSuccessTest() throws Exception {
        // when then
        mockMvc.perform(get("/login")).andExpect(status().isOk());
        mockMvc.perform(get("/register")).andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("The static resource path is accessible without authentication")
    void pathForStaticRessourcesSuccessTest() throws Exception {
        // when then
        mockMvc.perform(get("/favicon.ico")).andExpect(status().isOk());
        mockMvc.perform(get("/images/logo.png")).andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("The test API is accessible without authentication")
    void pathForApiSuccessTest() throws Exception {
        // when then
        mockMvc.perform(get("/apitest/billing")).andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Unauthorized access from an unauthenticated user is redirected to the login path")
    void pathForUnauthorizedAccessRedirectToLoginTest() throws Exception {
        // when then
        // Vérifie que la redirection contient "/login" quel que soit la base de l'URL car andExpect(redirectedUrl("/login") n'est pas bon.
        mockMvc.perform(get("/user")).andExpect(redirectedUrlPattern("**/login"));  
        mockMvc.perform(get("/admin")).andExpect(redirectedUrlPattern("**/login"));
    }
    
    // Ces tests nécessitent une authentification.
    // -------------------------------------------
    @Test
    /*
    Avec @WithMockUser, Spring Security crée un utilisateur fictif qui est considéré comme identifié dans l'application. 
    */
    @WithMockUser(username = "user@test.com", roles = "USER")
    @DisplayName("A user with role USER can access its paths")
    void pathForUserAccessAllowedTest() throws Exception {
        // when then
        mockMvc.perform(get("/user")).andExpect(status().isOk()); 
        mockMvc.perform(get("/transfer")).andExpect(status().isOk());
        mockMvc.perform(get("/profile")).andExpect(status().isOk());
        mockMvc.perform(get("/connection")).andExpect(status().isOk());
    }
    
    @DisplayName("A user with role USER can't access to the others paths")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void pathForUserAccessNotAllowedTest() throws Exception {
        // when then
        mockMvc.perform(get("/admin")).andExpect(status().isForbidden());
    }

    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("A user with role ADMIN can access its paths")
    void pathForAdminAccessAllowedTest() throws Exception {
        // when then
        mockMvc.perform(get("/admin")).andExpect(status().isOk()); 
        mockMvc.perform(get("/transfer")).andExpect(status().isOk());
        mockMvc.perform(get("/connection")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("A user with role ADMIN can't access to the others paths")
    void pathForAdminNotAllowedAccessTest() throws Exception {
        // when then
        mockMvc.perform(get("/user")).andExpect(status().isForbidden());
        // attention ce test existe uniqement à titre expérimental => voir remarque dans SpringSecurityConfiguration / securityFilterChain
        mockMvc.perform(get("/profile")).andExpect(status().isForbidden());
    }
   
    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    @DisplayName("The logout is redirected to the login path")
    void pathForLogoutSuccesTest() throws Exception {
        // when then
        mockMvc.perform(post("/logout")
                // Spring Security active la protection CSRF (Cross-Site Request Forgery) par défaut pour les requêtes POST, PUT, DELETE (jeton unique pour chaque session)
                .with(csrf()))
                .andExpect(redirectedUrl("/login?logout"));
    }

    @Test
    @DisplayName("When real authentication works for a user with role USER, the user is redirected to the correct dashboard")
    void loginSuccessForUserRedirectToDashboardTest() throws Exception {
        // given.
        UserDetails user = User.withUsername("user@test.com")
                .password(passwordEncoder.encode("userpassword"))              
                .roles("USER")
                .build();
        when(customUserDetailsService.loadUserByUsername("user@test.com")).thenReturn(user);

        // when then
        mockMvc.perform(post("/login")
                .with(csrf())
                .param("username", "user@test.com")
                .param("password", "userpassword"))
        .andExpect(redirectedUrl("/user")); 
    }

    @Test
    @DisplayName("When real authentication works for a user with role ADMIN, the user is redirected to the correct dashboard")
    void loginSuccessForAdminRedirectToDashboardTest() throws Exception {
        // given.
        UserDetails user = User.withUsername("admin@test.com")
                .password(passwordEncoder.encode("adminpassword"))              
                .roles("ADMIN")
                .build();
        when(customUserDetailsService.loadUserByUsername("admin@test.com")).thenReturn(user);
          
        // when then
        mockMvc.perform(post("/login")
                .with(csrf())
                .param("username", "admin@test.com")
                .param("password", "adminpassword"))
        .andExpect(redirectedUrl("/admin")); 
    }
    
}
