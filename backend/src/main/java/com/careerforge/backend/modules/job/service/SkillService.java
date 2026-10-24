package com.careerforge.backend.modules.job.service;

import com.careerforge.backend.common.exception.BadRequestException;
import com.careerforge.backend.common.exception.ResourceNotFoundException;
import com.careerforge.backend.modules.auth.model.Role;
import com.careerforge.backend.modules.auth.model.User;
import com.careerforge.backend.modules.auth.repository.UserRepository;
import com.careerforge.backend.modules.job.dto.SkillDto;
import com.careerforge.backend.modules.job.model.CandidateSkill;
import com.careerforge.backend.modules.job.model.CandidateSkillId;
import com.careerforge.backend.modules.job.model.Skill;
import com.careerforge.backend.modules.job.repository.CandidateSkillRepository;
import com.careerforge.backend.modules.job.repository.SkillRepository;
import com.careerforge.backend.config.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final CandidateSkillRepository candidateSkillRepository;
    private final UserRepository userRepository;

    public SkillService(
            SkillRepository skillRepository,
            CandidateSkillRepository candidateSkillRepository,
            UserRepository userRepository
    ) {
        this.skillRepository = skillRepository;
        this.candidateSkillRepository = candidateSkillRepository;
        this.userRepository = userRepository;
    }

    @CacheEvict(value = CacheConfig.CACHE_SKILLS, allEntries = true)
    @Transactional
    public SkillDto createSkill(SkillDto dto) {
        String trimmed = dto.getName().trim();
        if (skillRepository.existsByNameIgnoreCase(trimmed)) {
            throw new BadRequestException("Skill already exists: " + trimmed);
        }
        Skill saved = skillRepository.save(new Skill(trimmed));
        return new SkillDto(saved.getId(), saved.getName());
    }

    @Cacheable(value = CacheConfig.CACHE_SKILLS)
    @Transactional(readOnly = true)
    public List<SkillDto> getAllSkills() {
        return skillRepository.findAll().stream()
                .map(s -> new SkillDto(s.getId(), s.getName()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SkillDto> getCandidateSkills(Long userId) {
        validateCandidate(userId);
        return candidateSkillRepository.findByIdUserId(userId).stream()
                .map(cs -> new SkillDto(cs.getSkill().getId(), cs.getSkill().getName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public SkillDto addCandidateSkill(Long userId, Long skillId) {
        validateCandidate(userId);

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillId));

        if (candidateSkillRepository.existsByIdUserIdAndIdSkillId(userId, skillId)) {
            throw new BadRequestException("Skill already added to candidate profile: " + skill.getName());
        }

        CandidateSkill candidateSkill = new CandidateSkill(userId, skill);
        candidateSkillRepository.save(candidateSkill);

        return new SkillDto(skill.getId(), skill.getName());
    }

    @Transactional
    public void removeCandidateSkill(Long userId, Long skillId) {
        validateCandidate(userId);

        CandidateSkillId id = new CandidateSkillId(userId, skillId);
        if (!candidateSkillRepository.existsById(id)) {
            throw new ResourceNotFoundException("Skill not found on candidate profile with id: " + skillId);
        }

        candidateSkillRepository.deleteById(id);
    }

    private void validateCandidate(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() != Role.CANDIDATE && user.getRole() != Role.ADMIN) {
            throw new BadRequestException("User does not have CANDIDATE role");
        }
    }
}
