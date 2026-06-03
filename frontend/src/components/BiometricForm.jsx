import { useState } from "react";
import { biometricApi } from "../services/api";

const fields = [
  { key: "sleepHours", label: "Sleep", unit: "hrs", min: 0, max: 24, step: 0.5 },
  { key: "hrvMs", label: "HRV", unit: "ms", min: 0, max: 200, step: 1 },
  { key: "restingHeartRate", label: "Resting HR", unit: "bpm", min: 30, max: 120, step: 1 },
  { key: "steps", label: "Steps", unit: "", min: 0, max: 30000, step: 100 },
  { key: "caloriesBurned", label: "Calories", unit: "kcal", min: 0, max: 5000, step: 10 },
];

export default function BiometricForm({ userId, onSuccess }) {
  const [open, setOpen] = useState(true);
  const [form, setForm] = useState({
    sleepHours: "", hrvMs: "", restingHeartRate: "",
    steps: "", caloriesBurned: "",
  });
  const [preview, setPreview] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleChange = (key, value) => {
    setForm(f => ({ ...f, [key]: value }));
    setPreview(null);
  };

  const handlePreview = async () => {
    setLoading(true); setError(null);
    try {
      const res = await biometricApi.preview(userId, sanitize(form));
      setPreview(res.data);
    } catch {
      setError("Couldn't calculate score. Check your values.");
    } finally { setLoading(false); }
  };

  const handleSubmit = async () => {
    setLoading(true); setError(null);
    try {
      const res = await biometricApi.submit(userId, { ...sanitize(form), source: "MANUAL" });
      onSuccess(res.data);
      setOpen(false);
      setForm({ sleepHours: "", hrvMs: "", restingHeartRate: "", steps: "", caloriesBurned: "" });
      setPreview(null);
    } catch (e) {
      setError(e.response?.data?.message || "Submission failed.");
    } finally { setLoading(false); }
  };

  const sanitize = (f) =>
    Object.fromEntries(
      Object.entries(f).filter(([, v]) => v !== "").map(([k, v]) => [k, Number(v)])
    );

  const energyEmoji = score => ["", "😴", "🥱", "⚡", "💪", "🔥"][score] ?? "⚡";

  return (
    <div style={{
      marginTop: "1rem",
      border: "1px solid #2a2a2a",
      borderRadius: 12,
      overflow: "hidden",
      background: "#1a1a1a"
    }}>
      {/* Header toggle */}
      <button
        onClick={() => setOpen(o => !o)}
        style={{
          width: "100%", padding: "0.75rem 1rem",
          background: "#1a1a1a", border: "none",
          cursor: "pointer", textAlign: "left",
          fontWeight: 600, color: "#ffffff",
          display: "flex", justifyContent: "space-between", alignItems: "center",
          borderBottom: open ? "1px solid #2a2a2a" : "none"
        }}
      >
        <span>🩺 Biometric Check-in</span>
        <span style={{ fontSize: 12, color: "#555555" }}>{open ? "▲ hide" : "▼ expand"}</span>
      </button>

      {open && (
        <div style={{ padding: "1rem", background: "#1a1a1a" }}>
          <p style={{ fontSize: 13, color: "#888888", marginBottom: "1rem", marginTop: 0 }}>
            Enter your biometrics — Ember will calculate your energy score automatically.
          </p>

          {/* Input grid */}
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "0.75rem" }}>
            {fields.map(({ key, label, unit, min, max, step }) => (
              <div key={key}>
                <label style={{ fontSize: 12, color: "#888888", display: "block", marginBottom: 4 }}>
                  {label} {unit && <span style={{ color: "#555555" }}>({unit})</span>}
                </label>
                <input
                  type="number" min={min} max={max} step={step}
                  value={form[key]}
                  onChange={e => handleChange(key, e.target.value)}
                  placeholder={`e.g. ${min + (max - min) / 4}`}
                  style={{
                    width: "100%", padding: "0.4rem 0.6rem",
                    border: "1px solid #2a2a2a",
                    borderRadius: 8, fontSize: 14,
                    boxSizing: "border-box",
                    background: "#252525",
                    color: "#ffffff",
                    outline: "none",
                  }}
                />
              </div>
            ))}
          </div>

          {/* Error */}
          {error && (
            <p style={{ color: "#ef4444", fontSize: 13, marginTop: "0.75rem" }}>{error}</p>
          )}

          {/* Preview result */}
          {preview && (
            <div style={{
              marginTop: "1rem", padding: "0.75rem",
              background: "#1a2a1a",
              border: "1px solid #2a5a2a",
              borderRadius: 10, textAlign: "center"
            }}>
              <p style={{ margin: 0, fontWeight: 600, color: "#66bb6a" }}>
                Predicted energy: {energyEmoji(preview.energyScore)} {preview.energyScore}/5
              </p>
              <p style={{ margin: "4px 0 0", fontSize: 12, color: "#888888" }}>
                Looks right? Hit Confirm to log today's check-in.
              </p>
            </div>
          )}

          {/* Buttons */}
          <div style={{ marginTop: "1rem", display: "flex", gap: "0.5rem" }}>
            {!preview ? (
              <button
                onClick={handlePreview} disabled={loading}
                style={{
                  flex: 1, padding: "0.6rem",
                  background: loading ? "#2a2a2a" : "#FF6B35",
                  color: "#fff", border: "none",
                  borderRadius: 8, cursor: loading ? "not-allowed" : "pointer",
                  fontWeight: 600, fontSize: 14
                }}
              >
                {loading ? "Calculating…" : "Preview Score"}
              </button>
            ) : (
              <button
                onClick={handleSubmit} disabled={loading}
                style={{
                  flex: 1, padding: "0.6rem",
                  background: loading ? "#2a2a2a" : "#2a5a2a",
                  color: loading ? "#888" : "#66bb6a",
                  border: "1px solid #2a5a2a",
                  borderRadius: 8, cursor: loading ? "not-allowed" : "pointer",
                  fontWeight: 600, fontSize: 14
                }}
              >
                {loading ? "Logging…" : "Confirm & Log ✓"}
              </button>
            )}
            <button
              onClick={() => { setOpen(false); setPreview(null); }}
              style={{
                padding: "0.6rem 1rem",
                background: "#252525",
                color: "#888888",
                border: "1px solid #2a2a2a",
                borderRadius: 8, cursor: "pointer"
              }}
            >
              Cancel
            </button>
          </div>
        </div>
      )}
    </div>
  );
}