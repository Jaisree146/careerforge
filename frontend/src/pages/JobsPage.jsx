import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { jobsApi, applicationApi, matchingApi } from '../services/api';
import { FitScoreBadge } from '../components/common/FitScoreBadge';
import { 
  Search, 
  MapPin, 
  Briefcase, 
  DollarSign, 
  Calendar, 
  Bookmark, 
  BookmarkCheck, 
  Sparkles, 
  CheckCircle, 
  X, 
  Send,
  Building,
  Filter
} from 'lucide-react';

export const JobsPage = () => {
  const { isAuthenticated, isCandidate } = useAuth();

  const [jobs, setJobs] = useState([]);
  const [matchScores, setMatchScores] = useState({});
  const [savedJobIds, setSavedJobIds] = useState(new Set());
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [selectedJob, setSelectedJob] = useState(null);
  const [matchDetail, setMatchDetail] = useState(null);
  const [matchLoading, setMatchLoading] = useState(false);

  // Apply Modal state
  const [applyingJob, setApplyingJob] = useState(null);
  const [coverNote, setCoverNote] = useState('');
  const [applyLoading, setApplyLoading] = useState(false);
  const [applySuccess, setApplySuccess] = useState(false);
  const [applyError, setApplyError] = useState(null);

  // Fetch Jobs & Saved Jobs
  useEffect(() => {
    fetchJobs();
    if (isAuthenticated && isCandidate) {
      fetchSavedJobs();
    }
  }, [isAuthenticated, isCandidate]);

  const fetchJobs = async () => {
    setLoading(true);
    try {
      const data = await jobsApi.getAll({ search });
      const jobList = data?.content || (Array.isArray(data) ? data : []);
      setJobs(jobList);

      // If candidate is logged in, fetch match scores for visible jobs
      if (isAuthenticated && isCandidate && jobList.length > 0) {
        fetchBatchMatchScores(jobList);
      }
    } catch (err) {
      console.error('Failed to load jobs', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchSavedJobs = async () => {
    try {
      const saved = await applicationApi.getSavedJobs();
      if (Array.isArray(saved)) {
        const ids = new Set(saved.map((item) => item.job?.id || item.jobId));
        setSavedJobIds(ids);
      }
    } catch (err) {
      console.warn('Failed to fetch saved jobs', err);
    }
  };

  const fetchBatchMatchScores = async (jobList) => {
    const scores = {};
    for (const job of jobList.slice(0, 10)) {
      try {
        const match = await matchingApi.getMatchForJob(job.id);
        if (match) {
          scores[job.id] = match;
        }
      } catch (e) {
        // Ignore single score failure
      }
    }
    setMatchScores(scores);
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchJobs();
  };

  const toggleSaveJob = async (e, jobId) => {
    e.stopPropagation();
    if (!isAuthenticated || !isCandidate) return;

    const isSaved = savedJobIds.has(jobId);
    try {
      if (isSaved) {
        await applicationApi.unsaveJob(jobId);
        setSavedJobIds((prev) => {
          const next = new Set(prev);
          next.delete(jobId);
          return next;
        });
      } else {
        await applicationApi.saveJob(jobId);
        setSavedJobIds((prev) => new Set(prev).add(jobId));
      }
    } catch (err) {
      console.error('Failed to toggle save job', err);
    }
  };

  const openJobModal = async (job) => {
    setSelectedJob(job);
    setMatchDetail(null);

    if (isAuthenticated && isCandidate) {
      setMatchLoading(true);
      try {
        const detail = await matchingApi.getMatchForJob(job.id);
        setMatchDetail(detail);
      } catch (err) {
        console.warn('Failed to fetch deep match breakdown', err);
      } finally {
        setMatchLoading(false);
      }
    }
  };

  const handleApply = async (e) => {
    e.preventDefault();
    if (!applyingJob) return;

    setApplyLoading(true);
    setApplyError(null);
    try {
      await applicationApi.apply(applyingJob.id, coverNote);
      setApplySuccess(true);
      setTimeout(() => {
        setApplySuccess(false);
        setApplyingJob(null);
        setCoverNote('');
      }, 1800);
    } catch (err) {
      setApplyError(err.message || 'Application failed. You might have already applied.');
    } finally {
      setApplyLoading(false);
    }
  };

  return (
    <div className="container animate-fade-in" style={{ padding: '40px 1.5rem 80px 1.5rem' }}>
      {/* Header & Search */}
      <div style={{ marginBottom: '32px' }}>
        <h1 style={{ fontSize: '2.25rem', marginBottom: '8px' }}>
          Explore <span className="text-gradient">Career Opportunities</span>
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
          Real-time roles with AI fit score evaluations and 1-click application
        </p>

        {/* Search Bar Form */}
        <form onSubmit={handleSearchSubmit} style={{ marginTop: '24px', display: 'flex', gap: '12px', maxWidth: '720px' }}>
          <div style={{ position: 'relative', flex: 1 }}>
            <Search size={18} color="#64748b" style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)' }} />
            <input
              type="text"
              className="form-input"
              style={{ paddingLeft: '42px', height: '46px' }}
              placeholder="Search by job title, company, or technical skill..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <button type="submit" className="btn btn-primary" style={{ padding: '0 24px', height: '46px' }}>
            Search
          </button>
        </form>
      </div>

      {/* Jobs Grid */}
      {loading ? (
        <div style={{ textAlign: 'center', padding: '60px 0', color: 'var(--text-secondary)' }}>
          Loading live opportunities...
        </div>
      ) : jobs.length === 0 ? (
        <div className="glass-panel" style={{ textAlign: 'center', padding: '60px', color: 'var(--text-secondary)' }}>
          <Briefcase size={40} style={{ margin: '0 auto 12px auto', opacity: 0.5 }} />
          <h3>No matching job postings found</h3>
          <p style={{ fontSize: '0.875rem', marginTop: '6px' }}>Try adjusting your search criteria</p>
        </div>
      ) : (
        <div className="grid-cols-2" style={{ gap: '20px' }}>
          {jobs.map((job) => {
            const match = matchScores[job.id];
            const isSaved = savedJobIds.has(job.id);

            return (
              <div
                key={job.id}
                className="glass-panel"
                onClick={() => openJobModal(job)}
                style={{
                  padding: '24px',
                  cursor: 'pointer',
                  display: 'flex',
                  flexDirection: 'column',
                  justifyContent: 'space-between',
                  transition: 'all 0.2s',
                  position: 'relative',
                }}
              >
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '12px' }}>
                    <div>
                      <h3 style={{ fontSize: '1.2rem', color: '#f8fafc', marginBottom: '4px' }}>
                        {job.title}
                      </h3>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#3b82f6', fontSize: '0.875rem', fontWeight: 500 }}>
                        <Building size={15} />
                        <span>{job.company?.name || job.companyName || 'Verified Partner'}</span>
                      </div>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      {match && (
                        <FitScoreBadge score={match.overallScore} fitLevel={match.fitLevel} size="sm" />
                      )}

                      {isAuthenticated && isCandidate && (
                        <button
                          type="button"
                          onClick={(e) => toggleSaveJob(e, job.id)}
                          style={{
                            background: 'none',
                            border: 'none',
                            color: isSaved ? '#3b82f6' : 'var(--text-muted)',
                            cursor: 'pointer',
                            padding: '4px',
                          }}
                          title={isSaved ? 'Unsave Job' : 'Save Job'}
                        >
                          {isSaved ? <BookmarkCheck size={20} fill="#3b82f6" /> : <Bookmark size={20} />}
                        </button>
                      )}
                    </div>
                  </div>

                  {/* Metadata Chips */}
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '12px', margin: '14px 0', fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                      <MapPin size={14} color="#60a5fa" />
                      <span>{job.location?.city || job.locationName || 'Remote / Hybrid'}</span>
                    </div>

                    {job.salaryMin && job.salaryMax && (
                      <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                        <DollarSign size={14} color="#34d399" />
                        <span>${(job.salaryMin / 1000).toFixed(0)}k - ${(job.salaryMax / 1000).toFixed(0)}k</span>
                      </div>
                    )}

                    <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                      <Briefcase size={14} color="#a78bfa" />
                      <span>{job.experienceYears ? `${job.experienceYears}+ yrs exp` : 'Entry / Mid'}</span>
                    </div>
                  </div>

                  <p style={{
                    fontSize: '0.85rem',
                    color: 'var(--text-secondary)',
                    lineHeight: 1.5,
                    display: '-webkit-box',
                    WebkitLineClamp: 2,
                    WebkitBoxOrient: 'vertical',
                    overflow: 'hidden',
                  }}>
                    {job.description}
                  </p>
                </div>

                {/* Required Skills Badges */}
                <div style={{ marginTop: '16px', display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                  {(job.skills || job.requiredSkills || []).slice(0, 4).map((s, idx) => (
                    <span key={idx} className="badge badge-primary">
                      {s.name || s}
                    </span>
                  ))}
                  {(job.skills?.length > 4 || job.requiredSkills?.length > 4) && (
                    <span className="badge" style={{ background: 'rgba(255,255,255,0.05)', color: 'var(--text-muted)' }}>
                      +{(job.skills?.length || job.requiredSkills?.length) - 4} more
                    </span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Job Details Modal Drawer */}
      {selectedJob && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(0, 0, 0, 0.75)',
          backdropFilter: 'blur(8px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 100,
          padding: '20px',
        }}>
          <div
            className="glass-panel animate-fade-in"
            style={{
              maxWidth: '680px',
              width: '100%',
              maxHeight: '90vh',
              overflowY: 'auto',
              padding: '32px',
              background: '#0f172a',
              position: 'relative',
            }}
          >
            <button
              onClick={() => setSelectedJob(null)}
              style={{
                position: 'absolute',
                top: '20px',
                right: '20px',
                background: 'rgba(255, 255, 255, 0.05)',
                border: 'none',
                borderRadius: '8px',
                width: '32px',
                height: '32px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--text-secondary)',
                cursor: 'pointer',
              }}
            >
              <X size={18} />
            </button>

            <div>
              <div className="badge badge-primary" style={{ marginBottom: '10px' }}>
                {selectedJob.employmentType || 'Full-Time'}
              </div>
              <h2 style={{ fontSize: '1.75rem', marginBottom: '6px' }}>{selectedJob.title}</h2>
              <div style={{ color: '#3b82f6', fontWeight: 600, fontSize: '1rem', marginBottom: '16px' }}>
                {selectedJob.company?.name || selectedJob.companyName || 'Verified Partner'}
              </div>
            </div>

            {/* AI Fit Score Details Section */}
            {matchDetail && (
              <div style={{
                background: 'rgba(19, 28, 49, 0.9)',
                border: '1px solid var(--border-glow)',
                borderRadius: '12px',
                padding: '20px',
                marginBottom: '24px',
              }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '12px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontWeight: 600, color: '#f8fafc' }}>
                    <Sparkles size={18} color="#818cf8" />
                    <span>AI Profile Fit Analysis</span>
                  </div>
                  <FitScoreBadge score={matchDetail.overallScore} fitLevel={matchDetail.fitLevel} />
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px', textAlign: 'center', margin: '14px 0' }}>
                  <div style={{ padding: '8px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px' }}>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Skills Match</div>
                    <div style={{ fontSize: '1.1rem', fontWeight: 700, color: '#60a5fa' }}>{matchDetail.skillScore}%</div>
                  </div>
                  <div style={{ padding: '8px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px' }}>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Experience</div>
                    <div style={{ fontSize: '1.1rem', fontWeight: 700, color: '#34d399' }}>{matchDetail.experienceScore}%</div>
                  </div>
                  <div style={{ padding: '8px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px' }}>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Profile Quality</div>
                    <div style={{ fontSize: '1.1rem', fontWeight: 700, color: '#a78bfa' }}>{matchDetail.profileCompletenessScore}%</div>
                  </div>
                </div>

                {matchDetail.recommendationReasons?.length > 0 && (
                  <div style={{ marginTop: '12px', fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                    <div style={{ fontWeight: 600, color: '#cbd5e1', marginBottom: '4px' }}>Key Insights:</div>
                    <ul style={{ paddingLeft: '18px', display: 'flex', flexDirection: 'column', gap: '3px' }}>
                      {matchDetail.recommendationReasons.map((r, i) => (
                        <li key={i}>{r}</li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            )}

            {/* Description */}
            <div style={{ marginBottom: '24px' }}>
              <h4 style={{ fontSize: '1rem', marginBottom: '8px', color: '#f8fafc' }}>About The Role</h4>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', lineHeight: 1.7, whiteSpace: 'pre-line' }}>
                {selectedJob.description}
              </p>
            </div>

            {/* Skills Requirement */}
            <div style={{ marginBottom: '28px' }}>
              <h4 style={{ fontSize: '1rem', marginBottom: '10px', color: '#f8fafc' }}>Required Skills</h4>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                {(selectedJob.skills || selectedJob.requiredSkills || []).map((s, idx) => (
                  <span key={idx} className="badge badge-primary">
                    {s.name || s}
                  </span>
                ))}
              </div>
            </div>

            {/* Modal Actions */}
            <div style={{ display: 'flex', gap: '12px', borderTop: '1px solid var(--border-subtle)', paddingTop: '20px' }}>
              {isAuthenticated && isCandidate ? (
                <button
                  type="button"
                  onClick={() => {
                    setApplyingJob(selectedJob);
                    setSelectedJob(null);
                  }}
                  className="btn btn-primary"
                  style={{ flex: 1, padding: '12px' }}
                >
                  Apply Now with Profile <Send size={16} />
                </button>
              ) : (
                <a href="/login" className="btn btn-primary" style={{ flex: 1, padding: '12px' }}>
                  Sign in to Apply
                </a>
              )}
              <button
                type="button"
                onClick={() => setSelectedJob(null)}
                className="btn btn-secondary"
                style={{ padding: '12px 20px' }}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Quick Application Modal */}
      {applyingJob && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(0, 0, 0, 0.75)',
          backdropFilter: 'blur(8px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 110,
          padding: '20px',
        }}>
          <div className="glass-panel animate-fade-in" style={{
            maxWidth: '520px',
            width: '100%',
            padding: '32px',
            background: '#0f172a',
          }}>
            <h3 style={{ fontSize: '1.4rem', marginBottom: '6px' }}>
              Apply to {applyingJob.title}
            </h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '20px' }}>
              Your verified candidate profile and parsed resume will be submitted to {applyingJob.company?.name || 'the recruiter'}.
            </p>

            {applySuccess && (
              <div style={{
                padding: '12px 14px',
                borderRadius: '8px',
                background: 'rgba(16, 185, 129, 0.15)',
                color: '#6ee7b7',
                fontSize: '0.875rem',
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                marginBottom: '16px',
              }}>
                <CheckCircle size={18} />
                <span>Application submitted successfully! You can track it in Applications.</span>
              </div>
            )}

            {applyError && (
              <div style={{
                padding: '12px 14px',
                borderRadius: '8px',
                background: 'rgba(244, 63, 94, 0.15)',
                color: '#fda4af',
                fontSize: '0.875rem',
                marginBottom: '16px',
              }}>
                {applyError}
              </div>
            )}

            <form onSubmit={handleApply}>
              <div className="form-group">
                <label className="form-label">Optional Cover Note / Message to Recruiter</label>
                <textarea
                  className="form-textarea"
                  rows={4}
                  placeholder="Introduce yourself and explain why you're a great fit for this position..."
                  value={coverNote}
                  onChange={(e) => setCoverNote(e.target.value)}
                />
              </div>

              <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
                <button
                  type="submit"
                  disabled={applyLoading || applySuccess}
                  className="btn btn-primary"
                  style={{ flex: 1, padding: '12px' }}
                >
                  {applyLoading ? 'Submitting Application...' : 'Confirm Application'}
                </button>
                <button
                  type="button"
                  onClick={() => setApplyingJob(null)}
                  className="btn btn-secondary"
                  style={{ padding: '12px 20px' }}
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
