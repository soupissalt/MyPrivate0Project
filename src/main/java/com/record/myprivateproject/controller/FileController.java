package com.record.myprivateproject.controller;

import com.record.myprivateproject.domain.FileEntry;
import com.record.myprivateproject.dto.FileDtos.*;
import com.record.myprivateproject.dto.FileSummaryDto;
import com.record.myprivateproject.dto.PageResponse;
import com.record.myprivateproject.exception.ApiErrorResponse;
import com.record.myprivateproject.exception.BusinessException;
import com.record.myprivateproject.exception.ErrorCode;
import com.record.myprivateproject.security.SecurityUtils;
import com.record.myprivateproject.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag(name = "File", description = "파일 업로드/버저닝 API")
@RestController
@RequestMapping("/api/files")
public class FileController {
    private static final long MAX_SIZE = 20 *1024 * 1024;
    private static final Set<String> ALLOWED_TYPES =
            Set.of("application/pdf", "image/png", "image/jpeg", "text/csv");
    private final FileService fileService;
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = FileUploadResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 입력(허용되지 않은 타입/크기 초과 등)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "폴더 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> upload(
            @Parameter(description = "업로드할 대상 폴더 ID", required = true, example = "123")
            @RequestParam Long folderId,

            @Parameter(
                    description = "업로드 파일(binaries). Swagger에서 'file' 필드로 업로드하세요.",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestParam("file") MultipartFile file
    ) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "빈 파일은 업로드할 수 없습니다.") {
            };
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "최대 업로드 크기(20MB)를 초과했습니다.") {
            };
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "허용되지 않은 Content-Type: " + contentType) {
            };
        }

        String email = SecurityUtils.currentUsernameOrThrow();

        FileEntry saved = fileService.upload(email, folderId, file);
        var latest = saved.getLatestVersion();

        return ResponseEntity.ok(new FileUploadResponse(
                saved.getId(),
                latest.getVersionNo(),
                latest.getSize(),
                latest.getSha256()
        ));
    }

    @Operation(summary = "파일 다운로드", description = "fileId의 최신 버전을 다운로드합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "파일 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId,
                                                 @RequestParam(required = false) Integer version,
                                                 @AuthenticationPrincipal UserDetails principal)
            throws Exception {
        var d =fileService.prepareDownload(principal.getUsername(), fileId, version);


        ContentDisposition cd = ContentDisposition.attachment()
                .filename(d.resource().getFilename(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .contentType(MediaType.parseMediaType(d.contentType()))
                .contentLength(d.size())
                .body(d.resource());
    }

    @GetMapping("/{fileId}/versions")
    public ResponseEntity<List<Map<String, Object>>> versions(@PathVariable Long fileId){
        var list = fileService.versions(fileId).stream()
                .map(v -> Map.<String, Object>of(
                        "version", v.getVersionNo(),
                        "size", v.getSize(),
                        "checksum", v.getSha256(),
                        "createdAt", v.getCreatedAt(),
                        "createdBy", v.getCreatedBy() != null ? v.getCreatedBy().getEmail() : null
                ))
                .toList();
        return ResponseEntity.ok(list);
    }

    public record RenameRequest(String name) {}

    @PatchMapping("/{fileId}/rename")
    public ResponseEntity<Void> rename(@PathVariable Long fileId,
                                       @RequestBody RenameRequest req,
                                       Authentication authentication) {
        fileService.rename(fileId, req.name, authentication.getName());
        return ResponseEntity.ok().build();
    }

    public record MoveRequest(Long toFolderId){}
    @PatchMapping("/{fileId}/move")
    public ResponseEntity<Void> move(@PathVariable Long fileId,
                                     @RequestBody MoveRequest req,
                                     Authentication authentication) {
        fileService.move(fileId, req.toFolderId(), authentication.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(@PathVariable Long fileId,
                                       Authentication authentication) {
        fileService.deleteFile(fileId, authentication.getName());
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "파일 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제됨"),
            @ApiResponse(responseCode = "404", description = "파일 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/streamingVideo/{fileId}")
    public ResponseEntity<ResourceRegion> streamVideo(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long fileId
    ) {
        String pathStr = fileService.resolvePhysicalPathByFileId(fileId);
        return fileService.streamingPublicVideo(headers, pathStr);
    }
    @Operation(summary = "파일 목록 조회", description = "page/size/sort, 간단 검색(q) 지원. 예 : sort=createdAt,desc")
    @GetMapping("/list")
    public PageResponse<FileSummaryDto> list(
            @Parameter(description = "폴더 ID", required = true, example = "123")
            @RequestParam Long folderId,
            @Parameter(description = "이름 부분검색 키워드", example = "보고서")
            @RequestParam(required = false) String q,
            @ParameterObject Pageable pageable
    ){
        return fileService.list(folderId, q, pageable);
    }
}
