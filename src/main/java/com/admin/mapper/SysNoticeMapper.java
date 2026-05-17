package com.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.admin.entity.SysNotice;
import com.admin.vo.NoticeInboxVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysNoticeMapper extends BaseMapper<SysNotice> {

    @Select("SELECT n.id, n.title, n.content, n.notice_type AS noticeType, n.publish_time AS publishTime, " +
            "(r.user_id IS NOT NULL) AS `read` " +
            "FROM sys_notice n " +
            "LEFT JOIN sys_notice_read r ON n.id = r.notice_id AND r.user_id = #{userId} " +
            "WHERE n.status = 1 ORDER BY n.publish_time DESC LIMIT #{limit}")
    List<NoticeInboxVO> selectInbox(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM sys_notice n " +
            "LEFT JOIN sys_notice_read r ON n.id = r.notice_id AND r.user_id = #{userId} " +
            "WHERE n.status = 1 AND r.user_id IS NULL")
    long countUnread(@Param("userId") Long userId);

    @Insert("INSERT IGNORE INTO sys_notice_read (user_id, notice_id) VALUES (#{userId}, #{noticeId})")
    int markRead(@Param("userId") Long userId, @Param("noticeId") Long noticeId);
}
