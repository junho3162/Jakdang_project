package com.jakdang.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 지정된 이메일 주소로 인증 코드를 발송하는 메소드입니다.
     * @param toEmail 수신자 이메일 주소
     * @param subject 이메일 제목
     * @param code 인증 코드
     */
    public void sendVerificationCode(String toEmail, String subject, String code) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(fromEmail, "작당모의");
            helper.setTo(toEmail);
            helper.setSubject("[작당모의] " + subject);

            // --- 이 부분이 추가되었습니다! ---
            // (핵심!) 이메일의 중요도를 '가장 높음'으로 설정합니다.
            // 이 설정은 이메일 헤더에 X-Priority: 1 과 같은 정보를 추가하여,
            // 수신 서버가 이 메일을 스팸으로 분류할 확률을 낮춰줍니다.
            helper.setPriority(1);

            String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>"
                    + "<h1 style='color: #3b82f6;'>작당모의 인증 코드 안내</h1>"
                    + "<p>안녕하세요!</p>"
                    + "<p>요청하신 인증 코드는 다음과 같습니다. 아래 코드를 복사하여 인증번호 입력란에 입력해주세요.</p>"
                    + "<div style='background-color:#f0f9ff; padding: 20px; text-align:center; border-radius: 5px; margin: 20px 0;'>"
                    + "<h2 style='color:#2563eb; font-size:28px; letter-spacing: 5px; margin:0;'>" + code + "</h2>"
                    + "</div>"
                    + "<p>본인이 요청하지 않으셨다면 이 메일을 무시하셔도 됩니다.</p>"
                    + "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>"
                    + "<p style='font-size: 12px; color: #888;'>이 메일은 발신 전용입니다.</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }
}

