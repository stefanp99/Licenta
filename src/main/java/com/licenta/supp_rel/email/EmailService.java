package com.licenta.supp_rel.email;

import com.licenta.supp_rel.user.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender emailSender;

    public SimpleMailMessage constructEmail(String subject, String body,
                                            User user) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(body);
        email.setTo(user.getEmailAddress());
        email.setSentDate(new Date());
        email.setFrom("stefanpopescu99@gmail.com");
        return email;
    }

    public MimeMessage constructEmail(String subject, String body, String htmlBody,
                                      User user) throws MessagingException {
        MimeMessage email = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(email, true, "UTF-8");
        helper.setSubject(subject);
        helper.setText(body, htmlBody);
        helper.setTo(user.getEmailAddress());
        helper.setSentDate(new Date());
        helper.setFrom("stefanpopescu99@gmail.com");
        return email;
    }


    public void sendMail(SimpleMailMessage simpleMailMessage) {
        emailSender.send(simpleMailMessage);
    }

    public void sendMail(MimeMessage mimeMessage) {
        emailSender.send(mimeMessage);
    }

}
