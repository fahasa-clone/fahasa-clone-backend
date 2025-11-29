package vn.clone.fahasa_backend.service;

import java.security.SecureRandom;
import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import vn.clone.fahasa_backend.domain.Otp;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.repository.OtpRepository;

@Service
public class OtpService {

    public static final int MAX_ATTEMPTS = 5;

    // 5 minutes in seconds
    public static final long OTP_EXPIRY_SECONDS = 300;

    private final SecureRandom random = new SecureRandom();

    private final OtpRepository otpRepository;

    private final PasswordEncoder passwordEncoder;

    public OtpService(OtpRepository otpRepository, PasswordEncoder passwordEncoder) {
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Generates a 6-digit OTP (One-Time Password).
     * This implementation ensures leading zeros are not included.
     */
    // @Transactional
    public String generateOtp(int accountId) {
        // 1. Generate a number between 0 and 899.999
        int number = this.random.nextInt(900_000);

        // 2. Add 100.000 to get a number between 100.000 and 999.999
        number += 100_000;

        // 3. Convert to string
        String strNumber = String.valueOf(number);

        // 4. Delete the old OTP with the same accountId
        this.otpRepository.deleteByAccountId(accountId);

        // 5. Create a OTP entity and save it to database
        Otp otp = Otp.builder()
                     .accountId(accountId)
                     .otpHash(this.passwordEncoder.encode(strNumber))
                     .expiryTime(Instant.now()
                                        .plusSeconds(OTP_EXPIRY_SECONDS))
                     .build();
        this.otpRepository.save(otp);

        return strNumber;
    }

    public boolean verifyOtp(int accountId, String submittedOtp) {
        Otp otp = otpRepository.findByAccountId(accountId)
                               .orElseThrow(() -> new BadRequestException("Invalid account ID."));

        // 1. Check expiry
        if (otp.getExpiryTime()
               .isBefore(Instant.now())) {
            otpRepository.deleteById(otp.getId()); // Clean up expired code
            throw new BadRequestException("Expired OTP.");
        }

        // 2. Check the code
        if (passwordEncoder.matches(submittedOtp, otp.getOtpHash())) {
            // SUCCESS: Delete the code
            otpRepository.deleteById(otp.getId());
            return true;
        } else {
            // Check attempt limit
            if (otp.getAttempts() == MAX_ATTEMPTS - 1) {
                otpRepository.deleteById(otp.getId());
                throw new BadRequestException("You have no remaining attempts. Please request a new code.");
            }

            // FAILURE: Increment attempts and save
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepository.save(otp);
            throw new BadRequestException("Invalid OTP. " + (MAX_ATTEMPTS - otp.getAttempts()) + " attempts remaining.");
        }
    }
}
