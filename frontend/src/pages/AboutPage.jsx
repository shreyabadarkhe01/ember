import { Link } from 'react-router-dom';

export default function AboutPage() {
  return (
    <>
      <div className="about-page">

        {/* ── Minimal nav ── */}
        <header className="about-header">
          <Link to="/about" className="about-brand">
            <span className="about-brand__flame">🔥</span>
            <span className="about-brand__name">Ember</span>
          </Link>
          <Link to="/dashboard" className="about-nav-link">Go to app →</Link>
        </header>

        {/* ── Hero ── */}
        <section className="about-hero">
          <div className="about-hero__glow" aria-hidden="true" />
          <p className="about-hero__eyebrow">Built for real humans</p>
          <h1 className="about-hero__title">
            Habits that<br />
            <span className="about-hero__title--accent">bend, not break</span>
          </h1>
          <p className="about-hero__sub">
            Most habit trackers punish you for off days. Ember adapts to your energy —
            so you always show up, even when showing up means doing less.
          </p>
          <Link to="/dashboard" className="about-cta">Start tracking →</Link>
        </section>

        {/* ── How it works ── */}
        <section className="about-section">
          <h2 className="about-section__title">How Ember works</h2>
          <div className="about-cards">
            <div className="about-card">
              <div className="about-card__icon">⚡</div>
              <h3>Set your energy</h3>
              <p>Each morning, log how you're feeling on a scale of 1–5. No fluff, just a number.</p>
            </div>
            <div className="about-card">
              <div className="about-card__icon">🎯</div>
              <h3>Ember scales your habits</h3>
              <p>Every habit has three versions — Minimal, Lite, and Full. Ember picks the right one for your energy automatically.</p>
            </div>
            <div className="about-card">
              <div className="about-card__icon">🔥</div>
              <h3>Streaks that survive</h3>
              <p>Completing your Minimal version on a rough day still counts. Streaks reward consistency, not perfection.</p>
            </div>
            <div className="about-card">
              <div className="about-card__icon">🧠</div>
              <h3>AI that coaches</h3>
              <p>After check-in, Ember's AI reads your energy, sleep, and streaks — then tells you exactly what to do and in what order.</p>
            </div>
          </div>
        </section>

        {/* ── The idea ── */}
        <section className="about-section about-section--alt">
          <div className="about-story">
            <div className="about-story__label">The idea</div>
            <h2 className="about-story__title">Why we built this</h2>
            <p className="about-story__body">
              Every habit app assumes you wake up the same person every day. You don't.
              Some days you're a machine. Other days getting out of bed is the win.
            </p>
            <p className="about-story__body">
              Ember started from a simple question: what if your habits knew how you were feeling?
              Not a rigid schedule, not a guilt-tripping streak counter — just a system
              that meets you where you are and keeps the flame alive.
            </p>
            <p className="about-story__body">
              Built in Pune. Powered by Java, React, and a lot of coffee. ☕
            </p>
          </div>
          <div className="about-tiers">
            <div className="about-tier about-tier--minimal">
              <span className="about-tier__emoji">😴</span>
              <span className="about-tier__name">Minimal</span>
              <span className="about-tier__desc">Low energy day — just show up</span>
            </div>
            <div className="about-tier about-tier--lite">
              <span className="about-tier__emoji">⚡</span>
              <span className="about-tier__name">Lite</span>
              <span className="about-tier__desc">Normal day — steady progress</span>
            </div>
            <div className="about-tier about-tier--full">
              <span className="about-tier__emoji">🔥</span>
              <span className="about-tier__name">Full</span>
              <span className="about-tier__desc">High energy — go all in</span>
            </div>
          </div>
        </section>

        {/* ── Contact ── */}
        <section className="about-section" id="contact">
          <h2 className="about-section__title">Get in touch</h2>
          <p className="about-contact__sub">
            Questions, feedback, or just want to say hi — reach out.
          </p>
          <div className="about-contact-links">
            <a
              href="https://github.com/shreyabadarkhe01/ember"
              target="_blank"
              rel="noreferrer"
              className="about-contact-link"
            >
              <span>⌥</span> GitHub
            </a>
            <a
              href="mailto:hello@ember.app"
              className="about-contact-link"
            >
              <span>✉</span> Email us
            </a>
          </div>
        </section>

        {/* ── Footer ── */}
        <footer className="about-footer">
          <span className="about-brand">
            <span className="about-brand__flame">🔥</span>
            <span className="about-brand__name">Ember</span>
          </span>
          <span className="about-footer__copy">
            Built with intention. Keep the flame alive.
          </span>
        </footer>
      </div>

      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Sora:wght@300;400;500;600;700&family=DM+Serif+Display:ital@0;1&display=swap');

        * { box-sizing: border-box; margin: 0; padding: 0; }

        .about-page {
          min-height: 100vh;
          background: #0f0d0a;
          color: #fdf0e0;
          font-family: 'Sora', sans-serif;
        }

        /* ── Header ── */
        .about-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 1.25rem 2rem;
          border-bottom: 1px solid rgba(255,140,30,0.1);
          position: sticky;
          top: 0;
          background: rgba(15,13,10,0.9);
          backdrop-filter: blur(12px);
          z-index: 100;
        }
        .about-brand {
          display: flex;
          align-items: center;
          gap: 7px;
          text-decoration: none;
        }
        .about-brand__flame {
          font-size: 20px;
          display: inline-block;
          animation: flicker 2.6s ease-in-out infinite;
        }
        @keyframes flicker {
          0%,100% { transform: scaleY(1) rotate(-1deg); opacity:1; }
          40%      { transform: scaleY(1.1) rotate(1deg); opacity:.85; }
          70%      { transform: scaleY(.95); }
        }
        .about-brand__name {
          font-size: 18px;
          font-weight: 600;
          color: #ff8c1e;
          letter-spacing: .02em;
        }
        .about-nav-link {
          font-size: 13px;
          color: rgba(253,220,170,.55);
          text-decoration: none;
          padding: 6px 14px;
          border: 1px solid rgba(255,140,30,.2);
          border-radius: 7px;
          transition: all .15s;
        }
        .about-nav-link:hover {
          color: #ff8c1e;
          border-color: rgba(255,140,30,.5);
          background: rgba(255,140,30,.07);
        }

        /* ── Hero ── */
        .about-hero {
          position: relative;
          text-align: center;
          padding: 6rem 2rem 5rem;
          overflow: hidden;
        }
        .about-hero__glow {
          position: absolute;
          top: -80px; left: 50%;
          transform: translateX(-50%);
          width: 600px; height: 400px;
          background: radial-gradient(ellipse at center, rgba(255,140,30,.18) 0%, transparent 70%);
          pointer-events: none;
        }
        .about-hero__eyebrow {
          font-size: 12px;
          letter-spacing: .15em;
          text-transform: uppercase;
          color: #ff8c1e;
          margin-bottom: 1.25rem;
          font-weight: 500;
        }
        .about-hero__title {
          font-family: 'DM Serif Display', serif;
          font-size: clamp(2.8rem, 6vw, 5rem);
          line-height: 1.1;
          color: #fdf0e0;
          margin-bottom: 1.5rem;
        }
        .about-hero__title--accent {
          color: #ff8c1e;
          font-style: italic;
        }
        .about-hero__sub {
          max-width: 520px;
          margin: 0 auto 2.5rem;
          font-size: 16px;
          line-height: 1.75;
          color: rgba(253,220,170,.65);
          font-weight: 300;
        }
        .about-cta {
          display: inline-block;
          background: #ff8c1e;
          color: #0f0d0a;
          font-size: 14px;
          font-weight: 600;
          padding: 12px 28px;
          border-radius: 8px;
          text-decoration: none;
          letter-spacing: .02em;
          transition: background .15s, transform .15s;
        }
        .about-cta:hover {
          background: #ffa040;
          transform: translateY(-1px);
        }

        /* ── Sections ── */
        .about-section {
          padding: 5rem 2rem;
          max-width: 960px;
          margin: 0 auto;
        }
        .about-section--alt {
          max-width: 100%;
          background: #1a1510;
          border-top: 1px solid rgba(255,140,30,.08);
          border-bottom: 1px solid rgba(255,140,30,.08);
          padding: 5rem 2rem;
          display: flex;
          gap: 4rem;
          align-items: flex-start;
          justify-content: center;
          flex-wrap: wrap;
        }
        .about-section__title {
          font-family: 'DM Serif Display', serif;
          font-size: 2rem;
          color: #fdf0e0;
          margin-bottom: 2.5rem;
          text-align: center;
        }

        /* ── How it works cards ── */
        .about-cards {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
          gap: 1.25rem;
        }
        .about-card {
          background: #1a1510;
          border: 1px solid rgba(255,140,30,.14);
          border-radius: 14px;
          padding: 1.5rem;
          transition: border-color .2s, transform .2s;
        }
        .about-card:hover {
          border-color: rgba(255,140,30,.35);
          transform: translateY(-3px);
        }
        .about-card__icon {
          font-size: 26px;
          margin-bottom: .85rem;
        }
        .about-card h3 {
          font-size: 15px;
          font-weight: 600;
          color: #fdf0e0;
          margin-bottom: .5rem;
        }
        .about-card p {
          font-size: 13px;
          line-height: 1.65;
          color: rgba(253,220,170,.55);
          font-weight: 300;
        }

        /* ── Story ── */
        .about-story {
          max-width: 480px;
        }
        .about-story__label {
          font-size: 11px;
          letter-spacing: .14em;
          text-transform: uppercase;
          color: #ff8c1e;
          margin-bottom: .75rem;
        }
        .about-story__title {
          font-family: 'DM Serif Display', serif;
          font-size: 2rem;
          color: #fdf0e0;
          margin-bottom: 1.5rem;
        }
        .about-story__body {
          font-size: 14px;
          line-height: 1.8;
          color: rgba(253,220,170,.6);
          font-weight: 300;
          margin-bottom: 1rem;
        }

        /* ── Tiers ── */
        .about-tiers {
          display: flex;
          flex-direction: column;
          gap: .75rem;
          min-width: 240px;
        }
        .about-tier {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 1rem 1.25rem;
          border-radius: 12px;
          border: 1px solid transparent;
        }
        .about-tier--minimal {
          background: rgba(148,163,184,.07);
          border-color: rgba(148,163,184,.15);
        }
        .about-tier--lite {
          background: rgba(245,166,35,.07);
          border-color: rgba(245,166,35,.2);
        }
        .about-tier--full {
          background: rgba(255,140,30,.1);
          border-color: rgba(255,140,30,.25);
        }
        .about-tier__emoji { font-size: 20px; }
        .about-tier__name {
          font-size: 14px;
          font-weight: 600;
          color: #fdf0e0;
          min-width: 60px;
        }
        .about-tier__desc {
          font-size: 12px;
          color: rgba(253,220,170,.5);
        }

        /* ── Contact ── */
        .about-contact__sub {
          text-align: center;
          font-size: 15px;
          color: rgba(253,220,170,.5);
          margin-bottom: 2rem;
          font-weight: 300;
        }
        .about-contact-links {
          display: flex;
          gap: 1rem;
          justify-content: center;
          flex-wrap: wrap;
        }
        .about-contact-link {
          display: flex;
          align-items: center;
          gap: 8px;
          text-decoration: none;
          color: rgba(253,220,170,.65);
          border: 1px solid rgba(255,140,30,.2);
          padding: 10px 20px;
          border-radius: 8px;
          font-size: 14px;
          transition: all .15s;
        }
        .about-contact-link:hover {
          color: #ff8c1e;
          border-color: rgba(255,140,30,.5);
          background: rgba(255,140,30,.07);
        }

        /* ── Footer ── */
        .about-footer {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 1.5rem 2rem;
          border-top: 1px solid rgba(255,140,30,.08);
          flex-wrap: wrap;
          gap: 1rem;
        }
        .about-footer__copy {
          font-size: 12px;
          color: rgba(253,220,170,.3);
        }

        @media (max-width: 640px) {
          .about-hero { padding: 4rem 1.25rem 3.5rem; }
          .about-section { padding: 3.5rem 1.25rem; }
          .about-section--alt { flex-direction: column; gap: 2.5rem; padding: 3.5rem 1.25rem; }
          .about-footer { justify-content: center; text-align: center; }
        }
      `}</style>
    </>
  );
}