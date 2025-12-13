package vn.clone.fahasa_backend.controller;

import java.util.Optional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.clone.fahasa_backend.annotation.AdminOnly;
import vn.clone.fahasa_backend.domain.Account;
import vn.clone.fahasa_backend.domain.request.*;
import vn.clone.fahasa_backend.domain.response.AccountDTO;
import vn.clone.fahasa_backend.domain.response.PageResponse;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.service.AccountService;
import vn.clone.fahasa_backend.service.MailService;
import vn.clone.fahasa_backend.service.OtpService;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    private final MailService mailService;

    private final OtpService otpService;

    @PostMapping("/register")
    public ResponseEntity<String> registerAccount(@RequestBody @Valid RegisterDTO user) {
        Account newAccount = accountService.registerAccount(user);
        mailService.sendActivationEmail(newAccount);
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

    /**
     * {@code POST /api/accounts/reset-password/init} : Send an email with 6-digit OTP to reset the password of the user.
     *
     * @param initResetPasswordDTO the email of the user.
     * @throws RuntimeException {@code 400 (Bad Request)} if the email is not existed or not activated.
     */
    @PostMapping(path = "/reset-password/init")
    public ResponseEntity<Void> requestOtpForPasswordReset(@RequestBody InitResetPasswordDTO initResetPasswordDTO) {
        Account account = accountService.getActivatedAccount(initResetPasswordDTO.getEmail());
        account.setOtpValue(otpService.generateOtp(account.getId()));
        mailService.sendOtpMail(account);
        return ResponseEntity.ok()
                             .build();
    }

    /**
     * {@code DELETE /api/accounts/reset-password/verify} : Verify 6-digit OTP the user entered.
     *
     * @param verifyOtpDto the email and the OTP.
     * @throws RuntimeException {@code 400 (Bad Request)} if the OTP is invalid or expired
     *                          or if the user exceeds attempt limit.
     */
    @DeleteMapping(path = "/reset-password/verify")
    public ResponseEntity<Void> verifyOtp(@RequestBody VerifyOtpDTO verifyOtpDto) {
        Account account = accountService.getActivatedAccount(verifyOtpDto.getEmail());
        otpService.verifyOtp(account.getId(), verifyOtpDto.getOtpValue());
        return ResponseEntity.ok()
                             .build();
    }

    /**
     * {@code PATCH /api/accounts/reset-password/finish} : Reset the password with new password.
     *
     * @param resetPasswordDTO the email and the new password.
     * @throws RuntimeException {@code 400 (Bad Request)} if the password could not be reset.
     */
    @PatchMapping(path = "/reset-password/finish")
    public ResponseEntity<Void> finishPasswordReset(@RequestBody ResetPasswordDTO resetPasswordDTO) {
        accountService.resetPassword(resetPasswordDTO);
        return ResponseEntity.ok()
                             .build();
    }

    @PostMapping
    @AdminOnly
    public ResponseEntity<AccountDTO> createAccount(@Valid @RequestBody CreateUpdateAccountDTO accountDTO) {
        AccountDTO responseDTO = accountService.createAccount(accountDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(responseDTO);
    }

    @PutMapping("/{id}")
    @AdminOnly
    public ResponseEntity<AccountDTO> updateAccount(@PathVariable int id,
                                                    @Valid @RequestBody CreateUpdateAccountDTO accountDTO) {
        AccountDTO responseDTO = accountService.updateAccount(id, accountDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{id}")
    @AdminOnly
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable int id) {
        return ResponseEntity.ok(accountService.fetchAccountById(id));
    }

    @GetMapping
    @AdminOnly
    public ResponseEntity<?> getAllAccounts(Pageable pageable) {
        Page<AccountDTO> accountDTOPage = accountService.fetchAllAccounts(pageable);
        return ResponseEntity.ok(PageResponse.builder()
                                             .pageNumber(pageable.getPageNumber() + 1)
                                             .pageSize(pageable.getPageSize())
                                             .totalPages(accountDTOPage.getTotalPages())
                                             .items(accountDTOPage.getContent())
                                             .build());
    }
}
