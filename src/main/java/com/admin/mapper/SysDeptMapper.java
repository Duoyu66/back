package com.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.admin.entity.SysDept;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SysDeptMapper extends BaseMapper<SysDept> {

    @Select("SELECT COUNT(*) FROM sys_user WHERE dept_id = #{deptId}")
    int countUsersByDeptId(@Param("deptId") Long deptId);
}
