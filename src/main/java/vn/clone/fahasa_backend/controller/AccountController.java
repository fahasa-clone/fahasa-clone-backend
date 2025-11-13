package vn.clone.fahasa_backend.controller;

import java.util.Optional;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.clone.fahasa_backend.domain.Account;
import vn.clone.fahasa_backend.domain.DTO.ActivationUserDTO;
import vn.clone.fahasa_backend.domain.DTO.RegisterDTO;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.service.AccountService;
import vn.clone.fahasa_backend.service.EmailService;

@RestController
@RequestMapping("/accounts")
@AllArgsConstructor
public class AccountController {

    private final AccountService accountService;

    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<String> registerAccount(@RequestBody @Valid RegisterDTO user) {
        Account newUser = accountService.registerAccount(user);
        emailService.sendActivationEmail(new ActivationUserDTO(newUser.getEmail(),
                                                               newUser.getFirstName(),
                                                               newUser.getLastName(),
                                                               newUser.getActivationKey()
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body("Registered account successfully");
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateAccount(@RequestParam(value = "email") String email,
                                                  @RequestParam(value = "key") String key) {
        Optional<Account> accountOptional = accountService.activateRegistration(email, key);
        if (accountOptional.isEmpty()) {
            throw new BadRequestException("Email or activation key is invalid");
        }
        return ResponseEntity.ok("Activated account successfully");
    }
}
