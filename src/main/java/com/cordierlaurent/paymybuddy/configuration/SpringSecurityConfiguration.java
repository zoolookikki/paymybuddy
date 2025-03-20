package com.cordierlaurent.paymybuddy.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * Configuring application security with Spring Security.
 * <p>
 * This class defines authentication and authorization rules by specifying access to different routes and user management methods.
 * </p>
 *
 * <p>
 * Key features include:
 * <ul>
 *     <li>Definition of access authorizations</li>
 *     <li>Customizing authentication and redirection after login</li>
 *     <li>Managing disconnection and session invalidation</li>
 *     <li>Configuring Password Encryption</li>
 * </ul>
 * </p>
 */

// @Configuration indique que c'est une classe conteneur de beans géré par Spring (voir commentaire à propos de @Bean). 
@Configuration
//Active Spring Security et applique une configuration personnalisée.
@EnableWebSecurity 
public class SpringSecurityConfiguration {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    /*
    @Bean est automatiquement détecté et exécuté par Spring lors du démarrage de l’application ==>
    Dans le démarrage de Spring Security, Spring cherche une configuration personnalisée.
    Il regarde dans le contexte Spring s’il y a un @Bean de type SecurityFilterChain.
    S’il en trouve un, il l’applique.
    S’il n’en trouve pas, il applique une configuration de sécurité par défaut (tout est protégé).
   */
    @Bean
    // SecurityFilterChain => un ensemble de règles de sécurité : collection de filtres de sécurité.
    /*
     HttpSecurity => permet de définir :
         - Qui peut accéder à quelles routes (authorizeHttpRequests())
         - Comment s’authentifier (formLogin(), httpBasic(), etc.)
         - Comment se déconnecter (logout())
         - Gestion des sessions (sessionManagement())
    */
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                /*
                Permet de configurer les autorisations d'accès aux différentes routes.
                Définit qui peut accéder à quelles URLs, en fonction des rôles utilisateurs.
                */
                // Désactiver CSRF (Cross-Site Request Forgery) uniquement pour l'api de test sinon il est interdit d'y accéder pour la tester sans gérer un jeton unique pour chaque session (activée par défaut pour empêcher certaines attaques sur les formulaires HTML).
                .csrf(csrf -> csrf.ignoringRequestMatchers("/apitest/**")) 
                .authorizeHttpRequests(auth -> {
                    // Autoriser l'accès à la page de login et d'inscription sans authentification.
                    auth.requestMatchers("/login", "/register").permitAll();
                    // Autoriser les fichiers statiques : sinon le logo ne s'affichait pas sur la page de login.
                    auth.requestMatchers("/favicon.ico", "/images/**", "/css/**", "/js/**").permitAll();
                    // Autoriser l'accès aux api pour tout le monde : api de test uniquement pour le moment donc ok.
                    auth.requestMatchers("/apitest/**").permitAll();
                    /*
                    Seuls les utilisateurs ayant le rôle ADMIN peuvent accéder à /admin.
                    Un ADMIN peut accéder à /admin, mais pas à /user.
                    ** protège toute l'interface admin et ses sous-pages.
                    */
                    auth.requestMatchers("/admin/**").hasRole("ADMIN");
                    /*
                    Seuls les utilisateurs ayant le rôle USER peuvent accéder à /user.
                    Un USER peut accéder à /user, mais pas à /admin.
                    ** protège toute l'interface user et ses sous-pages.
                    */
                    auth.requestMatchers("/user/**").hasRole("USER");
                    // ajout pour l'exemple : j'ai interdit la modification de profile pour l'administrateur (il n'est pas affiché dans la navbar.html si ADMIN)
                    auth.requestMatchers("/profile").hasRole("USER");
                    /*
                    Toute autre requête nécessite une connexion, mais sans restriction de rôle spécifique.
                    Un utilisateur sans connexion sera redirigé vers la page de login.
                    */
                    auth.anyRequest().authenticated();
                })
                
                // Active l'authentification par formulaire avec les paramètres par défaut (formulaires par défaut).
//              .formLogin(Customizer.withDefaults()) 
//              .logout(Customizer.withDefaults())
                
                /*
                Personnalisation pour avoir 2 tableaux de bord différents pour admin et user.
                https://www.tutorialspoint.com/spring_security/spring_security_redirection.htm
                */
                // Pages de connexion/déconnexion personnalisées.
                .formLogin(form -> form.loginPage("/login") 
                        .failureUrl("/login?error=true")
                        // permet de définir un gestionnaire personnalisé qui sera exécuté après une connexion réussie afin de rediriger dynamiquement l'utilisateur selon son rôle.
                        .successHandler(authenticationSuccessHandler())
                        // Tout le monde peut y accéder
                        .permitAll()
                )       
                .logout(logout -> logout
                        // URL pour se déconnecter
                        .logoutUrl("/logout") 
                        // Redirige vers la page de login après la déconnexion
                        .logoutSuccessUrl("/login?logout")
                        // Invalide la session
                        .invalidateHttpSession(true)
                        // Supprime le cookie de session
                        .deleteCookies("JSESSIONID")
                        // Tout le monde peut y accéder                        
                        .permitAll() 
                )
                // permet de rediriger les erreurs 403 (acces denied) vers ErrorController car Spring Security intercepte l'erreur ==> ne va pas dans GlobalExceptionHandler.
                .exceptionHandling(exception -> exception.accessDeniedPage("/error/403"))
                
                // Construit et applique la configuration de sécurité.
                .build();
    }

    /*
    Cette méthode permet d’indiquer à Spring Security d’utiliser la classe CustomUserDetailsService pour authentifier des utilisateurs
    Cette méthode définit un AuthenticationManager personnalisé dans Spring Security
    Un AuthenticationManager est le composant principal qui gère l’authentification des utilisateurs en validant leur nom d’utilisateur et leur mot de passe.
    Spring utilisera ce AuthenticationManager pour l’authentification des utilisateurs.
        HttpSecurity http : Permet d’accéder aux objets de configuration de sécurité Spring
        BCryptPasswordEncoder bCryptPasswordEncoder : Utilisé pour vérifier les mots de passe hashés stockés en base de données.
    */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, BCryptPasswordEncoder bCryptPasswordEncoder) throws Exception {
        // Récupère un AuthenticationManagerBuilder à partir de l’objet HttpSecurity => Récupère l'objet pour construire l’authentification.
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        /*
        Définit la source des utilisateurs (UserDetailsService) et l’encodeur de mot de passe (BCrypt).
        Utilise CustomUserDetailsService pour charger les utilisateurs depuis la base de données.
            - userDetailsService(customUserDetailsService)
                customUserDetailsService est une classe qui implémente UserDetailsService.
                Cela signifie que l’authentification se fait en consultant la base de données.
            - passwordEncoder(bCryptPasswordEncoder)
                Vérifie que le mot de passe fourni par l’utilisateur correspond à celui hashé en base.
                Spring Security compare le hash du mot de passe entré avec celui stocké en base.
        */
        authenticationManagerBuilder.userDetailsService(customUserDetailsService).passwordEncoder(bCryptPasswordEncoder);
        /*
        Construit l’objet AuthenticationManager et le retourne.
        Ce AuthenticationManager sera utilisé par Spring Security pour authentifier les utilisateurs.
        */
        return authenticationManagerBuilder.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // permet de personnaliser ce qui se passe après une connexion réussie.
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
       return new AuthenticationHandler();
    }    
    
}
