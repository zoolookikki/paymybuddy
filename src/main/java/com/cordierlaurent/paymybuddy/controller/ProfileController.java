package com.cordierlaurent.paymybuddy.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.cordierlaurent.paymybuddy.dto.UpdateProfileRequestDTO;
import com.cordierlaurent.paymybuddy.model.User;
import com.cordierlaurent.paymybuddy.service.UserService;
import com.cordierlaurent.paymybuddy.util.Result;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
        
        // recherche de l'utilisateur courant.
        User user = userService.getAuthenticatedUser(principal);
        // pour l'afficher dans le formulaire.
        model.addAttribute("updateProfileRequest", new UpdateProfileRequestDTO(user.getName(), user.getEmail(), ""));
        
        return "profile";
    }

    @PostMapping("/profile")
    // @ModelAttribute permet de lier les champs d’un formulaire HTML à un objet Java.
    // @Valid pour valider tout ce qui a été déclaré comme à contrôler dans le DTO.
    // BindingResult est une interface qui sert à capturer et gérer les erreurs de validation lorsqu’un formulaire est soumis => attention si on oublie => MethodArgumentNotValidException.
    // Principal représente l'utilisateur actuellement authentifié => email est dans getName().
    // Model permet de transmettre des données de la couche serveur (Java) vers la vue (Thymeleaf).
    // HttpServletRequest et HttpServletResponse pour la déconnexion.
    public String updateProfile(
            @ModelAttribute("updateProfileRequest") @Valid UpdateProfileRequestDTO updateProfileRequest, 
            BindingResult bindingResult, 
            Principal principal, 
            Model model, 
            HttpServletRequest request, 
            HttpServletResponse response) {
        log.debug("PostMapping/profile,updateProfileRequest="+updateProfileRequest);

        // pour afficher le formulaire avec les erreurs automatiquement.
        if (bindingResult.hasErrors()) {
          return "profile"; 
      }
        
        User user = userService.getAuthenticatedUser(principal);
        Result result = userService.update(user, new User(updateProfileRequest.getName(), updateProfileRequest.getEmail(), updateProfileRequest.getPassword()));
        log.info(user.getEmail()+"=>"+result.getMessage());

        if (result.isSuccess()) {
            if (!principal.getName().equals(updateProfileRequest.getEmail())) {
                log.debug("PostMapping/email change,"+principal.getName()+"/"+updateProfileRequest.getEmail());
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
        model.addAttribute("updateProfileRequest", new UpdateProfileRequestDTO(user.getName(), user.getEmail(), ""));

        return "profile";
    }
 
}



