package com.sh.aishop.service.impl;

import com.sh.aishop.config.UploadConfig;
import com.sh.aishop.common.entity.FileRecord;
import com.sh.aishop.service.StorageStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * OSS存储策略（预留扩展）
 */
@Component
public class OssStorageStrategy implements StorageStrategy {
    @Autowired
    private UploadConfig uploadConfig;

    @Override
    public String upload(MultipartFile file, String businessType, FileRecord fileRecord) throws IOException {
        // TODO: 实现OSS上传
        // 1. 验证OSS配置
        // 2. 生成存储路径
        // 3. 上传到OSS
        // 4. 返回URL
        throw new UnsupportedOperationException("OSS存储暂未实现，请联系管理员");
    }

    @Override
    public void delete(String filePath) {
        // TODO: 实现OSS删除
    }

    @Override
    public String getUrl(String filePath) {
        // TODO: 返回OSS URL
        UploadConfig.OssConfig oss = uploadConfig.getOss();
        if (oss.getDomain() != null && !oss.getDomain().isEmpty()) {
            return oss.getDomain() + "/" + filePath;
        }
        return filePath;
    }
}