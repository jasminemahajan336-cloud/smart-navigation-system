package com.campus.nav.service;

import com.campus.nav.model.PathResult;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BFSService {

    // Campus graph: adjacency list
    private final Map<String, List<String>> graph = new HashMap<>();
    private final List<String> locations;

    public BFSService() {
        // GEU Campus locations based on campus map
        locations = List.of(
            // Entry & Roads
            "GEU Bell Road Entry",
            "GEU Dispensary",
            // Admin & Convention
            "GEU Graphic Era Jubilee Convention Center",
            "GEU President Estate",
            "GEU KP Nautiyal Block",
            // Academic blocks (North/West)
            "GEU CS IT Block",
            "GEU Law Block",
            "GEU Pharma Block",
            // Academic blocks (South/East)
            "GEU B.Tech Block",
            "GEU Civil Engineering Block",
            "GEU Petroleum Block",
            "GEU Chanakya Block",
            "GEU Param Lab",
            "GEU Aryabhatt Computer Center",
            // Library & Sports
            "GEU Library",
            "GEU Basket Ball Court",
            "GEU Indoor Badminton Court",
            "GEU Cafe and Gym",
            // Hostels
            "GEU Lakshmi Bai Girls Hostel",
            "GEU Priyadarshini Hostel",
            "GEU Boys Hostel",
            "GEU Mess"
        );

        for (String loc : locations) {
            graph.put(loc, new ArrayList<>());
        }

        // Bell Road spine (main road running through campus)
        addEdge("GEU Bell Road Entry", "GEU Dispensary");
        addEdge("GEU Bell Road Entry", "GEU CS IT Block");
        addEdge("GEU Bell Road Entry", "GEU Graphic Era Jubilee Convention Center");
        addEdge("GEU Dispensary", "GEU Graphic Era Jubilee Convention Center");
        addEdge("GEU Dispensary", "GEU President Estate");
        addEdge("GEU Dispensary", "GEU Aryabhatt Computer Center");
        addEdge("GEU Dispensary", "GEU Lakshmi Bai Girls Hostel");

        // Convention Center area
        addEdge("GEU Graphic Era Jubilee Convention Center", "GEU CS IT Block");
        addEdge("GEU Graphic Era Jubilee Convention Center", "GEU Pharma Block");

        // CS IT Block area (northwest)
        addEdge("GEU CS IT Block", "GEU Law Block");
        addEdge("GEU Law Block", "GEU Lakshmi Bai Girls Hostel");
        addEdge("GEU Law Block", "GEU Pharma Block");

        // Central campus
        addEdge("GEU President Estate", "GEU KP Nautiyal Block");
        addEdge("GEU President Estate", "GEU Aryabhatt Computer Center");
        addEdge("GEU KP Nautiyal Block", "GEU Chanakya Block");
        addEdge("GEU KP Nautiyal Block", "GEU Petroleum Block");

        // Academic south zone
        addEdge("GEU Aryabhatt Computer Center", "GEU B.Tech Block");
        addEdge("GEU Aryabhatt Computer Center", "GEU Param Lab");
        addEdge("GEU Aryabhatt Computer Center", "GEU Library");
        addEdge("GEU Param Lab", "GEU Petroleum Block");
        addEdge("GEU Param Lab", "GEU Library");
        addEdge("GEU Petroleum Block", "GEU Chanakya Block");
        addEdge("GEU B.Tech Block", "GEU Library");
        addEdge("GEU B.Tech Block", "GEU Civil Engineering Block");
        addEdge("GEU Library", "GEU Basket Ball Court");
        addEdge("GEU Library", "GEU Civil Engineering Block");
        addEdge("GEU Civil Engineering Block", "GEU Indoor Badminton Court");
        addEdge("GEU Basket Ball Court", "GEU Indoor Badminton Court");

        // Hostel & amenities zone (west)
        addEdge("GEU Lakshmi Bai Girls Hostel", "GEU Boys Hostel");
        addEdge("GEU Lakshmi Bai Girls Hostel", "GEU Mess");
        addEdge("GEU Boys Hostel", "GEU Mess");
        addEdge("GEU Boys Hostel", "GEU Cafe and Gym");
        addEdge("GEU Mess", "GEU Cafe and Gym");
        addEdge("GEU Cafe and Gym", "GEU B.Tech Block");
        addEdge("GEU Cafe and Gym", "GEU Indoor Badminton Court");

        // Priyadarshini Hostel (northeast)
        addEdge("GEU Priyadarshini Hostel", "GEU President Estate");
        addEdge("GEU Priyadarshini Hostel", "GEU KP Nautiyal Block");
    }

    private void addEdge(String a, String b) {
        graph.get(a).add(b);
        graph.get(b).add(a);
    }

    public List<String> getLocations() {
        return locations;
    }

    public PathResult findShortestPath(String source, String destination) {
        if (!graph.containsKey(source)) {
            return new PathResult("Invalid source location: " + source);
        }
        if (!graph.containsKey(destination)) {
            return new PathResult("Invalid destination location: " + destination);
        }
        if (source.equals(destination)) {
            return new PathResult(List.of(source), 0);
        }

        // BFS
        Queue<String> queue = new LinkedList<>();
        Map<String, Boolean> visited = new HashMap<>();
        Map<String, String> parent = new HashMap<>();

        queue.add(source);
        visited.put(source, true);
        parent.put(source, null);

        while (!queue.isEmpty()) {
            String current = queue.poll();

            for (String neighbor : graph.get(current)) {
                if (!visited.getOrDefault(neighbor, false)) {
                    visited.put(neighbor, true);
                    parent.put(neighbor, current);
                    queue.add(neighbor);

                    if (neighbor.equals(destination)) {
                        return buildResult(parent, source, destination);
                    }
                }
            }
        }

        return new PathResult("No path found between " + source + " and " + destination);
    }

    private PathResult buildResult(Map<String, String> parent, String source, String destination) {
        List<String> path = new ArrayList<>();
        String current = destination;

        while (current != null) {
            path.add(current);
            current = parent.get(current);
        }

        Collections.reverse(path);
        return new PathResult(path, path.size() - 1);
    }
}
