package com.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRolePermissionMapper {

    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    @Insert("<script>INSERT INTO sys_role_permission (role_id, permission_id) VALUES " +
            "<foreach collection='permIds' item='pid' separator=','>(#{roleId}, #{pid})</foreach></script>")
    int insertBatch(@Param("roleId") Long roleId, @Param("permIds") List<Long> permIds);

    @org.apache.ibatis.annotations.Select(
            "SELECT COUNT(*) FROM sys_role_permission WHERE permission_id = #{permissionId}")
    int countByPermissionId(@Param("permissionId") Long permissionId);

    @Delete("DELETE FROM sys_role_permission WHERE permission_id = #{permissionId}")
    int deleteByPermissionId(@Param("permissionId") Long permissionId);
}
