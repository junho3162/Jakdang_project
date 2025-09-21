package com.jakdang.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 이메일 인증 코드 생성, 저장, 검증을 담당하는 서비스 클래스입니다.
 */
@Service
public class VerificationService {

    // 인증 코드를 임시로 저장할 저장소입니다.
    // Key: 이메일 주소, Value: 인증 코드
    // ConcurrentHashMap은 여러 요청이 동시에 들어와도 안전하게 데이터를 처리합니다.
    // (실제 서비스에서는 이 데이터를 Redis와 같은 외부 캐시 저장소에 보관하는 것이 더 안정적이고 확장성이 좋습니다.)
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    /**
     * 지정된 이메일 주소에 대한 6자리 랜덤 인증 코드를 생성하고 저장합니다.
     * @param email 인증 코드를 받을 이메일 주소
     * @return 생성된 6자리 인증 코드
     */
    public String generateAndStoreCode(String email) {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 100000 ~ 999999 사이의 랜덤 숫자 생성
        String codeStr = String.valueOf(code);

        // 생성된 코드를 이메일 주소와 함께 저장합니다.
        verificationCodes.put(email, codeStr);

        // TODO: 인증 코드 유효 시간(예: 5분)을 설정하는 로직 추가 필요 (예: Redis의 TTL 기능 활용)

        return codeStr;
    }

    /**
     * 사용자가 입력한 인증 코드가 유효한지 검증합니다.
     * @param email 검증할 이메일 주소
     * @param code 사용자가 입력한 인증 코드
     * @return 코드가 일치하면 true, 아니면 false
     */
    public boolean verifyCode(String email, String code) {
        String storedCode = verificationCodes.get(email);

        // 저장된 코드가 있고, 사용자가 입력한 코드와 일치하는지 확인합니다.
        if (storedCode != null && storedCode.equals(code)) {
            // 검증이 완료된 코드는 즉시 삭제하여 재사용을 방지합니다.
            verificationCodes.remove(email);
            return true;
        }
        return false;
    }
}
