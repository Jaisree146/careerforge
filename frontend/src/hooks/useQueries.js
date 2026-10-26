import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  jobsApi,
  skillsApi,
  companyApi,
  profileApi,
  resumeApi,
  matchingApi,
  applicationApi,
  notificationApi,
  analyticsApi,
  aiApi,
} from '../services/api';

// -------------------------------------------------------------
// QUERIES
// -------------------------------------------------------------

// 1. Jobs Queries
export const useJobs = (filters = {}) => {
  return useQuery({
    queryKey: ['jobs', filters],
    queryFn: () => jobsApi.getAll(filters),
  });
};

export const useJob = (jobId) => {
  return useQuery({
    queryKey: ['job', jobId],
    queryFn: () => jobsApi.getById(jobId),
    enabled: !!jobId,
  });
};

// 2. Matching Engine Queries
export const useRecommendedJobs = (enabled = true) => {
  return useQuery({
    queryKey: ['recommended-jobs'],
    queryFn: () => matchingApi.getRecommendedJobs(10),
    enabled,
  });
};

export const useJobMatch = (jobId, enabled = true) => {
  return useQuery({
    queryKey: ['job-match', jobId],
    queryFn: () => matchingApi.getMatchForJob(jobId),
    enabled: enabled && !!jobId,
  });
};

export const useTopCandidatesForJob = (jobId, enabled = true) => {
  return useQuery({
    queryKey: ['job-candidates-match', jobId],
    queryFn: () => matchingApi.getTopCandidatesForJob(jobId),
    enabled: enabled && !!jobId,
  });
};

// 3. Taxonomy Queries
export const useSkills = () => {
  return useQuery({
    queryKey: ['skills'],
    queryFn: () => skillsApi.getAll(),
    staleTime: 1000 * 60 * 10, // 10 minutes cache
  });
};

export const useCompanies = () => {
  return useQuery({
    queryKey: ['companies'],
    queryFn: () => companyApi.getAll(),
    staleTime: 1000 * 60 * 5,
  });
};

// 4. Candidate & Recruiter Profile Queries
export const useCandidateProfile = (enabled = true) => {
  return useQuery({
    queryKey: ['candidate-profile'],
    queryFn: () => profileApi.getCandidateProfile(),
    enabled,
  });
};

export const useCandidateSkills = (enabled = true) => {
  return useQuery({
    queryKey: ['candidate-skills'],
    queryFn: () => skillsApi.getCandidateSkills(),
    enabled,
  });
};

export const useRecruiterProfile = (enabled = true) => {
  return useQuery({
    queryKey: ['recruiter-profile'],
    queryFn: () => profileApi.getRecruiterProfile(),
    enabled,
  });
};

// 5. Applications Queries
export const useCandidateApplications = (params = {}, enabled = true) => {
  return useQuery({
    queryKey: ['candidate-applications', params],
    queryFn: () => applicationApi.getMyApplications(params),
    enabled,
  });
};

export const useSavedJobs = (params = {}, enabled = true) => {
  return useQuery({
    queryKey: ['saved-jobs', params],
    queryFn: () => applicationApi.getSavedJobs(params),
    enabled,
  });
};

export const useJobApplications = (jobId, params = {}, enabled = true) => {
  return useQuery({
    queryKey: ['job-applications', jobId, params],
    queryFn: () => applicationApi.getJobApplications(jobId, params),
    enabled: enabled && !!jobId,
  });
};

export const useAllRecruiterApplications = (params = {}, enabled = true) => {
  return useQuery({
    queryKey: ['recruiter-applications', params],
    queryFn: () => applicationApi.getAllRecruiterApplications(params),
    enabled,
  });
};

// 6. Notification Queries
export const useNotifications = (params = { page: 0, size: 10 }, enabled = true) => {
  return useQuery({
    queryKey: ['notifications', params],
    queryFn: () => notificationApi.getNotifications(params),
    enabled,
  });
};

export const useUnreadCount = (enabled = true) => {
  return useQuery({
    queryKey: ['unread-count'],
    queryFn: () => notificationApi.getUnreadCount(),
    enabled,
    refetchInterval: 30000, // Poll count every 30 seconds as fallback to WebSocket
  });
};

// 7. Analytics Queries
export const useAnalyticsKpis = (enabled = true) => {
  return useQuery({
    queryKey: ['analytics-kpis'],
    queryFn: () => analyticsApi.getPlatformKpis(),
    enabled,
  });
};

export const useSkillDemandSupply = (enabled = true) => {
  return useQuery({
    queryKey: ['analytics-skills'],
    queryFn: () => analyticsApi.getSkillDemandSupply(),
    enabled,
  });
};

// -------------------------------------------------------------
// MUTATIONS
// -------------------------------------------------------------

export const useApplyJobMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ jobId, notes, resumeId }) => applicationApi.apply(jobId, notes, resumeId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['candidate-applications'] });
      queryClient.invalidateQueries({ queryKey: ['jobs'] });
    },
  });
};

export const useToggleSaveJobMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ jobId, isSaved }) => {
      if (isSaved) {
        await applicationApi.unsaveJob(jobId);
        return false;
      } else {
        await applicationApi.saveJob(jobId);
        return true;
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['saved-jobs'] });
    },
  });
};

export const useCreateJobMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (jobData) => jobsApi.create(jobData),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['jobs'] });
      queryClient.invalidateQueries({ queryKey: ['analytics-kpis'] });
    },
  });
};

export const useUpdateJobStatusMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ jobId, status }) => jobsApi.updateStatus(jobId, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['jobs'] });
    },
  });
};

export const useUpdateApplicationStatusMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ appId, status, notes }) => applicationApi.updateStatus(appId, status, notes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['job-applications'] });
      queryClient.invalidateQueries({ queryKey: ['recruiter-applications'] });
      queryClient.invalidateQueries({ queryKey: ['candidate-applications'] });
    },
  });
};

export const useUpdateCandidateBioMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (bioData) => profileApi.updateCandidateBio(bioData),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['candidate-profile'] });
    },
  });
};

export const useAddEducationMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (edu) => profileApi.addEducation(edu),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['candidate-profile'] });
    },
  });
};

export const useDeleteEducationMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id) => profileApi.deleteEducation(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['candidate-profile'] });
    },
  });
};

export const useAddExperienceMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (exp) => profileApi.addExperience(exp),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['candidate-profile'] });
    },
  });
};

export const useDeleteExperienceMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id) => profileApi.deleteExperience(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['candidate-profile'] });
    },
  });
};

export const useAddSkillMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (skillId) => skillsApi.addCandidateSkill(skillId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['candidate-skills'] });
      queryClient.invalidateQueries({ queryKey: ['candidate-profile'] });
    },
  });
};

export const useRemoveSkillMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (skillId) => skillsApi.removeCandidateSkill(skillId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['candidate-skills'] });
      queryClient.invalidateQueries({ queryKey: ['candidate-profile'] });
    },
  });
};

export const useUploadResumeMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ file, analyzeWithAi }) => resumeApi.upload(file, analyzeWithAi),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['candidate-profile'] });
    },
  });
};
