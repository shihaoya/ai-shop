package com.sh.aishop.user.service;

import com.sh.aishop.common.Result;
import com.sh.aishop.user.dto.UserImportDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IUserService {
    Result<?> getUserInfo(Long userId);
    Result<?> updateUserInfo(Long userId, String nickname);
    Result<?> getInviteCode(Long operatorId);
    Result<?> createInviteCode(Long operatorId);
    Result<?> createUser(Long operatorId, String username, String nickname, String password);
    Result<?> resetUserPassword(Long operatorId, Long userId);
    Result<?> importUsers(Long operatorId, MultipartFile file);
    Result<?> doImportUsers(Long operatorId, List<UserImportDTO> rows);
}