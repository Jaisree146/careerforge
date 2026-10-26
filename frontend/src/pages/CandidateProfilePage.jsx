import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import {
  useCandidateProfile,
  useCandidateSkills,
  useSkills,
  useUpdateCandidateBioMutation,
  useAddEducationMutation,
  useDeleteEducationMutation,
  useAddExperienceMutation,
  useDeleteExperienceMutation,
  useAddSkillMutation,
  useRemoveSkillMutation,
  useUploadResumeMutation,
} from '../hooks/useQueries';
import { 
  User, 
  UploadCloud, 
  FileText, 
  Sparkles, 
  CheckCircle, 
  AlertCircle, 
  Download, 
  Plus, 
  Trash2, 
  Save,
  Brain,
  Lightbulb,
  GraduationCap,
  Briefcase,
  Layers,
  Calendar,
  X
} from 'lucide-react';

export const CandidateProfilePage = () => {
  const { user, isAuthenticated, isCandidate } = useAuth();

  // TanStack Queries
  const { data: profile, isLoading: profileLoading } = useCandidateProfile(isAuthenticated && isCandidate);
  const { data: candidateSkills = [] } = useCandidateSkills(isAuthenticated && isCandidate);
  const { data: taxonomySkills = [] } = useSkills();

  // Mutations
  const updateBioMutation = useUpdateCandidateBioMutation();
  const addEduMutation = useAddEducationMutation();
  const deleteEduMutation = useDeleteEducationMutation();
  const addExpMutation = useAddExperienceMutation();
  const deleteExpMutation = useDeleteExperienceMutation();
  const addSkillMutation = useAddSkillMutation();
  const removeSkillMutation = useRemoveSkillMutation();
  const uploadResumeMutation = useUploadResumeMutation();

  // Form States
  const [bio, setBio] = useState('');
  const [bioSuccess, setBioSuccess] = useState(false);

  // Resume Upload State
  const [selectedFile, setSelectedFile] = useState(null);
  const [dragActive, setDragActive] = useState(false);
  const [aiAnalysisResult, setAiAnalysisResult] = useState(null);

  // Education Modal
  const [showEduModal, setShowEduModal] = useState(false);
  const [eduDegree, setEduDegree] = useState('');
  const [eduInstitution, setEduInstitution] = useState('');
  const [eduStart, setEduStart] = useState('');
  const [eduEnd, setEduEnd] = useState('');

  // Experience Modal
  const [showExpModal, setShowExpModal] = useState(false);
  const [expRole, setExpRole] = useState('');
  const [expCompany, setExpCompany] = useState('');
  const [expStart, setExpStart] = useState('');
  const [expEnd, setExpEnd] = useState('');

  // Skill Add
  const [selectedSkillId, setSelectedSkillId] = useState('');

  // Sync profile bio
  useEffect(() => {
    if (profile?.bio) {
      setBio(profile.bio);
    }
  }, [profile]);

  // Handle Bio Update
  const handleBioSubmit = async (e) => {
    e.preventDefault();
    await updateBioMutation.mutateAsync({ bio });
    setBioSuccess(true);
    setTimeout(() => setBioSuccess(false), 2500);
  };

  // Handle Resume Drop & File Select
  const handleFileDrop = (e) => {
    e.preventDefault();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFileSelected(e.dataTransfer.files[0]);
    }
  };

  const handleFileSelected = (file) => {
    if (!file.name.toLowerCase().endsWith('.pdf')) {
      alert('Only PDF resume documents are supported');
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      alert('File size exceeds 5MB limit');
      return;
    }
    setSelectedFile(file);
  };

  // Handle Resume Upload with immediate AI Analysis
  const handleResumeUpload = async () => {
    if (!selectedFile) return;
    try {
      const res = await uploadResumeMutation.mutateAsync({
        file: selectedFile,
        analyzeWithAi: true,
      });
      if (res?.aiAnalysis) {
        setAiAnalysisResult(res.aiAnalysis);
      }
      setSelectedFile(null);
    } catch (err) {
      console.error('Resume upload failed', err);
    }
  };

  // Handle Add Education
  const handleAddEducation = async (e) => {
    e.preventDefault();
    if (!eduDegree || !eduInstitution) return;
    await addEduMutation.mutateAsync({
      degree: eduDegree,
      institution: eduInstitution,
      startDate: eduStart || null,
      endDate: eduEnd || null,
    });
    setEduDegree('');
    setEduInstitution('');
    setEduStart('');
    setEduEnd('');
    setShowEduModal(false);
  };

  // Handle Add Experience
  const handleAddExperience = async (e) => {
    e.preventDefault();
    if (!expRole || !expCompany) return;
    await addExpMutation.mutateAsync({
      role: expRole,
      companyName: expCompany,
      startDate: expStart || null,
      endDate: expEnd || null,
    });
    setExpRole('');
    setExpCompany('');
    setExpStart('');
    setExpEnd('');
    setShowExpModal(false);
  };

  // Handle Add Skill from Taxonomy
  const handleAddSkill = async (e) => {
    e.preventDefault();
    if (!selectedSkillId) return;
    await addSkillMutation.mutateAsync(Number(selectedSkillId));
    setSelectedSkillId('');
  };

  if (!isAuthenticated) {
    return (
      <div className="container" style={{ padding: '80px 1.5rem', textAlign: 'center' }}>
        <h2 style={{ fontSize: '1.75rem', marginBottom: '12px' }}>Authentication Required</h2>
        <p style={{ color: 'var(--text-secondary)' }}>Please log in to manage your candidate profile.</p>
      </div>
    );
  }

  return (
    <div className="container animate-fade-in" style={{ padding: '40px 1.5rem 80px 1.5rem' }}>
      {/* Header */}
      <div style={{ marginBottom: '32px' }}>
        <h1 style={{ fontSize: '2.25rem', marginBottom: '8px' }}>
          Candidate <span className="text-gradient">Profile & Career Vault</span>
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
          Manage your verified professional credentials, PDF resume, and AI matching parameters.
        </p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: '28px' }}>
        {/* Left Column: Personal Bio & Skills */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
          {/* Identity & Bio Card */}
          <div className="card" style={{ padding: '24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '14px', marginBottom: '20px' }}>
              <div style={{
                width: '46px',
                height: '46px',
                borderRadius: '50%',
                background: 'var(--primary-gradient)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '1.25rem',
                fontWeight: 700,
              }}>
                {user?.name?.[0]?.toUpperCase() || 'C'}
              </div>
              <div>
                <h2 style={{ fontSize: '1.2rem', fontWeight: 700, margin: 0 }}>{user?.name}</h2>
                <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>{user?.email}</div>
              </div>
            </div>

            <form onSubmit={handleBioSubmit}>
              <div style={{ marginBottom: '16px' }}>
                <label className="form-label" style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Professional Summary & Bio</span>
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{bio.length}/2000</span>
                </label>
                <textarea
                  className="form-input"
                  rows="4"
                  value={bio}
                  onChange={(e) => setBio(e.target.value)}
                  placeholder="Summarize your engineering expertise, core stack, architecture philosophy, and career achievements..."
                  style={{ resize: 'vertical' }}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', gap: '12px' }}>
                {bioSuccess && (
                  <span style={{ color: '#10b981', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <CheckCircle size={15} /> Bio updated!
                  </span>
                )}
                <button
                  type="submit"
                  disabled={updateBioMutation.isPending}
                  className="btn btn-primary"
                  style={{ padding: '8px 18px', fontSize: '0.875rem' }}
                >
                  <Save size={16} /> {updateBioMutation.isPending ? 'Saving...' : 'Save Summary'}
                </button>
              </div>
            </form>
          </div>

          {/* Skills Management Card */}
          <div className="card" style={{ padding: '24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Layers size={20} color="var(--primary-glow)" />
                <h3 style={{ fontSize: '1.1rem', fontWeight: 600 }}>Technical Skill Taxonomy</h3>
              </div>
              <span className="badge badge-purple">{candidateSkills.length} Verified</span>
            </div>

            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '16px' }}>
              Select skills to power the multi-factor AI compatibility engine (60% match weight).
            </p>

            {/* Existing Skills */}
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px', marginBottom: '20px' }}>
              {candidateSkills.map((s) => (
                <span
                  key={s.id}
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '6px',
                    padding: '6px 12px',
                    borderRadius: '8px',
                    background: 'rgba(59, 130, 246, 0.12)',
                    border: '1px solid rgba(59, 130, 246, 0.3)',
                    color: '#93c5fd',
                    fontSize: '0.825rem',
                    fontWeight: 500,
                  }}
                >
                  {s.name}
                  <button
                    onClick={() => removeSkillMutation.mutate(s.id)}
                    disabled={removeSkillMutation.isPending}
                    style={{
                      background: 'none',
                      border: 'none',
                      color: '#93c5fd',
                      cursor: 'pointer',
                      padding: 0,
                      display: 'flex',
                    }}
                  >
                    <X size={14} />
                  </button>
                </span>
              ))}
              {candidateSkills.length === 0 && (
                <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>No skills added yet. Select below.</div>
              )}
            </div>

            {/* Add Skill Selector */}
            <form onSubmit={handleAddSkill} style={{ display: 'flex', gap: '10px' }}>
              <select
                className="form-input"
                value={selectedSkillId}
                onChange={(e) => setSelectedSkillId(e.target.value)}
                style={{ flex: 1 }}
              >
                <option value="">Select a skill to add...</option>
                {taxonomySkills
                  .filter((ts) => !candidateSkills.some((cs) => cs.id === ts.id))
                  .map((ts) => (
                    <option key={ts.id} value={ts.id}>
                      {ts.name}
                    </option>
                  ))}
              </select>
              <button
                type="submit"
                disabled={!selectedSkillId || addSkillMutation.isPending}
                className="btn btn-secondary"
                style={{ padding: '8px 16px' }}
              >
                <Plus size={16} /> Add
              </button>
            </form>
          </div>

          {/* Education Records */}
          <div className="card" style={{ padding: '24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <GraduationCap size={20} color="#10b981" />
                <h3 style={{ fontSize: '1.1rem', fontWeight: 600 }}>Education History</h3>
              </div>
              <button
                onClick={() => setShowEduModal(true)}
                className="btn btn-secondary"
                style={{ padding: '6px 12px', fontSize: '0.8rem' }}
              >
                <Plus size={14} /> Add
              </button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {(profile?.educations || []).map((edu) => (
                <div
                  key={edu.id}
                  style={{
                    padding: '12px 14px',
                    borderRadius: '8px',
                    background: 'rgba(255, 255, 255, 0.02)',
                    border: '1px solid var(--border-subtle)',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                  }}
                >
                  <div>
                    <div style={{ fontWeight: 600, fontSize: '0.9rem', color: '#f8fafc' }}>{edu.degree}</div>
                    <div style={{ color: 'var(--text-secondary)', fontSize: '0.825rem' }}>{edu.institution}</div>
                    {(edu.startDate || edu.endDate) && (
                      <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem', marginTop: '4px' }}>
                        {edu.startDate || ''} {edu.endDate ? `— ${edu.endDate}` : ''}
                      </div>
                    )}
                  </div>
                  <button
                    onClick={() => deleteEduMutation.mutate(edu.id)}
                    style={{
                      background: 'none',
                      border: 'none',
                      color: 'var(--text-muted)',
                      cursor: 'pointer',
                    }}
                  >
                    <Trash2 size={16} />
                  </button>
                </div>
              ))}
              {(!profile?.educations || profile.educations.length === 0) && (
                <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>No education records added yet.</div>
              )}
            </div>
          </div>

          {/* Work Experience Records */}
          <div className="card" style={{ padding: '24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Briefcase size={20} color="#f59e0b" />
                <h3 style={{ fontSize: '1.1rem', fontWeight: 600 }}>Work Experience</h3>
              </div>
              <button
                onClick={() => setShowExpModal(true)}
                className="btn btn-secondary"
                style={{ padding: '6px 12px', fontSize: '0.8rem' }}
              >
                <Plus size={14} /> Add
              </button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {(profile?.experiences || []).map((exp) => (
                <div
                  key={exp.id}
                  style={{
                    padding: '12px 14px',
                    borderRadius: '8px',
                    background: 'rgba(255, 255, 255, 0.02)',
                    border: '1px solid var(--border-subtle)',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                  }}
                >
                  <div>
                    <div style={{ fontWeight: 600, fontSize: '0.9rem', color: '#f8fafc' }}>{exp.role}</div>
                    <div style={{ color: 'var(--text-secondary)', fontSize: '0.825rem' }}>{exp.companyName}</div>
                    {(exp.startDate || exp.endDate) && (
                      <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem', marginTop: '4px' }}>
                        {exp.startDate || ''} {exp.endDate ? `— ${exp.endDate}` : '— Present'}
                      </div>
                    )}
                  </div>
                  <button
                    onClick={() => deleteExpMutation.mutate(exp.id)}
                    style={{
                      background: 'none',
                      border: 'none',
                      color: 'var(--text-muted)',
                      cursor: 'pointer',
                    }}
                  >
                    <Trash2 size={16} />
                  </button>
                </div>
              ))}
              {(!profile?.experiences || profile.experiences.length === 0) && (
                <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>No experience records added yet.</div>
              )}
            </div>
          </div>
        </div>

        {/* Right Column: PDF Resume Vault & Instant Gemini AI Analysis */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
          {/* Resume Upload Card */}
          <div className="card" style={{ padding: '24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
              <FileText size={20} color="var(--primary-glow)" />
              <h3 style={{ fontSize: '1.1rem', fontWeight: 600 }}>PDF Resume Vault & Parser</h3>
            </div>

            {profile?.resume?.fileUrl ? (
              <div
                style={{
                  padding: '16px',
                  borderRadius: '10px',
                  background: 'rgba(59, 130, 246, 0.08)',
                  border: '1px solid rgba(59, 130, 246, 0.25)',
                  marginBottom: '20px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                }}
              >
                <div>
                  <div style={{ fontWeight: 600, color: '#f8fafc', fontSize: '0.9rem' }}>
                    Active Resume On File
                  </div>
                  <div style={{ fontSize: '0.75rem', color: '#93c5fd', marginTop: '2px' }}>
                    Uploaded {profile.resume.uploadedAt ? new Date(profile.resume.uploadedAt).toLocaleDateString() : 'Active'}
                  </div>
                </div>
                <a
                  href={profile.resume.fileUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="btn btn-secondary"
                  style={{ padding: '6px 12px', fontSize: '0.8rem', display: 'flex', alignItems: 'center', gap: '6px' }}
                >
                  <Download size={14} /> View File
                </a>
              </div>
            ) : null}

            {/* Drag & Drop Box */}
            <div
              onDragOver={(e) => { e.preventDefault(); setDragActive(true); }}
              onDragLeave={() => setDragActive(false)}
              onDrop={handleFileDrop}
              style={{
                border: dragActive ? '2px dashed var(--primary-glow)' : '2px dashed var(--border-medium)',
                borderRadius: '12px',
                padding: '32px 20px',
                textAlign: 'center',
                background: dragActive ? 'rgba(59, 130, 246, 0.05)' : 'rgba(255, 255, 255, 0.01)',
                cursor: 'pointer',
                transition: 'var(--transition-fast)',
              }}
              onClick={() => document.getElementById('resume-file-input').click()}
            >
              <input
                id="resume-file-input"
                type="file"
                accept=".pdf"
                style={{ display: 'none' }}
                onChange={(e) => {
                  if (e.target.files?.[0]) handleFileSelected(e.target.files[0]);
                }}
              />
              <UploadCloud size={36} color="var(--primary-glow)" style={{ margin: '0 auto 12px auto' }} />
              <div style={{ fontWeight: 600, color: '#f8fafc', fontSize: '0.95rem' }}>
                {selectedFile ? selectedFile.name : 'Upload New PDF Resume'}
              </div>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginTop: '6px' }}>
                {selectedFile ? `${(selectedFile.size / 1024 / 1024).toFixed(2)} MB • Ready to upload` : 'Drag and drop your file here, or click to browse (Max 5MB)'}
              </div>
            </div>

            {selectedFile && (
              <div style={{ marginTop: '16px', display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                <button
                  type="button"
                  onClick={() => setSelectedFile(null)}
                  className="btn btn-secondary"
                  style={{ padding: '8px 14px', fontSize: '0.85rem' }}
                >
                  Cancel
                </button>
                <button
                  type="button"
                  onClick={handleResumeUpload}
                  disabled={uploadResumeMutation.isPending}
                  className="btn btn-primary"
                  style={{ padding: '8px 18px', fontSize: '0.85rem' }}
                >
                  <Sparkles size={16} />
                  {uploadResumeMutation.isPending ? 'Extracting & Analyzing with AI...' : 'Upload & Analyze Resume'}
                </button>
              </div>
            )}
          </div>

          {/* Gemini AI Resume Intelligence Analysis Card */}
          {aiAnalysisResult && (
            <div className="card animate-fade-in" style={{ padding: '24px', border: '1px solid rgba(139, 92, 246, 0.4)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
                <Brain size={22} color="#a78bfa" />
                <h3 style={{ fontSize: '1.15rem', fontWeight: 700, color: '#f8fafc' }}>
                  Gemini AI Resume Intelligence
                </h3>
              </div>

              {aiAnalysisResult.overallScore !== undefined && (
                <div style={{ marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '12px' }}>
                  <div style={{
                    fontSize: '2rem',
                    fontWeight: 800,
                    color: aiAnalysisResult.overallScore >= 80 ? '#10b981' : '#3b82f6',
                  }}>
                    {aiAnalysisResult.overallScore}%
                  </div>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                    AI Resume Strength Rating
                  </div>
                </div>
              )}

              {aiAnalysisResult.summary && (
                <div style={{ marginBottom: '16px', padding: '12px', background: 'rgba(139, 92, 246, 0.08)', borderRadius: '8px' }}>
                  <div style={{ fontSize: '0.75rem', fontWeight: 700, color: '#a78bfa', textTransform: 'uppercase', marginBottom: '4px' }}>
                    Executive AI Summary
                  </div>
                  <div style={{ fontSize: '0.875rem', color: '#e2e8f0', lineHeight: 1.5 }}>
                    {aiAnalysisResult.summary}
                  </div>
                </div>
              )}

              {aiAnalysisResult.skills && aiAnalysisResult.skills.length > 0 && (
                <div style={{ marginBottom: '16px' }}>
                  <div style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '8px' }}>
                    Detected Technologies & Frameworks:
                  </div>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                    {aiAnalysisResult.skills.map((s, idx) => (
                      <span key={idx} className="badge badge-primary">{s}</span>
                    ))}
                  </div>
                </div>
              )}

              {aiAnalysisResult.improvementSuggestions && aiAnalysisResult.improvementSuggestions.length > 0 && (
                <div>
                  <div style={{ fontSize: '0.8rem', fontWeight: 600, color: '#f59e0b', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Lightbulb size={15} /> Actionable Recommendations:
                  </div>
                  <ul style={{ margin: 0, paddingLeft: '18px', fontSize: '0.825rem', color: 'var(--text-secondary)' }}>
                    {aiAnalysisResult.improvementSuggestions.map((rec, idx) => (
                      <li key={idx} style={{ marginBottom: '4px' }}>{rec}</li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Add Education Modal */}
      {showEduModal && (
        <div style={{
          position: 'fixed',
          inset: 0,
          background: 'rgba(0,0,0,0.7)',
          backdropFilter: 'blur(8px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 100,
          padding: '20px',
        }}>
          <div className="card animate-fade-in" style={{ maxWidth: '480px', width: '100%', padding: '24px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <h3 style={{ fontSize: '1.2rem', fontWeight: 700 }}>Add Education Record</h3>
              <button onClick={() => setShowEduModal(false)} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleAddEducation}>
              <div style={{ marginBottom: '14px' }}>
                <label className="form-label">Degree / Certificate</label>
                <input
                  className="form-input"
                  required
                  placeholder="e.g. B.S. Computer Science"
                  value={eduDegree}
                  onChange={(e) => setEduDegree(e.target.value)}
                />
              </div>
              <div style={{ marginBottom: '14px' }}>
                <label className="form-label">Institution / University</label>
                <input
                  className="form-input"
                  required
                  placeholder="e.g. Stanford University"
                  value={eduInstitution}
                  onChange={(e) => setEduInstitution(e.target.value)}
                />
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '20px' }}>
                <div>
                  <label className="form-label">Start Date</label>
                  <input
                    type="date"
                    className="form-input"
                    value={eduStart}
                    onChange={(e) => setEduStart(e.target.value)}
                  />
                </div>
                <div>
                  <label className="form-label">End Date</label>
                  <input
                    type="date"
                    className="form-input"
                    value={eduEnd}
                    onChange={(e) => setEduEnd(e.target.value)}
                  />
                </div>
              </div>
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                <button type="button" onClick={() => setShowEduModal(false)} className="btn btn-secondary">
                  Cancel
                </button>
                <button type="submit" disabled={addEduMutation.isPending} className="btn btn-primary">
                  {addEduMutation.isPending ? 'Adding...' : 'Add Record'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Add Experience Modal */}
      {showExpModal && (
        <div style={{
          position: 'fixed',
          inset: 0,
          background: 'rgba(0,0,0,0.7)',
          backdropFilter: 'blur(8px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 100,
          padding: '20px',
        }}>
          <div className="card animate-fade-in" style={{ maxWidth: '480px', width: '100%', padding: '24px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <h3 style={{ fontSize: '1.2rem', fontWeight: 700 }}>Add Work Experience</h3>
              <button onClick={() => setShowExpModal(false)} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleAddExperience}>
              <div style={{ marginBottom: '14px' }}>
                <label className="form-label">Role / Title</label>
                <input
                  className="form-input"
                  required
                  placeholder="e.g. Senior Backend Engineer"
                  value={expRole}
                  onChange={(e) => setExpRole(e.target.value)}
                />
              </div>
              <div style={{ marginBottom: '14px' }}>
                <label className="form-label">Company Name</label>
                <input
                  className="form-input"
                  required
                  placeholder="e.g. Stripe, AWS"
                  value={expCompany}
                  onChange={(e) => setExpCompany(e.target.value)}
                />
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '20px' }}>
                <div>
                  <label className="form-label">Start Date</label>
                  <input
                    type="date"
                    className="form-input"
                    value={expStart}
                    onChange={(e) => setExpStart(e.target.value)}
                  />
                </div>
                <div>
                  <label className="form-label">End Date</label>
                  <input
                    type="date"
                    className="form-input"
                    value={expEnd}
                    onChange={(e) => setExpEnd(e.target.value)}
                  />
                </div>
              </div>
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                <button type="button" onClick={() => setShowExpModal(false)} className="btn btn-secondary">
                  Cancel
                </button>
                <button type="submit" disabled={addExpMutation.isPending} className="btn btn-primary">
                  {addExpMutation.isPending ? 'Adding...' : 'Add Experience'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
