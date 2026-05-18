package com.sh.aishop.service.impl;

import com.sh.aishop.config.UploadConfig;
import com.sh.aishop.common.entity.FileRecord;
import com.sh.aishop.service.StorageStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地存储策略
 */
@Component
public class LocalStorageStrategy implements StorageStrategy {
    @Autowired
    private UploadConfig uploadConfig;

    @Override
    public String upload(MultipartFile file, String businessType, FileRecord fileRecord) throws IOException {
        // 创建日期目录
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        // 生成唯一文件名
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String ext = getExt(file.getOriginalFilename());
        String fileName = uuid + "." + ext;

        // 完整存储路径
        String relativePath = businessType + "/" + datePath + "/" + fileName;
        Path destPath = Paths.get(uploadConfig.getLocal().getBasePath(), relativePath);

        // 创建目录并写入文件
        Files.createDirectories(destPath.getParent());
        file.transferTo(destPath);

        // 更新fileRecord
        fileRecord.setFilePath(relativePath);
        fileRecord.setStorageType(1); // 本地存储

        // 返回访问URL
        return uploadConfig.getLocal().getAccessPath() + relativePath;
    }

    @Override
    public void delete(String filePath) {
        try {
            Path path = Paths.get(uploadConfig.getLocal().getBasePath(), filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // 忽略删除失败
        }
    }

    @Override
    public String getUrl(String filePath) {
        return uploadConfig.getLocal().getAccessPath() + filePath;
    }

    private String getExt(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDot = filename.lastIndexOf(".");
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "";
    }
}