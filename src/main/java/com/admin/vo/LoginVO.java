package com.admin.vo;

import lombok.Data;

import java.util.List;

@Data
public class LoginVO {
    private String token;
    private UserInfoVO user;
    private List<String> roles;
    private List<String> permissions;
    private List<MenuVO> menus;
    private UserPreferencesVO preferences;
}
