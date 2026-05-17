package com.admin.vo;

import lombok.Data;

import java.util.List;

@Data
public class MenuVO {
    private Long id;
    private Long parentId;
    private String permCode;
    private String permName;
    private Integer permType;
    private String path;
    private String icon;
    private Integer sortOrder;
    private List<MenuVO> children;
}
