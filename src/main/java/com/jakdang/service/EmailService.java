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
            // 멀티파트 모드 + UTF-8
            MimeMessageHelper helper =
                    new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

            helper.setFrom(fromEmail, "작당모의");     // fromEmail은 SMTP 로그인 계정과 동일 도메인
            helper.setTo(toEmail);
            helper.setSubject("[작당모의] " + subject);
            helper.setSentDate(new java.util.Date());

            // 텍스트 대체본 (plain)
            String plain = """
                안녕하세요, 작당모의입니다.

                아래 인증 코드를 입력해 주세요.

                인증 코드: %s

                본인이 요청하지 않으셨다면 이 메일은 무시하셔도 됩니다.
                """.formatted(code);

            // HTML 본문
            String html = """
            <div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>
              <h1 style='color: #3b82f6;'>작당모의 인증 코드 안내</h1>
              <p>안녕하세요!</p>
              <p>요청하신 인증 코드는 다음과 같습니다. 아래 코드를 복사하여 인증번호 입력란에 입력해주세요.</p>
              <div style='background-color:#f0f9ff; padding: 20px; text-align:center; border-radius: 5px; margin: 20px 0;'>
                <h2 style='color:#2563eb; font-size:28px; letter-spacing: 5px; margin:0;'>%s</h2>
              </div>
              <p>본인이 요청하지 않으셨다면 이 메일을 무시하셔도 됩니다.</p>
              <hr style='border-top: 1px solid #eee; margin: 20px 0;'>
              <p style='font-size: 12px; color: #888;'>이 메일은 발신 전용입니다.</p>
            </div>
            """.formatted(code);

            // plain + html 동시 설정 (중요!)
            helper.setText(plain, html);

            // 권장 헤더(스팸엔 직접 영향은 적지만 품질/회신/루프 방지에 도움)
            mimeMessage.addHeader("Auto-Submitted", "auto-generated");
            mimeMessage.addHeader("X-Auto-Response-Suppress", "All");
            // 필요 시 구독해지 헤더(트랜잭셔널 메일에도 무방)
            // mimeMessage.addHeader("List-Unsubscribe", "<mailto:no-reply@yourdomain>");

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }

}

