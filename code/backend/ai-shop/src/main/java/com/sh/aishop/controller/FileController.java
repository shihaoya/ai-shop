package com.sh.aishop.controller;

import com.sh.aishop.common.Result;
import com.sh.aishop.entity.FileRecord;
import com.sh.aishop.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Tag(name = "文件管理", description = "文件上传、删除、访问")
@RestController
@RequestMapping("/api/file")
public class FileController {
    @Autowired
    private FileService fileService;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<FileRecord> upload(
            @Parameter(description = "业务类型: product, avatar等") @RequestParam String businessType,
            @Parameter(description = "关联业务ID(可选)") @RequestParam(required = false) Long businessId,
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file) throws IOException {
        return fileService.uploadFile(file, businessType, businessId);
    }

    @Operation(summary = "上传商品图片")
    @PostMapping("/upload/product")
    public Result<FileRecord> uploadProductImage(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file) throws IOException {
        Long userId = (Long) request.getAttribute("userId");
        // businessType固定为product，businessId由前端传入或为空
        return fileService.uploadFile(file, "product", null);
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{fileId}")
    public Result<?> delete(@PathVariable Long fileId) {
        return fileService.deleteFile(fileId);
    }

    @Operation(summary = "获取文件信息")
    @GetMapping("/{fileId}")
    public Result<FileRecord> getFile(@PathVariable Long fileId) {
        return fileService.getFile(fileId);
    }

    @Operation(summary = "根据业务查询文件列表")
    @GetMapping("/list")
    public Result<List<FileRecord>> getFiles(
            @Parameter(description = "业务类型") @RequestParam String businessType,
            @Parameter(description = "业务ID") @RequestParam Long businessId) {
        return fileService.getFilesByBusiness(businessType, businessId);
    }

    @Operation(summary = "访问文件")
    @GetMapping("/view/{fileId}")
    public ResponseEntity<Resource> view(@PathVariable Long fileId) throws IOException {
        Result<FileRecord> result = fileService.getFile(fileId);
        if (result.getCode() != 200 || result.getData() == null) {
            return ResponseEntity.notFound().build();
        }

        FileRecord file = result.getData();
        Path path = Paths.get("/uploads", file.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        if (resource.exists() && resource.isReadable()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}