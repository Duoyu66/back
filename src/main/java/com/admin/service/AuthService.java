package com.admin.service;

import com.admin.dto.LoginRequest;
import com.admin.dto.PasswordRequest;
import com.admin.dto.ProfileUpdateRequest;
import com.admin.dto.RegisterRequest;
import com.admin.dto.UserPreferencesRequest;
import com.admin.dto.VerifyPasswordRequest;
import com.admin.entity.SysRole;
import com.admin.entity.SysUser;
import com.admin.mapper.SysRoleMapper;
import com.admin.mapper.SysUserMapper;
import com.admin.mapper.SysUserRoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.admin.monitor.OnlineUserRegistry;
import com.admin.security.JwtUtils;
import com.admin.security.LoginUser;
import com.admin.util.HttpUtils;
import com.admin.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import com.admin.util.PreferencesUtils;
import com.admin.vo.LoginVO;
import com.admin.vo.MenuVO;
import com.admin.vo.UserInfoVO;
import com.admin.vo.UserPreferencesVO;
import com.admin.common.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PermissionService permissionService;
    private final PasswordEncoder passwordEncoder;
    private final OnlineUserRegistry onlineUserRegistry;
    private final PreferencesUtils preferencesUtils;

    private static final String GUEST_ROLE_CODE = "GUEST";

    @Transactional
    public LoginVO register(RegisterRequest request, HttpServletRequest httpRequest) {
        long exists = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (exists > 0) {
            throw new BusinessException("用户名已被注册");
        }

        SysRole guestRole = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, GUEST_ROLE_CODE)
                .eq(SysRole::getStatus, 1));
        if (guestRole == null) {
            throw new BusinessException("游客角色未配置，请联系管理员");
        }

        String nickname = StringUtils.hasText(request.getNickname())
                ? request.getNickname().trim()
                : request.getUsername();

        SysUser user = new SysUser();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(nickname);
        user.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null);
        user.setStatus(1);
        user.setAvatar(nickname.substring(0, 1).toUpperCase());
        userMapper.insert(user);
        userRoleMapper.insertBatch(user.getId(), List.of(guestRole.getId()));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(user.getUsername());
        loginRequest.setPassword(request.getPassword());
        return login(loginRequest, httpRequest);
    }

    public LoginVO login(LoginRequest request, HttpServletRequest httpRequest) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        LoginUser loginUser = (LoginUser) auth.getPrincipal();
        SysUser user = loginUser.getSysUser();
        List<String> roles = userMapper.selectRoleCodesByUserId(user.getId());
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), roles, sessionId);
        onlineUserRegistry.register(sessionId, loginUser, httpRequest);
        return buildLoginVO(token, loginUser);
    }

    public void logoutCurrentSession() {
        HttpServletRequest request = HttpUtils.currentRequest();
        String token = resolveBearerToken(request);
        if (token != null && jwtUtils.validate(token)) {
            String sessionId = jwtUtils.getSessionId(token);
            onlineUserRegistry.remove(sessionId);
        }
    }

    private String resolveBearerToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    public LoginVO getCurrentUserInfo() {
        LoginUser loginUser = SecurityUtils.currentUser();
        return buildLoginVO(null, loginUser);
    }

    public void updateProfile(ProfileUpdateRequest request) {
        Long userId = SecurityUtils.currentUserId();
        SysUser user = userMapper.selectById(userId);
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        userMapper.updateById(user);
    }

    public void updatePassword(PasswordRequest request) {
        LoginUser loginUser = SecurityUtils.currentUser();
        SysUser user = userMapper.selectById(loginUser.getSysUser().getId());
        if (StringUtils.hasText(request.getOldPassword())
                && !passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    public UserPreferencesVO getPreferences() {
        SysUser user = userMapper.selectById(SecurityUtils.currentUserId());
        return preferencesUtils.parse(user.getPreferences());
    }

    public UserPreferencesVO updatePreferences(UserPreferencesRequest request) {
        Long userId = SecurityUtils.currentUserId();
        SysUser user = userMapper.selectById(userId);
        user.setPreferences(preferencesUtils.toJson(request));
        userMapper.updateById(user);
        return preferencesUtils.parse(user.getPreferences());
    }

    public void verifyPassword(VerifyPasswordRequest request) {
        SysUser user = userMapper.selectById(SecurityUtils.currentUserId());
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }
    }

    private LoginVO buildLoginVO(String token, LoginUser loginUser) {
        SysUser user = loginUser.getSysUser();
        List<String> roles = userMapper.selectRoleCodesByUserId(user.getId());
        List<String> perms = loginUser.getPermCodes();
        List<MenuVO> menus = permissionService.getMenuTreeByUserId(user.getId());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setRoles(roles);
        vo.setPermissions(perms);
        vo.setMenus(menus);
        vo.setUser(toUserInfo(user, roles));
        vo.setPreferences(preferencesUtils.parse(user.getPreferences()));
        return vo;
    }

    private UserInfoVO toUserInfo(SysUser user, List<String> roles) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar() != null ? user.getAvatar()
                : user.getNickname().substring(0, 1).toUpperCase());
        vo.setRoleName(roles.stream().collect(Collectors.joining(", ")));
        return vo;
    }
}
