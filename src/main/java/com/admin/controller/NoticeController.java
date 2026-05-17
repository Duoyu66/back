package com.admin.controller;

import com.admin.annotation.OperLog;
import com.admin.common.PageResult;
import com.admin.common.R;
import com.admin.dto.NoticeSaveRequest;
import com.admin.entity.SysNotice;
import com.admin.service.NoticeService;
import com.admin.vo.NoticeInboxVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    @PreAuthorize("hasAuthority('sys:notice:list')")
    public R<PageResult<SysNotice>> page(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return R.ok(noticeService.page(current, size, keyword, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:notice:list')")
    public R<SysNotice> detail(@PathVariable Long id) {
        return R.ok(noticeService.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sys:notice:add')")
    @OperLog(module = "公告管理", operation = "新增公告")
    public R<Void> create(@Validated @RequestBody NoticeSaveRequest request) {
        noticeService.create(request);
        return R.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:notice:edit')")
    @OperLog(module = "公告管理", operation = "编辑公告")
    public R<Void> update(@PathVariable Long id, @Validated @RequestBody NoticeSaveRequest request) {
        request.setId(id);
        noticeService.update(request);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:notice:delete')")
    @OperLog(module = "公告管理", operation = "删除公告")
    public R<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return R.ok();
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('sys:notice:publish')")
    @OperLog(module = "公告管理", operation = "发布公告")
    public R<Void> publish(@PathVariable Long id) {
        noticeService.publish(id);
        return R.ok();
    }

    @GetMapping("/published/{id}")
    public R<SysNotice> publishedDetail(@PathVariable Long id) {
        return R.ok(noticeService.publishedDetail(id));
    }

    @GetMapping("/inbox")
    public R<List<NoticeInboxVO>> inbox(@RequestParam(defaultValue = "20") int limit) {
        return R.ok(noticeService.inbox(limit));
    }

    @GetMapping("/unread-count")
    public R<Map<String, Long>> unreadCount() {
        return R.ok(Map.of("count", noticeService.unreadCount()));
    }

    @PostMapping("/{id}/read")
    public R<Void> markRead(@PathVariable Long id) {
        noticeService.markRead(id);
        return R.ok();
    }

    @PostMapping("/read-all")
    public R<Void> markAllRead() {
        noticeService.markAllRead();
        return R.ok();
    }
}
