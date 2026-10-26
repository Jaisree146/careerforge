import React from 'react';
import { Sparkles, CheckCircle2, AlertTriangle, AlertCircle } from 'lucide-react';

export const FitScoreBadge = ({ score, fitLevel, size = 'md', showIcon = true }) => {
  if (score === undefined || score === null) return null;

  const rounded = Math.round(score);

  let badgeStyle = {
    background: 'rgba(59, 130, 246, 0.15)',
    color: '#93c5fd',
    border: '1px solid rgba(59, 130, 246, 0.3)',
  };
  let Icon = Sparkles;
  let label = fitLevel || 'MATCH';

  if (rounded >= 80 || fitLevel === 'STRONG_MATCH') {
    badgeStyle = {
      background: 'rgba(16, 185, 129, 0.15)',
      color: '#6ee7b7',
      border: '1px solid rgba(16, 185, 129, 0.35)',
      boxShadow: '0 0 12px rgba(16, 185, 129, 0.2)',
    };
    Icon = CheckCircle2;
    label = 'Strong Match';
  } else if (rounded >= 60 || fitLevel === 'GOOD_MATCH') {
    badgeStyle = {
      background: 'rgba(6, 182, 212, 0.15)',
      color: '#67e8f9',
      border: '1px solid rgba(6, 182, 212, 0.35)',
    };
    Icon = Sparkles;
    label = 'Good Match';
  } else if (rounded >= 40 || fitLevel === 'MODERATE_MATCH') {
    badgeStyle = {
      background: 'rgba(245, 158, 11, 0.15)',
      color: '#fde68a',
      border: '1px solid rgba(245, 158, 11, 0.35)',
    };
    Icon = AlertTriangle;
    label = 'Moderate Match';
  } else {
    badgeStyle = {
      background: 'rgba(244, 63, 94, 0.15)',
      color: '#fda4af',
      border: '1px solid rgba(244, 63, 94, 0.35)',
    };
    Icon = AlertCircle;
    label = 'Low Match';
  }

  const isSmall = size === 'sm';

  return (
    <div
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: '6px',
        padding: isSmall ? '2px 8px' : '4px 12px',
        borderRadius: '9999px',
        fontSize: isSmall ? '0.75rem' : '0.8125rem',
        fontWeight: 600,
        ...badgeStyle,
      }}
    >
      {showIcon && <Icon size={isSmall ? 13 : 15} />}
      <span>{rounded}%</span>
      <span style={{ opacity: 0.85, fontWeight: 500 }}>• {label}</span>
    </div>
  );
};
