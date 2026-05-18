package com.sh.aishop.service;

import com.sh.aishop.config.UploadConfig;
import com.sh.aishop.common.entity.FileRecord;
import com.sh.aishop.mapper.FileRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 存储策略接口
 */
public interface StorageStrategy {
    /**
     * 上传文件
     * @param file 文件
     * @param businessType 业务类型
     * @param fileRecord 文件记录
     * @return 访问URL
     */
    String upload(MultipartFile file, String businessType, FileRecord fileRecord) throws IOException;

    /**
     * 删除文件
     * @param filePath 文件路径
     */
    void delete(String filePath);

    /**
     * 获取文件URL
     * @param filePath 文件路径
     * @return URL
     */
    String getUrl(String filePath);
}