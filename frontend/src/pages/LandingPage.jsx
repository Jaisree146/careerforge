import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { analyticsApi } from '../services/api';
import { 
  Zap, 
  Sparkles, 
  Briefcase, 
  Users, 
  FileCheck2, 
  TrendingUp, 
  ArrowRight, 
  ShieldCheck, 
  Cpu, 
  BellRing,
  Layers
} from 'lucide-react';

export const LandingPage = () => {
  const [kpis, setKpis] = useState(null);
  const [demandSupply, setDemandSupply] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchAnalytics = async () => {
      try {
        const [kpiData, skillData] = await Promise.all([
          analyticsApi.getPlatformKpis().catch(() => null),
          analyticsApi.getSkillDemandSupply().catch(() => []),
        ]);
        if (kpiData) setKpis(kpiData);
        if (skillData) setDemandSupply(skillData.slice(0, 6));
      } catch (err) {
        console.warn('Analytics API unavailable, showing fallback statistics', err);
      } finally {
        setLoading(false);
      }
    };
    fetchAnalytics();
  }, []);

  return (
    <div className="animate-fade-in" style={{ paddingBottom: '80px' }}>
      {/* Hero Section */}
      <section style={{
        padding: '90px 0 60px 0',
        textAlign: 'center',
        position: 'relative',
      }}>
        <div className="container" style={{ maxWidth: '880px' }}>
          <div style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '8px',
            padding: '6px 16px',
            borderRadius: '9999px',
            background: 'rgba(99, 102, 241, 0.12)',
            border: '1px solid var(--border-glow)',
            color: '#a5b4fc',
            fontSize: '0.85rem',
            fontWeight: 600,
            marginBottom: '24px',
          }}>
            <Sparkles size={16} color="#818cf8" />
            <span>Powered by Google Gemini Generative AI & Java 21</span>
          </div>

          <h1 style={{
            fontSize: 'clamp(2.5rem, 5vw, 4rem)',
            lineHeight: 1.15,
            marginBottom: '20px',
            fontWeight: 800,
          }}>
            Precision AI Hiring & <br />
            <span className="text-gradient">Career Engineering</span> Platform
          </h1>

          <p style={{
            fontSize: 'clamp(1rem, 2vw, 1.25rem)',
            color: 'var(--text-secondary)',
            lineHeight: 1.6,
            marginBottom: '36px',
            maxWidth: '680px',
            margin: '0 auto 36px auto',
          }}>
            Connect candidates and top recruiters through automated PDFBox resume parsing, transparent weighted match scoring, and real-time STOMP WebSocket notifications.
          </p>

          <div style={{
            display: 'flex',
            flexWrap: 'wrap',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '16px',
          }}>
            <Link to="/jobs" className="btn btn-primary btn-lg">
              Explore Active Jobs <ArrowRight size={18} />
            </Link>
            <Link to="/register" className="btn btn-secondary btn-lg">
              Join as Recruiter or Candidate
            </Link>
          </div>
        </div>
      </section>

      {/* Real-time KPI Stats Counter */}
      <section style={{ padding: '20px 0 60px 0' }}>
        <div className="container">
          <div className="grid-cols-4">
            <div className="glass-panel" style={{ padding: '24px', textAlign: 'center' }}>
              <div style={{
                width: '44px',
                height: '44px',
                borderRadius: '12px',
                background: 'rgba(59, 130, 246, 0.15)',
                color: '#60a5fa',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                margin: '0 auto 14px auto',
              }}>
                <Briefcase size={22} />
              </div>
              <div style={{ fontSize: '2rem', fontWeight: 800, color: '#f8fafc' }}>
                {kpis?.totalJobs ?? 12}+
              </div>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
                Active Opportunities
              </div>
            </div>

            <div className="glass-panel" style={{ padding: '24px', textAlign: 'center' }}>
              <div style={{
                width: '44px',
                height: '44px',
                borderRadius: '12px',
                background: 'rgba(16, 185, 129, 0.15)',
                color: '#34d399',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                margin: '0 auto 14px auto',
              }}>
                <FileCheck2 size={22} />
              </div>
              <div style={{ fontSize: '2rem', fontWeight: 800, color: '#f8fafc' }}>
                {kpis?.totalApplications ?? 48}+
              </div>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
                Applications Tracked
              </div>
            </div>

            <div className="glass-panel" style={{ padding: '24px', textAlign: 'center' }}>
              <div style={{
                width: '44px',
                height: '44px',
                borderRadius: '12px',
                background: 'rgba(139, 92, 246, 0.15)',
                color: '#a78bfa',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                margin: '0 auto 14px auto',
              }}>
                <Users size={22} />
              </div>
              <div style={{ fontSize: '2rem', fontWeight: 800, color: '#f8fafc' }}>
                {kpis?.totalCandidates ?? 25}+
              </div>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
                Verified Candidates
              </div>
            </div>

            <div className="glass-panel" style={{ padding: '24px', textAlign: 'center' }}>
              <div style={{
                width: '44px',
                height: '44px',
                borderRadius: '12px',
                background: 'rgba(245, 158, 11, 0.15)',
                color: '#fbbf24',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                margin: '0 auto 14px auto',
              }}>
                <TrendingUp size={22} />
              </div>
              <div style={{ fontSize: '2rem', fontWeight: 800, color: '#f8fafc' }}>
                {kpis?.avgMatchScore ? `${Math.round(kpis.avgMatchScore)}%` : '88%'}
              </div>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
                Average AI Match Precision
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Core Architectural Highlights */}
      <section style={{ padding: '40px 0' }}>
        <div className="container">
          <div style={{ textAlign: 'center', marginBottom: '48px' }}>
            <h2 style={{ fontSize: '2.25rem', marginBottom: '12px' }}>
              Engineered For <span className="text-gradient">Production Resilience</span>
            </h2>
            <p style={{ color: 'var(--text-secondary)', maxWidth: '600px', margin: '0 auto' }}>
              Every feature is built on top of battle-tested enterprise design patterns and live backend endpoints.
            </p>
          </div>

          <div className="grid-cols-3">
            <div className="glass-panel" style={{ padding: '28px' }}>
              <div style={{
                width: '48px',
                height: '48px',
                borderRadius: '12px',
                background: 'rgba(59, 130, 246, 0.15)',
                color: '#60a5fa',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                marginBottom: '20px',
              }}>
                <Sparkles size={24} />
              </div>
              <h3 style={{ fontSize: '1.25rem', marginBottom: '10px' }}>Weighted Matching Engine</h3>
              <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', lineHeight: 1.6 }}>
                Multi-factor scoring algorithm calculating a 60% skills overlap, 25% experience alignment, and 15% profile completeness score with instant feedback.
              </p>
            </div>

            <div className="glass-panel" style={{ padding: '28px' }}>
              <div style={{
                width: '48px',
                height: '48px',
                borderRadius: '12px',
                background: 'rgba(16, 185, 129, 0.15)',
                color: '#34d399',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                marginBottom: '20px',
              }}>
                <FileCheck2 size={24} />
              </div>
              <h3 style={{ fontSize: '1.25rem', marginBottom: '10px' }}>Apache PDFBox Resume Parser</h3>
              <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', lineHeight: 1.6 }}>
                Collision-resistant UUID file naming, path traversal defense, and automatic text extraction feeding structured skills directly to Gemini AI.
              </p>
            </div>

            <div className="glass-panel" style={{ padding: '28px' }}>
              <div style={{
                width: '48px',
                height: '48px',
                borderRadius: '12px',
                background: 'rgba(139, 92, 246, 0.15)',
                color: '#a78bfa',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                marginBottom: '20px',
              }}>
                <BellRing size={24} />
              </div>
              <h3 style={{ fontSize: '1.25rem', marginBottom: '10px' }}>STOMP WebSockets Real-Time</h3>
              <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', lineHeight: 1.6 }}>
                Instant browser notification delivery when recruiters shortlist an application, change interview status, or broadcast announcements.
              </p>
            </div>

            <div className="glass-panel" style={{ padding: '28px' }}>
              <div style={{
                width: '48px',
                height: '48px',
                borderRadius: '12px',
                background: 'rgba(245, 158, 11, 0.15)',
                color: '#fbbf24',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                marginBottom: '20px',
              }}>
                <ShieldCheck size={24} />
              </div>
              <h3 style={{ fontSize: '1.25rem', marginBottom: '10px' }}>Bucket4j Rate Limiting</h3>
              <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', lineHeight: 1.6 }}>
                Thread-safe token bucket algorithm capping brute force login attacks (10/min) and Gemini LLM requests (5/min) with standard Retry-After headers.
              </p>
            </div>

            <div className="glass-panel" style={{ padding: '28px' }}>
              <div style={{
                width: '48px',
                height: '48px',
                borderRadius: '12px',
                background: 'rgba(6, 182, 212, 0.15)',
                color: '#22d3ee',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                marginBottom: '20px',
              }}>
                <Cpu size={24} />
              </div>
              <h3 style={{ fontSize: '1.25rem', marginBottom: '10px' }}>In-Memory Spring Cache</h3>
              <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', lineHeight: 1.6 }}>
                Sub-millisecond query responses for skill catalogs, companies, and platform analytics with automated CacheEvict mutation triggers.
              </p>
            </div>

            <div className="glass-panel" style={{ padding: '28px' }}>
              <div style={{
                width: '48px',
                height: '48px',
                borderRadius: '12px',
                background: 'rgba(244, 63, 94, 0.15)',
                color: '#fb7185',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                marginBottom: '20px',
              }}>
                <Layers size={24} />
              </div>
              <h3 style={{ fontSize: '1.25rem', marginBottom: '10px' }}>SpringDoc OpenAPI 3.0</h3>
              <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', lineHeight: 1.6 }}>
                Interactive Swagger UI documentation exploring 30+ secured REST endpoints, schemas, and live execution right from the browser.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Demand & Supply Skill Radar */}
      {demandSupply.length > 0 && (
        <section style={{ padding: '60px 0' }}>
          <div className="container">
            <div className="glass-panel glass-panel-glow" style={{ padding: '36px' }}>
              <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between', gap: '16px', marginBottom: '28px' }}>
                <div>
                  <h3 style={{ fontSize: '1.5rem', marginBottom: '4px' }}>Real-Time Skill Market Intelligence</h3>
                  <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
                    Live demand vs supply metrics aggregated across all active job postings in MySQL
                  </p>
                </div>
                <Link to="/jobs" className="btn btn-secondary btn-sm">
                  View Matching Roles
                </Link>
              </div>

              <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
                gap: '16px',
              }}>
                {demandSupply.map((item, idx) => (
                  <div
                    key={idx}
                    style={{
                      padding: '16px',
                      background: 'rgba(15, 23, 42, 0.8)',
                      borderRadius: '10px',
                      border: '1px solid var(--border-subtle)',
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                      <span style={{ fontWeight: 600, color: '#f8fafc' }}>{item.skillName || item.name}</span>
                      <span className="badge badge-primary">{item.jobCount || item.demandCount || 0} Openings</span>
                    </div>
                    <div style={{
                      height: '6px',
                      background: 'rgba(255, 255, 255, 0.08)',
                      borderRadius: '9999px',
                      overflow: 'hidden',
                    }}>
                      <div
                        style={{
                          height: '100%',
                          width: `${Math.min(100, ((item.jobCount || 1) / 10) * 100)}%`,
                          background: 'var(--primary-gradient)',
                          borderRadius: '9999px',
                        }}
                      />
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </section>
      )}
    </div>
  );
};
