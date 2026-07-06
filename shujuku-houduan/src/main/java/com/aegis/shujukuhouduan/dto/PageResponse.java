package com.aegis.shujukuhouduan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应包装类
 * 将Spring Data的Page对象转为前端友好的格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    /** 当前页的数据列表 */
    private List<T> content;

    /** 当前页码（从0开始） */
    private int page;

    /** 每页条数 */
    private int size;

    /** 总记录数 */
    private long totalElements;

    /** 总页数 */
    private int totalPages;
}
