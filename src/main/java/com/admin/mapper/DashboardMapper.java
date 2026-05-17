package com.admin.mapper;

import com.admin.vo.DashboardStatsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DashboardMapper {

    @Select("SELECT COUNT(*) FROM sys_user")
    long countUsers();

    @Select("SELECT COUNT(*) FROM sys_user WHERE status = 1")
    long countUsersEnabled();

    @Select("SELECT COUNT(*) FROM sys_role")
    long countRoles();

    @Select("SELECT COUNT(*) FROM sys_dept")
    long countDepts();

    @Select("SELECT COUNT(*) FROM sys_permission")
    long countPermissions();

    @Select("SELECT DATE_FORMAT(DATE(created_at), '%m-%d') AS name, COUNT(*) AS value " +
            "FROM sys_user WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            "GROUP BY DATE(created_at) ORDER BY DATE(created_at)")
    List<DashboardStatsVO.NameValueVO> selectUserTrend();

    @Select("SELECT IFNULL(d.dept_name, '未分配') AS name, COUNT(u.id) AS value " +
            "FROM sys_user u LEFT JOIN sys_dept d ON u.dept_id = d.id " +
            "GROUP BY u.dept_id, d.dept_name ORDER BY value DESC")
    List<DashboardStatsVO.NameValueVO> selectUsersByDept();

    @Select("SELECT r.role_name AS name, COUNT(ur.user_id) AS value " +
            "FROM sys_role r LEFT JOIN sys_user_role ur ON r.id = ur.role_id " +
            "GROUP BY r.id, r.role_name ORDER BY value DESC")
    List<DashboardStatsVO.NameValueVO> selectUsersByRole();
}
