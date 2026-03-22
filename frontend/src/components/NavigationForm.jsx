import { useState, useEffect } from "react";

const API = "http://localhost:8080/api/navigation";

export default function NavigationForm() {
  const [locations, setLocations] = useState([]);
  const [source, setSource] = useState("");
  const [destination, setDestination] = useState("");
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetch(`${API}/locations`)
      .then((res) => res.json())
      .then((data) => {
        setLocations(data);
        setSource(data[0]);
        setDestination(data[1]);
      })
      .catch(() => setError("Failed to load campus locations."));
  }, []);

  const handleSearch = async (e) => {
    e.preventDefault();
    setError("");
    setResult(null);

    if (source === destination) {
      setError("Source and destination cannot be the same.");
      return;
    }

    setLoading(true);
    try {
      const res = await fetch(
        `${API}/bfs?source=${encodeURIComponent(source)}&destination=${encodeURIComponent(destination)}`
      );
      const data = await res.json();

      if (data.hops === -1) {
        setError(data.message);
      } else {
        setResult(data);
      }
    } catch {
      setError("Could not connect to the server.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <h1 style={styles.title}>🏫 Campus Navigation</h1>
      <p style={styles.subtitle}>Find the shortest path (minimum hops) using BFS</p>

      <form onSubmit={handleSearch} style={styles.form}>
        <div style={styles.row}>
          <label style={styles.label}>Source</label>
          <select
            value={source}
            onChange={(e) => setSource(e.target.value)}
            style={styles.select}
          >
            {locations.map((loc) => (
              <option key={loc} value={loc}>{loc}</option>
            ))}
          </select>
        </div>

        <div style={styles.row}>
          <label style={styles.label}>Destination</label>
          <select
            value={destination}
            onChange={(e) => setDestination(e.target.value)}
            style={styles.select}
          >
            {locations.map((loc) => (
              <option key={loc} value={loc}>{loc}</option>
            ))}
          </select>
        </div>

        <button type="submit" style={styles.button} disabled={loading}>
          {loading ? "Searching..." : "Find Path"}
        </button>
      </form>

      {error && <div style={styles.error}>{error}</div>}

      {result && (
        <div style={styles.result}>
          <h2 style={styles.resultTitle}>Shortest Path (Minimum Hops)</h2>
          <div style={styles.pathList}>
            {result.path.map((node, i) => (
              <div key={i}>
                <div style={styles.pathStep}>
                  <span style={styles.stepBadge}>{i + 1}</span>
                  <span style={styles.stepLabel}>{node}</span>
                </div>
                {i < result.path.length - 1 && (
                  <div style={styles.connector}>↓</div>
                )}
              </div>
            ))}
          </div>
          <p style={styles.hops}>Total Hops: <strong>{result.hops}</strong></p>
        </div>
      )}
    </div>
  );
}

const styles = {
  container: {
    maxWidth: 720,
    margin: "40px auto",
    padding: "32px",
    fontFamily: "Segoe UI, sans-serif",
    background: "#f9fafb",
    borderRadius: 12,
    boxShadow: "0 4px 20px rgba(0,0,0,0.08)",
  },
  title: { margin: 0, fontSize: 26, color: "#1e293b" },
  subtitle: { color: "#64748b", marginTop: 6, marginBottom: 24 },
  form: { display: "flex", flexDirection: "column", gap: 16 },
  row: { display: "flex", flexDirection: "column", gap: 4 },
  label: { fontWeight: 600, color: "#334155", fontSize: 14 },
  select: {
    padding: "10px 12px",
    borderRadius: 8,
    border: "1px solid #cbd5e1",
    fontSize: 15,
    background: "#fff",
  },
  button: {
    padding: "12px",
    background: "#3b82f6",
    color: "#fff",
    border: "none",
    borderRadius: 8,
    fontSize: 16,
    cursor: "pointer",
    fontWeight: 600,
    marginTop: 4,
  },
  error: {
    marginTop: 20,
    padding: 12,
    background: "#fee2e2",
    color: "#b91c1c",
    borderRadius: 8,
  },
  result: {
    marginTop: 24,
    padding: 20,
    background: "#ecfdf5",
    borderRadius: 10,
    border: "1px solid #6ee7b7",
  },
  resultTitle: { margin: "0 0 16px", color: "#065f46", fontSize: 17 },
  pathList: { display: "flex", flexDirection: "column", gap: 6 },
  pathStep: {
    display: "flex",
    alignItems: "center",
    gap: 10,
  },
  stepBadge: {
    minWidth: 26,
    height: 26,
    borderRadius: "50%",
    background: "#10b981",
    color: "#fff",
    fontSize: 12,
    fontWeight: 700,
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
  },
  stepLabel: { fontSize: 15, color: "#1e293b", fontWeight: 500 },
  connector: { marginLeft: 12, color: "#10b981", fontSize: 18, lineHeight: 1 },
  hops: { marginTop: 16, color: "#047857", fontSize: 15 },
};
