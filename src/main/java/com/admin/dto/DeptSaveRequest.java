package com.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeptSaveRequest {
    private Long id;
    @NotNull(message = "上级部门不能为空")
    private Long parentId;
    @NotBlank(message = "部门名称不能为空")
    private String deptName;
    @NotBlank(message = "部门编码不能为空")
    private String deptCode;
    private String leader;
    private String phone;
    private Integer sortOrder;
    @NotNull(message = "状态不能为空")
    private Integer status;
}
