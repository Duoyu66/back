package com.admin.service;

import com.admin.mapper.DashboardMapper;
import com.admin.vo.DashboardStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("MM-dd");

    private final DashboardMapper dashboardMapper;

    public DashboardStatsVO getStats() {
        long userTotal = dashboardMapper.countUsers();
        long userEnabled = dashboardMapper.countUsersEnabled();

        DashboardStatsVO vo = new DashboardStatsVO();
        vo.setUserTotal(userTotal);
        vo.setUserEnabled(userEnabled);
        vo.setUserDisabled(userTotal - userEnabled);
        vo.setRoleCount(dashboardMapper.countRoles());
        vo.setDeptCount(dashboardMapper.countDepts());
        vo.setPermissionCount(dashboardMapper.countPermissions());
        vo.setNoticeTotal(dashboardMapper.countNotices());
        vo.setNoticePublished(dashboardMapper.countNoticesPublished());
        vo.setOperLogToday(dashboardMapper.countOperLogToday());
        vo.setLoginLogToday(dashboardMapper.countLoginLogToday());
        vo.setUserTrend(fillUserTrend(dashboardMapper.selectUserTrend()));
        vo.setUsersByDept(emptyToPlaceholder(dashboardMapper.selectUsersByDept(), "暂无部门数据"));
        vo.setUsersByRole(emptyToPlaceholder(dashboardMapper.selectUsersByRole(), "暂无角色数据"));
        vo.setUserStatus(List.of(
                new DashboardStatsVO.NameValueVO("启用", userEnabled),
                new DashboardStatsVO.NameValueVO("停用", userTotal - userEnabled)
        ));
        vo.setRecentNotices(dashboardMapper.selectRecentNotices());
        vo.setRecentOperLogs(dashboardMapper.selectRecentOperLogs());
        vo.setRecentLoginLogs(dashboardMapper.selectRecentLoginLogs());
        return vo;
    }

    /** 补全近 7 日无数据的日期为 0 */
    private List<DashboardStatsVO.NameValueVO> fillUserTrend(List<DashboardStatsVO.NameValueVO> raw) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            map.put(LocalDate.now().minusDays(i).format(DAY_FMT), 0L);
        }
        if (raw != null) {
            for (DashboardStatsVO.NameValueVO item : raw) {
                if (item.getName() != null && map.containsKey(item.getName())) {
                    map.put(item.getName(), item.getValue() != null ? item.getValue() : 0L);
                }
            }
        }
        List<DashboardStatsVO.NameValueVO> result = new ArrayList<>();
        map.forEach((name, value) -> result.add(new DashboardStatsVO.NameValueVO(name, value)));
        return result;
    }

    private List<DashboardStatsVO.NameValueVO> emptyToPlaceholder(
            List<DashboardStatsVO.NameValueVO> list, String placeholder) {
        if (list == null || list.isEmpty()) {
            return List.of(new DashboardStatsVO.NameValueVO(placeholder, 0L));
        }
        return list;
    }
}
