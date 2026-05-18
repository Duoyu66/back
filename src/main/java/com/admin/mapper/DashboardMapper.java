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

    @Select("SELECT DATE_FORMAT(d, '%m-%d') AS name, cnt AS value " +
            "FROM (SELECT DATE(created_at) AS d, COUNT(*) AS cnt FROM sys_user " +
            "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            "GROUP BY DATE(created_at)) t ORDER BY d")
    List<DashboardStatsVO.NameValueVO> selectUserTrend();

    @Select("SELECT COUNT(*) FROM sys_notice")
    long countNotices();

    @Select("SELECT COUNT(*) FROM sys_notice WHERE status = 1")
    long countNoticesPublished();

    @Select("SELECT COUNT(*) FROM sys_oper_log WHERE DATE(created_at) = CURDATE()")
    long countOperLogToday();

    @Select("SELECT COUNT(*) FROM sys_login_log WHERE DATE(created_at) = CURDATE()")
    long countLoginLogToday();

    @Select("SELECT id, title, notice_type AS noticeType, publish_time AS publishTime " +
            "FROM sys_notice WHERE status = 1 ORDER BY publish_time DESC LIMIT 5")
    List<DashboardStatsVO.RecentNoticeVO> selectRecentNotices();

    @Select("SELECT username, module, operation, status, created_at AS createdAt " +
            "FROM sys_oper_log ORDER BY created_at DESC LIMIT 8")
    List<DashboardStatsVO.RecentOperLogVO> selectRecentOperLogs();

    @Select("SELECT username, ip, status, msg, created_at AS createdAt " +
            "FROM sys_login_log ORDER BY created_at DESC LIMIT 8")
    List<DashboardStatsVO.RecentLoginLogVO> selectRecentLoginLogs();

    @Select("SELECT IFNULL(d.dept_name, '未分配') AS name, COUNT(u.id) AS value " +
            "FROM sys_user u LEFT JOIN sys_dept d ON u.dept_id = d.id " +
            "GROUP BY u.dept_id, d.dept_name ORDER BY value DESC")
    List<DashboardStatsVO.NameValueVO> selectUsersByDept();

    @Select("SELECT r.role_name AS name, COUNT(ur.user_id) AS value " +
            "FROM sys_role r LEFT JOIN sys_user_role ur ON r.id = ur.role_id " +
            "GROUP BY r.id, r.role_name ORDER BY value DESC")
    List<DashboardStatsVO.NameValueVO> selectUsersByRole();
}
