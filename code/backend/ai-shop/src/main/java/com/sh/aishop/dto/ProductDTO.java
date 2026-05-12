package com.sh.aishop.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "商品信息")
public class ProductDTO {
    @Schema(description = "商品ID", example = "1234567890")
    private String id;
    
    @Schema(description = "店铺ID", example = "100")
    private String shopId;
    
    @Schema(description = "店铺名称", example = "官方商城")
    private String shopName;
    
    @Schema(description = "分类ID", example = "10")
    private String categoryId;
    
    @Schema(description = "分类名称", example = "数码产品")
    private String categoryName;
    
    @Schema(description = "商品名称", example = "iPhone 15 Pro")
    private String name;
    
    @Schema(description = "商品类型：1虚拟 2实体", example = "2")
    private Integer type;
    
    @Schema(description = "商品类型描述", example = "实物商品")
    private String typeDesc;
    
    @Schema(description = "价格（积分）", example = "9999")
    private Integer price;
    
    @Schema(description = "库存数量", example = "100")
    private Integer stock;
    
    @Schema(description = "每人限购数量", example = "5")
    private Integer limitPerUser;
    
    @Schema(description = "主图URL", example = "https://example.com/image.jpg")
    private String mainImage;
    
    @Schema(description = "详情图URL列表，JSON格式")
    private String detailImages;
    
    @Schema(description = "商品描述")
    private String description;
    
    @Schema(description = "配送信息")
    private String deliveryInfo;
    
    @Schema(description = "状态：1上架 2下架", example = "1")
    private Integer status;
    
    @Schema(description = "状态描述", example = "已上架")
    private String statusDesc;

    // all getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getShopId() { return shopId; }
    public void setShopId(String shopId) { this.shopId = shopId; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public String getTypeDesc() { return typeDesc; }
    public void setTypeDesc(String typeDesc) { this.typeDesc = typeDesc; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Integer getLimitPerUser() { return limitPerUser; }
    public void setLimitPerUser(Integer limitPerUser) { this.limitPerUser = limitPerUser; }
    public String getMainImage() { return mainImage; }
    public void setMainImage(String mainImage) { this.mainImage = mainImage; }
    public String getDetailImages() { return detailImages; }
    public void setDetailImages(String detailImages) { this.detailImages = detailImages; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDeliveryInfo() { return deliveryInfo; }
    public void setDeliveryInfo(String deliveryInfo) { this.deliveryInfo = deliveryInfo; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getStatusDesc() { return statusDesc; }
    public void setStatusDesc(String statusDesc) { this.statusDesc = statusDesc; }
}