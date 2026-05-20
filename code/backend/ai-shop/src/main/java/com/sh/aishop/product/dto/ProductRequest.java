package com.sh.aishop.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "商品创建/更新请求")
@Data
public class ProductRequest {

    @NotBlank(message = "商品名称不能为空")
    @Schema(description = "商品名称", example = "iPhone 15 Pro")
    private String name;

    @Schema(description = "分类ID", example = "1234567890")
    private String categoryId;

    @NotNull(message = "发货类型不能为空")
    @Min(value = 1, message = "发货类型只能是1或2")
    @Schema(description = "发货类型：1实体 2虚拟", example = "1")
    private Integer type;

    @NotNull(message = "积分价格不能为空")
    @Min(value = 0, message = "积分价格不能为负数")
    @Schema(description = "积分价格", example = "9999")
    private Integer price;

    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    @Schema(description = "库存数量", example = "100")
    private Integer stock;

    @Min(value = 0, message = "单人限购不能为负数")
    @Schema(description = "每人限购数量，0表示不限购", example = "5")
    private Integer limitPerUser;

    @Schema(description = "主图 file_record.id", example = "1234567890")
    private String mainImage;

    @Schema(description = "详情图 file_record.id，多个用逗号分隔", example = "123,456,789")
    private String detailImages;

    @Schema(description = "商品描述", example = "全新正品，假一赔十")
    private String description;
}