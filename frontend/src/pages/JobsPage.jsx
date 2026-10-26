import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import {
  useJobs,
  useRecommendedJobs,
  useSavedJobs,
  useCandidateApplications,
  useCandidateProfile,
  useApplyJobMutation,
  useToggleSaveJobMutation,
  useUploadResumeMutation,
} from '../hooks/useQueries';
import { matchingApi } from '../services/api';
import { FitScoreBadge } from '../components/common/FitScoreBadge';
import { 
  Search, 
  MapPin, 
  Briefcase, 
  DollarSign, 
  Bookmark, 
  BookmarkCheck, 
  Sparkles, 
  CheckCircle, 
  CheckCircle2,
  X, 
  Send,
  Building,
  Check,
  AlertCircle,
  UploadCloud,
  FileText,
  Flame,
  Globe
} from 'lucide-react';

export const JobsPage = () => {
  const { isAuthenticated, isCandidate } = useAuth();

  // View Mode: 'recommended' (AI Match Feed) or 'all' (Standard Catalog)
  const [viewMode, setViewMode] = useState(isAuthenticated && isCandidate ? 'recommended' : 'all');

  // Search & Filter state
  const [searchTerm, setSearchTerm] = useState('');
  const [activeSearch, setActiveSearch] = useState('');
  const [selectedType, setSelectedType] = useState('');

  // Queries
  const { data: allJobsData, isLoading: allJobsLoading } = useJobs({
    search: activeSearch || undefined,
    employmentType: selectedType || undefined,
  });

  const { data: recommendedData, isLoading: recLoading } = useRecommendedJobs(
    isAuthenticated && isCandidate
  );

  const { data: savedJobsData } = useSavedJobs({}, isAuthenticated && isCandidate);
  const { data: applicationsData } = useCandidateApplications({}, isAuthenticated && isCandidate);
  const { data: candidateProfile } = useCandidateProfile(isAuthenticated && isCandidate);

  // Mutations
  const applyMutation = useApplyJobMutation();
  const toggleSaveMutation = useToggleSaveJobMutation();
  const uploadResumeMutation = useUploadResumeMutation();

  // Selected Job & AI Breakdown Modal State
  const [selectedJob, setSelectedJob] = useState(null);
  const [matchDetail, setMatchDetail] = useState(null);
  const [matchLoading, setMatchLoading] = useState(false);

  // Apply Modal State
  const [applyingJob, setApplyingJob] = useState(null);
  const [coverNote, setCoverNote] = useState('');
  const [inlineResumeFile, setInlineResumeFile] = useState(null);
  const [applySuccess, setApplySuccess] = useState(false);
  const [applyError, setApplyError] = useState(null);

  // Normalize Data
  const allJobsList = allJobsData?.content || (Array.isArray(allJobsData) ? allJobsData : []);
  const recommendedList = Array.isArray(recommendedData) ? recommendedData : (recommendedData?.content || []);

  // Map of job ID to recommendation match object
  const recMap = new Map();
  recommendedList.forEach((r) => {
    recMap.set(r.jobId || r.id, r.match);
  });

  // Current Display List
  const displayedJobs = viewMode === 'recommended' && isAuthenticated && isCandidate
    ? recommendedList.map((r) => ({
        id: r.jobId,
        title: r.title,
        company: { name: r.companyName },
        location: { name: r.locationName },
        salary: r.salary,
        employmentType: r.employmentType,
        match: r.match,
        description: r.description || 'Open position with high AI compatibility fit for your profile and skills.',
        skills: r.match?.skillBreakdown?.matchedSkills?.map(s => ({ skillName: s })) || [],
      }))
    : allJobsList.map((j) => ({
        ...j,
        match: j.match || recMap.get(j.id) || null,
      }));

  // Saved Jobs Set
  const savedList = savedJobsData?.content || (Array.isArray(savedJobsData) ? savedJobsData : []);
  const savedJobIds = new Set(savedList.map((item) => item.job?.id || item.jobId));

  // Applied Jobs Map (jobId -> application status)
  const appList = applicationsData?.content || (Array.isArray(applicationsData) ? applicationsData : []);
  const appliedJobMap = new Map();
  appList.forEach((app) => {
    const jId = app.job?.id || app.jobId;
    if (jId) appliedJobMap.set(jId, app.status);
  });

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setActiveSearch(searchTerm);
    setViewMode('all');
  };

  const handleToggleSave = async (e, jobId) => {
    e.stopPropagation();
    if (!isAuthenticated || !isCandidate) return;
    const isSaved = savedJobIds.has(jobId);
    await toggleSaveMutation.mutateAsync({ jobId, isSaved });
  };

  const handleOpenDetailModal = async (job) => {
    setSelectedJob(job);
    setMatchDetail(job.match ? { match: job.match } : null);

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
      // If candidate has no resume and uploaded an inline file in the modal, upload it first
      if (!candidateProfile?.resume?.fileUrl && inlineResumeFile) {
        await uploadResumeMutation.mutateAsync({
          file: inlineResumeFile,
          analyzeWithAi: false,
        });
      }

      await applyMutation.mutateAsync({
        jobId: applyingJob.id,
        notes: coverNote,
      });

      setApplySuccess(true);
      setTimeout(() => {
        setApplySuccess(false);
        setApplyingJob(null);
        setCoverNote('');
        setInlineResumeFile(null);
      }, 1800);
    } catch (err) {
      setApplyError(err.message || 'Application failed. Please verify your resume is uploaded.');
    }
  };

  const employmentTypes = [
    { label: 'All Types', value: '' },
    { label: 'Full-Time', value: 'FULL_TIME' },
    { label: 'Part-Time', value: 'PART_TIME' },
    { label: 'Contract', value: 'CONTRACT' },
    { label: 'Remote', value: 'REMOTE' },
  ];

  const isLoading = viewMode === 'recommended' ? recLoading : allJobsLoading;

  return (
    <div className="container animate-fade-in" style={{ padding: '40px 1.5rem 80px 1.5rem' }}>
      {/* Header & Tabs */}
      <div style={{ marginBottom: '32px' }}>
        <h1 style={{ fontSize: '2.25rem', marginBottom: '8px' }}>
          Explore <span className="text-gradient">Career Opportunities</span>
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
          Discover verified roles scored by our multi-dimensional AI matching engine.
        </p>

        {/* AI Recommendation vs All Opportunities Tab Bar */}
        {isAuthenticated && isCandidate && (
          <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
            <button
              onClick={() => setViewMode('recommended')}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                padding: '10px 18px',
                borderRadius: '10px',
                border: '1px solid',
                borderColor: viewMode === 'recommended' ? 'var(--primary-glow)' : 'var(--border-subtle)',
                background: viewMode === 'recommended' ? 'rgba(59, 130, 246, 0.2)' : 'rgba(255, 255, 255, 0.02)',
                color: viewMode === 'recommended' ? '#93c5fd' : 'var(--text-secondary)',
                fontWeight: 600,
                fontSize: '0.9rem',
                cursor: 'pointer',
                transition: 'var(--transition-fast)',
              }}
            >
              <Flame size={18} color={viewMode === 'recommended' ? '#f59e0b' : 'var(--text-muted)'} />
              AI Recommended For You
              <span className="badge badge-primary" style={{ fontSize: '0.7rem' }}>
                {recommendedList.length} Top Matches
              </span>
            </button>

            <button
              onClick={() => setViewMode('all')}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                padding: '10px 18px',
                borderRadius: '10px',
                border: '1px solid',
                borderColor: viewMode === 'all' ? 'var(--primary-glow)' : 'var(--border-subtle)',
                background: viewMode === 'all' ? 'rgba(59, 130, 246, 0.2)' : 'rgba(255, 255, 255, 0.02)',
                color: viewMode === 'all' ? '#93c5fd' : 'var(--text-secondary)',
                fontWeight: 600,
                fontSize: '0.9rem',
                cursor: 'pointer',
                transition: 'var(--transition-fast)',
              }}
            >
              <Globe size={18} color={viewMode === 'all' ? 'var(--primary-glow)' : 'var(--text-muted)'} />
              All Published Roles ({allJobsList.length})
            </button>
          </div>
        )}

        {/* Search Bar & Filters */}
        <div style={{ marginTop: '20px', display: 'flex', flexDirection: 'column', gap: '14px' }}>
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
                placeholder="Search by title, technology stack, architecture..."
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
                onClick={() => {
                  setSelectedType(type.value);
                  setViewMode('all');
                }}
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
      {isLoading ? (
        <div style={{ textAlign: 'center', padding: '80px 0', color: 'var(--text-muted)' }}>
          Calculating AI matches and loading job opportunities...
        </div>
      ) : displayedJobs.length === 0 ? (
        <div className="card" style={{ padding: '60px 20px', textAlign: 'center' }}>
          <Briefcase size={48} color="var(--text-muted)" style={{ margin: '0 auto 16px auto' }} />
          <h3 style={{ fontSize: '1.25rem', marginBottom: '8px' }}>
            {viewMode === 'recommended' ? 'No AI recommended jobs found yet' : 'No matching jobs found'}
          </h3>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '16px' }}>
            {viewMode === 'recommended'
              ? 'Update your skills and verified experience in your profile to boost your AI match recommendations.'
              : 'Try broadening your search query or selecting a different employment type.'}
          </p>
          {viewMode === 'recommended' && (
            <button onClick={() => setViewMode('all')} className="btn btn-secondary">
              Browse All Opportunities
            </button>
          )}
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(360px, 1fr))', gap: '24px' }}>
          {displayedJobs.map((job) => {
            const isSaved = savedJobIds.has(job.id);
            const appliedStatus = appliedJobMap.get(job.id);
            const isApplied = !!appliedStatus;

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
                  border: job.match?.overallScore >= 80 ? '1px solid rgba(16, 185, 129, 0.4)' : undefined,
                }}
                onClick={() => handleOpenDetailModal(job)}
              >
                <div>
                  {/* Top Header: Company, AI Match Badge & Bookmark */}
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <div style={{
                        width: '38px',
                        height: '38px',
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

                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      {/* AI Compatibility Score Badge */}
                      {isAuthenticated && isCandidate && job.match && (
                        <FitScoreBadge
                          score={job.match.overallScore}
                          fitLevel={job.match.fitLevel}
                          size="sm"
                        />
                      )}

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
                  </div>

                  {/* Title */}
                  <h3 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '10px', color: '#ffffff' }}>
                    {job.title}
                  </h3>

                  {/* Badges: Salary, Experience, Type */}
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px', marginBottom: '14px' }}>
                    {job.salary && (
                      <span className="badge badge-success" style={{ fontSize: '0.75rem' }}>
                        <DollarSign size={12} /> {job.salary}
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
                  {/* AI Breakdown Link */}
                  {isAuthenticated && isCandidate ? (
                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleOpenDetailModal(job);
                      }}
                      style={{
                        background: 'none',
                        border: 'none',
                        color: '#93c5fd',
                        fontSize: '0.8rem',
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '4px',
                        padding: 0,
                      }}
                    >
                      <Sparkles size={14} color="#60a5fa" /> View AI Match
                    </button>
                  ) : (
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Verified Posting</span>
                  )}

                  <div style={{ display: 'flex', gap: '8px' }}>
                    {isAuthenticated && isCandidate && (
                      isApplied ? (
                        <span className="badge badge-success" style={{ fontSize: '0.75rem', padding: '6px 12px' }}>
                          <CheckCircle2 size={13} /> Applied ({appliedStatus})
                        </span>
                      ) : (
                        <button
                          type="button"
                          onClick={(e) => {
                            e.stopPropagation();
                            setApplyingJob(job);
                          }}
                          className="btn btn-primary"
                          style={{ padding: '6px 16px', fontSize: '0.8rem' }}
                        >
                          Apply Now
                        </button>
                      )
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

            {/* Deep AI Multi-Factor Compatibility Breakdown Section */}
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
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Sparkles size={20} color="var(--primary-glow)" />
                    <h3 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#f8fafc', margin: 0 }}>
                      AI Compatibility Match Engine
                    </h3>
                  </div>

                  {matchDetail?.match && (
                    <FitScoreBadge
                      score={matchDetail.match.overallScore}
                      fitLevel={matchDetail.match.fitLevel}
                      size="md"
                    />
                  )}
                </div>

                {matchLoading ? (
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                    Calculating 3-dimensional compatibility fit breakdown...
                  </div>
                ) : matchDetail?.match ? (
                  <div>
                    {/* Multi-factor Score Gauges */}
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px', marginBottom: '16px' }}>
                      <div style={{ padding: '12px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', textAlign: 'center' }}>
                        <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                          Skills Match (60%)
                        </div>
                        <div style={{ fontSize: '1.35rem', fontWeight: 800, color: '#93c5fd', marginTop: '2px' }}>
                          {Math.round(matchDetail.match.skillScore)}%
                        </div>
                      </div>
                      <div style={{ padding: '12px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', textAlign: 'center' }}>
                        <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                          Experience (25%)
                        </div>
                        <div style={{ fontSize: '1.35rem', fontWeight: 800, color: '#a78bfa', marginTop: '2px' }}>
                          {Math.round(matchDetail.match.experienceScore)}%
                        </div>
                      </div>
                      <div style={{ padding: '12px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', textAlign: 'center' }}>
                        <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                          Profile Fit (15%)
                        </div>
                        <div style={{ fontSize: '1.35rem', fontWeight: 800, color: '#34d399', marginTop: '2px' }}>
                          {Math.round(matchDetail.match.profileCompletenessScore)}%
                        </div>
                      </div>
                    </div>

                    {/* Matched vs Missing Skills */}
                    {matchDetail.match.skillBreakdown && (
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginBottom: '16px' }}>
                        {matchDetail.match.skillBreakdown.matchedSkills?.length > 0 && (
                          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
                            <span style={{ fontSize: '0.75rem', fontWeight: 700, color: '#10b981' }}>✓ Matched Skills:</span>
                            {matchDetail.match.skillBreakdown.matchedSkills.map((s, idx) => (
                              <span key={idx} className="badge badge-success" style={{ fontSize: '0.75rem' }}>
                                <Check size={12} /> {s}
                              </span>
                            ))}
                          </div>
                        )}

                        {matchDetail.match.skillBreakdown.missingRequiredSkills?.length > 0 && (
                          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
                            <span style={{ fontSize: '0.75rem', fontWeight: 700, color: '#f59e0b' }}>⚠️ Missing Required:</span>
                            {matchDetail.match.skillBreakdown.missingRequiredSkills.map((s, idx) => (
                              <span key={idx} className="badge badge-warning" style={{ fontSize: '0.75rem' }}>
                                {s}
                              </span>
                            ))}
                          </div>
                        )}

                        {matchDetail.match.skillBreakdown.missingOptionalSkills?.length > 0 && (
                          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
                            <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)' }}>Optional:</span>
                            {matchDetail.match.skillBreakdown.missingOptionalSkills.map((s, idx) => (
                              <span key={idx} style={{ padding: '2px 8px', borderRadius: '4px', background: 'rgba(255,255,255,0.05)', fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                                {s}
                              </span>
                            ))}
                          </div>
                        )}
                      </div>
                    )}

                    {/* AI Recommendation Insights */}
                    {matchDetail.match.recommendationReasons?.length > 0 && (
                      <div style={{ padding: '12px 14px', borderRadius: '8px', background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border-subtle)' }}>
                        <div style={{ fontSize: '0.75rem', fontWeight: 700, color: '#93c5fd', textTransform: 'uppercase', marginBottom: '6px' }}>
                          AI Recommendation Rationale
                        </div>
                        <ul style={{ margin: 0, paddingLeft: '18px', fontSize: '0.825rem', color: 'var(--text-secondary)' }}>
                          {matchDetail.match.recommendationReasons.map((reason, idx) => (
                            <li key={idx} style={{ marginBottom: '3px' }}>{reason}</li>
                          ))}
                        </ul>
                      </div>
                    )}
                  </div>
                ) : (
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                    Match breakdown calculation unavailable.
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

            {/* Modal Bottom Actions */}
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '24px' }}>
              <button onClick={() => setSelectedJob(null)} className="btn btn-secondary">
                Close
              </button>
              {isAuthenticated && isCandidate && (
                appliedJobMap.has(selectedJob.id) ? (
                  <span className="badge badge-success" style={{ padding: '8px 16px', fontSize: '0.85rem' }}>
                    <CheckCircle2 size={16} /> Applied ({appliedJobMap.get(selectedJob.id)})
                  </span>
                ) : (
                  <button
                    onClick={() => {
                      setApplyingJob(selectedJob);
                      setSelectedJob(null);
                    }}
                    className="btn btn-primary"
                  >
                    Apply for this Position
                  </button>
                )
              )}
            </div>
          </div>
        </div>
      )}

      {/* Comprehensive Application Submission Modal */}
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
          <div className="card animate-fade-in" style={{ maxWidth: '540px', width: '100%', padding: '28px' }}>
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
                  Your profile and verified resume have been delivered to the hiring team.
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

                {/* Resume Status Check & Inline Uploader */}
                <div style={{ marginBottom: '18px' }}>
                  <label className="form-label">Resume Attachment *</label>

                  {candidateProfile?.resume?.fileUrl ? (
                    <div style={{
                      padding: '12px 14px',
                      borderRadius: '8px',
                      background: 'rgba(16, 185, 129, 0.08)',
                      border: '1px solid rgba(16, 185, 129, 0.25)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                    }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <FileText size={18} color="#10b981" />
                        <div>
                          <div style={{ fontSize: '0.85rem', fontWeight: 600, color: '#f8fafc' }}>
                            Verified Resume On File
                          </div>
                          <div style={{ fontSize: '0.75rem', color: '#6ee7b7' }}>
                            Ready to submit with application
                          </div>
                        </div>
                      </div>
                      <a
                        href={candidateProfile.resume.fileUrl}
                        target="_blank"
                        rel="noreferrer"
                        className="btn btn-secondary"
                        style={{ padding: '4px 10px', fontSize: '0.75rem' }}
                      >
                        Preview
                      </a>
                    </div>
                  ) : (
                    /* Inline Resume Uploader if none is on file */
                    <div style={{
                      border: '2px dashed var(--border-medium)',
                      borderRadius: '10px',
                      padding: '20px',
                      textAlign: 'center',
                      background: 'rgba(255,255,255,0.01)',
                      cursor: 'pointer',
                    }}
                    onClick={() => document.getElementById('apply-resume-input').click()}
                    >
                      <input
                        id="apply-resume-input"
                        type="file"
                        accept=".pdf"
                        style={{ display: 'none' }}
                        onChange={(e) => {
                          if (e.target.files?.[0]) setInlineResumeFile(e.target.files[0]);
                        }}
                      />
                      <UploadCloud size={28} color="var(--primary-glow)" style={{ margin: '0 auto 8px auto' }} />
                      <div style={{ fontSize: '0.85rem', fontWeight: 600, color: '#f8fafc' }}>
                        {inlineResumeFile ? inlineResumeFile.name : 'Upload PDF Resume (Required)'}
                      </div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '4px' }}>
                        {inlineResumeFile ? `${(inlineResumeFile.size / 1024 / 1024).toFixed(2)} MB • Ready` : 'Click to select PDF document (max 5MB)'}
                      </div>
                    </div>
                  )}
                </div>

                <div style={{ marginBottom: '16px' }}>
                  <label className="form-label">Cover Note / Candidate Pitch</label>
                  <textarea
                    className="form-input"
                    rows="3"
                    placeholder="Briefly explain your relevant experience and why you are an ideal fit for this role..."
                    value={coverNote}
                    onChange={(e) => setCoverNote(e.target.value)}
                  />
                </div>

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                  <button type="button" onClick={() => setApplyingJob(null)} className="btn btn-secondary">
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={applyMutation.isPending || (!candidateProfile?.resume?.fileUrl && !inlineResumeFile)}
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
