package com.sh.aishop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.config.UploadConfig;
import com.sh.aishop.entity.FileRecord;
import com.sh.aishop.mapper.FileRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class FileService {
    @Autowired
    private FileRecordMapper fileRecordMapper;

    @Autowired
    @Qualifier("localStorageStrategy")
    private StorageStrategy localStorageStrategy;

    @Autowired
    @Qualifier("ossStorageStrategy")
    private StorageStrategy ossStorageStrategy;

    @Autowired
    private UploadConfig uploadConfig;

    @Transactional
    public Result<FileRecord> uploadFile(MultipartFile file, String businessType, Long businessId) throws IOException {
        validateFile(file);
        StorageStrategy strategy = getStorageStrategy();

        FileRecord fileRecord = new FileRecord();
        fileRecord.setFileName(file.getOriginalFilename());
        fileRecord.setFileSize(file.getSize());
        fileRecord.setFileType(file.getContentType());
        fileRecord.setFileExt(getExt(file.getOriginalFilename()));
        fileRecord.setBusinessType(businessType);
        fileRecord.setBusinessId(businessId);

        String url = strategy.upload(file, businessType, fileRecord);
        fileRecord.setUrl(url);
        fileRecordMapper.insert(fileRecord);

        return Result.success(fileRecord);
    }

    @Transactional
    public Result<?> deleteFile(String fileId) {
        FileRecord file = fileRecordMapper.selectById(Long.parseLong(fileId));
        if (file == null || file.getDeleted() == 1) {
            return Result.fail(ResultCode.DATA_NOT_FOUND, "文件不存在");
        }

        StorageStrategy strategy = getStorageStrategy(file.getStorageType());
        strategy.delete(file.getFilePath());
        fileRecordMapper.deleteById(fileId);

        return Result.success();
    }

    public Result<FileRecord> getFile(String fileId) {
        FileRecord file = fileRecordMapper.selectOne(new LambdaQueryWrapper<FileRecord>()
                .eq(FileRecord::getId, Long.parseLong(fileId))
                .eq(FileRecord::getDeleted, 0));
        if (file == null) {
            return Result.fail(ResultCode.DATA_NOT_FOUND, "文件不存在");
        }
        return Result.success(file);
    }

    public Result<List<FileRecord>> getFilesByBusiness(String businessType, Long businessId) {
        List<FileRecord> files = fileRecordMapper.selectList(new LambdaQueryWrapper<FileRecord>()
                .eq(FileRecord::getBusinessType, businessType)
                .eq(FileRecord::getBusinessId, businessId)
                .eq(FileRecord::getDeleted, 0));
        return Result.success(files);
    }

    private StorageStrategy getStorageStrategy() {
        int storageType = "oss".equals(uploadConfig.getStorageType()) ? 2 : 1;
        return storageType == 2 ? ossStorageStrategy : localStorageStrategy;
    }

    private StorageStrategy getStorageStrategy(Integer storageType) {
        return storageType == 2 ? ossStorageStrategy : localStorageStrategy;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_VALID_FAIL, "文件不能为空");
        }
        if (file.getSize() > uploadConfig.getMaxSize()) {
            throw new BusinessException(ResultCode.PARAM_VALID_FAIL,
                    "文件大小不能超过" + (uploadConfig.getMaxSize() / 1024 / 1024) + "MB");
        }
        String ext = getExt(file.getOriginalFilename()).toLowerCase();
        if (!uploadConfig.getAllowedExtensions().contains(ext)) {
            throw new BusinessException(ResultCode.PARAM_VALID_FAIL,
                    "不支持的文件类型：" + ext + "，允许的类型：" + uploadConfig.getAllowedExtensions());
        }
    }

    private String getExt(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDot = filename.lastIndexOf(".");
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "";
    }

    public static class BusinessException extends RuntimeException {
        private final int code;

        public BusinessException(int code, String message) {
            super(message);
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}