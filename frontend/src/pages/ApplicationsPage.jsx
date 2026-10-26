import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useCandidateApplications } from '../hooks/useQueries';
import { 
  Briefcase, 
  Clock, 
  Building, 
  MapPin, 
  CheckCircle2, 
  Clock3, 
  Calendar, 
  XCircle,
  Award,
  FileText,
  MessageSquare
} from 'lucide-react';

export const ApplicationsPage = () => {
  const { isAuthenticated, isCandidate } = useAuth();
  const [selectedStatus, setSelectedStatus] = useState('');

  // TanStack Query
  const { data, isLoading } = useCandidateApplications(
    { status: selectedStatus || undefined },
    isAuthenticated && isCandidate
  );

  const applications = data?.content || (Array.isArray(data) ? data : []);

  const getStatusBadge = (status) => {
    switch (status) {
      case 'SHORTLISTED':
        return <span className="badge badge-success"><CheckCircle2 size={13} /> Shortlisted</span>;
      case 'INTERVIEW_SCHEDULED':
      case 'INTERVIEWING':
        return <span className="badge badge-warning"><Calendar size={13} /> Interview Scheduled</span>;
      case 'OFFER_EXTENDED':
      case 'ACCEPTED':
        return <span className="badge badge-success" style={{ background: 'rgba(16,185,129,0.25)', borderColor: '#10b981' }}><Award size={13} /> Offer Extended!</span>;
      case 'REJECTED':
        return <span className="badge badge-danger"><XCircle size={13} /> Not Selected</span>;
      case 'UNDER_REVIEW':
      case 'REVIEWING':
        return <span className="badge badge-purple"><Clock3 size={13} /> In Review</span>;
      default:
        return <span className="badge badge-primary"><Clock size={13} /> Submitted</span>;
    }
  };

  const statusFilters = [
    { label: 'All Applications', value: '' },
    { label: 'Submitted', value: 'SUBMITTED' },
    { label: 'In Review', value: 'UNDER_REVIEW' },
    { label: 'Shortlisted', value: 'SHORTLISTED' },
    { label: 'Interviewing', value: 'INTERVIEW_SCHEDULED' },
    { label: 'Offered', value: 'OFFER_EXTENDED' },
    { label: 'Rejected', value: 'REJECTED' },
  ];

  if (!isAuthenticated) {
    return (
      <div className="container" style={{ padding: '80px 1.5rem', textAlign: 'center' }}>
        <h2 style={{ fontSize: '1.75rem', marginBottom: '12px' }}>Authentication Required</h2>
        <p style={{ color: 'var(--text-secondary)' }}>Please log in to track your job applications.</p>
      </div>
    );
  }

  return (
    <div className="container animate-fade-in" style={{ padding: '40px 1.5rem 80px 1.5rem' }}>
      {/* Header */}
      <div style={{ marginBottom: '32px' }}>
        <h1 style={{ fontSize: '2.25rem', marginBottom: '8px' }}>
          My <span className="text-gradient">Job Applications</span>
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
          Live applicant pipeline tracking with stage changes and recruiter review feedback.
        </p>

        {/* Status Filter Tabs */}
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px', marginTop: '20px' }}>
          {statusFilters.map((tab) => (
            <button
              key={tab.value}
              onClick={() => setSelectedStatus(tab.value)}
              style={{
                padding: '6px 14px',
                borderRadius: '20px',
                border: '1px solid',
                borderColor: selectedStatus === tab.value ? 'var(--primary-glow)' : 'var(--border-subtle)',
                background: selectedStatus === tab.value ? 'rgba(59, 130, 246, 0.15)' : 'rgba(255, 255, 255, 0.02)',
                color: selectedStatus === tab.value ? '#93c5fd' : 'var(--text-secondary)',
                fontSize: '0.8rem',
                cursor: 'pointer',
                fontWeight: selectedStatus === tab.value ? 600 : 400,
                transition: 'var(--transition-fast)',
              }}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {/* Applications List */}
      {isLoading ? (
        <div style={{ textAlign: 'center', padding: '60px 0', color: 'var(--text-muted)' }}>
          Loading your application pipeline...
        </div>
      ) : applications.length === 0 ? (
        <div className="card" style={{ padding: '60px 20px', textAlign: 'center' }}>
          <Briefcase size={44} color="var(--text-muted)" style={{ margin: '0 auto 16px auto' }} />
          <h3 style={{ fontSize: '1.25rem', marginBottom: '8px' }}>No applications in this category</h3>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '20px' }}>
            Explore open opportunities and submit your profile to get started.
          </p>
          <a href="/jobs" className="btn btn-primary" style={{ display: 'inline-flex' }}>
            Browse Open Jobs
          </a>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {applications.map((app) => (
            <div
              key={app.id}
              className="card"
              style={{
                padding: '24px',
                display: 'flex',
                flexDirection: 'column',
                gap: '16px',
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '12px' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
                    <Building size={16} color="var(--text-muted)" />
                    <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 600 }}>
                      {app.job?.company?.name || 'Hiring Company'}
                    </span>
                  </div>
                  <h3 style={{ fontSize: '1.2rem', fontWeight: 700, color: '#ffffff', margin: 0 }}>
                    {app.job?.title || 'Applied Position'}
                  </h3>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                  {getStatusBadge(app.status)}
                </div>
              </div>

              {/* Cover Note & Feedback */}
              {app.notes && (
                <div style={{
                  padding: '12px 14px',
                  borderRadius: '8px',
                  background: 'rgba(255, 255, 255, 0.02)',
                  border: '1px solid var(--border-subtle)',
                  fontSize: '0.85rem',
                  color: 'var(--text-secondary)',
                  display: 'flex',
                  alignItems: 'flex-start',
                  gap: '8px',
                }}>
                  <MessageSquare size={16} color="var(--text-muted)" style={{ flexShrink: 0, marginTop: '2px' }} />
                  <div>
                    <span style={{ fontWeight: 600, color: '#f8fafc' }}>Candidate Note: </span>
                    {app.notes}
                  </div>
                </div>
              )}

              {/* Meta details */}
              <div style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                paddingTop: '12px',
                borderTop: '1px solid var(--border-subtle)',
                fontSize: '0.8rem',
                color: 'var(--text-muted)',
              }}>
                <div>Applied on {new Date(app.appliedAt || app.createdAt).toLocaleDateString()}</div>
                <div>Application Reference #{app.id}</div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
