import React, { useState, useEffect } from 'react';
import { jobsApi, applicationApi, aiApi, resumeApi } from '../services/api';
import { FitScoreBadge } from '../components/common/FitScoreBadge';
import { 
  Plus, 
  Briefcase, 
  Users, 
  Sparkles, 
  CheckCircle2, 
  XCircle, 
  Clock, 
  Download, 
  FileText, 
  Building, 
  MapPin, 
  DollarSign, 
  Calendar,
  AlertCircle,
  X
} from 'lucide-react';

export const RecruiterDashboardPage = () => {
  const [jobs, setJobs] = useState([]);
  const [selectedJob, setSelectedJob] = useState(null);
  const [applications, setApplications] = useState([]);
  const [loadingJobs, setLoadingJobs] = useState(true);
  const [loadingApps, setLoadingApps] = useState(false);

  // Post Job Modal State
  const [showPostModal, setShowPostModal] = useState(false);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [requirements, setRequirements] = useState('');
  const [experienceYears, setExperienceYears] = useState(2);
  const [salaryMin, setSalaryMin] = useState(80000);
  const [salaryMax, setSalaryMax] = useState(120000);
  const [aiGenerating, setAiGenerating] = useState(false);
  const [postLoading, setPostLoading] = useState(false);
  const [modalError, setModalError] = useState(null);

  // Status Update State
  const [reviewNotes, setReviewNotes] = useState('');
  const [updatingAppId, setUpdatingAppId] = useState(null);
  const [actionSuccess, setActionSuccess] = useState(null);

  useEffect(() => {
    fetchRecruiterJobs();
  }, []);

  const fetchRecruiterJobs = async () => {
    setLoadingJobs(true);
    try {
      const data = await jobsApi.getAll();
      const list = data?.content || (Array.isArray(data) ? data : []);
      setJobs(list);
      if (list.length > 0 && !selectedJob) {
        selectJob(list[0]);
      }
    } catch (err) {
      console.error('Failed to load recruiter jobs', err);
    } finally {
      setLoadingJobs(false);
    }
  };

  const selectJob = async (job) => {
    setSelectedJob(job);
    setLoadingApps(true);
    try {
      const data = await applicationApi.getJobApplications(job.id);
      setApplications(Array.isArray(data) ? data : (data?.content || []));
    } catch (err) {
      console.warn('Could not load job applications', err);
      setApplications([]);
    } finally {
      setLoadingApps(false);
    }
  };

  const handleAiGenerateJob = async () => {
    if (!title) {
      setModalError('Please enter a Job Title first so Gemini AI can generate relevant content.');
      return;
    }

    setAiGenerating(true);
    setModalError(null);
    try {
      const aiResponse = await aiApi.generateJobDescription({
        jobTitle: title,
        industry: 'Technology / Software Engineering',
        experienceLevel: `${experienceYears} years`,
        keyRequirements: requirements || 'Java, Spring Boot, MySQL, REST APIs, Microservices',
      });

      if (aiResponse) {
        setDescription(aiResponse.generatedDescription || aiResponse.description || '');
      }
    } catch (err) {
      setModalError(err.message || 'Gemini AI generation error or rate limit reached.');
    } finally {
      setAiGenerating(false);
    }
  };

  const handleCreateJob = async (e) => {
    e.preventDefault();
    setPostLoading(true);
    setModalError(null);

    try {
      await jobsApi.create({
        title,
        description,
        experienceYears: parseFloat(experienceYears) || 0,
        salaryMin: parseFloat(salaryMin) || null,
        salaryMax: parseFloat(salaryMax) || null,
        status: 'ACTIVE',
        skillNames: ['Java', 'Spring Boot', 'MySQL', 'REST APIs'],
      });

      setShowPostModal(false);
      setTitle('');
      setDescription('');
      setRequirements('');
      fetchRecruiterJobs();
    } catch (err) {
      setModalError(err.message || 'Failed to publish new job');
    } finally {
      setPostLoading(false);
    }
  };

  const handleUpdateStatus = async (appId, newStatus) => {
    setUpdatingAppId(appId);
    try {
      await applicationApi.updateStatus(appId, newStatus, reviewNotes || `Status updated to ${newStatus}`);
      setActionSuccess(`Candidate status updated to ${newStatus}! Real-time STOMP alert sent.`);
      setTimeout(() => setActionSuccess(null), 3000);
      setReviewNotes('');
      // Refresh current applications
      if (selectedJob) {
        const data = await applicationApi.getJobApplications(selectedJob.id);
        setApplications(Array.isArray(data) ? data : (data?.content || []));
      }
    } catch (err) {
      console.error('Failed to update status', err);
    } finally {
      setUpdatingAppId(null);
    }
  };

  return (
    <div className="container animate-fade-in" style={{ padding: '40px 1.5rem 80px 1.5rem' }}>
      {/* Top Header */}
      <div style={{
        display: 'flex',
        flexWrap: 'wrap',
        alignItems: 'center',
        justifyContent: 'space-between',
        gap: '20px',
        marginBottom: '32px',
      }}>
        <div>
          <h1 style={{ fontSize: '2.25rem', marginBottom: '8px' }}>
            Recruiter <span className="text-gradient">Intelligence Hub</span>
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
            Manage active requisitions, evaluate candidate fit scores, and push real-time status alerts
          </p>
        </div>

        <button
          onClick={() => setShowPostModal(true)}
          className="btn btn-primary btn-lg"
          style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
        >
          <Plus size={20} /> Post New Opportunity
        </button>
      </div>

      {actionSuccess && (
        <div style={{
          padding: '12px 16px',
          borderRadius: '8px',
          background: 'rgba(16, 185, 129, 0.15)',
          color: '#6ee7b7',
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          marginBottom: '24px',
        }}>
          <CheckCircle2 size={18} />
          <span>{actionSuccess}</span>
        </div>
      )}

      {/* Main Two-Column Layout */}
      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1fr) minmax(0, 1.6fr)', gap: '28px' }}>
        {/* Left Column: Job Postings List */}
        <div>
          <div style={{ fontWeight: 700, fontSize: '1.1rem', color: '#f8fafc', marginBottom: '16px' }}>
            Your Active Postings ({jobs.length})
          </div>

          {loadingJobs ? (
            <div style={{ color: 'var(--text-muted)' }}>Loading active postings...</div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {jobs.map((job) => {
                const isCurrent = selectedJob?.id === job.id;
                return (
                  <div
                    key={job.id}
                    onClick={() => selectJob(job)}
                    className="glass-panel"
                    style={{
                      padding: '18px',
                      cursor: 'pointer',
                      borderLeft: isCurrent ? '4px solid #3b82f6' : '1px solid var(--border-subtle)',
                      background: isCurrent ? 'rgba(30, 41, 59, 0.9)' : 'var(--bg-card)',
                      transition: 'all 0.2s',
                    }}
                  >
                    <div style={{ fontWeight: 600, fontSize: '1.05rem', color: '#f8fafc', marginBottom: '4px' }}>
                      {job.title}
                    </div>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', display: 'flex', gap: '12px' }}>
                      <span>{job.experienceYears ? `${job.experienceYears}+ yrs` : 'Any exp'}</span>
                      <span>•</span>
                      <span>{job.status || 'ACTIVE'}</span>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Right Column: Applicants & Talent Review */}
        <div>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
            <div style={{ fontWeight: 700, fontSize: '1.1rem', color: '#f8fafc' }}>
              Candidate Pipeline for: <span style={{ color: '#3b82f6' }}>{selectedJob?.title || 'Select a Job'}</span>
            </div>
            <span className="badge badge-primary">{applications.length} Applicants</span>
          </div>

          {loadingApps ? (
            <div style={{ color: 'var(--text-muted)', padding: '24px 0' }}>Loading applicants...</div>
          ) : applications.length === 0 ? (
            <div className="glass-panel" style={{ textAlign: 'center', padding: '48px', color: 'var(--text-secondary)' }}>
              <Users size={36} style={{ margin: '0 auto 10px auto', opacity: 0.5 }} />
              <h4>No applicants for this role yet</h4>
              <p style={{ fontSize: '0.85rem', marginTop: '4px' }}>
                Candidates will appear here as soon as they submit applications.
              </p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              {applications.map((app) => {
                const candidate = app.candidate || {};
                const resumeFileName = candidate.resumeFileName || app.resumeFileName;

                return (
                  <div
                    key={app.id}
                    className="glass-panel"
                    style={{ padding: '24px' }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
                      <div>
                        <div style={{ fontWeight: 700, fontSize: '1.15rem', color: '#f8fafc' }}>
                          {candidate.name || app.candidateName || 'Candidate'}
                        </div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                          {candidate.email || app.candidateEmail}
                        </div>
                      </div>

                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        {app.fitScore !== undefined && (
                          <FitScoreBadge score={app.fitScore} fitLevel={app.fitLevel} size="sm" />
                        )}
                        <span className="badge badge-purple">{app.status}</span>
                      </div>
                    </div>

                    {/* Candidate Quick Stats */}
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px', fontSize: '0.825rem', color: 'var(--text-secondary)', margin: '12px 0' }}>
                      <div>Experience: <strong style={{ color: '#f8fafc' }}>{candidate.experienceYears ?? 0} yrs</strong></div>
                      <div>Education: <strong style={{ color: '#f8fafc' }}>{candidate.education || 'Degree specified'}</strong></div>
                    </div>

                    {app.coverNote && (
                      <div style={{
                        padding: '10px 14px',
                        borderRadius: '8px',
                        background: 'rgba(255, 255, 255, 0.03)',
                        borderLeft: '3px solid #6366f1',
                        fontSize: '0.825rem',
                        color: '#cbd5e1',
                        marginBottom: '16px',
                      }}>
                        "{app.coverNote}"
                      </div>
                    )}

                    {/* Action Bar */}
                    <div style={{
                      display: 'flex',
                      flexWrap: 'wrap',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      gap: '12px',
                      borderTop: '1px solid var(--border-subtle)',
                      paddingTop: '16px',
                    }}>
                      {resumeFileName ? (
                        <a
                          href={resumeApi.downloadUrl(resumeFileName)}
                          target="_blank"
                          rel="noreferrer"
                          className="btn btn-secondary btn-sm"
                        >
                          <Download size={14} /> Download Resume
                        </a>
                      ) : (
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>No resume file attached</span>
                      )}

                      <div style={{ display: 'flex', gap: '8px' }}>
                        <button
                          type="button"
                          disabled={updatingAppId === app.id}
                          onClick={() => handleUpdateStatus(app.id, 'SHORTLISTED')}
                          className="btn btn-emerald btn-sm"
                        >
                          <CheckCircle2 size={14} /> Shortlist
                        </button>
                        <button
                          type="button"
                          disabled={updatingAppId === app.id}
                          onClick={() => handleUpdateStatus(app.id, 'INTERVIEW_SCHEDULED')}
                          className="btn btn-primary btn-sm"
                        >
                          <Calendar size={14} /> Interview
                        </button>
                        <button
                          type="button"
                          disabled={updatingAppId === app.id}
                          onClick={() => handleUpdateStatus(app.id, 'REJECTED')}
                          className="btn btn-danger btn-sm"
                        >
                          <XCircle size={14} /> Reject
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>

      {/* Post Job Modal with Gemini LLM Assist */}
      {showPostModal && (
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
          <div className="glass-panel animate-fade-in" style={{
            maxWidth: '680px',
            width: '100%',
            maxHeight: '90vh',
            overflowY: 'auto',
            padding: '36px',
            background: '#0f172a',
            position: 'relative',
          }}>
            <button
              onClick={() => setShowPostModal(false)}
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

            <h2 style={{ fontSize: '1.6rem', marginBottom: '6px' }}>Publish Job Requisition</h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginBottom: '24px' }}>
              Create an opportunity or let Google Gemini LLM generate a compelling description.
            </p>

            {modalError && (
              <div style={{
                padding: '12px 14px',
                borderRadius: '8px',
                background: 'rgba(244, 63, 94, 0.15)',
                color: '#fda4af',
                fontSize: '0.85rem',
                marginBottom: '20px',
              }}>
                {modalError}
              </div>
            )}

            <form onSubmit={handleCreateJob}>
              <div className="form-group">
                <label className="form-label">Job Title</label>
                <div style={{ display: 'flex', gap: '8px' }}>
                  <input
                    type="text"
                    required
                    className="form-input"
                    placeholder="e.g. Senior Backend Engineer (Java & Cloud)"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                  />
                  <button
                    type="button"
                    disabled={aiGenerating}
                    onClick={handleAiGenerateJob}
                    className="btn btn-secondary"
                    style={{ whiteSpace: 'nowrap', display: 'flex', alignItems: 'center', gap: '6px' }}
                  >
                    <Sparkles size={16} color="#818cf8" />
                    {aiGenerating ? 'Generating...' : 'Gemini AI Draft'}
                  </button>
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '14px' }}>
                <div className="form-group">
                  <label className="form-label">Experience (Years)</label>
                  <input
                    type="number"
                    min="0"
                    step="0.5"
                    className="form-input"
                    value={experienceYears}
                    onChange={(e) => setExperienceYears(e.target.value)}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Min Salary ($)</label>
                  <input
                    type="number"
                    className="form-input"
                    value={salaryMin}
                    onChange={(e) => setSalaryMin(e.target.value)}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Max Salary ($)</label>
                  <input
                    type="number"
                    className="form-input"
                    value={salaryMax}
                    onChange={(e) => setSalaryMax(e.target.value)}
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Key Requirements / Technology Stack</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. Java 21, Spring Boot, MySQL, Docker, Kubernetes"
                  value={requirements}
                  onChange={(e) => setRequirements(e.target.value)}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Full Job Description</label>
                <textarea
                  required
                  className="form-textarea"
                  rows={6}
                  placeholder="Detail the responsibilities, team impact, and required qualifications..."
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                />
              </div>

              <div style={{ display: 'flex', gap: '12px', marginTop: '24px' }}>
                <button
                  type="submit"
                  disabled={postLoading}
                  className="btn btn-primary"
                  style={{ flex: 1, padding: '12px' }}
                >
                  {postLoading ? 'Publishing Opportunity...' : 'Publish Opportunity'}
                </button>
                <button
                  type="button"
                  onClick={() => setShowPostModal(false)}
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
