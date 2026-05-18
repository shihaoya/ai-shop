package com.sh.aishop.user.service;

import com.alibaba.excel.EasyExcel;
import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.common.entity.InviteCode;
import com.sh.aishop.common.entity.User;
import com.sh.aishop.common.enums.InviteCodeStatus;
import com.sh.aishop.common.enums.RoleEnum;
import com.sh.aishop.common.enums.UserStatus;
import com.sh.aishop.user.dto.UserImportDTO;
import com.sh.aishop.auth.mapper.InviteCodeMapper;
import com.sh.aishop.user.mapper.UserMapper;
import com.sh.aishop.util.SecurityUtil;
import com.sh.aishop.util.SnowflakeIdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private InviteCodeMapper inviteCodeMapper;

    public Result<?> getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        return Result.success(toUserDTO(user));
    }

    @Transactional
    public Result<?> updateUserInfo(Long userId, String nickname) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        if (nickname != null && !nickname.isEmpty()) {
            user.setNickname(nickname);
        }
        userMapper.updateById(user);
        return Result.success(toUserDTO(user));
    }

    // ============ 邀请码 ============
    public Result<?> getInviteCode(Long operatorId) {
        InviteCode code = inviteCodeMapper.selectOne(new LambdaQueryWrapper<InviteCode>()
                .eq(InviteCode::getCreatorId, operatorId)
                .eq(InviteCode::getRole, RoleEnum.NORMAL_USER.getCode())
                .eq(InviteCode::getStatus, InviteCodeStatus.ACTIVE.getCode())
                .eq(InviteCode::getDeleted, 0));

        return Result.success(code != null ? code.getCode() : null);
    }

    @Transactional
    public Result<?> createInviteCode(Long operatorId) {
        InviteCode old = inviteCodeMapper.selectOne(new LambdaQueryWrapper<InviteCode>()
                .eq(InviteCode::getCreatorId, operatorId)
                .eq(InviteCode::getRole, RoleEnum.NORMAL_USER.getCode())
                .eq(InviteCode::getStatus, InviteCodeStatus.ACTIVE.getCode())
                .eq(InviteCode::getDeleted, 0));

        if (old != null) {
            old.setStatus(InviteCodeStatus.INVALID.getCode());
            inviteCodeMapper.updateById(old);
        }

        InviteCode newCode = new InviteCode();
        newCode.setId(SnowflakeIdUtil.nextId());
        newCode.setCode(generateCode());
        newCode.setRole(RoleEnum.NORMAL_USER.getCode());
        newCode.setCreatorId(operatorId);
        newCode.setStatus(InviteCodeStatus.ACTIVE.getCode());
        inviteCodeMapper.insert(newCode);

        return Result.success(newCode.getCode());
    }

    // ============ 创建/导入用户 ============
    @Transactional
    public Result<?> createUser(Long operatorId, String username, String nickname, String password) {
        User exist = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username).eq(User::getDeleted, 0));
        if (exist != null) {
            return Result.fail(ResultCode.USERNAME_EXISTS, "用户名已存在");
        }

        User user = new User();
        user.setId(SnowflakeIdUtil.nextId());
        user.setUsername(username);
        user.setNickname(nickname != null ? nickname : username);
        user.setPassword(SecurityUtil.encryptPassword(password));
        user.setRole(RoleEnum.NORMAL_USER.getCode());
        user.setParentId(operatorId);
        user.setStatus(UserStatus.NORMAL.getCode());
        userMapper.insert(user);

        return Result.success(user.getId().toString());
    }

    @Transactional
    public Result<?> resetUserPassword(Long operatorId, Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        if (user.getParentId() == null || !user.getParentId().equals(operatorId)) {
            return Result.fail(ResultCode.FAIL, "无权重置该用户密码");
        }

        String newPassword = generateRandomPassword();
        user.setPassword(SecurityUtil.encryptPassword(newPassword));
        userMapper.updateById(user);

        return Result.success(Collections.singletonMap("password", newPassword));
    }

    public Result<?> importUsers(Long operatorId, MultipartFile file) {
        List<UserImportDTO> rows;
        try {
            rows = EasyExcel.read(file.getInputStream())
                    .head(UserImportDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            return Result.fail("文件读取失败，请检查文件格式");
        } catch (Exception e) {
            return Result.fail("文件解析失败，请确认上传的是正确的 Excel 文件");
        }

        if (rows == null || rows.isEmpty()) {
            return Result.fail("文件中没有数据，请填写后再上传");
        }

        List<Map<String, Object>> errors = new ArrayList<>();
        Set<String> importUsernames = new HashSet<>();
        Set<String> existingUsernames = new HashSet<>();

        List<User> allUsers = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getDeleted, 0)
                .select(User::getUsername));
        allUsers.forEach(u -> existingUsernames.add(u.getUsername()));

        for (int i = 0; i < rows.size(); i++) {
            UserImportDTO row = rows.get(i);
            int rowNum = i + 2;
            List<String> rowErrors = new ArrayList<>();

            String username = row.getUsername() != null ? row.getUsername().trim() : "";
            String nickname = row.getNickname() != null ? row.getNickname().trim() : "";

            if (username.isEmpty()) {
                rowErrors.add("用户名为空");
            } else if (username.length() > 50) {
                rowErrors.add("用户名不能超过50个字符");
            } else if (importUsernames.contains(username)) {
                rowErrors.add("文件中存在重复的用户名: " + username);
            } else if (existingUsernames.contains(username)) {
                rowErrors.add("用户名已存在: " + username);
            }

            if (!nickname.isEmpty() && nickname.length() > 50) {
                rowErrors.add("昵称超过50个字符");
            }

            if (!rowErrors.isEmpty()) {
                Map<String, Object> err = new HashMap<>();
                err.put("row", rowNum);
                err.put("message", String.join("; ", rowErrors));
                errors.add(err);
            }

            if (!username.isEmpty()) {
                importUsernames.add(username);
            }
        }

        if (!errors.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("hasErrors", true);
            result.put("errors", errors);
            return Result.success(result);
        }

        return doImportUsers(operatorId, rows);
    }

    @Transactional
    public Result<?> doImportUsers(Long operatorId, List<UserImportDTO> rows) {
        List<Map<String, String>> importedUsers = new ArrayList<>();

        for (UserImportDTO row : rows) {
            String username = row.getUsername().trim();
            String nickname = row.getNickname() != null && !row.getNickname().trim().isEmpty()
                    ? row.getNickname().trim() : username;

            String password = generateRandomPassword();

            User user = new User();
            user.setId(SnowflakeIdUtil.nextId());
            user.setUsername(username);
            user.setNickname(nickname);
            user.setPassword(SecurityUtil.encryptPassword(password));
            user.setRole(RoleEnum.NORMAL_USER.getCode());
            user.setParentId(operatorId);
            user.setStatus(UserStatus.NORMAL.getCode());
            userMapper.insert(user);

            Map<String, String> userMap = new HashMap<>();
            userMap.put("username", username);
            userMap.put("nickname", nickname);
            userMap.put("password", password);
            importedUsers.add(userMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("hasErrors", false);
        result.put("success", true);
        result.put("users", importedUsers);
        return Result.success(result);
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generateCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private com.sh.aishop.common.dto.UserDTO toUserDTO(User user) {
        com.sh.aishop.common.dto.UserDTO dto = new com.sh.aishop.common.dto.UserDTO();
        dto.setId(user.getId().toString());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        return dto;
    }
}