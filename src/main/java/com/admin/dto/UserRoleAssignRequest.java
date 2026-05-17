package com.admin.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class UserRoleAssignRequest {
    @NotNull(message = "角色列表不能为 null")
    private List<Long> roleIds;
}
