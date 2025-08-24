package com.record.myprivateproject.service;

import com.record.myprivateproject.domain.FileEntry;
import com.record.myprivateproject.domain.FileVersion;
import com.record.myprivateproject.domain.Folder;
import com.record.myprivateproject.domain.User;
import com.record.myprivateproject.repository.FileEntryRepository;
import com.record.myprivateproject.repository.FileVersionRepository;
import com.record.myprivateproject.repository.FolderRepository;
import com.record.myprivateproject.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.util.HexFormat;
@Service
public class FileService {
    private final FileEntryRepository fileRepo;
    private final FolderRepository folderRepo;
    private final FileVersionRepository versionRepo;
    private final UserRepository userRepo;
    private final StorageService storage;

    public FileService(FileEntryRepository fileRepo, FolderRepository folderRepo,
                       FileVersionRepository versionRepo, UserRepository userRepo,
                       StorageService storage){
        this.fileRepo = fileRepo;
        this.folderRepo = folderRepo;
        this.versionRepo = versionRepo;
        this.userRepo = userRepo;
        this.storage = storage;
    }

    @Transactional
    public FileEntry upload(String userEmail, Long folderId, MultipartFile multipart) throws Exception{
        Folder folder = folderRepo.findById(folderId).orElseThrow();

        String fileName = multipart.getOriginalFilename() == null ? "file.bin" : multipart.getOriginalFilename();
        String contentType = multipart.getContentType();

        //같은 이름 있으면 기존 파일에 새 버전, 없으면 신규 파일
        FileEntry file = fileRepo.findByFolderIdAndName(folderId, fileName)
                .orElseGet(() -> fileRepo.save(new FileEntry(folder, fileName, contentType)));

        int nextVer = 1 + versionRepo.countByFile(file);
        byte[] data = multipart.getBytes();
        String sha256 = sha256Hex(data);
        String path = storage.store(folder.getRepository().getId(), file.getId(), nextVer, fileName,multipart);

        User creator = userRepo.findByEmail(userEmail).orElseThrow();
        FileVersion ver = versionRepo.save(new FileVersion(file, nextVer, path, sha256, (long)data.length, creator));

        // latestVersion 갱신 + size 반영
        file.setLatestVersion(ver);
        file.setSize((long)data.length);
        file.setContentType(contentType);
        return fileRepo.save(file);
    }

    private static String sha256Hex(byte[] bytes) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(bytes);
        return HexFormat.of().formatHex(digest);
    }
}
