package com.sh.aishop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "upload")
public class UploadConfig {
    /** 是否启用上传 */
    private boolean enabled = true;

    /** 存储类型: local / oss */
    private String storageType = "local";

    /** 本地存储配置 */
    private LocalConfig local = new LocalConfig();

    /** OSS配置 */
    private OssConfig oss = new OssConfig();

    /** 允许的文件扩展名 */
    private List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "gif");

    /** 最大文件大小(字节)，默认10MB */
    private long maxSize = 10485760L;

    @Data
    public static class LocalConfig {
        /** 存储基础路径 */
        private String basePath = "/uploads";

        /** 访问路径前缀 */
        private String accessPath = "/api/file/";
    }

    @Data
    public static class OssConfig {
        private boolean enabled = false;
        private String endpoint;
        private String bucket;
        private String accessKey;
        private String secretKey;
        private String domain;
    }
}