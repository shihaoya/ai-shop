package com.sh.aishop.interceptor;

import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行OPTIONS请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (!StringUtils.hasText(token)) {
            sendUnauthorized(response, ResultCode.TOKEN_INVALID, "未提供认证令牌");
            return false;
        }

        // 去除 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 检查Redis黑名单
        if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token))) {
            sendUnauthorized(response, ResultCode.TOKEN_INVALID, "令牌已失效");
            return false;
        }

        try {
            if (jwtUtil.isTokenExpired(token)) {
                sendUnauthorized(response, ResultCode.TOKEN_EXPIRED, "令牌已过期");
                return false;
            }

            // 将用户信息存入request
            Long userId = jwtUtil.getUserId(token);
            String username = jwtUtil.getUsername(token);
            int role = jwtUtil.getRole(token);

            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("role", role);

            return true;
        } catch (Exception e) {
            sendUnauthorized(response, ResultCode.TOKEN_INVALID, "无效的认证令牌");
            return false;
        }
    }

    private void sendUnauthorized(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = Result.fail(code, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}