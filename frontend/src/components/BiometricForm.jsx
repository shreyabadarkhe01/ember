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
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState({
    sleepHours: "", hrvMs: "", restingHeartRate: "",
    steps: "", caloriesBurned: "",
  });
  const [preview, setPreview] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleChange = (key, value) => {
    setForm(f => ({ ...f, [key]: value }));
    setPreview(null); // clear preview on any change
  };

  const handlePreview = async () => {
    setLoading(true); setError(null);
    try {
      const res = await biometricApi.preview(userId, sanitize(form));
      setPreview(res.data); // { energyScore, breakdown? }
    } catch {
      setError("Couldn't calculate score. Check your values.");
    } finally { setLoading(false); }
  };

  const handleSubmit = async () => {
    setLoading(true); setError(null);
    try {
      const res = await biometricApi.submit(userId, { ...sanitize(form), source: "MANUAL" });
      onSuccess(res.data); // bubble up to Dashboard (same shape as regular check-in)
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

  const energyEmoji = score =>
    ["", "😴", "🥱", "⚡", "💪", "🔥"][score] ?? "⚡";

  return (
    <div style={{ marginTop: "1rem", border: "1px solid #e5e7eb", borderRadius: 12, overflow: "hidden" }}>
      <button
        onClick={() => setOpen(o => !o)}
        style={{ width: "100%", padding: "0.75rem 1rem", background: "#f9fafb",
          border: "none", cursor: "pointer", textAlign: "left", fontWeight: 600,
          display: "flex", justifyContent: "space-between", alignItems: "center" }}
      >
        <span>🩺 Biometric Check-in</span>
        <span style={{ fontSize: 12, color: "#6b7280" }}>{open ? "▲ hide" : "▼ expand"}</span>
      </button>

      {open && (
        <div style={{ padding: "1rem", background: "#fff" }}>
          <p style={{ fontSize: 13, color: "#6b7280", marginBottom: "1rem" }}>
            Enter your biometrics — Ember will calculate your energy score automatically.
          </p>

          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "0.75rem" }}>
            {fields.map(({ key, label, unit, min, max, step }) => (
              <div key={key}>
                <label style={{ fontSize: 12, color: "#374151", display: "block", marginBottom: 4 }}>
                  {label} {unit && <span style={{ color: "#9ca3af" }}>({unit})</span>}
                </label>
                <input
                  type="number" min={min} max={max} step={step}
                  value={form[key]}
                  onChange={e => handleChange(key, e.target.value)}
                  placeholder={`e.g. ${min + (max - min) / 4}`}
                  style={{ width: "100%", padding: "0.4rem 0.6rem", border: "1px solid #d1d5db",
                    borderRadius: 8, fontSize: 14, boxSizing: "border-box" }}
                />
              </div>
            ))}
          </div>

          {error && <p style={{ color: "#ef4444", fontSize: 13, marginTop: "0.75rem" }}>{error}</p>}

          {preview && (
            <div style={{ marginTop: "1rem", padding: "0.75rem", background: "#f0fdf4",
              borderRadius: 10, textAlign: "center" }}>
              <p style={{ margin: 0, fontWeight: 600 }}>
                Predicted energy: {energyEmoji(preview.energyScore)} {preview.energyScore}/5
              </p>
              <p style={{ margin: "4px 0 0", fontSize: 12, color: "#6b7280" }}>
                Looks right? Hit Confirm to log today's check-in.
              </p>
            </div>
          )}

          <div style={{ marginTop: "1rem", display: "flex", gap: "0.5rem" }}>
            {!preview ? (
              <button onClick={handlePreview} disabled={loading}
                style={{ flex: 1, padding: "0.6rem", background: "#6366f1", color: "#fff",
                  border: "none", borderRadius: 8, cursor: "pointer", fontWeight: 600 }}>
                {loading ? "Calculating…" : "Preview Score"}
              </button>
            ) : (
              <button onClick={handleSubmit} disabled={loading}
                style={{ flex: 1, padding: "0.6rem", background: "#10b981", color: "#fff",
                  border: "none", borderRadius: 8, cursor: "pointer", fontWeight: 600 }}>
                {loading ? "Logging…" : "Confirm & Log"}
              </button>
            )}
            <button onClick={() => { setOpen(false); setPreview(null); }}
              style={{ padding: "0.6rem 1rem", background: "#f3f4f6",
                border: "none", borderRadius: 8, cursor: "pointer" }}>
              Cancel
            </button>
          </div>
        </div>
      )}
    </div>
  );
}