package com.campus.nav;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@SpringBootApplication
public class CampusNavApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusNavApplication.class, args);
    }
}

// ── Models ───────────────────────────────────────────────────────────────────
class PathResult {
    private List<String> path;
    private int hops;
    private double distance;
    private String message;
    private String mode;

    PathResult(List<String> path, int hops) {
        this.path = path; this.hops = hops; this.distance = -1;
        this.message = "Path found"; this.mode = "BFS";
    }
    PathResult(List<String> path, double distance, String mode) {
        this.path = path; this.hops = path.size() - 1;
        this.distance = Math.round(distance * 1000.0) / 1000.0;
        this.message = "Path found"; this.mode = mode;
    }
    PathResult(String error) { this.message = error; this.hops = -1; this.distance = -1; }

    public List<String> getPath()    { return path; }
    public int getHops()             { return hops; }
    public double getDistance()      { return distance; }
    public String getMessage()       { return message; }
    public String getMode()          { return mode; }
}

// ── Campus Graph (shared data) ───────────────────────────────────────────────
@Service
class CampusGraph {

    static class Node {
        String name; double x, y; String category;
        Node(String name, String category, double x, double y) {
            this.name = name; this.category = category; this.x = x; this.y = y;
        }
    }

    static class Edge {
        String to; double weight; String type;
        Edge(String to, double weight, String type) {
            this.to = to; this.weight = weight; this.type = type;
        }
    }

    final List<Node> nodes = new ArrayList<>();
    final Map<String, List<Edge>> weightedGraph = new HashMap<>();
    final Map<String, List<String>> unweightedGraph = new HashMap<>();

    CampusGraph() {
        // All GEU campus locations with coordinates
        nodes.add(new Node("GEU Bell Road Entry",                    "Path",     0.45, 0.50));
        nodes.add(new Node("GEU Dispensary",                         "Medical",  0.50, 0.43));
        nodes.add(new Node("GEU Graphic Era Jubilee Convention Center","Building",0.50, 0.55));
        nodes.add(new Node("GEU President Estate",                   "Building", 0.60, 0.42));
        nodes.add(new Node("GEU KP Nautiyal Block",                  "Building", 0.75, 0.45));
        nodes.add(new Node("GEU CS IT Block",                        "Building", 0.10, 0.05));
        nodes.add(new Node("GEU Law Block",                          "Building", 0.20, 0.15));
        nodes.add(new Node("GEU Pharma Block",                       "Building", 0.15, 0.20));
        nodes.add(new Node("GEU B.Tech Block",                       "Building", 0.45, 0.28));
        nodes.add(new Node("GEU Civil Engineering Block",            "Building", 0.30, 0.40));
        nodes.add(new Node("GEU Petroleum Block",                    "Building", 0.78, 0.40));
        nodes.add(new Node("GEU Chanakya Block",                     "Building", 0.70, 0.50));
        nodes.add(new Node("GEU Param Lab",                          "Building", 0.60, 0.37));
        nodes.add(new Node("GEU Aryabhatt Computer Center",          "Building", 0.55, 0.35));
        nodes.add(new Node("GEU Library",                            "Building", 0.35, 0.35));
        nodes.add(new Node("GEU Basket Ball Court",                  "Ground",   0.55, 0.26));
        nodes.add(new Node("GEU Indoor Badminton Court",             "Ground",   0.45, 0.22));
        nodes.add(new Node("GEU Cafe and Gym",                       "Cafe",     0.42, 0.30));
        nodes.add(new Node("GEU Lakshmi Bai Girls Hostel",           "Building", 0.25, 0.40));
        nodes.add(new Node("GEU Priyadarshini Hostel",               "Building", 0.65, 0.48));
        nodes.add(new Node("GEU Boys Hostel",                        "Building", 0.35, 0.28));
        nodes.add(new Node("GEU Mess",                               "Cafe",     0.38, 0.32));

        for (Node n : nodes) {
            weightedGraph.put(n.name, new ArrayList<>());
            unweightedGraph.put(n.name, new ArrayList<>());
        }

        // Auto-connect nodes within distance threshold
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                Node a = nodes.get(i), b = nodes.get(j);
                double d = Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
                if (d < 0.30) {
                    String type = (a.category.equals("Building") && b.category.equals("Building"))
                                  ? "INDOOR" : "OUTDOOR";
                    weightedGraph.get(a.name).add(new Edge(b.name, d, type));
                    weightedGraph.get(b.name).add(new Edge(a.name, d, type));
                    unweightedGraph.get(a.name).add(b.name);
                    unweightedGraph.get(b.name).add(a.name);
                }
            }
        }

        // Explicit BFS edges for logical campus connectivity
        addUnweighted("GEU Bell Road Entry",   "GEU Dispensary");
        addUnweighted("GEU Bell Road Entry",   "GEU CS IT Block");
        addUnweighted("GEU Bell Road Entry",   "GEU Graphic Era Jubilee Convention Center");
        addUnweighted("GEU Dispensary",        "GEU President Estate");
        addUnweighted("GEU Dispensary",        "GEU Aryabhatt Computer Center");
        addUnweighted("GEU Dispensary",        "GEU Lakshmi Bai Girls Hostel");
        addUnweighted("GEU Graphic Era Jubilee Convention Center", "GEU CS IT Block");
        addUnweighted("GEU Graphic Era Jubilee Convention Center", "GEU Pharma Block");
        addUnweighted("GEU CS IT Block",       "GEU Law Block");
        addUnweighted("GEU Law Block",         "GEU Lakshmi Bai Girls Hostel");
        addUnweighted("GEU Law Block",         "GEU Pharma Block");
        addUnweighted("GEU President Estate",  "GEU KP Nautiyal Block");
        addUnweighted("GEU President Estate",  "GEU Aryabhatt Computer Center");
        addUnweighted("GEU KP Nautiyal Block", "GEU Chanakya Block");
        addUnweighted("GEU KP Nautiyal Block", "GEU Petroleum Block");
        addUnweighted("GEU Aryabhatt Computer Center", "GEU B.Tech Block");
        addUnweighted("GEU Aryabhatt Computer Center", "GEU Param Lab");
        addUnweighted("GEU Aryabhatt Computer Center", "GEU Library");
        addUnweighted("GEU Param Lab",         "GEU Petroleum Block");
        addUnweighted("GEU Param Lab",         "GEU Library");
        addUnweighted("GEU Petroleum Block",   "GEU Chanakya Block");
        addUnweighted("GEU B.Tech Block",      "GEU Library");
        addUnweighted("GEU B.Tech Block",      "GEU Civil Engineering Block");
        addUnweighted("GEU Library",           "GEU Basket Ball Court");
        addUnweighted("GEU Library",           "GEU Civil Engineering Block");
        addUnweighted("GEU Civil Engineering Block", "GEU Indoor Badminton Court");
        addUnweighted("GEU Basket Ball Court", "GEU Indoor Badminton Court");
        addUnweighted("GEU Lakshmi Bai Girls Hostel", "GEU Boys Hostel");
        addUnweighted("GEU Lakshmi Bai Girls Hostel", "GEU Mess");
        addUnweighted("GEU Boys Hostel",       "GEU Mess");
        addUnweighted("GEU Boys Hostel",       "GEU Cafe and Gym");
        addUnweighted("GEU Mess",              "GEU Cafe and Gym");
        addUnweighted("GEU Cafe and Gym",      "GEU B.Tech Block");
        addUnweighted("GEU Cafe and Gym",      "GEU Indoor Badminton Court");
        addUnweighted("GEU Priyadarshini Hostel", "GEU President Estate");
        addUnweighted("GEU Priyadarshini Hostel", "GEU KP Nautiyal Block");
    }

    private void addUnweighted(String a, String b) {
        if (!unweightedGraph.get(a).contains(b)) unweightedGraph.get(a).add(b);
        if (!unweightedGraph.get(b).contains(a)) unweightedGraph.get(b).add(a);
    }

    List<String> getLocations() {
        List<String> names = new ArrayList<>();
        for (Node n : nodes) names.add(n.name);
        return names;
    }
}

// ── BFS Service ──────────────────────────────────────────────────────────────
@Service
class BFSService {
    private final CampusGraph campus;
    BFSService(CampusGraph campus) { this.campus = campus; }

    List<String> getLocations() { return campus.getLocations(); }

    PathResult findShortestPath(String source, String destination) {
        Map<String, List<String>> graph = campus.unweightedGraph;
        if (!graph.containsKey(source))      return new PathResult("Invalid source: " + source);
        if (!graph.containsKey(destination)) return new PathResult("Invalid destination: " + destination);
        if (source.equals(destination))      return new PathResult(List.of(source), 0);

        Queue<String> queue = new LinkedList<>();
        Map<String, Boolean> visited = new HashMap<>();
        Map<String, String> parent = new HashMap<>();

        queue.add(source); visited.put(source, true); parent.put(source, null);

        while (!queue.isEmpty()) {
            String cur = queue.poll();
            for (String nb : graph.get(cur)) {
                if (!visited.getOrDefault(nb, false)) {
                    visited.put(nb, true); parent.put(nb, cur); queue.add(nb);
                    if (nb.equals(destination)) return buildPath(parent, destination);
                }
            }
        }
        return new PathResult("No path found between " + source + " and " + destination);
    }

    private PathResult buildPath(Map<String, String> parent, String dest) {
        List<String> path = new ArrayList<>();
        for (String n = dest; n != null; n = parent.get(n)) path.add(n);
        Collections.reverse(path);
        return new PathResult(path, path.size() - 1);
    }
}

// ── Dijkstra Service ─────────────────────────────────────────────────────────
@Service
class DijkstraService {
    private final CampusGraph campus;
    DijkstraService(CampusGraph campus) { this.campus = campus; }

    PathResult findShortestPath(String source, String destination, String mode) {
        Map<String, List<CampusGraph.Edge>> graph = campus.weightedGraph;
        if (!graph.containsKey(source))      return new PathResult("Invalid source: " + source);
        if (!graph.containsKey(destination)) return new PathResult("Invalid destination: " + destination);
        if (source.equals(destination))      return new PathResult(List.of(source), 0.0, mode);

        Map<String, Double> dist = new HashMap<>();
        Map<String, String> parent = new HashMap<>();
        for (String n : graph.keySet()) dist.put(n, Double.MAX_VALUE);
        dist.put(source, 0.0);

        PriorityQueue<double[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[1]));
        Map<Integer, String> idxToName = new HashMap<>();
        Map<String, Integer> nameToIdx = new HashMap<>();
        List<String> keys = new ArrayList<>(graph.keySet());
        for (int i = 0; i < keys.size(); i++) { idxToName.put(i, keys.get(i)); nameToIdx.put(keys.get(i), i); }

        pq.add(new double[]{nameToIdx.get(source), 0.0});
        parent.put(source, null);

        while (!pq.isEmpty()) {
            double[] cur = pq.poll();
            String u = idxToName.get((int) cur[0]);
            for (CampusGraph.Edge e : graph.getOrDefault(u, new ArrayList<>())) {
                if (mode.equalsIgnoreCase("INDOOR")  && e.type.equals("OUTDOOR")) continue;
                if (mode.equalsIgnoreCase("OUTDOOR") && e.type.equals("INDOOR"))  continue;
                double nd = dist.get(u) + e.weight;
                if (nd < dist.getOrDefault(e.to, Double.MAX_VALUE)) {
                    dist.put(e.to, nd); parent.put(e.to, u);
                    pq.add(new double[]{nameToIdx.getOrDefault(e.to, -1), nd});
                }
            }
        }

        if (dist.get(destination) == Double.MAX_VALUE)
            return new PathResult("No " + mode + " path found between " + source + " and " + destination);

        List<String> path = new ArrayList<>();
        for (String n = destination; n != null; n = parent.get(n)) path.add(n);
        Collections.reverse(path);
        return new PathResult(path, dist.get(destination), mode.toUpperCase());
    }
}

// ── Controller ───────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/navigation")
@CrossOrigin(origins = "*")
class NavigationController {
    private final BFSService bfs;
    private final DijkstraService dijkstra;

    NavigationController(BFSService bfs, DijkstraService dijkstra) {
        this.bfs = bfs; this.dijkstra = dijkstra;
    }

    @GetMapping("/locations")
    public List<String> locations() { return bfs.getLocations(); }

    @GetMapping("/bfs")
    public PathResult bfs(@RequestParam String source, @RequestParam String destination) {
        return bfs.findShortestPath(source, destination);
    }

    @GetMapping("/dijkstra")
    public PathResult dijkstra(
        @RequestParam String source,
        @RequestParam String destination,
        @RequestParam(defaultValue = "MIXED") String mode
    ) {
        return dijkstra.findShortestPath(source, destination, mode);
    }
}
