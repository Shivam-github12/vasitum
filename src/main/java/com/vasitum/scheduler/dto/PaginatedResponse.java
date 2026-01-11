package com.vasitum.scheduler.dto;

import java.util.List;

public class PaginatedResponse<T> {
    private List<T> data;
    private String nextCursor;
    private String prevCursor;
    private boolean hasNext;
    private boolean hasPrev;
    private int size;

    // Constructors
    public PaginatedResponse() {}

    public PaginatedResponse(List<T> data, String nextCursor, String prevCursor, boolean hasNext, boolean hasPrev, int size) {
        this.data = data;
        this.nextCursor = nextCursor;
        this.prevCursor = prevCursor;
        this.hasNext = hasNext;
        this.hasPrev = hasPrev;
        this.size = size;
    }

    // Getters and Setters
    public List<T> getData() { return data; }
    public void setData(List<T> data) { this.data = data; }

    public String getNextCursor() { return nextCursor; }
    public void setNextCursor(String nextCursor) { this.nextCursor = nextCursor; }

    public String getPrevCursor() { return prevCursor; }
    public void setPrevCursor(String prevCursor) { this.prevCursor = prevCursor; }

    public boolean isHasNext() { return hasNext; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }

    public boolean isHasPrev() { return hasPrev; }
    public void setHasPrev(boolean hasPrev) { this.hasPrev = hasPrev; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}