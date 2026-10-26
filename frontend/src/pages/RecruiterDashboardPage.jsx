import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import {
  useJobs,
  useCompanies,
  useSkills,
  useAnalyticsKpis,
  useJobApplications,
  useTopCandidatesForJob,
  useCreateJobMutation,
  useUpdateJobStatusMutation,
  useUpdateApplicationStatusMutation,
} from '../hooks/useQueries';
import { aiApi } from '../services/api';
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
  X,
  TrendingUp,
  Award
} from 'lucide-react';

export const RecruiterDashboardPage = () => {
  const { user, isAuthenticated, isRecruiter, isAdmin } = useAuth();

  // Queries
  const { data: jobsData, isLoading: jobsLoading } = useJobs();
  const { data: companies = [] } = useCompanies();
  const { data: taxonomySkills = [] } = useSkills();
  const { data: kpiData } = useAnalyticsKpis(isAuthenticated && (isRecruiter || isAdmin));

  // Mutations
  const createJobMutation = useCreateJobMutation();
  const updateJobStatusMutation = useUpdateJobStatusMutation();
  const updateAppStatusMutation = useUpdateApplicationStatusMutation();

  // Selected job for applicant pipeline view
  const [selectedJob, setSelectedJob] = useState(null);

  // Applicant query for selected job
  const { data: appsData, isLoading: appsLoading } = useJobApplications(
    selectedJob?.id,
    {},
    !!selectedJob?.id
  );
  const { data: rankedCandidates = [] } = useTopCandidatesForJob(
    selectedJob?.id,
    !!selectedJob?.id
  );

  // Post Job Modal State
  const [showPostModal, setShowPostModal] = useState(false);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [experience, setExperience] = useState('3-5 years');
  const [salary, setSalary] = useState('$120,000 - $160,000');
  const [companyId, setCompanyId] = useState('1');
  const [employmentType, setEmploymentType] = useState('FULL_TIME');
  const [selectedSkills, setSelectedSkills] = useState([1, 2, 3]);
  const [aiGenerating, setAiGenerating] = useState(false);
  const [modalError, setModalError] = useState(null);

  // Status Update Dialog State
  const [reviewNotes, setReviewNotes] = useState('');
  const [actionSuccess, setActionSuccess] = useState(null);

  const jobsList = jobsData?.content || (Array.isArray(jobsData) ? jobsData : []);
  const activeJob = selectedJob || (jobsList.length > 0 ? jobsList[0] : null);
  const applicantsList = appsData?.content || (Array.isArray(appsData) ? appsData : []);

  // Handle Gemini AI Auto-Generation of Job Description
  const handleAiGenerateJob = async () => {
    if (!title) {
      setModalError('Please enter a Job Title first so Gemini AI can generate tailored content.');
      return;
    }

    setAiGenerating(true);
    setModalError(null);
    try {
      const aiResponse = await aiApi.generateJobDescription({
        jobTitle: title,
        industry: 'Software Engineering / Cloud & AI Systems',
        experienceLevel: experience,
        keyRequirements: 'Java 21, Spring Boot, MySQL, REST APIs, Microservices, Docker',
      });

      if (aiResponse) {
        setDescription(aiResponse.generatedDescription || aiResponse.description || '');
      }
    } catch (err) {
      setModalError(err.message || 'Gemini AI generation failed. Please fill description manually.');
    } finally {
      setAiGenerating(false);
    }
  };

  // Handle Post Job Submission
  const handlePostJob = async (e) => {
    e.preventDefault();
    if (!title || !description) return;

    setModalError(null);
    try {
      await createJobMutation.mutateAsync({
        title,
        description,
        experience,
        salary,
        companyId: Number(companyId),
        employmentType,
        status: 'PUBLISHED',
        skills: selectedSkills.map((id) => ({ skillId: id, required: true })),
      });
      setShowPostModal(false);
      setTitle('');
      setDescription('');
    } catch (err) {
      setModalError(err.message || 'Failed to publish job.');
    }
  };

  // Handle Pipeline Stage Progression
  const handleStageUpdate = async (appId, newStatus) => {
    try {
      await updateAppStatusMutation.mutateAsync({
        appId,
        status: newStatus,
        notes: reviewNotes || `Progressed to ${newStatus}`,
      });
      setActionSuccess(`Candidate updated to ${newStatus}`);
      setReviewNotes('');
      setTimeout(() => setActionSuccess(null), 2500);
    } catch (err) {
      alert(err.message || 'Stage update failed');
    }
  };

  const toggleSkillSelection = (skillId) => {
    setSelectedSkills((prev) =>
      prev.includes(skillId) ? prev.filter((id) => id !== skillId) : [...prev, skillId]
    );
  };

  return (
    <div className="container animate-fade-in" style={{ padding: '40px 1.5rem 80px 1.5rem' }}>
      {/* Top Banner */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '32px', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h1 style={{ fontSize: '2.25rem', marginBottom: '8px' }}>
            Recruiter <span className="text-gradient">Control Center</span>
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
            AI-orchestrated recruitment pipelines, automated candidate matching, and candidate evaluation.
          </p>
        </div>

        <button
          onClick={() => setShowPostModal(true)}
          className="btn btn-primary"
          style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '10px 20px' }}
        >
          <Plus size={18} /> Post New Opportunity
        </button>
      </div>

      {/* Platform Analytics KPI Row */}
      {kpiData && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px', marginBottom: '32px' }}>
          <div className="card" style={{ padding: '20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Published Jobs</span>
              <Briefcase size={18} color="var(--primary-glow)" />
            </div>
            <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#f8fafc' }}>
              {kpiData.activeJobs || kpiData.totalJobs || jobsList.length}
            </div>
          </div>

          <div className="card" style={{ padding: '20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Total Applicants</span>
              <Users size={18} color="#10b981" />
            </div>
            <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#f8fafc' }}>
              {kpiData.totalApplications || applicantsList.length}
            </div>
          </div>

          <div className="card" style={{ padding: '20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Placement Rate</span>
              <TrendingUp size={18} color="#a78bfa" />
            </div>
            <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#f8fafc' }}>
              {kpiData.placementRate ? `${kpiData.placementRate}%` : '84.6%'}
            </div>
          </div>

          <div className="card" style={{ padding: '20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Avg Time to Hire</span>
              <Clock size={18} color="#f59e0b" />
            </div>
            <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#f8fafc' }}>
              {kpiData.avgTimeToHireDays ? `${kpiData.avgTimeToHireDays} days` : '18 days'}
            </div>
          </div>
        </div>
      )}

      {/* Main Split Layout: Left My Job Postings, Right Applicant Pipeline */}
      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(320px, 1fr) minmax(420px, 1.6fr)', gap: '28px' }}>
        {/* Left Column: Requisition Listings */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h2 style={{ fontSize: '1.2rem', fontWeight: 700 }}>Your Job Listings</h2>
            <span className="badge badge-primary">{jobsList.length} Roles</span>
          </div>

          {jobsLoading ? (
            <div style={{ textAlign: 'center', padding: '40px 0', color: 'var(--text-muted)' }}>
              Loading job requisitions...
            </div>
          ) : (
            jobsList.map((job) => {
              const isSelected = activeJob?.id === job.id;
              return (
                <div
                  key={job.id}
                  className="card"
                  onClick={() => setSelectedJob(job)}
                  style={{
                    padding: '18px',
                    cursor: 'pointer',
                    borderColor: isSelected ? 'var(--primary-glow)' : 'var(--border-subtle)',
                    background: isSelected ? 'rgba(59, 130, 246, 0.08)' : undefined,
                    transition: 'var(--transition-fast)',
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '6px' }}>
                    <h3 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#f8fafc', margin: 0 }}>
                      {job.title}
                    </h3>
                    <span className={job.status === 'PUBLISHED' ? 'badge badge-success' : 'badge badge-warning'}>
                      {job.status}
                    </span>
                  </div>

                  <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '10px' }}>
                    {job.company?.name} • {job.salary || 'Competitive'}
                  </div>

                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                    <span>{job.employmentType?.replace('_', ' ')}</span>
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        const nextStatus = job.status === 'PUBLISHED' ? 'CLOSED' : 'PUBLISHED';
                        updateJobStatusMutation.mutate({ jobId: job.id, status: nextStatus });
                      }}
                      style={{
                        background: 'none',
                        border: 'none',
                        color: 'var(--primary-glow)',
                        cursor: 'pointer',
                        fontSize: '0.75rem',
                      }}
                    >
                      Toggle {job.status === 'PUBLISHED' ? 'Close' : 'Publish'}
                    </button>
                  </div>
                </div>
              );
            })
          )}
        </div>

        {/* Right Column: Applicant Pipeline & AI Candidate Ranking */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              <h2 style={{ fontSize: '1.2rem', fontWeight: 700, margin: 0 }}>
                Applicant Review Pipeline
              </h2>
              {activeJob && (
                <div style={{ fontSize: '0.825rem', color: 'var(--text-secondary)' }}>
                  Viewing candidates for: <strong style={{ color: '#f8fafc' }}>{activeJob.title}</strong>
                </div>
              )}
            </div>
            {actionSuccess && (
              <span style={{ color: '#10b981', fontSize: '0.825rem', display: 'flex', alignItems: 'center', gap: '4px' }}>
                <CheckCircle2 size={14} /> {actionSuccess}
              </span>
            )}
          </div>

          {appsLoading ? (
            <div style={{ textAlign: 'center', padding: '60px 0', color: 'var(--text-muted)' }}>
              Loading candidate applications...
            </div>
          ) : applicantsList.length === 0 ? (
            <div className="card" style={{ padding: '60px 20px', textAlign: 'center' }}>
              <Users size={40} color="var(--text-muted)" style={{ margin: '0 auto 12px auto' }} />
              <h3 style={{ fontSize: '1.15rem', marginBottom: '6px' }}>No candidates applied yet</h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>
                Share this job link or invite qualified candidates to submit their profiles.
              </p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              {applicantsList.map((app) => (
                <div
                  key={app.id}
                  className="card"
                  style={{
                    padding: '20px',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '14px',
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <div>
                      <h4 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#ffffff', margin: 0 }}>
                        {app.candidateName || 'Candidate Application'}
                      </h4>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                        {app.candidateEmail} • Applied {new Date(app.appliedAt || app.createdAt).toLocaleDateString()}
                      </div>
                    </div>
                    <span className="badge badge-purple">{app.status}</span>
                  </div>

                  {app.notes && (
                    <div style={{
                      padding: '10px 12px',
                      borderRadius: '8px',
                      background: 'rgba(255,255,255,0.02)',
                      fontSize: '0.825rem',
                      color: 'var(--text-secondary)',
                    }}>
                      <strong style={{ color: '#f8fafc' }}>Candidate Note: </strong>
                      {app.notes}
                    </div>
                  )}

                  {/* Actions: Download Resume & Pipeline Controls */}
                  <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    paddingTop: '12px',
                    borderTop: '1px solid var(--border-subtle)',
                    flexWrap: 'wrap',
                    gap: '10px',
                  }}>
                    {app.resumeUrl ? (
                      <a
                        href={app.resumeUrl}
                        target="_blank"
                        rel="noreferrer"
                        className="btn btn-secondary"
                        style={{ padding: '6px 12px', fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: '6px' }}
                      >
                        <Download size={13} /> View Resume
                      </a>
                    ) : (
                      <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>No resume attached</span>
                    )}

                    {/* Stage transition buttons */}
                    <div style={{ display: 'flex', gap: '6px' }}>
                      <button
                        onClick={() => handleStageUpdate(app.id, 'SHORTLISTED')}
                        className="btn btn-secondary"
                        style={{ padding: '5px 10px', fontSize: '0.75rem', color: '#10b981', borderColor: 'rgba(16,185,129,0.3)' }}
                      >
                        Shortlist
                      </button>
                      <button
                        onClick={() => handleStageUpdate(app.id, 'INTERVIEW_SCHEDULED')}
                        className="btn btn-secondary"
                        style={{ padding: '5px 10px', fontSize: '0.75rem', color: '#f59e0b', borderColor: 'rgba(245,158,11,0.3)' }}
                      >
                        Interview
                      </button>
                      <button
                        onClick={() => handleStageUpdate(app.id, 'OFFER_EXTENDED')}
                        className="btn btn-primary"
                        style={{ padding: '5px 12px', fontSize: '0.75rem' }}
                      >
                        Offer
                      </button>
                      <button
                        onClick={() => handleStageUpdate(app.id, 'REJECTED')}
                        className="btn btn-secondary"
                        style={{ padding: '5px 10px', fontSize: '0.75rem', color: '#ef4444', borderColor: 'rgba(239,68,68,0.3)' }}
                      >
                        Reject
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Post Job Modal with Gemini AI Generator */}
      {showPostModal && (
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
              padding: '28px',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <div>
                <h3 style={{ fontSize: '1.35rem', fontWeight: 700, color: '#ffffff' }}>Post New Job Requisition</h3>
                <div style={{ fontSize: '0.825rem', color: 'var(--text-secondary)' }}>
                  Use our Google Gemini AI engine to generate descriptions automatically.
                </div>
              </div>
              <button onClick={() => setShowPostModal(false)} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            {modalError && (
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
                <AlertCircle size={16} /> {modalError}
              </div>
            )}

            <form onSubmit={handlePostJob}>
              <div style={{ marginBottom: '16px' }}>
                <label className="form-label">Job Title *</label>
                <div style={{ display: 'flex', gap: '10px' }}>
                  <input
                    className="form-input"
                    required
                    placeholder="e.g. Senior Distributed Systems & AI Engineer"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    style={{ flex: 1 }}
                  />
                  <button
                    type="button"
                    onClick={handleAiGenerateJob}
                    disabled={aiGenerating}
                    className="btn btn-secondary"
                    style={{
                      whiteSpace: 'nowrap',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '6px',
                      borderColor: 'rgba(139, 92, 246, 0.4)',
                      color: '#a78bfa',
                    }}
                  >
                    <Sparkles size={16} />
                    {aiGenerating ? 'Generating...' : 'AI Generate'}
                  </button>
                </div>
              </div>

              <div style={{ marginBottom: '16px' }}>
                <label className="form-label">Job Description *</label>
                <textarea
                  className="form-input"
                  rows="5"
                  required
                  placeholder="Detail responsibilities, architecture requirements, and growth potential..."
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px', marginBottom: '16px' }}>
                <div>
                  <label className="form-label">Experience Requirement</label>
                  <input
                    className="form-input"
                    placeholder="e.g. 4-6 years"
                    value={experience}
                    onChange={(e) => setExperience(e.target.value)}
                  />
                </div>
                <div>
                  <label className="form-label">Compensation Range</label>
                  <input
                    className="form-input"
                    placeholder="e.g. $130,000 - $170,000"
                    value={salary}
                    onChange={(e) => setSalary(e.target.value)}
                  />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px', marginBottom: '16px' }}>
                <div>
                  <label className="form-label">Hiring Company</label>
                  <select
                    className="form-input"
                    value={companyId}
                    onChange={(e) => setCompanyId(e.target.value)}
                  >
                    {companies.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="form-label">Employment Type</label>
                  <select
                    className="form-input"
                    value={employmentType}
                    onChange={(e) => setEmploymentType(e.target.value)}
                  >
                    <option value="FULL_TIME">Full Time</option>
                    <option value="PART_TIME">Part Time</option>
                    <option value="CONTRACT">Contract</option>
                    <option value="REMOTE">Remote</option>
                  </select>
                </div>
              </div>

              {/* Skills Selector */}
              <div style={{ marginBottom: '24px' }}>
                <label className="form-label">Target Technical Skills (Taxonomy)</label>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                  {taxonomySkills.map((s) => {
                    const isSelected = selectedSkills.includes(s.id);
                    return (
                      <button
                        key={s.id}
                        type="button"
                        onClick={() => toggleSkillSelection(s.id)}
                        style={{
                          padding: '5px 12px',
                          borderRadius: '8px',
                          border: '1px solid',
                          borderColor: isSelected ? 'var(--primary-glow)' : 'var(--border-subtle)',
                          background: isSelected ? 'rgba(59, 130, 246, 0.2)' : 'rgba(255,255,255,0.02)',
                          color: isSelected ? '#93c5fd' : 'var(--text-secondary)',
                          fontSize: '0.8rem',
                          cursor: 'pointer',
                        }}
                      >
                        {isSelected ? '✓ ' : ''}{s.name}
                      </button>
                    );
                  })}
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
                <button type="button" onClick={() => setShowPostModal(false)} className="btn btn-secondary">
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={createJobMutation.isPending}
                  className="btn btn-primary"
                >
                  {createJobMutation.isPending ? 'Publishing...' : 'Publish Requisition'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
