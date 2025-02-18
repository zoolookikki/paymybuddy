package com.cordierlaurent.paymybuddy.configuration;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// interface de Spring Security qui permet de personnaliser ce qui se passe après une connexion réussie.
@Component
public class AuthenticationHandler implements AuthenticationSuccessHandler {

   /*
   Méthode à implémenter appelée après une connexion réussie.
   HttpServletRequest request représente la requête HTTP en cours.
   HttpServletResponse response : permet d'envoyer une réponse au navigateur.
   Authentication authentication : contient les détails de l'utilisateur authentifié (nom, rôles, etc.)
   */
   @Override public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
      // redirection par défaut en cas de problème.
      String redirect = "/";

      // si l'utilisateur est un admin
      if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
         // on va au tableau de bord administrateur.
         redirect = "/admin";
      // si l'utilisateur est un user
      } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
         // on va au tableau de bord utilisateur.
         redirect = "/user";
      }

      response.sendRedirect(redirect);
   }
}
