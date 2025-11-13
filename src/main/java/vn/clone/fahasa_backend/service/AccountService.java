package vn.clone.fahasa_backend.service;

import java.util.Optional;

import vn.clone.fahasa_backend.domain.Account;
import vn.clone.fahasa_backend.domain.DTO.RegisterDTO;

public interface AccountService {

    Account registerAccount(RegisterDTO user);

    Optional<Account> activateRegistration(String email, String key);

    void deleteRefreshToken(String refreshToken);

    Account getUserInfo(String email);

    Account getUserByRefreshToken(String refreshToken);

    void addRefreshToken(int account_id, String token);
}
