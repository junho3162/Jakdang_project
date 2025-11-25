package com.jakdang.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    // 1. 파일이 저장될 서버의 경로입니다.
    // (주의) 이 경로는 실제 서버 환경에 맞게 변경해야 할 수 있습니다.
    private final String uploadDir = Paths.get("uploads").toAbsolutePath().toString();

    public FileService() {
        // 2. 서버 실행 시 'uploads' 폴더가 없으면 자동으로 생성합니다.
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("파일을 업로드할 디렉토리를 생성할 수 없습니다.", e);
        }
    }

    /**
     * MultipartFile을 서버에 저장하고, 접근 가능한 URL 경로를 반환합니다.
     * @param file 업로드된 파일
     * @return 서버에서 접근 가능한 파일 경로 (예: "/uploads/uuid-filename.jpg")
     */
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            return null; // 파일이 없으면 null 반환
        }

        // 3. 파일 이름이 중복되지 않도록 UUID를 파일명 앞에 붙입니다.
        String originalFilename = file.getOriginalFilename();
        String storedFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        Path destinationPath = Paths.get(uploadDir).resolve(storedFilename);

        try {
            // 4. 파일을 실제 경로에 저장합니다.
            Files.copy(file.getInputStream(), destinationPath);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장에 실패했습니다: " + storedFilename, e);
        }

        // 5. 웹에서 접근할 수 있는 상대 경로를 반환합니다.
        // (예: "/uploads/abc-123.jpg")
        return "/uploads/" + storedFilename;
    }

    // TODO: 공고 삭제 시, 서버에 저장된 실제 파일도 함께 삭제하는 로직 (deleteFile) 추가 필요
}
