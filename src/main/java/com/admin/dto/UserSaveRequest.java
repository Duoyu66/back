package com.admin.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class UserSaveRequest {
    private Long id;
    @NotBlank(message = "用户名不能为空")
    private String username;
    private String password;
    @NotBlank(message = "昵称不能为空")
    private String nickname;
    @Email(message = "邮箱格式不正确")
    private String email;
    private String phone;
    @NotNull(message = "状态不能为空")
    private Integer status;
    private Long deptId;
    private List<Long> roleIds;
}
