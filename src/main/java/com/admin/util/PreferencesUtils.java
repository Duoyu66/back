package com.admin.util;

import com.admin.dto.UserPreferencesRequest;
import com.admin.vo.UserPreferencesVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class PreferencesUtils {

    private final ObjectMapper objectMapper;

    public UserPreferencesVO defaults() {
        UserPreferencesVO vo = new UserPreferencesVO();
        vo.setColorMode("light");
        vo.setPreset("green");
        vo.setBorderRadius(0.5);
        return vo;
    }

    public UserPreferencesVO parse(String json) {
        if (!StringUtils.hasText(json)) {
            return defaults();
        }
        try {
            UserPreferencesVO vo = objectMapper.readValue(json, UserPreferencesVO.class);
            if (vo.getColorMode() == null) {
                vo.setColorMode("light");
            }
            if (vo.getPreset() == null) {
                vo.setPreset("green");
            }
            if (vo.getBorderRadius() == null) {
                vo.setBorderRadius(0.5);
            }
            return vo;
        } catch (JsonProcessingException e) {
            return defaults();
        }
    }

    public String toJson(UserPreferencesRequest request) {
        UserPreferencesVO vo = new UserPreferencesVO();
        vo.setColorMode(request.getColorMode());
        vo.setPreset(request.getPreset());
        vo.setBorderRadius(request.getBorderRadius());
        try {
            return objectMapper.writeValueAsString(vo);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("序列化偏好设置失败", e);
        }
    }
}
