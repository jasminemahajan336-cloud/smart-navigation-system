import { useState, useEffect } from "react";

const API = "http://localhost:8080/api/navigation";

export default function NavigationForm() {
  const [locations, setLocations] = useState([]);
  const [source, setSource] = useState("");
  const [destination, setDestination] = useState("");
  const [algorithm, setAlgorithm] = useState("bfs");
  const [mode, setMode] = useState("MIXED");
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetch(`${API}/locations`)
      .then((r) => r.json())
      .then((d) => { setLocations(d); setSource(d[0]); setDestination(d[1]); })
      .catch(() => setError("Failed to load campus locations."));
  }, []);

  const handleSearch = async (e) => {
    e.preventDefault();
    setError(""); setResult(null);
    if (source === destination) { setError("Source and destination cannot be the same."); return; }
    setLoading(true);
    try {
      const url = algorithm === "bfs"
        ? `${API}/bfs?source=${encodeURIComponent(source)}&destination=${encodeURIComponent(destination)}`
        : `${API}/dijkstra?source=${encodeURIComponent(source)}&destination=${encodeURIComponent(destination)}&mode=${mode}`;
      const res = await fetch(url);
      const data = await res.json();
      if (data.hops === -1) setError(data.message);
      else setResult(data);
    } catch { setError("Could not connect to the server."); }
    finally { setLoading(false); }
  };

  return (
    <div style={s.container}>
      <h1 style={s.title}>🏫 GEU Campus Navigation</h1>
      <p style={s.subtitle}>Find your path using BFS (min hops) or Dijkstra (min distance)</p>

      {/* Algorithm Toggle */}
      <div style={s.toggleRow}>
        {["bfs", "dijkstra"].map((alg) => (
          <button key={alg} onClick={() => { setAlgorithm(alg); setResult(null); }}
            style={{ ...s.toggleBtn, ...(algorithm === alg ? s.toggleActive : {}) }}>
            {alg === "bfs" ? "🔵 BFS – Min Hops" : "🟢 Dijkstra – Min Distance"}
          </button>
        ))}
      </div>

      <form onSubmit={handleSearch} style={s.form}>
        <div style={s.row}>
          <label style={s.label}>Source</label>
          <select value={source} onChange={(e) => setSource(e.target.value)} style={s.select}>
            {locations.map((l) => <option key={l} value={l}>{l}</option>)}
          </select>
        </div>
        <div style={s.row}>
          <label style={s.label}>Destination</label>
          <select value={destination} onChange={(e) => setDestination(e.target.value)} style={s.select}>
            {locations.map((l) => <option key={l} value={l}>{l}</option>)}
          </select>
        </div>

        {/* Mode selector — only for Dijkstra */}
        {algorithm === "dijkstra" && (
          <div style={s.row}>
            <label style={s.label}>Route Mode</label>
            <div style={s.modeRow}>
              {["MIXED", "INDOOR", "OUTDOOR"].map((m) => (
                <button type="button" key={m} onClick={() => setMode(m)}
                  style={{ ...s.modeBtn, ...(mode === m ? s.modeActive : {}) }}>
                  {m === "MIXED" ? "🔀 Mixed" : m === "INDOOR" ? "🏠 Indoor" : "🌳 Outdoor"}
                </button>
              ))}
            </div>
          </div>
        )}

        <button type="submit" style={s.button} disabled={loading}>
          {loading ? "Searching..." : "Find Path"}
        </button>
      </form>

      {error && <div style={s.error}>{error}</div>}

      {result && (
        <div style={s.result}>
          <h2 style={s.resultTitle}>
            {algorithm === "bfs" ? "🔵 Shortest Path (Min Hops)" : "🟢 Shortest Path (Min Distance)"}
          </h2>
          <div style={s.pathList}>
            {result.path.map((node, i) => (
              <div key={i}>
                <div style={s.pathStep}>
                  <span style={s.stepBadge}>{i + 1}</span>
                  <span style={s.stepLabel}>{node}</span>
                </div>
                {i < result.path.length - 1 && <div style={s.connector}>↓</div>}
              </div>
            ))}
          </div>
          <div style={s.stats}>
            <span>Total Hops: <strong>{result.hops}</strong></span>
            {result.distance > 0 && (
              <span style={{ marginLeft: 20 }}>Distance: <strong>{result.distance} units</strong></span>
            )}
            {result.mode && algorithm === "dijkstra" && (
              <span style={{ marginLeft: 20 }}>Mode: <strong>{result.mode}</strong></span>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

const s = {
  container: { maxWidth: 720, margin: "40px auto", padding: 32, fontFamily: "Segoe UI, sans-serif", background: "#f9fafb", borderRadius: 12, boxShadow: "0 4px 20px rgba(0,0,0,0.08)" },
  title: { margin: 0, fontSize: 26, color: "#1e293b" },
  subtitle: { color: "#64748b", marginTop: 6, marginBottom: 20 },
  toggleRow: { display: "flex", gap: 10, marginBottom: 20 },
  toggleBtn: { flex: 1, padding: "10px 16px", borderRadius: 8, border: "2px solid #e2e8f0", background: "#fff", cursor: "pointer", fontWeight: 600, fontSize: 14, color: "#475569" },
  toggleActive: { border: "2px solid #3b82f6", background: "#eff6ff", color: "#1d4ed8" },
  form: { display: "flex", flexDirection: "column", gap: 16 },
  row: { display: "flex", flexDirection: "column", gap: 4 },
  label: { fontWeight: 600, color: "#334155", fontSize: 14 },
  select: { padding: "10px 12px", borderRadius: 8, border: "1px solid #cbd5e1", fontSize: 15, background: "#fff" },
  modeRow: { display: "flex", gap: 8 },
  modeBtn: { flex: 1, padding: "8px", borderRadius: 8, border: "2px solid #e2e8f0", background: "#fff", cursor: "pointer", fontWeight: 600, fontSize: 13, color: "#475569" },
  modeActive: { border: "2px solid #10b981", background: "#ecfdf5", color: "#065f46" },
  button: { padding: 12, background: "#3b82f6", color: "#fff", border: "none", borderRadius: 8, fontSize: 16, cursor: "pointer", fontWeight: 600, marginTop: 4 },
  error: { marginTop: 20, padding: 12, background: "#fee2e2", color: "#b91c1c", borderRadius: 8 },
  result: { marginTop: 24, padding: 20, background: "#ecfdf5", borderRadius: 10, border: "1px solid #6ee7b7" },
  resultTitle: { margin: "0 0 16px", color: "#065f46", fontSize: 17 },
  pathList: { display: "flex", flexDirection: "column", gap: 6 },
  pathStep: { display: "flex", alignItems: "center", gap: 10 },
  stepBadge: { minWidth: 26, height: 26, borderRadius: "50%", background: "#10b981", color: "#fff", fontSize: 12, fontWeight: 700, display: "flex", alignItems: "center", justifyContent: "center" },
  stepLabel: { fontSize: 15, color: "#1e293b", fontWeight: 500 },
  connector: { marginLeft: 12, color: "#10b981", fontSize: 18 },
  stats: { marginTop: 16, color: "#047857", fontSize: 15 },
};
