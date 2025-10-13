package vn.clone.fahasa_backend.service;

import java.util.Optional;

import vn.clone.fahasa_backend.domain.Account;
import vn.clone.fahasa_backend.domain.DTO.RegisterDTO;

public interface AccountService {
    public Account registerAccount(RegisterDTO user);

    public Optional<Account> activateRegistration(String email, String key);

    public void updateUserToken(String email, String token);

    public Account getUserInfo(String email);

    public Account getUserByRefreshToken(String refreshToken, String email);
}
