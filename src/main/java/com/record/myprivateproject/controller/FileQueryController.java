package com.record.myprivateproject.controller;

import com.record.myprivateproject.domain.FileEntry;
import com.record.myprivateproject.dto.FileDtos;
import com.record.myprivateproject.exception.ApiErrorResponse;
import com.record.myprivateproject.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "File", description = "파일 관리 API")
@RestController
@RequestMapping("/api/files")
public class FileQueryController {

    private final FileService fileService;
    public FileQueryController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(summary = "파일 단건 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
            content = @Content(schema = @Schema(implementation = FileDtos.class))),
            @ApiResponse(responseCode = "404", description = "파일 없음",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public List<FileEntry> getFileById(@PathVariable Long id) {
        return fileService.list(id);
    }
}
