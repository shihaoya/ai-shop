package com.sh.aishop.dto;

public class PageRequest {
    private Integer page = 1;
    private Integer pageSize = 10;
    private String keyword;
    private Integer role;    // 角色筛选：1=管理员，2=店铺用户，3=普通用户
    private Integer status;  // 状态筛选：1=待审核，2=正常，3=已冻结

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