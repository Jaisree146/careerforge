import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { profileApi, resumeApi, aiApi } from '../services/api';
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
  Lightbulb
} from 'lucide-react';

export const CandidateProfilePage = () => {
  const { user } = useAuth();

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saveSuccess, setSaveSuccess] = useState(false);
  const [error, setError] = useState(null);

  // Resume Upload State
  const [selectedFile, setSelectedFile] = useState(null);
  const [analyzeWithAi, setAnalyzeWithAi] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [uploadResult, setUploadResult] = useState(null);
  const [aiAnalysis, setAiAnalysis] = useState(null);
  const [aiLoading, setAiLoading] = useState(false);

  // Profile Form State
  const [bio, setBio] = useState('');
  const [experienceYears, setExperienceYears] = useState(0);
  const [education, setEducation] = useState('');
  const [newSkill, setNewSkill] = useState('');
  const [skills, setSkills] = useState([]);

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    setLoading(true);
    try {
      const data = await profileApi.getCandidateProfile();
      if (data) {
        setProfile(data);
        setBio(data.bio || '');
        setExperienceYears(data.experienceYears || 0);
        setEducation(data.education || '');
        setSkills(data.skills || []);
        if (data.resumeFileName) {
          setUploadResult({
            storedFileName: data.resumeFileName,
            originalFileName: data.resumeOriginalName || data.resumeFileName,
          });
        }
      }
    } catch (err) {
      console.warn('Could not load profile', err);
    } finally {
      setLoading(false);
    }
  };

  const handleProfileSave = async (e) => {
    e.preventDefault();
    setError(null);
    try {
      const updated = await profileApi.updateCandidateProfile({
        bio,
        experienceYears: parseFloat(experienceYears) || 0,
        education,
        skillNames: skills.map((s) => (typeof s === 'string' ? s : s.name)),
      });
      setProfile(updated);
      setSaveSuccess(true);
      setTimeout(() => setSaveSuccess(false), 2500);
    } catch (err) {
      setError(err.message || 'Failed to update candidate profile');
    }
  };

  const handleAddSkill = (e) => {
    e.preventDefault();
    const trimmed = newSkill.trim();
    if (!trimmed) return;
    if (!skills.some((s) => (typeof s === 'string' ? s.toLowerCase() : s.name.toLowerCase()) === trimmed.toLowerCase())) {
      setSkills([...skills, trimmed]);
      setNewSkill('');
    }
  };

  const handleRemoveSkill = (skillToRemove) => {
    setSkills(skills.filter((s) => (typeof s === 'string' ? s : s.name) !== (typeof skillToRemove === 'string' ? skillToRemove : skillToRemove.name)));
  };

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
    }
  };

  const handleResumeUpload = async () => {
    if (!selectedFile) return;

    setUploading(true);
    setError(null);
    try {
      const result = await resumeApi.upload(selectedFile, analyzeWithAi);
      setUploadResult(result);
      if (result.aiAnalysis) {
        setAiAnalysis(result.aiAnalysis);
      }
      // Refresh profile to reflect uploaded resume
      fetchProfile();
    } catch (err) {
      setError(err.message || 'Failed to upload and parse resume');
    } finally {
      setUploading(false);
    }
  };

  const triggerDirectAiAnalysis = async () => {
    if (!uploadResult?.extractedText && !profile?.bio) {
      setError('No resume text available for AI analysis. Please upload a PDF resume first.');
      return;
    }

    setAiLoading(true);
    setError(null);
    try {
      const textToAnalyze = uploadResult?.extractedText || profile?.bio || '';
      const analysis = await aiApi.analyzeResume({ resumeText: textToAnalyze });
      setAiAnalysis(analysis);
    } catch (err) {
      setError(err.message || 'AI resume analysis encountered an error or rate limit');
    } finally {
      setAiLoading(false);
    }
  };

  return (
    <div className="container animate-fade-in" style={{ padding: '40px 1.5rem 80px 1.5rem' }}>
      <div style={{ marginBottom: '32px' }}>
        <h1 style={{ fontSize: '2.25rem', marginBottom: '8px' }}>
          Candidate <span className="text-gradient">Profile & AI Intelligence</span>
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
          Upload your resume for automated PDFBox parsing and Gemini LLM skills extraction
        </p>
      </div>

      {saveSuccess && (
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
          <CheckCircle size={18} />
          <span>Profile changes saved successfully!</span>
        </div>
      )}

      {error && (
        <div style={{
          padding: '12px 16px',
          borderRadius: '8px',
          background: 'rgba(244, 63, 94, 0.15)',
          color: '#fda4af',
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          marginBottom: '24px',
        }}>
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1.2fr) minmax(0, 1fr)', gap: '30px' }}>
        {/* Left Column: Structured Profile Editor */}
        <div className="glass-panel" style={{ padding: '32px' }}>
          <h2 style={{ fontSize: '1.4rem', marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '10px' }}>
            <User size={20} color="#3b82f6" />
            <span>Professional Profile Details</span>
          </h2>

          <form onSubmit={handleProfileSave}>
            <div className="form-group">
              <label className="form-label">Professional Summary & Bio</label>
              <textarea
                className="form-textarea"
                rows={4}
                placeholder="Highlight your software engineering background, focus areas, and achievements..."
                value={bio}
                onChange={(e) => setBio(e.target.value)}
              />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <div className="form-group">
                <label className="form-label">Years of Experience</label>
                <input
                  type="number"
                  step="0.5"
                  min="0"
                  className="form-input"
                  value={experienceYears}
                  onChange={(e) => setExperienceYears(e.target.value)}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Education / Degree</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. B.Tech Computer Science"
                  value={education}
                  onChange={(e) => setEducation(e.target.value)}
                />
              </div>
            </div>

            {/* Technical Skills Catalog */}
            <div style={{ marginBottom: '24px' }}>
              <label className="form-label" style={{ marginBottom: '8px', display: 'block' }}>
                Technical Skills ({skills.length})
              </label>

              <div style={{ display: 'flex', gap: '8px', marginBottom: '14px' }}>
                <input
                  type="text"
                  className="form-input"
                  placeholder="Add skill (e.g. Docker, React, Java)..."
                  value={newSkill}
                  onChange={(e) => setNewSkill(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      e.preventDefault();
                      handleAddSkill(e);
                    }
                  }}
                />
                <button
                  type="button"
                  onClick={handleAddSkill}
                  className="btn btn-secondary"
                  style={{ padding: '0 16px' }}
                >
                  <Plus size={16} /> Add
                </button>
              </div>

              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                {skills.map((s, idx) => {
                  const skillName = typeof s === 'string' ? s : s.name;
                  return (
                    <span
                      key={idx}
                      className="badge badge-primary"
                      style={{ padding: '6px 12px', fontSize: '0.8rem' }}
                    >
                      {skillName}
                      <button
                        type="button"
                        onClick={() => handleRemoveSkill(s)}
                        style={{
                          background: 'none',
                          border: 'none',
                          color: '#93c5fd',
                          cursor: 'pointer',
                          marginLeft: '4px',
                          display: 'flex',
                          alignItems: 'center',
                        }}
                      >
                        ×
                      </button>
                    </span>
                  );
                })}
              </div>
            </div>

            <button type="submit" className="btn btn-primary" style={{ padding: '12px 24px' }}>
              <Save size={16} /> Save Profile Changes
            </button>
          </form>
        </div>

        {/* Right Column: PDF Resume Ingestion & AI Intelligence */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          {/* Resume Upload Card */}
          <div className="glass-panel" style={{ padding: '32px' }}>
            <h2 style={{ fontSize: '1.4rem', marginBottom: '6px', display: 'flex', alignItems: 'center', gap: '10px' }}>
              <UploadCloud size={22} color="#10b981" />
              <span>Resume Ingestion (PDFBox)</span>
            </h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '20px' }}>
              Upload your resume in PDF, DOC, or DOCX format (Max 5MB).
            </p>

            <div style={{
              border: '2px dashed var(--border-medium)',
              borderRadius: '12px',
              padding: '28px',
              textAlign: 'center',
              background: 'rgba(15, 23, 42, 0.4)',
              cursor: 'pointer',
              marginBottom: '16px',
            }}>
              <input
                type="file"
                id="resumeFileInput"
                accept=".pdf,.doc,.docx"
                style={{ display: 'none' }}
                onChange={handleFileChange}
              />
              <label htmlFor="resumeFileInput" style={{ cursor: 'pointer', display: 'block' }}>
                <FileText size={36} color="#60a5fa" style={{ margin: '0 auto 10px auto' }} />
                <div style={{ fontWeight: 600, fontSize: '0.9rem', color: '#f8fafc' }}>
                  {selectedFile ? selectedFile.name : 'Click to browse or drop resume file'}
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '4px' }}>
                  {selectedFile ? `${(selectedFile.size / 1024).toFixed(1)} KB` : 'PDF, DOC, DOCX up to 5MB'}
                </div>
              </label>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '20px' }}>
              <input
                type="checkbox"
                id="aiCheckbox"
                checked={analyzeWithAi}
                onChange={(e) => setAnalyzeWithAi(e.target.checked)}
                style={{ cursor: 'pointer', accentColor: '#3b82f6' }}
              />
              <label htmlFor="aiCheckbox" style={{ fontSize: '0.85rem', color: '#cbd5e1', cursor: 'pointer' }}>
                Automatically run Gemini AI skill extraction upon upload
              </label>
            </div>

            <button
              type="button"
              disabled={!selectedFile || uploading}
              onClick={handleResumeUpload}
              className="btn btn-emerald"
              style={{ width: '100%', padding: '12px' }}
            >
              {uploading ? 'Parsing & Extracting Text...' : 'Upload & Process Resume'}
            </button>

            {/* Currently Active Resume */}
            {uploadResult?.storedFileName && (
              <div style={{
                marginTop: '20px',
                padding: '14px',
                borderRadius: '8px',
                background: 'rgba(59, 130, 246, 0.08)',
                border: '1px solid rgba(59, 130, 246, 0.25)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
              }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <FileText size={20} color="#60a5fa" />
                  <div>
                    <div style={{ fontSize: '0.85rem', fontWeight: 600, color: '#f8fafc' }}>
                      {uploadResult.originalFileName || 'Active Resume on Record'}
                    </div>
                    <div style={{ fontSize: '0.725rem', color: '#34d399' }}>Verified & Stored</div>
                  </div>
                </div>

                <a
                  href={resumeApi.downloadUrl(uploadResult.storedFileName)}
                  target="_blank"
                  rel="noreferrer"
                  className="btn btn-secondary btn-sm"
                  style={{ display: 'flex', alignItems: 'center', gap: '4px' }}
                >
                  <Download size={14} /> Download
                </a>
              </div>
            )}
          </div>

          {/* AI Insights Card */}
          <div className="glass-panel" style={{ padding: '32px' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
              <h2 style={{ fontSize: '1.3rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Brain size={20} color="#a855f7" />
                <span>Gemini AI Resume Insights</span>
              </h2>

              <button
                type="button"
                disabled={aiLoading}
                onClick={triggerDirectAiAnalysis}
                className="btn btn-secondary btn-sm"
              >
                <Sparkles size={14} color="#a855f7" />
                {aiLoading ? 'Analyzing...' : 'Run AI Analysis'}
              </button>
            </div>

            {aiAnalysis ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                <div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                    Detected Seniority
                  </div>
                  <div className="badge badge-purple" style={{ marginTop: '4px' }}>
                    {aiAnalysis.seniorityLevel || 'Mid-Senior Level'}
                  </div>
                </div>

                <div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                    Executive Summary
                  </div>
                  <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '4px', lineHeight: 1.5 }}>
                    {aiAnalysis.summary || 'Strong technical foundation with enterprise Java and full-stack capabilities.'}
                  </p>
                </div>

                {aiAnalysis.extractedSkills?.length > 0 && (
                  <div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '6px' }}>
                      Skills Extracted By LLM
                    </div>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                      {aiAnalysis.extractedSkills.map((sk, i) => (
                        <span key={i} className="badge badge-primary">
                          {sk}
                        </span>
                      ))}
                    </div>
                  </div>
                )}

                {aiAnalysis.improvementSuggestions?.length > 0 && (
                  <div>
                    <div style={{ fontSize: '0.75rem', color: '#fbbf24', textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: '4px', marginBottom: '6px' }}>
                      <Lightbulb size={14} /> Optimization Suggestions
                    </div>
                    <ul style={{ paddingLeft: '18px', fontSize: '0.8rem', color: 'var(--text-secondary)', display: 'flex', flexDirection: 'column', gap: '4px' }}>
                      {aiAnalysis.improvementSuggestions.map((sug, i) => (
                        <li key={i}>{sug}</li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            ) : (
              <div style={{ textAlign: 'center', padding: '24px 0', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                Upload a resume or click "Run AI Analysis" to generate structured AI feedback.
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
