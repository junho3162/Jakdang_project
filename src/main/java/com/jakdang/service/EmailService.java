package com.jakdang.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // [수정 핵심] Brevo 아이디(9d04...) 대신, 수신자가 보게 될 '진짜 이메일 주소'를 직접 적습니다.
    private final String fromEmail = "jakdangmoeui.certification@gmail.com";

    /**
     * 지정된 이메일 주소로 인증 코드를 발송하는 메소드입니다.
     */
    public void sendVerificationCode(String toEmail, String subject, String code) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            // 멀티파트 모드(이미지/첨부파일 가능) + UTF-8 인코딩 설정
            // (보내주신 코드의 MULTIPART_MODE_MIXED_RELATED 옵션도 좋지만,
            //  단순 텍스트+HTML 구조에서는 기본 true 옵션이 더 안전하고 호환성이 좋습니다.)
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // [중요] 보내는 사람 이름 설정 ("작당모의" <이메일>)
            helper.setFrom(fromEmail, "작당모의");
            helper.setTo(toEmail);
            helper.setSubject("[작당모의] " + subject);
            helper.setSentDate(new java.util.Date());

            // 1. HTML 본문 (디자인 적용)
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

            // 2. 메일 본문 설정 (HTML을 사용하겠다고 true 설정)
            // (참고: MimeMessageHelper에는 setText(plain, html) 메소드가 없으므로, HTML을 우선으로 설정합니다.)
            helper.setText(html, true);

            // 권장 헤더 추가 (자동 발송 메일임을 명시하여 스팸 분류 확률 감소)
            mimeMessage.addHeader("Auto-Submitted", "auto-generated");
            mimeMessage.addHeader("X-Auto-Response-Suppress", "All");

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            e.printStackTrace(); // 에러 발생 시 로그에 상세 내용 출력
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }
}