package com.sh.aishop.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "分页请求参数")
public class PageRequest {
    @Schema(description = "页码，从1开始", example = "1")
    private Integer page = 1;
    
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
    
    @Schema(description = "搜索关键词")
    private String keyword;
    
    @Schema(description = "角色筛选：1管理员 2店铺运营 3普通用户", example = "3")
    private Integer role;
    
    @Schema(description = "状态筛选：1待审核 2正常 3冻结", example = "2")
    private Integer status;

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Long getOffset() {
        return ((long) page - 1) * pageSize;
    }
}