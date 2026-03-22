package com.campus.nav.model;

import java.util.List;

public class PathResult {
    private List<String> path;
    private int hops;
    private String message;

    public PathResult(List<String> path, int hops) {
        this.path = path;
        this.hops = hops;
        this.message = "Path found successfully";
    }

    public PathResult(String errorMessage) {
        this.message = errorMessage;
        this.hops = -1;
    }

    public List<String> getPath() { return path; }
    public int getHops() { return hops; }
    public String getMessage() { return message; }
}
