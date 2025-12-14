package vn.clone.fahasa_backend.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.core.user.OAuth2User;

import vn.clone.fahasa_backend.domain.Account;
import vn.clone.fahasa_backend.domain.request.*;
import vn.clone.fahasa_backend.domain.response.AccountDTO;

public interface AccountService {

    Account registerAccount(RegisterDTO user);

    Optional<Account> activateRegistration(String email, String key);

    void deleteRefreshToken(String refreshToken);

    Account getUserInfo(String email);

    Account getUserByRefreshToken(String refreshToken);

    void addRefreshToken(Account account, String token);

    Account getAccountBySecurityContext();

    void resetPassword(ResetPasswordDTO resetPasswordDTO);

    Account getOrCreateUser(String email, OAuth2User oauth2User);

    AccountDTO createAccount(CreateAccountDTO requestDTO);

    AccountDTO updateAccount(int id, UpdateMeDTO requestDTO);

    AccountDTO updateMyAccount(UpdateMeDTO requestDTO);

    AccountDTO fetchAccountById(int id);

    Page<AccountDTO> fetchAllAccounts(Pageable pageable);

    void updatePassword(UpdatePasswordDTO passwordDTO);
}
