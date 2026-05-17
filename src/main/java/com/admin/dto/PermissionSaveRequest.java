package com.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PermissionSaveRequest {
    private Long id;
    @NotNull(message = "上级节点不能为空")
    private Long parentId;
    @NotBlank(message = "权限标识不能为空")
    private String permCode;
    @NotBlank(message = "权限名称不能为空")
    private String permName;
    @NotNull(message = "权限类型不能为空")
    private Integer permType;
    private String path;
    private String icon;
    private Integer sortOrder;
    @NotNull(message = "状态不能为空")
    private Integer status;
}
