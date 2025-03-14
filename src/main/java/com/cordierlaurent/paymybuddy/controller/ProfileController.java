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

/**
 * Controller managing the display and updating of the user profile (name, email, password).
 */
//@RestController uniquement si on veut renvoyer du JSON ou du texte brut, par exemple pour une API REST
//@Controller pour renvoyer une vue HTML en utilisant Thymeleaf (ou un autre moteur de template).
@Controller
@Log4j2
public class ProfileController {
    
    @Autowired
    private UserService userService;

    /**
     * Displays the profile edit form.
     * <p>
     * This method is called when a user accesses the profile.
     * It loads the current information of the logged in user and pre-populates the form.
     * </p>
     *
     * @param principal Contains the authenticated user's information.
     * @param model The model for passing data to the Thymeleaf view.
     * @return The "profile" view containing the update form.
     */
    @GetMapping("/profile")
    public String displayProfileForm(Principal principal, Model model) {
        log.debug("GetMapping/profile");
        
        // recherche de l'utilisateur courant.
        User user = userService.getAuthenticatedUser(principal);
        // pour l'afficher dans le formulaire.
        model.addAttribute("updateProfileRequest", new UpdateProfileRequestDTO(user.getName(), user.getEmail(), ""));
        
        return "profile";
    }

    /**
     * Updates user profile after form submission.
     * <p>
     * This method is called when a user submits an edit.
     * It checks the validity of the data, applies the changes, and, if the email address changes, forces the user to log out and reconnect securely.
     * </p>
     *
     * @param updateProfileRequestDTO Contains new profile information.
     * @param bindingResult Result of form validation.
     * @param principal Contains the authenticated user's information.
     * @param model The model for passing data to the Thymeleaf view.
     * @param request HTTP request used for session management.
     * @param response HTTP response used for session management.
     * @return Updated "profile" view or redirection to login page if email has changed.
     */
    @PostMapping("/profile")
    // @ModelAttribute permet de lier les champs d’un formulaire HTML à un objet Java.
    // @Valid pour valider tout ce qui a été déclaré comme à contrôler dans le DTO.
    // BindingResult est une interface qui sert à capturer et gérer les erreurs de validation lorsqu’un formulaire est soumis => attention si on oublie => MethodArgumentNotValidException.
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



