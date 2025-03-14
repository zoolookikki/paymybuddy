package com.cordierlaurent.paymybuddy.configuration;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

/**
 * Spring Security authentication event listener.
 * <p>
 * This class listens for successful, failed, and logout authentication events to log user actions.
 * </p>
 */
@Component
@Log4j2
public class AuthenticationEventListener {

    @EventListener
    // Capture puis journalise un événement de succès d'authentification.
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        log.info("Connexion réussie pour : {}", username);
    }

    @EventListener
    // Capture puis journalise un événement d'échec d'authentification.
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
     // Récupère le username saisi
        String username = (String) event.getAuthentication().getPrincipal(); 
        log.info("Connexion échouée pour : {}", username);
    }
    
    @EventListener
    // Capture puis journalise un événement de déconnexion.
    public void onLogoutSuccess(LogoutSuccessEvent event) {
        String username = event.getAuthentication().getName();
        log.info("Déconnexion de : {}", username);
    }
}
