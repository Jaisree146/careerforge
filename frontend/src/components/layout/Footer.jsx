import React from 'react';
import { Zap, Code2, Heart } from 'lucide-react';

export const Footer = () => {
  return (
    <footer style={{
      marginTop: 'auto',
      borderTop: '1px solid var(--border-subtle)',
      background: 'rgba(9, 13, 22, 0.95)',
      padding: '48px 0 24px 0',
    }}>
      <div className="container">
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
          gap: '32px',
          marginBottom: '40px',
        }}>
          {/* Brand Info */}
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '14px' }}>
              <div style={{
                width: '28px',
                height: '28px',
                borderRadius: '8px',
                background: 'var(--primary-gradient)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}>
                <Zap size={16} color="#ffffff" />
              </div>
              <span style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: '1.1rem' }}>
                Career<span className="text-gradient">Forge</span>
              </span>
            </div>
            <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', lineHeight: 1.6 }}>
              Enterprise-grade recruitment platform driven by Google Gemini LLM intelligence, automated resume parsing, and high-precision candidate fit scoring.
            </p>
          </div>

          {/* Platform Tech Stack Badges */}
          <div>
            <div style={{ fontWeight: 600, fontSize: '0.9rem', marginBottom: '14px', color: '#f8fafc' }}>
              Built With Modern Architecture
            </div>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
              <span className="badge badge-primary">Java 21</span>
              <span className="badge badge-primary">Spring Boot 4.x</span>
              <span className="badge badge-primary">MySQL 8 InnoDB</span>
              <span className="badge badge-purple">Google Gemini LLM</span>
              <span className="badge badge-success">Apache PDFBox</span>
              <span className="badge badge-warning">Bucket4j Rate Limiter</span>
              <span className="badge badge-primary">STOMP WebSockets</span>
              <span className="badge badge-primary">Spring Cache</span>
            </div>
          </div>

          {/* Quick Links */}
          <div>
            <div style={{ fontWeight: 600, fontSize: '0.9rem', marginBottom: '14px', color: '#f8fafc' }}>
              Platform Navigation
            </div>
            <ul style={{ listStyle: 'none', display: 'flex', flexDirection: 'column', gap: '8px', fontSize: '0.85rem' }}>
              <li><a href="/jobs" style={{ color: 'var(--text-secondary)', textDecoration: 'none' }}>Explore Job Postings</a></li>
              <li><a href="/profile" style={{ color: 'var(--text-secondary)', textDecoration: 'none' }}>AI Resume Extraction</a></li>
              <li><a href="/recruiter" style={{ color: 'var(--text-secondary)', textDecoration: 'none' }}>Recruiter Analytics Hub</a></li>
              <li><a href="/swagger-ui.html" target="_blank" rel="noreferrer" style={{ color: '#3b82f6', textDecoration: 'none' }}>OpenAPI 3.0 (Swagger UI) ↗</a></li>
            </ul>
          </div>
        </div>

        <div style={{
          paddingTop: '24px',
          borderTop: '1px solid var(--border-subtle)',
          display: 'flex',
          flexWrap: 'wrap',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: '12px',
          fontSize: '0.8rem',
          color: 'var(--text-muted)',
        }}>
          <div>© 2026 CareerForge. All rights reserved. Production-grade placement portfolio.</div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span>Engineered with precision</span>
            <Heart size={14} color="#f43f5e" fill="#f43f5e" />
          </div>
        </div>
      </div>
    </footer>
  );
};
