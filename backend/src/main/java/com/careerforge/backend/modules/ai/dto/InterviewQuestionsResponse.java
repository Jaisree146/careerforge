package com.careerforge.backend.modules.ai.dto;

import java.util.ArrayList;
import java.util.List;

public class InterviewQuestionsResponse {

    private String candidateName;
    private String jobTitle;
    private List<InterviewQuestionDto> questions = new ArrayList<>();

    public InterviewQuestionsResponse() {
    }

    public InterviewQuestionsResponse(String candidateName, String jobTitle, List<InterviewQuestionDto> questions) {
        this.candidateName = candidateName;
        this.jobTitle = jobTitle;
        this.questions = questions;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public List<InterviewQuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<InterviewQuestionDto> questions) {
        this.questions = questions;
    }
}
