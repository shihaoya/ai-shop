-- 积分商城系统 - 数据库初始化脚本
-- 数据库：ai_shop
-- 字符集：utf8mb4

CREATE DATABASE IF NOT EXISTS ai_shop DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ai_shop;

-- ============================================
-- 1. user 用户表
-- ============================================
CREATE TABLE `user` (
  `id` BIGINT NOT NULL COMMENT '雪花ID，主键',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `nickname` VARCHAR(50) COMMENT '昵称',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `role` TINYINT NOT NULL COMMENT '1=管理员 2=店铺用户 3=普通用户',
  `parent_id` BIGINT DEFAULT 0 COMMENT '上级用户ID',
  `status` TINYINT DEFAULT 1 COMMENT '1=待审核 2=正常 3=已冻结',
  `deleted` TINYINT DEFAULT 0 COMMENT '0=未删除 1=已删除',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. shop 店铺表
-- ============================================
CREATE TABLE `shop` (
  `id` BIGINT NOT NULL COMMENT '雪花ID，主键',
  `operator_id` BIGINT NOT NULL COMMENT '店铺用户ID',
  `name` VARCHAR(100) NOT NULL COMMENT '店铺名称',
  `description` TEXT COMMENT '店铺简介',
  `status` TINYINT DEFAULT 1 COMMENT '1=待审核 2=已通过 3=已拒绝 4=已禁用',
  `is_active` TINYINT DEFAULT 1 COMMENT '0=歇业 1=营业',
  `deleted` TINYINT DEFAULT 0 COMMENT '0=未删除 1=已删除',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺表';

-- ============================================
-- 3. category 商品分类表
-- ============================================
CREATE TABLE `category` (
  `id` BIGINT NOT NULL COMMENT '雪花ID，主键',
  `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `deleted` TINYINT DEFAULT 0 COMMENT '0=未删除 1=已删除',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- ============================================
-- 4. product 商品表
-- ============================================
CREATE TABLE `product` (
  `id` BIGINT NOT NULL COMMENT '雪花ID，主键',
  `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
  `category_id` BIGINT COMMENT '分类ID',
  `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `type` TINYINT NOT NULL COMMENT '1=虚拟 2=实体',
  `price` INT NOT NULL COMMENT '积分价格',
  `stock` INT DEFAULT -1 COMMENT '-1=无限 0=不可下单',
  `limit_per_user` INT DEFAULT 0 COMMENT '0=不限',
  `main_image` VARCHAR(255) COMMENT '主图URL',
  `detail_images` TEXT COMMENT '详情图JSON数组',
  `description` TEXT COMMENT '商品描述',
  `delivery_info` TEXT COMMENT '发货说明',
  `status` TINYINT DEFAULT 1 COMMENT '1=上架 2=下架',
  `deleted` TINYINT DEFAULT 0 COMMENT '0=未删除 1=已删除',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ============================================
-- 5. orders 订单表（含收货地址快照）
-- ============================================
CREATE TABLE `orders` (
  `id` BIGINT NOT NULL COMMENT '雪花ID，主键',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号（唯一）',
  `user_id` BIGINT NOT NULL COMMENT '普通用户ID',
  `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `points` INT NOT NULL COMMENT '消耗积分',
  `quantity` INT DEFAULT 1 COMMENT '兑换数量',
  `status` TINYINT DEFAULT 1 COMMENT '1=已下单 2=已确认 3=已发货 4=已完成 5=已关闭',
  `deleted` TINYINT DEFAULT 0 COMMENT '0=未删除 1=已删除',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `completed_at` DATETIME COMMENT '完成时间',
  `closed_at` DATETIME COMMENT '关闭时间',
  `close_reason` VARCHAR(255) COMMENT '关闭原因',
  `receiver_name` VARCHAR(50) COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(20) COMMENT '手机号',
  `receiver_province` VARCHAR(50) COMMENT '省',
  `receiver_city` VARCHAR(50) COMMENT '市',
  `receiver_district` VARCHAR(50) COMMENT '区',
  `receiver_detail` VARCHAR(255) COMMENT '详细地址',
  `express_company` VARCHAR(50) COMMENT '快递公司',
  `express_no` VARCHAR(100) COMMENT '快递单号',
  `delivery_content` TEXT COMMENT '发货内容（虚拟商品）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================
-- 6. points 积分表
-- ============================================
CREATE TABLE `points` (
  `id` BIGINT NOT NULL COMMENT '雪花ID，主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `amount` INT NOT NULL COMMENT '积分变动（正=发放 负=扣除）',
  `balance` INT NOT NULL COMMENT '变动后余额',
  `type` TINYINT NOT NULL COMMENT '1=发放 2=扣除 3=兑换 4=退款',
  `remark` VARCHAR(255) COMMENT '备注',
  `operator_id` BIGINT COMMENT '操作人ID',
  `deleted` TINYINT DEFAULT 0 COMMENT '0=未删除 1=已删除',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分表';

-- ============================================
-- 7. address 收货地址表
-- ============================================
CREATE TABLE `address` (
  `id` BIGINT NOT NULL COMMENT '雪花ID，主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `province` VARCHAR(50) NOT NULL COMMENT '省',
  `city` VARCHAR(50) NOT NULL COMMENT '市',
  `district` VARCHAR(50) NOT NULL COMMENT '区',
  `detail` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `is_default` TINYINT DEFAULT 0 COMMENT '0=否 1=是',
  `deleted` TINYINT DEFAULT 0 COMMENT '0=未删除 1=已删除',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收货地址表';

-- ============================================
-- 8. invite_code 邀请码表
-- ============================================
CREATE TABLE `invite_code` (
  `id` BIGINT NOT NULL COMMENT '雪花ID，主键',
  `code` VARCHAR(20) NOT NULL COMMENT '邀请码',
  `role` TINYINT NOT NULL COMMENT '2=店铺用户 3=普通用户',
  `creator_id` BIGINT NOT NULL COMMENT '创建者ID',
  `status` TINYINT DEFAULT 1 COMMENT '1=有效 2=已作废',
  `deleted` TINYINT DEFAULT 0 COMMENT '0=未删除 1=已删除',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邀请码表';

-- ============================================
-- 9. message 消息表
-- ============================================
CREATE TABLE `message` (
  `id` BIGINT NOT NULL COMMENT '雪花ID，主键',
  `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
  `title` VARCHAR(100) NOT NULL COMMENT '消息标题',
  `content` TEXT COMMENT '消息内容',
  `type` TINYINT NOT NULL COMMENT '1=积分通知 2=订单通知',
  `related_id` BIGINT COMMENT '关联ID（订单ID或积分ID）',
  `is_read` TINYINT DEFAULT 0 COMMENT '0=未读 1=已读',
  `deleted` TINYINT DEFAULT 0 COMMENT '0=未删除 1=已删除',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- ============================================
-- 预置管理员账号
-- 用户名：admin，密码：admin123
-- 密码为 BCrypt 加密后的值
-- ============================================
INSERT INTO `user` (`id`, `username`, `nickname`, `password`, `role`, `parent_id`, `status`, `deleted`)
VALUES (1, 'admin', '管理员', '$2a$10$mxdpkIwUAXyKYprDS8nIKOQr8W3v47PZuv3pgMwGEayB3sDRdiHMq', 1, 0, 2, 0);