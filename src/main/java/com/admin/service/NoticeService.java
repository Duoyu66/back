package com.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.admin.common.BusinessException;
import com.admin.common.PageResult;
import com.admin.dto.NoticeSaveRequest;
import com.admin.entity.SysNotice;
import com.admin.mapper.SysNoticeMapper;
import com.admin.util.SecurityUtils;
import com.admin.vo.NoticeInboxVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final SysNoticeMapper noticeMapper;
    private final NoticePushService noticePushService;

    public PageResult<SysNotice> page(int current, int size, String keyword, Integer status) {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysNotice::getTitle, keyword)
                    .or().like(SysNotice::getContent, keyword));
        }
        if (status != null) {
            wrapper.eq(SysNotice::getStatus, status);
        }
        wrapper.orderByDesc(SysNotice::getCreatedAt);
        Page<SysNotice> page = noticeMapper.selectPage(new Page<>(current, size), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    public SysNotice detail(Long id) {
        SysNotice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        return notice;
    }

    public SysNotice publishedDetail(Long id) {
        SysNotice notice = detail(id);
        if (notice.getStatus() == null || notice.getStatus() != 1) {
            throw new BusinessException("公告未发布");
        }
        return notice;
    }

    public void create(NoticeSaveRequest req) {
        SysNotice notice = new SysNotice();
        apply(req, notice);
        notice.setCreatedBy(SecurityUtils.currentUserId());
        if (notice.getStatus() != null && notice.getStatus() == 1) {
            notice.setPublishTime(LocalDateTime.now());
        }
        noticeMapper.insert(notice);
        if (notice.getStatus() != null && notice.getStatus() == 1) {
            noticePushService.pushPublished(notice);
        }
    }

    public void update(NoticeSaveRequest req) {
        SysNotice notice = noticeMapper.selectById(req.getId());
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        int prevStatus = notice.getStatus() != null ? notice.getStatus() : 0;
        apply(req, notice);
        if (notice.getStatus() == 1 && notice.getPublishTime() == null) {
            notice.setPublishTime(LocalDateTime.now());
        }
        noticeMapper.updateById(notice);
        if (notice.getStatus() == 1 && prevStatus != 1) {
            noticePushService.pushPublished(notice);
        }
    }

    public void delete(Long id) {
        noticeMapper.deleteById(id);
    }

    public void publish(Long id) {
        SysNotice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        notice.setStatus(1);
        notice.setPublishTime(LocalDateTime.now());
        noticeMapper.updateById(notice);
        noticePushService.pushPublished(notice);
    }

    public List<NoticeInboxVO> inbox(int limit) {
        return noticeMapper.selectInbox(SecurityUtils.currentUserId(), limit);
    }

    public long unreadCount() {
        return noticeMapper.countUnread(SecurityUtils.currentUserId());
    }

    public void markRead(Long noticeId) {
        SysNotice notice = noticeMapper.selectById(noticeId);
        if (notice == null || notice.getStatus() != 1) {
            throw new BusinessException("公告不存在或未发布");
        }
        noticeMapper.markRead(SecurityUtils.currentUserId(), noticeId);
    }

    public void markAllRead() {
        List<NoticeInboxVO> list = noticeMapper.selectInbox(SecurityUtils.currentUserId(), 500);
        for (NoticeInboxVO item : list) {
            if (Boolean.FALSE.equals(item.getRead())) {
                noticeMapper.markRead(SecurityUtils.currentUserId(), item.getId());
            }
        }
    }

    private void apply(NoticeSaveRequest req, SysNotice notice) {
        if (!StringUtils.hasText(req.getTitle())) {
            throw new BusinessException("标题不能为空");
        }
        if (!StringUtils.hasText(stripHtml(req.getContent()))) {
            throw new BusinessException("内容不能为空");
        }
        notice.setTitle(req.getTitle());
        notice.setContent(req.getContent());
        notice.setNoticeType(req.getNoticeType());
        notice.setStatus(req.getStatus() != null ? req.getStatus() : 0);
    }

    private String stripHtml(String html) {
        if (!StringUtils.hasText(html)) {
            return "";
        }
        return html.replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
