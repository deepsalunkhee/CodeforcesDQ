package com.deepsalunkhee.cfdqServer.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document(collection = "users")
public class UserModel {

    @Id
    private String id;
    
    private String handle; // The user's Codeforces handle.
    private List<Week> weeks; // List of weeks, each containing questions.
    private Set<String> solved;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public List<Week> getWeeks() {
        return weeks;
    }

    public void setWeeks(List<Week> weeks) {
        this.weeks = weeks;
    }

    public Set<String> getSolved() {
        
        return solved != null ? solved : new HashSet<>();
    }

    public void setSolved(Set<String> solved) {
        this.solved = solved;
    }

    

    // Inner class for Week structure
    public static class Week {
        private int weekNo;  // Week number (1, 2, 3, etc.)
        private List<Question> questions;  // List of questions for that week
        private boolean isCompleted;  // Whether the week is completed or not

        // Getters and Setters
        public int getWeekNo() {
            return weekNo;
        }

        public void setWeekNo(int weekNo) {
            this.weekNo = weekNo;
        }

        public List<Question> getQuestions() {
            return questions;
        }

        public void setQuestions(List<Question> questions) {
            this.questions = questions;
        }

        public boolean isCompleted() {
            return isCompleted;
        }

        public void setCompleted(boolean completed) {
            isCompleted = completed;
        }

        public boolean checkIfCompleted() {
            for (Question question : questions) {
                if (question.getStatus().equals("unsolved")) {
                    isCompleted = false;
                    return false;
                }
            }
            isCompleted = true;
            return true;
        }
    }

    // Inner class for Question structure
    public static class Question {
        private String status;  // Status of the question (e.g., solved, unsolved)
        private String url;  // URL to the question on Codeforces
        private String submission="";

        // Getters and Setters
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getSubmission() {
            return submission;
        }

        public void setSubmission(String submission) {
            this.submission = submission;
        }
    }
}
