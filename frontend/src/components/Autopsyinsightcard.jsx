import { useEffect, useState } from "react";
import { nudgeApi } from "../services/api";

/**
 * AutopsyInsightCard
 * -------------------
 * Drop this anywhere on AutopsyPage.jsx.
 * It self-fetches using userId from props.
 *
 * Props:
 *   userId  {number|string}  — from AuthContext
 *
 * Usage in AutopsyPage.jsx:
 *   import AutopsyInsightCard from "../components/AutopsyInsightCard";
 *   ...
 *   <AutopsyInsightCard userId={user.userId} />
 */
export default function AutopsyInsightCard({ userId }) {
  const [insight, setInsight] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!userId) return;
    setLoading(true);
    setError(false);

    nudgeApi
      .getAutopsyInsight(userId)
      .then((res) => {
        setInsight(res.data?.insight ?? res.data);
        setLoading(false);
      })
      .catch(() => {
        setError(true);
        setLoading(false);
      });
  }, [userId]);

  return (
    <div className="ai-insight-card">
      <div className="ai-insight-card__header">
        <span className="ai-insight-card__label">
          <span className="ai-insight-card__icon" aria-hidden="true">✦</span>
          Weekly AI insight
        </span>
        <span className="ai-insight-card__powered">GPT-4.1 Nano</span>
      </div>

      <div className="ai-insight-card__body">
        {loading && (
          <span className="ai-insight-card__dots" aria-label="Loading insight">
            <span /><span /><span />
          </span>
        )}
        {!loading && error && (
          <p className="ai-insight-card__error">
            Couldn't load insight right now — check your OpenAI key in{" "}
            <code>application-local.properties</code>.
          </p>
        )}
        {!loading && !error && insight && (
          <p className="ai-insight-card__text">{insight}</p>
        )}
      </div>

      <style>{`
        .ai-insight-card {
          background: #1a1510;
          border: 1px solid rgba(255, 140, 30, 0.18);
          border-radius: 14px;
          padding: 1.125rem 1.25rem;
          position: relative;
          overflow: hidden;
        }
        .ai-insight-card::before {
          content: '';
          position: absolute;
          top: 0; left: 0; right: 0;
          height: 2px;
          background: linear-gradient(90deg, #ff8c1e 0%, #ffd166 50%, transparent 100%);
          opacity: 0.45;
        }
        .ai-insight-card__header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 0.75rem;
        }
        .ai-insight-card__label {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 12px;
          font-weight: 500;
          color: #ff8c1e;
          letter-spacing: 0.02em;
          text-transform: uppercase;
        }
        .ai-insight-card__icon {
          font-size: 11px;
          animation: spin 8s linear infinite;
          display: inline-block;
        }
        @keyframes spin {
          from { transform: rotate(0deg); }
          to   { transform: rotate(360deg); }
        }
        .ai-insight-card__powered {
          font-size: 11px;
          color: rgba(253, 220, 170, 0.3);
          letter-spacing: 0.03em;
          font-family: 'DM Mono', 'Courier New', monospace;
        }
        .ai-insight-card__body {
          min-height: 48px;
          display: flex;
          align-items: flex-start;
        }
        .ai-insight-card__text {
          font-size: 14px;
          line-height: 1.7;
          color: rgba(253, 220, 170, 0.82);
          margin: 0;
        }
        .ai-insight-card__error {
          font-size: 13px;
          color: rgba(253, 220, 170, 0.4);
          margin: 0;
        }
        .ai-insight-card__error code {
          font-family: 'DM Mono', 'Courier New', monospace;
          font-size: 12px;
          background: rgba(255,140,30,0.1);
          padding: 1px 5px;
          border-radius: 4px;
          color: #ff8c1e;
        }

        /* Loading dots */
        .ai-insight-card__dots {
          display: flex;
          align-items: center;
          gap: 5px;
          padding: 6px 0;
        }
        .ai-insight-card__dots span {
          width: 6px;
          height: 6px;
          border-radius: 50%;
          background: rgba(255, 140, 30, 0.55);
          display: inline-block;
          animation: dotPulse 1.4s ease-in-out infinite;
        }
        .ai-insight-card__dots span:nth-child(2) { animation-delay: 0.2s; }
        .ai-insight-card__dots span:nth-child(3) { animation-delay: 0.4s; }
        @keyframes dotPulse {
          0%, 80%, 100% { opacity: 0.3; transform: scale(0.85); }
          40%           { opacity: 1;   transform: scale(1); }
        }
      `}</style>
    </div>
  );
}
