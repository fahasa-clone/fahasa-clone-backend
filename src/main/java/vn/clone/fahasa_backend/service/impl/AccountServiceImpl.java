package vn.clone.fahasa_backend.service.impl;

import java.util.Optional;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import vn.clone.fahasa_backend.domain.Account;
import vn.clone.fahasa_backend.domain.DTO.RegisterDTO;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.repository.AccountRepository;
import vn.clone.fahasa_backend.service.AccountService;
import vn.clone.fahasa_backend.util.RandomUtil;

@Service
@AllArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
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
        Optional<Account> accountOptional = accountRepository.findByEmailAndActivationKey(email, key);
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            account.setActivated(true);
            account.setActivationKey(null);
            return Optional.of(accountRepository.save(account));
        }
        return Optional.empty();
    }

    @Override
    public void updateUserToken(String email, String token) {
        Optional<Account> accountOptional = accountRepository.findByEmailAndIsActivated(email, true);
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            account.setToken(token);
            accountRepository.save(account);
        }
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
    public Account getUserByRefreshToken(String refreshToken, String email) {
        Optional<Account> accountOptional = accountRepository.findByEmailAndToken(email, refreshToken);
        if (accountOptional.isEmpty()) {
            throw new BadRequestException("Invalid refresh token!");
        }
        return accountOptional.get();
    }
}