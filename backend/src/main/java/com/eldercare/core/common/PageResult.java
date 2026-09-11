package com.eldercare.core.common;

import com.github.pagehelper.PageInfo;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 通用分页结果封装
 */
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<T> list;
    private long total;
    private int pageNum;
    private int pageSize;
    private int pages;

    public PageResult() {
    }

    public PageResult(List<T> list, long total, int pageNum, int pageSize, int pages) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = pages;
    }

    public static <T> PageResult<T> of(PageInfo<T> pageInfo) {
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(),
                pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getPages());
    }

    public List<T> getList() { return list; }
    public void setList(List<T> list) { this.list = list; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public int getPageNum() { return pageNum; }
    public void setPageNum(int pageNum) { this.pageNum = pageNum; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public int getPages() { return pages; }
    public void setPages(int pages) { this.pages = pages; }
}
