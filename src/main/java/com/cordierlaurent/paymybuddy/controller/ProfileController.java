package com.cordierlaurent.paymybuddy.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.cordierlaurent.paymybuddy.model.User;
import com.cordierlaurent.paymybuddy.service.UserService;
import com.cordierlaurent.paymybuddy.util.Result;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

//@RestController uniquement si on veut renvoyer du JSON ou du texte brut, par exemple pour une API REST
//@Controller pour renvoyer une vue HTML en utilisant Thymeleaf (ou un autre moteur de template).
@Controller
@Log4j2
public class ProfileController {

    @Autowired
    private UserService userService;
    
    @GetMapping("/profile")
    // Principal représente l'utilisateur actuellement authentifié => email est dans getName().
    public String displayProfileForm(Principal principal, Model model) {
        log.debug("GetMapping/profile");
        
        // recherche de l'utilisateur pour qu'il s'affiche dans le formulaire Thymeleaf : model.addAttribute de user.
        model.addAttribute("user", userService.getAuthenticatedUser(principal));
        // on affiche la page de modification de profil.
        return "profile";
    }

    @PostMapping("/profile")
    // @ModelAttribute permet de lier les champs d’un formulaire HTML à un objet Java.
    // Principal représente l'utilisateur actuellement authentifié => email est dans getName().
    // Model permet de transmettre des données de la couche serveur (Java) vers la vue (Thymeleaf).
    public String updateProfile(@ModelAttribute User userToUpdate, Principal principal, Model model, HttpServletRequest request, HttpServletResponse response) {
        log.debug("PostMapping/profile");

        User user = userService.getAuthenticatedUser(principal);
        Result result = userService.update(user, userToUpdate);

        if (result.isSuccess()) {
            if (!principal.getName().equals(userToUpdate.getEmail())) {
                log.debug("PostMapping/email change,"+principal.getName()+"/"+userToUpdate.getEmail());
                // bug Spring Security ? ne fonctionne pas => à la place, j'ai trouvé cette solution.
    /*            
//                  model.addAttribute("successMessage", "Votre email a été modifié. Veuillez vous reconnecter.");
                return "redirect:/logout";
    */
                // Déconnexion propre en supprimant la session et les cookies
                new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
                return "redirect:/login?emailChanged=true";
            } else {
                model.addAttribute("successMessage", result.getMessage());
            }
        } else {
            model.addAttribute("errorMessage", result.getMessage());
        }

        // On affiche la même page avec les données misent à jour ou non et le message (ok ou nok).
        model.addAttribute("user", user);
        return "profile";
    }
 
}



