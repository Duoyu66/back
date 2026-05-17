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
        vo.setUserTrend(fillUserTrend(dashboardMapper.selectUserTrend()));
        vo.setUsersByDept(dashboardMapper.selectUsersByDept());
        vo.setUsersByRole(dashboardMapper.selectUsersByRole());
        vo.setUserStatus(List.of(
                new DashboardStatsVO.NameValueVO("启用", userEnabled),
                new DashboardStatsVO.NameValueVO("停用", userTotal - userEnabled)
        ));
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
}
