package vn.clone.fahasa_backend.service.impl;

import java.util.Optional;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import vn.clone.fahasa_backend.domain.Account;
import vn.clone.fahasa_backend.domain.DTO.RegisterDTO;
import vn.clone.fahasa_backend.domain.RefreshToken;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.repository.AccountRepository;
import vn.clone.fahasa_backend.repository.RefreshTokenRepository;
import vn.clone.fahasa_backend.service.AccountService;
import vn.clone.fahasa_backend.util.RandomUtil;

@Service
@AllArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public Account registerAccount(RegisterDTO user) {
        accountRepository.findByEmail(user.getEmail())
                         .ifPresent(account -> {
                             if (!removeNonActiveAccount(account)) {
                                 throw new BadRequestException("Email already used!");
                             }
                         });

        Account account = new Account();
        account.setEmail(user.getEmail());
        account.setPassword(passwordEncoder.encode(user.getPassword()));
        account.setFirstName(user.getFirstName());
        account.setLastName(user.getLastName());
        account.setPhone(user.getPhone());
        account.setGender(user.getGender());
        account.setBirthday(user.getBirthday());
        account.setActivated(false);
        account.setActivationKey(RandomUtil.generateActivateKey());
        account.setToken(null);
        account.setOauth2(false);
        return accountRepository.save(account);
    }

    public boolean removeNonActiveAccount(Account account) {
        if (!account.isActivated()) {
            accountRepository.delete(account);
            accountRepository.flush();
            return true;
        }
        return false;
    }

    @Override
    public Optional<Account> activateRegistration(String email, String key) {
        return accountRepository.findByEmailAndActivationKey(email, key)
                                .map(account -> {
                                    account.setActivated(true);
                                    account.setActivationKey(null);
                                    return accountRepository.save(account);
                                });
    }

    @Override
    public void deleteRefreshToken(String refreshToken) {
        refreshTokenRepository.deleteById(refreshToken);
    }

    @Override
    public Account getUserInfo(String email) {
        Optional<Account> accountOptional = accountRepository.findByEmail(email);
        if (accountOptional.isEmpty()) {
            throw new BadRequestException("Account with email does not exist!");
        }
        return accountOptional.get();
    }

    @Override
    public Account getUserByRefreshToken(String refreshToken) {
        RefreshToken refreshTokenObj = refreshTokenRepository.findByToken(refreshToken)
                                                             .orElseThrow(() -> new BadRequestException("Invalid refresh token!"));
        return refreshTokenObj.getAccount();
    }

    @Override
    public void addRefreshToken(int account_id, String token) {
        accountRepository.findById(account_id)
                         .ifPresent(account -> {
                             RefreshToken refreshToken = RefreshToken.builder()
                                                                     .token(token)
                                                                     .account(account)
                                                                     .build();
                             refreshTokenRepository.save(refreshToken);
                         });
    }
}