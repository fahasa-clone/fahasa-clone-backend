package vn.clone.fahasa_backend.service.impl;

import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.clone.fahasa_backend.domain.Account;
import vn.clone.fahasa_backend.domain.RefreshToken;
import vn.clone.fahasa_backend.domain.request.CreateUpdateAccountDTO;
import vn.clone.fahasa_backend.domain.request.RegisterDTO;
import vn.clone.fahasa_backend.domain.request.ResetPasswordDTO;
import vn.clone.fahasa_backend.domain.response.AccountDTO;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.repository.AccountRepository;
import vn.clone.fahasa_backend.repository.RefreshTokenRepository;
import vn.clone.fahasa_backend.service.AccountService;
import vn.clone.fahasa_backend.service.MailService;
import vn.clone.fahasa_backend.util.RandomUtils;

@Service
@AllArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final MailService mailService;

    private final PasswordEncoder passwordEncoder;

    @Override
    public Account registerAccount(RegisterDTO accountDTO) {
        accountRepository.findByEmail(accountDTO.getEmail())
                         .ifPresent(account -> {
                             if (!removeNonActiveAccount(account)) {
                                 throw new BadRequestException("Email already used!");
                             }
                         });

        Account.AccountBuilder builder = Account.builder()
                                                .email(accountDTO.getEmail())
                                                .password(passwordEncoder.encode(accountDTO.getPassword()))
                                                .firstName(accountDTO.getFirstName())
                                                .lastName(accountDTO.getLastName())
                                                .phone(accountDTO.getPhone())
                                                .gender(accountDTO.getGender())
                                                .birthday(accountDTO.getBirthday())
                                                .isActivated(false)
                                                .activationKey(RandomUtils.generateActivateKey());
        if (accountDTO instanceof CreateUpdateAccountDTO createAccountDTO) {
            builder.isActivated(createAccountDTO.getIsActivated())
                   .activationKey(null);
        }
        return accountRepository.save(builder.build());
    }

    @Override
    public AccountDTO createAccount(CreateUpdateAccountDTO requestDTO) {
        Account account = registerAccount(requestDTO);
        return convertToDTO(account);
    }

    @Override
    public AccountDTO updateAccount(int id, CreateUpdateAccountDTO requestDTO) {
        Account account = getAccountById(id);

        // account.setEmail(requestDTO.getEmail());
        // account.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        account.setFirstName(requestDTO.getFirstName());
        account.setLastName(requestDTO.getLastName());
        account.setPhone(requestDTO.getPhone());
        account.setGender(requestDTO.getGender());
        account.setBirthday(requestDTO.getBirthday());
        account.setActivated(requestDTO.getIsActivated());

        Account savedAccount = accountRepository.save(account);
        return convertToDTO(savedAccount);
    }

    @Override
    public AccountDTO fetchAccountById(int id) {
        Account account = getAccountById(id);
        return convertToDTO(account);
    }

    @Override
    public Page<AccountDTO> fetchAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable)
                                .map(this::convertToDTO);
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
    @Transactional
    public void deleteRefreshToken(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
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
        return refreshTokenRepository.findByToken(refreshToken)
                                     .map(RefreshToken::getAccount)
                                     .orElseThrow(() -> new BadRequestException("Invalid refresh token!"));
    }

    @Override
    public void addRefreshToken(Account account, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                                                .token(token)
                                                .account(account)
                                                .build();
        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public Account getActivatedAccount(String email) {
        Account account = accountRepository.findByEmailIgnoreCase(email)
                                           .orElseThrow(() -> new BadRequestException("Email is not existed!"));
        if (!account.isActivated()) {
            throw new BadRequestException("Account is not activated!");
        }
        return account;
    }

    @Override
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        Account account = getActivatedAccount(resetPasswordDTO.getEmail());
        account.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        accountRepository.save(account);
    }

    @Override
    public Account getOrCreateUser(String email, OAuth2User oauth2User) {
        return accountRepository.findByEmailIgnoreCase(email)
                                .orElseGet(() -> createAccountFromOAuth2User(oauth2User));
    }

    private boolean removeNonActiveAccount(Account account) {
        if (!account.isActivated()) {
            accountRepository.delete(account);
            accountRepository.flush();
            return true;
        }
        return false;
    }

    private Account getAccountById(int id) {
        return accountRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + id));
    }

    private Account createAccountFromOAuth2User(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        // String name = oauth2User.getAttribute("name");

        String randomPassword = RandomUtils.generatePassword();

        Account account = Account.builder()
                                 .email(email)
                                 // .fullName(name)
                                 .password(passwordEncoder.encode(randomPassword))
                                 .rawPassword(randomPassword)
                                 .isActivated(true)
                                 .build();

        // Send password mail
        mailService.sendPasswordMail(account);

        return accountRepository.save(account);
    }

    // =========== Helper method to convert Account to AccountDTO ===========
    private AccountDTO convertToDTO(Account account) {
        return AccountDTO.builder()
                         .id(account.getId())
                         .email(account.getEmail())
                         .firstName(account.getFirstName())
                         .lastName(account.getLastName())
                         .phone(account.getPhone())
                         .gender(account.getGender())
                         .birthday(account.getBirthday())
                         .isActivated(account.isActivated())
                         .build();
    }
}