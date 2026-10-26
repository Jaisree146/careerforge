import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import {
  useJobs,
  useSavedJobs,
  useApplyJobMutation,
  useToggleSaveJobMutation,
} from '../hooks/useQueries';
import { matchingApi } from '../services/api';
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
  Check,
  AlertCircle
} from 'lucide-react';

export const JobsPage = () => {
  const { isAuthenticated, isCandidate } = useAuth();

  // Search & Filter state
  const [searchTerm, setSearchTerm] = useState('');
  const [activeSearch, setActiveSearch] = useState('');
  const [selectedType, setSelectedType] = useState('');

  // TanStack Queries
  const { data: jobsData, isLoading: jobsLoading } = useJobs({
    search: activeSearch || undefined,
    employmentType: selectedType || undefined,
  });

  const { data: savedJobsData } = useSavedJobs({}, isAuthenticated && isCandidate);

  // Mutations
  const applyMutation = useApplyJobMutation();
  const toggleSaveMutation = useToggleSaveJobMutation();

  // Selected Job & AI Breakdown Modal State
  const [selectedJob, setSelectedJob] = useState(null);
  const [matchDetail, setMatchDetail] = useState(null);
  const [matchLoading, setMatchLoading] = useState(false);

  // Apply Modal State
  const [applyingJob, setApplyingJob] = useState(null);
  const [coverNote, setCoverNote] = useState('');
  const [applySuccess, setApplySuccess] = useState(false);
  const [applyError, setApplyError] = useState(null);

  // Normalize jobs list
  const jobList = jobsData?.content || (Array.isArray(jobsData) ? jobsData : []);

  // Saved job IDs set
  const savedList = savedJobsData?.content || (Array.isArray(savedJobsData) ? savedJobsData : []);
  const savedJobIds = new Set(savedList.map((item) => item.job?.id || item.jobId));

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setActiveSearch(searchTerm);
  };

  const handleToggleSave = async (e, jobId) => {
    e.stopPropagation();
    if (!isAuthenticated || !isCandidate) return;
    const isSaved = savedJobIds.has(jobId);
    await toggleSaveMutation.mutateAsync({ jobId, isSaved });
  };

  const handleOpenDetailModal = async (job) => {
    setSelectedJob(job);
    setMatchDetail(null);

    if (isAuthenticated && isCandidate) {
      setMatchLoading(true);
      try {
        const detail = await matchingApi.getMatchForJob(job.id);
        setMatchDetail(detail);
      } catch (err) {
        console.warn('Match detail fetch failed', err);
      } finally {
        setMatchLoading(false);
      }
    }
  };

  const handleApplySubmit = async (e) => {
    e.preventDefault();
    if (!applyingJob) return;

    setApplyError(null);
    try {
      await applyMutation.mutateAsync({
        jobId: applyingJob.id,
        notes: coverNote,
      });
      setApplySuccess(true);
      setTimeout(() => {
        setApplySuccess(false);
        setApplyingJob(null);
        setCoverNote('');
      }, 1800);
    } catch (err) {
      setApplyError(err.message || 'Application failed. You might have already applied.');
    }
  };

  const employmentTypes = [
    { label: 'All Types', value: '' },
    { label: 'Full-Time', value: 'FULL_TIME' },
    { label: 'Part-Time', value: 'PART_TIME' },
    { label: 'Contract', value: 'CONTRACT' },
    { label: 'Remote', value: 'REMOTE' },
  ];

  return (
    <div className="container animate-fade-in" style={{ padding: '40px 1.5rem 80px 1.5rem' }}>
      {/* Header & Search */}
      <div style={{ marginBottom: '32px' }}>
        <h1 style={{ fontSize: '2.25rem', marginBottom: '8px' }}>
          Explore <span className="text-gradient">Career Opportunities</span>
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
          Discover verified roles scored by our multi-dimensional AI matching engine.
        </p>

        {/* Search Bar & Type Filters */}
        <div style={{ marginTop: '24px', display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '10px' }}>
            <div style={{ position: 'relative', flex: 1 }}>
              <Search
                size={18}
                style={{
                  position: 'absolute',
                  left: '16px',
                  top: '50%',
                  transform: 'translateY(-50%)',
                  color: 'var(--text-muted)',
                }}
              />
              <input
                type="text"
                className="form-input"
                style={{ paddingLeft: '44px' }}
                placeholder="Search by job title, technology stack, architecture..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            <button type="submit" className="btn btn-primary" style={{ padding: '0 24px' }}>
              Search Roles
            </button>
          </form>

          {/* Filter Pills */}
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
            {employmentTypes.map((type) => (
              <button
                key={type.value}
                onClick={() => setSelectedType(type.value)}
                style={{
                  padding: '6px 14px',
                  borderRadius: '20px',
                  border: '1px solid',
                  borderColor: selectedType === type.value ? 'var(--primary-glow)' : 'var(--border-subtle)',
                  background: selectedType === type.value ? 'rgba(59, 130, 246, 0.15)' : 'rgba(255, 255, 255, 0.02)',
                  color: selectedType === type.value ? '#93c5fd' : 'var(--text-secondary)',
                  fontSize: '0.8rem',
                  cursor: 'pointer',
                  fontWeight: selectedType === type.value ? 600 : 400,
                  transition: 'var(--transition-fast)',
                }}
              >
                {type.label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Jobs Grid */}
      {jobsLoading ? (
        <div style={{ textAlign: 'center', padding: '80px 0', color: 'var(--text-muted)' }}>
          Loading live opportunities...
        </div>
      ) : jobList.length === 0 ? (
        <div className="card" style={{ padding: '60px 20px', textAlign: 'center' }}>
          <Briefcase size={48} color="var(--text-muted)" style={{ margin: '0 auto 16px auto' }} />
          <h3 style={{ fontSize: '1.25rem', marginBottom: '8px' }}>No matching job postings found</h3>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
            Try broadening your search query or removing filters.
          </p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(360px, 1fr))', gap: '24px' }}>
          {jobList.map((job) => {
            const isSaved = savedJobIds.has(job.id);

            return (
              <div
                key={job.id}
                className="card"
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  justifyContent: 'space-between',
                  padding: '24px',
                  cursor: 'pointer',
                  position: 'relative',
                }}
                onClick={() => handleOpenDetailModal(job)}
              >
                <div>
                  {/* Top Header: Company & Bookmark */}
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <div style={{
                        width: '36px',
                        height: '36px',
                        borderRadius: '8px',
                        background: 'rgba(59, 130, 246, 0.12)',
                        border: '1px solid rgba(59, 130, 246, 0.25)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                      }}>
                        <Building size={18} color="var(--primary-glow)" />
                      </div>
                      <div>
                        <div style={{ fontWeight: 600, fontSize: '0.85rem', color: '#f8fafc' }}>
                          {job.company?.name || 'Top Enterprise'}
                        </div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '4px' }}>
                          <MapPin size={12} /> {job.location?.name || 'Remote / Hybrid'}
                        </div>
                      </div>
                    </div>

                    {isAuthenticated && isCandidate && (
                      <button
                        onClick={(e) => handleToggleSave(e, job.id)}
                        disabled={toggleSaveMutation.isPending}
                        style={{
                          background: 'none',
                          border: 'none',
                          color: isSaved ? '#3b82f6' : 'var(--text-muted)',
                          cursor: 'pointer',
                          padding: '4px',
                        }}
                      >
                        {isSaved ? <BookmarkCheck size={20} /> : <Bookmark size={20} />}
                      </button>
                    )}
                  </div>

                  {/* Title */}
                  <h3 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '10px', color: '#ffffff' }}>
                    {job.title}
                  </h3>

                  {/* Badges: Salary, Experience, Type */}
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px', marginBottom: '16px' }}>
                    {job.salary && (
                      <span className="badge badge-success" style={{ fontSize: '0.75rem' }}>
                        <DollarSign size={12} /> {job.salary}
                      </span>
                    )}
                    {job.experience && (
                      <span className="badge badge-primary" style={{ fontSize: '0.75rem' }}>
                        <Briefcase size={12} /> {job.experience}
                      </span>
                    )}
                    {job.employmentType && (
                      <span className="badge badge-purple" style={{ fontSize: '0.75rem' }}>
                        {job.employmentType.replace('_', ' ')}
                      </span>
                    )}
                  </div>

                  {/* Description preview */}
                  <p style={{
                    fontSize: '0.85rem',
                    color: 'var(--text-secondary)',
                    lineHeight: 1.5,
                    marginBottom: '16px',
                    display: '-webkit-box',
                    WebkitLineClamp: 3,
                    WebkitBoxOrient: 'vertical',
                    overflow: 'hidden',
                  }}>
                    {job.description}
                  </p>

                  {/* Skills tags */}
                  {job.skills && job.skills.length > 0 && (
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px', marginBottom: '16px' }}>
                      {job.skills.slice(0, 4).map((s, idx) => (
                        <span
                          key={idx}
                          style={{
                            padding: '3px 8px',
                            borderRadius: '6px',
                            background: 'rgba(255, 255, 255, 0.04)',
                            border: '1px solid var(--border-subtle)',
                            fontSize: '0.75rem',
                            color: 'var(--text-secondary)',
                          }}
                        >
                          {s.skillName || s.name || s}
                        </span>
                      ))}
                      {job.skills.length > 4 && (
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', alignSelf: 'center' }}>
                          +{job.skills.length - 4} more
                        </span>
                      )}
                    </div>
                  )}
                </div>

                {/* Footer Actions */}
                <div style={{
                  paddingTop: '16px',
                  borderTop: '1px solid var(--border-subtle)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                }}>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                    Posted {new Date(job.createdAt).toLocaleDateString()}
                  </div>

                  <div style={{ display: 'flex', gap: '8px' }}>
                    {isAuthenticated && isCandidate && (
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          setApplyingJob(job);
                        }}
                        className="btn btn-primary"
                        style={{ padding: '6px 14px', fontSize: '0.8rem' }}
                      >
                        Apply Now
                      </button>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Deep AI Match Breakdown & Job Detail Modal */}
      {selectedJob && (
        <div style={{
          position: 'fixed',
          inset: 0,
          background: 'rgba(0,0,0,0.75)',
          backdropFilter: 'blur(8px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 100,
          padding: '20px',
        }}>
          <div
            className="card animate-fade-in"
            style={{
              maxWidth: '680px',
              width: '100%',
              maxHeight: '90vh',
              overflowY: 'auto',
              padding: '32px',
            }}
          >
            {/* Modal Header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '20px' }}>
              <div>
                <span className="badge badge-purple" style={{ marginBottom: '8px' }}>
                  {selectedJob.employmentType?.replace('_', ' ') || 'FULL TIME'}
                </span>
                <h2 style={{ fontSize: '1.45rem', fontWeight: 800, color: '#ffffff', marginBottom: '4px' }}>
                  {selectedJob.title}
                </h2>
                <div style={{ fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
                  {selectedJob.company?.name} • {selectedJob.location?.name || 'Remote'}
                </div>
              </div>
              <button
                onClick={() => setSelectedJob(null)}
                style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}
              >
                <X size={22} />
              </button>
            </div>

            {/* AI Multi-factor Compatibility Breakdown Section */}
            {isAuthenticated && isCandidate && (
              <div
                style={{
                  padding: '20px',
                  borderRadius: '12px',
                  background: 'rgba(59, 130, 246, 0.06)',
                  border: '1px solid rgba(59, 130, 246, 0.25)',
                  marginBottom: '24px',
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '14px' }}>
                  <Sparkles size={20} color="var(--primary-glow)" />
                  <h3 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#f8fafc' }}>
                    AI Compatibility Match Engine
                  </h3>
                </div>

                {matchLoading ? (
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                    Calculating 3-dimensional candidate-job fit breakdown...
                  </div>
                ) : matchDetail ? (
                  <div>
                    {/* Overall score gauge */}
                    <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '16px' }}>
                      <div style={{
                        fontSize: '2.25rem',
                        fontWeight: 800,
                        color: matchDetail.matchScore >= 80 ? '#10b981' : matchDetail.matchScore >= 60 ? '#3b82f6' : '#f59e0b',
                      }}>
                        {matchDetail.matchScore}%
                      </div>
                      <div>
                        <div style={{ fontWeight: 600, fontSize: '0.9rem', color: '#f8fafc' }}>
                          Compatibility Fit Rating
                        </div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                          Multi-factor: 60% skills alignment • 25% experience depth • 15% profile completeness
                        </div>
                      </div>
                    </div>

                    {/* Breakdown bars */}
                    {matchDetail.breakdown && (
                      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px', marginBottom: '16px' }}>
                        <div style={{ padding: '10px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', textAlign: 'center' }}>
                          <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>Skills Match (60%)</div>
                          <div style={{ fontSize: '1.1rem', fontWeight: 700, color: '#93c5fd' }}>
                            {matchDetail.breakdown.skillMatchScore}%
                          </div>
                        </div>
                        <div style={{ padding: '10px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', textAlign: 'center' }}>
                          <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>Experience (25%)</div>
                          <div style={{ fontSize: '1.1rem', fontWeight: 700, color: '#a78bfa' }}>
                            {matchDetail.breakdown.experienceScore}%
                          </div>
                        </div>
                        <div style={{ padding: '10px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', textAlign: 'center' }}>
                          <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>Completeness (15%)</div>
                          <div style={{ fontSize: '1.1rem', fontWeight: 700, color: '#34d399' }}>
                            {matchDetail.breakdown.profileCompletenessScore}%
                          </div>
                        </div>
                      </div>
                    )}

                    {/* Matched vs Missing Skills */}
                    {matchDetail.breakdown && (
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                        {matchDetail.breakdown.matchedSkills && matchDetail.breakdown.matchedSkills.length > 0 && (
                          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', flexWrap: 'wrap' }}>
                            <span style={{ fontSize: '0.75rem', fontWeight: 600, color: '#10b981' }}>Matched:</span>
                            {matchDetail.breakdown.matchedSkills.map((s, idx) => (
                              <span key={idx} className="badge badge-success" style={{ fontSize: '0.7rem' }}>
                                <Check size={11} /> {s}
                              </span>
                            ))}
                          </div>
                        )}
                        {matchDetail.breakdown.missingSkills && matchDetail.breakdown.missingSkills.length > 0 && (
                          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', flexWrap: 'wrap' }}>
                            <span style={{ fontSize: '0.75rem', fontWeight: 600, color: '#f59e0b' }}>To Learn:</span>
                            {matchDetail.breakdown.missingSkills.map((s, idx) => (
                              <span key={idx} className="badge badge-warning" style={{ fontSize: '0.7rem' }}>
                                {s}
                              </span>
                            ))}
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                ) : (
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                    Match breakdown unavailable for this role.
                  </div>
                )}
              </div>
            )}

            {/* Role Details */}
            <div style={{ marginBottom: '24px' }}>
              <h4 style={{ fontSize: '1rem', fontWeight: 700, marginBottom: '8px', color: '#f8fafc' }}>
                About the Role
              </h4>
              <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', lineHeight: 1.6, whiteSpace: 'pre-line' }}>
                {selectedJob.description}
              </p>
            </div>

            {/* Required Skills */}
            {selectedJob.skills && selectedJob.skills.length > 0 && (
              <div style={{ marginBottom: '24px' }}>
                <h4 style={{ fontSize: '1rem', fontWeight: 700, marginBottom: '10px', color: '#f8fafc' }}>
                  Target Technical Stack
                </h4>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                  {selectedJob.skills.map((s, idx) => (
                    <span key={idx} className="badge badge-primary">
                      {s.skillName || s.name || s}
                    </span>
                  ))}
                </div>
              </div>
            )}

            {/* Modal Bottom Actions */}
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '24px' }}>
              <button onClick={() => setSelectedJob(null)} className="btn btn-secondary">
                Close
              </button>
              {isAuthenticated && isCandidate && (
                <button
                  onClick={() => {
                    setApplyingJob(selectedJob);
                    setSelectedJob(null);
                  }}
                  className="btn btn-primary"
                >
                  Apply for this Position
                </button>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Apply Submission Modal */}
      {applyingJob && (
        <div style={{
          position: 'fixed',
          inset: 0,
          background: 'rgba(0,0,0,0.75)',
          backdropFilter: 'blur(8px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 110,
          padding: '20px',
        }}>
          <div className="card animate-fade-in" style={{ maxWidth: '520px', width: '100%', padding: '28px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <div>
                <h3 style={{ fontSize: '1.25rem', fontWeight: 700, color: '#f8fafc' }}>
                  Apply to {applyingJob.title}
                </h3>
                <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                  {applyingJob.company?.name}
                </div>
              </div>
              <button
                onClick={() => setApplyingJob(null)}
                style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}
              >
                <X size={20} />
              </button>
            </div>

            {applySuccess ? (
              <div style={{ textAlign: 'center', padding: '32px 0' }}>
                <CheckCircle size={48} color="#10b981" style={{ margin: '0 auto 12px auto' }} />
                <h4 style={{ fontSize: '1.2rem', fontWeight: 700, color: '#ffffff' }}>Application Submitted!</h4>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
                  The hiring team has received your application and resume.
                </p>
              </div>
            ) : (
              <form onSubmit={handleApplySubmit}>
                {applyError && (
                  <div style={{
                    padding: '12px',
                    borderRadius: '8px',
                    background: 'rgba(239, 68, 68, 0.1)',
                    border: '1px solid rgba(239, 68, 68, 0.3)',
                    color: '#f87171',
                    fontSize: '0.85rem',
                    marginBottom: '16px',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                  }}>
                    <AlertCircle size={16} /> {applyError}
                  </div>
                )}

                <div style={{ marginBottom: '16px' }}>
                  <label className="form-label">Cover Note / Candidate Message</label>
                  <textarea
                    className="form-input"
                    rows="4"
                    placeholder="Highlight your relevant experience, architectural strengths, and motivation for this role..."
                    value={coverNote}
                    onChange={(e) => setCoverNote(e.target.value)}
                  />
                </div>

                <div style={{
                  padding: '12px 14px',
                  borderRadius: '8px',
                  background: 'rgba(255,255,255,0.02)',
                  border: '1px solid var(--border-subtle)',
                  marginBottom: '20px',
                  fontSize: '0.8rem',
                  color: 'var(--text-secondary)',
                }}>
                  Your verified candidate profile and uploaded PDF resume will be automatically attached with this application.
                </div>

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                  <button type="button" onClick={() => setApplyingJob(null)} className="btn btn-secondary">
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={applyMutation.isPending}
                    className="btn btn-primary"
                    style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                  >
                    <Send size={15} /> {applyMutation.isPending ? 'Submitting...' : 'Submit Application'}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
