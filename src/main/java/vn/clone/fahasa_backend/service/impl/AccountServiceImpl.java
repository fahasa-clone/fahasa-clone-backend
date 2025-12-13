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
import vn.clone.fahasa_backend.domain.Role;
import vn.clone.fahasa_backend.domain.request.*;
import vn.clone.fahasa_backend.domain.response.AccountDTO;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.repository.AccountRepository;
import vn.clone.fahasa_backend.repository.RefreshTokenRepository;
import vn.clone.fahasa_backend.repository.RoleRepository;
import vn.clone.fahasa_backend.security.AuthoritiesConstants;
import vn.clone.fahasa_backend.service.AccountService;
import vn.clone.fahasa_backend.service.MailService;
import vn.clone.fahasa_backend.util.RandomUtils;
import vn.clone.fahasa_backend.util.SecurityUtils;

@Service
@AllArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    private final RoleRepository roleRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final MailService mailService;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Account registerAccount(RegisterDTO accountDTO) {
        validateEmailNotInUse(accountDTO.getEmail());

        Account account = Account.builder()
                                 .email(accountDTO.getEmail())
                                 .password(passwordEncoder.encode(accountDTO.getPassword()))
                                 .firstName(accountDTO.getFirstName())
                                 .lastName(accountDTO.getLastName())
                                 .phone(accountDTO.getPhone())
                                 .gender(accountDTO.getGender())
                                 .birthday(accountDTO.getBirthday())
                                 .role(getRoleByName(AuthoritiesConstants.CLIENT))
                                 .isActivated(false)
                                 .activationKey(RandomUtils.generateActivateKey())
                                 .build();

        if (accountDTO instanceof CreateAccountDTO createAccountDTO) {
            account.setActivated(true);
            account.setActivationKey(null);
            account.setRole(getRoleById(createAccountDTO.getRoleId()));
        }

        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public AccountDTO createAccount(CreateAccountDTO requestDTO) {
        Account account = registerAccount(requestDTO);
        return convertToDTO(account);
    }

    @Override
    @Transactional
    public AccountDTO updateAccount(int id, UpdateMeDTO requestDTO) {
        Account account = getAccountById(id);

        if (requestDTO.getEmail() != null) {
            validateEmailNotInUse(requestDTO.getEmail());
            account.setEmail(requestDTO.getEmail());
        }

        if (requestDTO.getPhone() != null) {
            validatePhoneNotInUse(requestDTO.getPhone());
            account.setPhone(requestDTO.getPhone());
        }

        account.setFirstName(requestDTO.getFirstName());
        account.setLastName(requestDTO.getLastName());
        account.setGender(requestDTO.getGender());
        account.setBirthday(requestDTO.getBirthday());

        if (requestDTO instanceof UpdateAccountDTO updateAccountDTO) {
            if (updateAccountDTO.getPassword() != null) {
                account.setPassword(passwordEncoder.encode(updateAccountDTO.getPassword()));
            }
            account.setRole(getRoleById(updateAccountDTO.getRoleId()));
            account.setActivated(updateAccountDTO.getIsActivated());
        }

        Account savedAccount = accountRepository.save(account);
        return convertToDTO(savedAccount);
    }

    @Override
    @Transactional
    public AccountDTO updateMyAccount(UpdateMeDTO requestDTO) {
        String email = SecurityUtils.getCurrentUserLogin()
                                    .orElseThrow(() -> new BadRequestException("Current user email not found!"));
        Account account = getActivatedAccount(email);

        return updateAccount(account.getId(), requestDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDTO fetchAccountById(int id) {
        Account account = getAccountById(id);
        return convertToDTO(account);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountDTO> fetchAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable)
                                .map(this::convertToDTO);
    }

    @Override
    @Transactional
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
    @Transactional(readOnly = true)
    public Account getUserInfo(String email) {
        Optional<Account> accountOptional = accountRepository.findByEmail(email);
        if (accountOptional.isEmpty()) {
            throw new BadRequestException("Account with email does not exist!");
        }
        return accountOptional.get();
    }

    @Override
    @Transactional(readOnly = true)
    public Account getUserByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByToken(refreshToken)
                                     .map(RefreshToken::getAccount)
                                     .orElseThrow(() -> new BadRequestException("Invalid refresh token!"));
    }

    @Override
    @Transactional
    public void addRefreshToken(Account account, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                                                .token(token)
                                                .account(account)
                                                .build();
        refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public Account getActivatedAccount(String email) {
        Account account = accountRepository.findByEmailIgnoreCase(email)
                                           .orElseThrow(() -> new BadRequestException("Email is not existed!"));
        if (!account.isActivated()) {
            throw new BadRequestException("Account is not activated!");
        }
        return account;
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        Account account = getActivatedAccount(resetPasswordDTO.getEmail());
        account.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public Account getOrCreateUser(String email, OAuth2User oauth2User) {
        return accountRepository.findByEmailIgnoreCase(email)
                                .orElseGet(() -> createAccountFromOAuth2User(oauth2User));
    }

    private void validateEmailNotInUse(String email) {
        accountRepository.findByEmail(email)
                         .ifPresent(account -> {
                             if (!removeNonActiveAccount(account)) {
                                 throw new BadRequestException("Email already used!");
                             }
                         });
    }

    private void validatePhoneNotInUse(String phone) {
        accountRepository.findByPhone(phone)
                         .ifPresent(account -> {
                             if (!removeNonActiveAccount(account)) {
                                 throw new BadRequestException("Phone already used!");
                             }
                         });
    }

    private Role getRoleById(int id) {
        return roleRepository.findById(id)
                             .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));
    }

    private Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                             .orElseThrow(() -> new EntityNotFoundException("Role not found with name: " + name));
    }

    private boolean removeNonActiveAccount(Account account) {
        if (account.isActivated()) {
            return false;
        }
        accountRepository.delete(account);
        return true;
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
                         .roleName(account.getRole()
                                          .getName())
                         .isActivated(account.isActivated())
                         .build();
    }
}