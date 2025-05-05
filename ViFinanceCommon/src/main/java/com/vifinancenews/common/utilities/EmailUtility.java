package com.vifinancenews.common.utilities;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

import io.github.cdimascio.dotenv.Dotenv;

public class EmailUtility {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static String SENDER_EMAIL;
    private static String SENDER_PASSWORD;

    // Load environment variables
    static {
        Dotenv dotenv = Dotenv.load();
        SENDER_EMAIL = dotenv.get("EMAIL_USERNAME");
        SENDER_PASSWORD = dotenv.get("EMAIL_PASSWORD");

        if (SENDER_EMAIL == null || SENDER_PASSWORD == null) {
            throw new RuntimeException("Missing email credentials in .env file.");
        }
    }

    public static void sendOTP(String recipientEmail, String otpCode) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject("Your OTP Code");
        message.setText("Your OTP code is: " + otpCode + "\nThis code is valid for 5 minutes.");

        Transport.send(message);
        System.out.println("OTP email sent to: " + recipientEmail);
    }
}
