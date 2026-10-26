import React, { useState, useEffect } from 'react';
import { applicationApi } from '../services/api';
import { 
  Briefcase, 
  Clock, 
  Building, 
  MapPin, 
  CheckCircle2, 
  Clock3, 
  Calendar, 
  XCircle,
  Award
} from 'lucide-react';

export const ApplicationsPage = () => {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchApplications();
  }, []);

  const fetchApplications = async () => {
    setLoading(true);
    try {
      const data = await applicationApi.getMyApplications();
      setApplications(Array.isArray(data) ? data : (data?.content || []));
    } catch (err) {
      console.error('Failed to load applications', err);
    } finally {
      setLoading(false);
    }
  };

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

  return (
    <div className="container animate-fade-in" style={{ padding: '40px 1.5rem 80px 1.5rem' }}>
      <div style={{ marginBottom: '32px' }}>
        <h1 style={{ fontSize: '2.25rem', marginBottom: '8px' }}>
          My <span className="text-gradient">Job Applications</span>
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
          Track real-time candidate status updates, interview schedules, and recruiter notes
        </p>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '60px 0', color: 'var(--text-secondary)' }}>
          Loading your applications...
        </div>
      ) : applications.length === 0 ? (
        <div className="glass-panel" style={{ textAlign: 'center', padding: '60px', color: 'var(--text-secondary)' }}>
          <Briefcase size={40} style={{ margin: '0 auto 12px auto', opacity: 0.5 }} />
          <h3>No applications submitted yet</h3>
          <p style={{ fontSize: '0.875rem', marginTop: '6px' }}>
            Browse through active openings and apply with your verified profile.
          </p>
          <a href="/jobs" className="btn btn-primary" style={{ marginTop: '20px', display: 'inline-flex' }}>
            Explore Jobs
          </a>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {applications.map((app) => {
            const job = app.job || {};
            const created = app.appliedAt || app.createdAt;
            const formattedDate = created ? new Date(created).toLocaleDateString(undefined, {
              year: 'numeric',
              month: 'short',
              day: 'numeric',
            }) : 'Recently';

            return (
              <div
                key={app.id}
                className="glass-panel"
                style={{
                  padding: '24px',
                  display: 'flex',
                  flexWrap: 'wrap',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  gap: '20px',
                }}
              >
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '6px' }}>
                    <h3 style={{ fontSize: '1.25rem', color: '#f8fafc' }}>
                      {job.title || app.jobTitle || 'Software Engineer'}
                    </h3>
                    {getStatusBadge(app.status)}
                  </div>

                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '14px', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '4px', color: '#3b82f6', fontWeight: 500 }}>
                      <Building size={15} />
                      <span>{job.company?.name || job.companyName || 'Partner Company'}</span>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                      <MapPin size={15} />
                      <span>{job.location?.city || job.locationName || 'Remote'}</span>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                      <Clock size={15} />
                      <span>Applied on {formattedDate}</span>
                    </div>
                  </div>

                  {app.reviewNotes && (
                    <div style={{
                      marginTop: '12px',
                      padding: '8px 12px',
                      borderRadius: '6px',
                      background: 'rgba(255, 255, 255, 0.04)',
                      borderLeft: '3px solid #3b82f6',
                      fontSize: '0.825rem',
                      color: '#cbd5e1',
                    }}>
                      <span style={{ fontWeight: 600, color: '#f8fafc' }}>Recruiter Feedback: </span>
                      {app.reviewNotes}
                    </div>
                  )}
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <a
                    href={`/jobs`}
                    className="btn btn-secondary btn-sm"
                  >
                    View Job Details
                  </a>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
