package vn.clone.fahasa_backend.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import vn.clone.fahasa_backend.config.FahasaProperties;
import vn.clone.fahasa_backend.domain.Account;

/**
 * Service for sending emails asynchronously.
 * <p>
 * We use the {@link Async} annotation to send emails asynchronously.
 */
@Service
@RequiredArgsConstructor
public class MailService {

    private static final String ACCOUNT = "account";

    private static final String BASE_URL = "baseUrl";

    private final FahasaProperties fahasaProperties;

    private final JavaMailSender javaMailSender;

    private final MessageSource messageSource;

    private final SpringTemplateEngine templateEngine;

    public void sendEmailSync(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content, isHtml);
            this.javaMailSender.send(mimeMessage);
        } catch (MailException | MessagingException e) {
            System.out.println("ERROR SEND EMAIL: " + e);
        }
    }

    private void sendEmailFromTemplateSync(Account account, String templateName, String titleKey) {
        // if (account.getEmail() == null) {
        //     LOG.debug("Email doesn't exist for user '{}'", user.getLogin());
        //     return;
        // }
        Locale locale = Locale.forLanguageTag("vn");
        Context context = new Context(locale);
        context.setVariable(ACCOUNT, account);
        context.setVariable(BASE_URL, fahasaProperties.getMail()
                                                      .getBaseUrl());
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, null, titleKey, locale);
        sendEmailSync(account.getEmail(), subject, content, false, true);
    }

    @Async
    public void sendActivationEmail(Account account) {
        sendEmailFromTemplateSync(account, "mail/activationEmail", "Kích hoạt tài khoản");
    }

    @Async
    public void sendOtpMail(Account account) {
        sendEmailFromTemplateSync(account,
                                  "mail/passwordResetEmail",
                                  String.format("%s là mã đặt lại mật khẩu của bạn", account.getOtpValue()));
    }

    @Async
    public void sendPasswordMail(Account account) {
        sendEmailFromTemplateSync(account,
                                  "mail/passwordEmail",
                                  "Mật khẩu tạo tự động của bạn");
    }
}
