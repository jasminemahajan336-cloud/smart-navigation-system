package com.campus.nav.controller;

import com.campus.nav.model.PathResult;
import com.campus.nav.service.BFSService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/navigation")
@CrossOrigin(origins = "*")
public class NavigationController {

    private final BFSService bfsService;

    public NavigationController(BFSService bfsService) {
        this.bfsService = bfsService;
    }

    // GET /api/navigation/locations
    @GetMapping("/locations")
    public List<String> getLocations() {
        return bfsService.getLocations();
    }

    // GET /api/navigation/bfs?source=Sai Hostel&destination=SC Bose Hostel
    @GetMapping("/bfs")
    public PathResult findPath(
        @RequestParam String source,
        @RequestParam String destination
    ) {
        return bfsService.findShortestPath(source, destination);
    }
}
