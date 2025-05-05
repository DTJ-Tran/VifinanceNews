package com.vifinancenews.common.utilities;

import java.security.SecureRandom;

public class OTPGenerator {
    private static final SecureRandom random = new SecureRandom();

    public static String generateOTP() {
        int otp = 100000 + random.nextInt(900000); // Generates a 6-digit OTP
        return String.valueOf(otp);
    }
}
