import { useState } from "react";

/**
 * NudgeCard
 * -----------
 * Props:
 *   nudge  {string}  — coaching message from OpenAIService.generateNudge()
 *   onDismiss {fn}   — called when user dismisses; parent should set nudge to null
 *
 * Usage in Dashboard.jsx:
 *   {nudge && <NudgeCard nudge={nudge} onDismiss={() => setNudge(null)} />}
 */
export default function NudgeCard({ nudge, onDismiss }) {
  const [exiting, setExiting] = useState(false);

  function handleDismiss() {
    setExiting(true);
    setTimeout(() => onDismiss(), 320);
  }

  if (!nudge) return null;

  return (
    <div
      className={`nudge-card ${exiting ? "nudge-card--exit" : "nudge-card--enter"}`}
      role="status"
      aria-live="polite"
    >
      <div className="nudge-card__header">
        <span className="nudge-card__badge">
          <span className="nudge-card__flame" aria-hidden="true">🔥</span>
          Today's nudge
        </span>
        <button
          className="nudge-card__dismiss"
          onClick={handleDismiss}
          aria-label="Dismiss nudge"
        >
          ✕
        </button>
      </div>

      <p className="nudge-card__message">{nudge}</p>

      <style>{`
        .nudge-card {
          background: #201a12;
          border: 1px solid rgba(255, 140, 30, 0.22);
          border-radius: 14px;
          padding: 1rem 1.125rem;
          margin-top: 1.25rem;
          position: relative;
          overflow: hidden;
        }
        .nudge-card::before {
          content: '';
          position: absolute;
          inset: 0;
          background: linear-gradient(135deg, rgba(255,140,30,0.07) 0%, transparent 55%);
          pointer-events: none;
        }
        .nudge-card--enter {
          animation: nudgeSlideIn 0.32s cubic-bezier(0.22, 1, 0.36, 1) both;
        }
        .nudge-card--exit {
          animation: nudgeSlideOut 0.28s ease-in both;
        }
        @keyframes nudgeSlideIn {
          from { opacity: 0; transform: translateY(-10px); }
          to   { opacity: 1; transform: translateY(0); }
        }
        @keyframes nudgeSlideOut {
          from { opacity: 1; transform: translateY(0); max-height: 200px; }
          to   { opacity: 0; transform: translateY(-8px); max-height: 0; }
        }
        .nudge-card__header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 0.6rem;
        }
        .nudge-card__badge {
          display: flex;
          align-items: center;
          gap: 5px;
          background: rgba(255, 140, 30, 0.12);
          border: 1px solid rgba(255, 140, 30, 0.25);
          border-radius: 20px;
          padding: 3px 10px 3px 8px;
          font-size: 12px;
          font-weight: 500;
          color: #ff8c1e;
          letter-spacing: 0.01em;
        }
        .nudge-card__flame {
          font-size: 13px;
          display: inline-block;
          animation: flicker 2.4s ease-in-out infinite;
        }
        @keyframes flicker {
          0%, 100% { transform: scaleY(1) rotate(-1deg); opacity: 1; }
          40%       { transform: scaleY(1.08) rotate(1deg); opacity: 0.85; }
          70%       { transform: scaleY(0.95) rotate(-0.5deg); opacity: 0.95; }
        }
        .nudge-card__dismiss {
          background: none;
          border: none;
          cursor: pointer;
          color: rgba(253, 220, 170, 0.35);
          font-size: 14px;
          padding: 4px 6px;
          border-radius: 6px;
          line-height: 1;
          transition: background 0.15s, color 0.15s;
        }
        .nudge-card__dismiss:hover {
          background: rgba(255, 140, 30, 0.1);
          color: #ff8c1e;
        }
        .nudge-card__message {
          font-size: 14px;
          line-height: 1.65;
          color: rgba(253, 220, 170, 0.85);
          margin: 0;
        }
      `}</style>
    </div>
  );
}
