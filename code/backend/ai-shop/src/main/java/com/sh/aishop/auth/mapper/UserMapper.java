package com.sh.aishop.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sh.aishop.common.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}