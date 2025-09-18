package com.record.myprivateproject.service;

import com.record.myprivateproject.domain.FileEntry;
import com.record.myprivateproject.domain.FileVersion;
import com.record.myprivateproject.domain.Folder;
import com.record.myprivateproject.domain.User;
import com.record.myprivateproject.dto.AuditAction;
import com.record.myprivateproject.exception.BusinessException;
import com.record.myprivateproject.exception.ErrorCode;
import com.record.myprivateproject.repository.FileEntryRepository;
import com.record.myprivateproject.repository.FileVersionRepository;
import com.record.myprivateproject.repository.FolderRepository;
import com.record.myprivateproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class FileService {
    private final FileEntryRepository fileRepo;
    private final FolderRepository folderRepo;
    private final FileVersionRepository versionRepo;
    private final UserRepository userRepo;
    private final StorageService storage;
    private final AuditService auditService;

    public record Download(Resource resource, String name, String contentType, long size) {}

    public FileService(FileEntryRepository fileRepo, FolderRepository folderRepo,
                       FileVersionRepository versionRepo, UserRepository userRepo,
                       StorageService storage, AuditService auditService){
        this.fileRepo = fileRepo;
        this.folderRepo = folderRepo;
        this.versionRepo = versionRepo;
        this.userRepo = userRepo;
        this.storage = storage;
        this.auditService = auditService;
    }

    @Value("${app.storage.root}")
    private String storageBaseDir;

    public String resolvePhysicalPathByFileId(Long fileId) {
        var latest = versionRepo.findByFileIdOrderByVersionNoDesc(fileId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_STORAGE_ERROR,"파일 버전을 찾을 수 없습니다.") {
                });
        return Paths.get(storageBaseDir, latest.getSha256()).toString();
    }

    @Transactional
    public FileEntry upload(String userEmail, Long folderId, MultipartFile multipart) throws Exception{
        if (multipart == null || multipart.isEmpty()){
            throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR, "파일 버전을 찾을 수 없습니다.") {
            };
        }

        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE,"사용자를 찾을 수 없습니다.") {
                });

        Folder folder = folderRepo.findById(folderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_STORAGE_ERROR, "파일 버전을 찾을 수 없습니다.") {
                });

        // 권한 체크(폴더 소유자 = 업로더)
        if (!folder.getRepository().getOwner().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED,"해당 폴더에 업로드 권한이 없습니다.") {
            };
        }

        String fileName = multipart.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,"파일명이 비어있습니다.") {
            };
        }

        String ct = multipart.getContentType();
        final String finalContentType =
                (ct == null || ct.isBlank()) ? "application/octet-stream" : ct;

        // 같은 이름 파일 존재 시 재사용, 없으면 신규 생성
        FileEntry file = fileRepo.findByFolderIdAndName(folderId, fileName)
                .orElseGet(() -> fileRepo.save(new FileEntry(folder, fileName, finalContentType)));

        long count = versionRepo.countByFile(file);  // 리포지토리 반환형이 long일 수 있어 안전하게
        int nextVer = (int) (count + 1);

        byte[] data = multipart.getBytes();
        String sha256 = sha256Hex(data);

        // 프로젝트의 StorageService API 그대로 사용
        String objectKeyOrPath = storage.store(
                folder.getRepository().getId(),
                file.getId(),
                nextVer,
                fileName,
                multipart
        );

        // FileVersion 시그니처: (file, versionNo, objectKey, sha256, size, uploadedBy)
        FileVersion ver = versionRepo.save(
                new FileVersion(file, nextVer, objectKeyOrPath, sha256, (long) data.length, user)
        );

        // latestVersion, size, contentType 갱신
        file.setLatestVersion(ver);
        file.setSize((long) data.length);
        file.setContentType(finalContentType);
        auditService.record(
                AuditAction.FILE_UPLOAD.name(),
                "FILE",
                file.getId(),
                "name" + file.getName() + ", ver = "+ ver.getVersionNo()
        );
        return fileRepo.save(file);
    }
    @Transactional(readOnly = true)
    public List<FileEntry> list(Long folderId){
        Folder folder = folderRepo.findById(folderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHEET_NOT_FOUND,"폴더를 찾을 수 없습니다.") {
                });
        return fileRepo.findByFolderOrderByIdAsc(folder);
    }

    private static String sha256Hex(byte[] bytes) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(bytes);
        return HexFormat.of().formatHex(digest);
    }
    @Transactional(readOnly = true)
    public Download prepareDownload(String userEmail, Long folderId, Integer version) throws Exception{
        var user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE,"사용자를 찾을 수 없습니다.") {
                });
        var file = fileRepo.findById(folderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHEET_NOT_FOUND, "파일이 존재하지 않습니다.") {
                });

        if (!Objects.equals(file.getFolder().getRepository().getOwner().getId(), user.getId())) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED,"다운로드 권한이 없습니다.") {
            };
        }

        FileVersion fv = (version ==null)
                ?file.getLatestVersion()
                :versionRepo.findByFileAndVersionNo(file, version)
                .orElseThrow(() -> new BusinessException(ErrorCode.JSON_PARSE_ERROR,"요청한 버전이 없습니다.") {
                });

        Resource res = storage.load(fv.getStoragePath());

        String ct = file.getContentType();
        if (ct == null || ct.isBlank()) {
            try {
                Path p = Paths.get(res.getURI());
                String probed = Files.probeContentType(p);
                if (probed != null) ct =probed;
            }catch (Exception ignore){}
            if (ct == null || ct.isBlank()) ct = "application/octet-stream";}
        auditService.record(
                AuditAction.FILE_DOWNLOAD.name(),
                "FILE",
                file.getId(),
                "ver = "+ fv.getVersionNo()
        );
        return new Download(res, file.getName(), ct, file.getSize());
    }
    @Transactional(readOnly = true)
    public List<FileVersion> versions(Long fileId) {
        return versionRepo.findByFileIdOrderByVersionNoDesc(fileId);
    }
    @Transactional
    public void rename(Long fileId, String newName, String userEmail) {
        if (newName == null || newName.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,"이름이 비어있습니다.") {
            };
        }
        FileEntry file = fileRepo.findById(fileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHEET_NOT_FOUND,"파일을 찾을 수 없습니다.") {
                });

        Long ownerId = file.getFolder().getRepository().getOwner().getId();
        Long actorId = userRepo.findByEmail(userEmail).orElseThrow().getId();
        if (!ownerId.equals(actorId)) throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED,"이 파일을 수정할 권한이 없습니다.") {
        };

        if (fileRepo.findByFolderIdAndName(file.getFolder().getId(), newName).isPresent()) {
            throw new BusinessException(ErrorCode.CONFLICT_STATE,"같은 폴더에 동일한 이름의 파일이 이미 있습니다.") {
            };
        }
        file.setName(newName);
        fileRepo.save(file);
        auditService.record(
          AuditAction.FILE_RENAME.name(),
          "FILE",
                file.getId(),
                "rename to = " + newName
        );
    }

    @Transactional
    public void move(Long fileId, Long toFolderId, String userEmail) {
        FileEntry file = fileRepo.findById(fileId)
                .orElseThrow(()-> new BusinessException(ErrorCode.SHEET_NOT_FOUND,"파일을 찾을 수 없습니다.") {
                });

        Folder dest = folderRepo.findById(toFolderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHEET_NOT_FOUND, "대상 폴더를 찾을 수 없습니다.") {
                });

        if (!file.getFolder().getRepository().getId().equals(dest.getRepository().getId())) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND,"다른 저장소로는 이동할 수 없습니다.") {
            };
        }

        Long ownerId = file.getFolder().getRepository().getOwner().getId();
        Long actorId = userRepo.findByEmail(userEmail).orElseThrow().getId();

        if (!ownerId.equals(actorId)) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED,"이 파일을 이동할 권한이 없습니다.") {
            };
        }

        if (fileRepo.findByFolderIdAndName(dest.getId(),file.getName()).isPresent()){
            throw new BusinessException(ErrorCode.CONFLICT_STATE,"대상 폴더에 동일한 이름의 파일이 이미 존재합니다.") {
            };
        }
        file.setFolder(dest);
        fileRepo.save(file);
        auditService.record(
                AuditAction.FILE_MOVE.name(),
                "FILE",
                file.getId(),
                "toFolderId = " + toFolderId
        );
    }

    @Transactional
    public void deleteFile(Long fileId, String userEmail) {
        FileEntry file =fileRepo.findById(fileId)
                .orElseThrow(()-> new BusinessException(ErrorCode.SHEET_NOT_FOUND,"파일을 찾을 수 없습니다.") {
                });

        Long ownerId = file.getFolder().getRepository().getOwner().getId();
        Long actorId = userRepo.findByEmail(userEmail).orElseThrow().getId();
        if (!ownerId.equals(actorId)) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED,"이 파일을 삭제할 권한이 없습니다.") {
            };
        }

        List<FileVersion> versions = versionRepo.findByFileOrderByVersionNoDesc(file);
        versionRepo.deleteAll(versions);
        fileRepo.delete(file);
        auditService.record(
                AuditAction.FILE_DELETE.name(),
                "FILE",
                file.getId(),
                "deleted"
        );
    }

    public ResponseEntity<ResourceRegion> streamingPublicVideo(HttpHeaders headers, String pathStr) {
        Path path = Paths.get(pathStr);
        Resource resource = new FileSystemResource(path);

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        final long CHUNK_SIZE = 1024L * 1024L; // 1MB
        try {
            long contentLength = resource.contentLength();

            List<HttpRange> ranges = headers.getRange();
            ResourceRegion region;

            if (ranges == null || ranges.isEmpty()) {
                // Range 없이 들어온 경우: 첫 청크만 보냄(206)
                long rangeLen = Math.min(CHUNK_SIZE, contentLength);
                region = new ResourceRegion(resource, 0, rangeLen);
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES))
                        .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .body(region);
            } else {
                // 첫 번째 Range만 처리(대부분 1개)
                HttpRange range = ranges.get(0);
                long start = range.getRangeStart(contentLength);
                long end   = range.getRangeEnd(contentLength);

                // 잘못된 범위(파일 길이 벗어남)
                if (start >= contentLength) {
                    return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                            .header(HttpHeaders.CONTENT_RANGE, "bytes */" + contentLength)
                            .build();
                }

                long rangeLen = Math.min(CHUNK_SIZE, end - start + 1);
                region = new ResourceRegion(resource, start, rangeLen);

                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES))
                        .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .body(region);
            }
        } catch (IOException e) {
            // 파일 길이 조회 실패 등
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @Transactional
    public List<Map<String,Object>> versionSp(Long fileId){
        fileRepo.findById(fileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHEET_NOT_FOUND,"파일을 찾을 수 없습니다.") {
                });

        return versionRepo.callGetFileVersions(fileId).stream()
                .map(r -> Map.<String, Object> of(
                        "version", r.getVersionNo(),
                        "size", r.getSize(),
                        "checksum", r.getChecksum(),
                        "createdAt", r.getCreatedAt().toInstant(),
                        "createdBy", r.getCreatedBy()
                ))
                .toList();
    }
}
