package com.careerforge.backend.modules.ai.dto;

public class InterviewQuestionDto {

    private String question;
    private String category;
    private String rationale;

    public InterviewQuestionDto() {
    }

    public InterviewQuestionDto(String question, String category, String rationale) {
        this.question = question;
        this.category = category;
        this.rationale = rationale;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }
}
