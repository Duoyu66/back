package com.admin.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class PermissionIconRequest {
    @NotBlank(message = "图标不能为空")
    @Size(max = 64, message = "图标名称过长")
    private String icon;
}
