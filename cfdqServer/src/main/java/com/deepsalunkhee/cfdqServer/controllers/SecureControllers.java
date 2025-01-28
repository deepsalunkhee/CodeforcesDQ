package com.deepsalunkhee.cfdqServer.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.deepsalunkhee.cfdqServer.Services.UserServices;
import com.deepsalunkhee.cfdqServer.models.UserModel;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
@RestController
@RequestMapping("api/v1")
public class SecureControllers {

    private static final String problemUrl = "https://codeforces.com/api/problemset.problems?tags=";
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(SecureControllers.class);
    
    @Autowired
    private UserServices userServices;

    @PostMapping("/createweek")
    public ResponseEntity<String> createWeek(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("handle", request.getHeader("handle"));
        headers.put("tag", request.getHeader("tag"));

        // check if user exists
        UserModel currUser = userServices.getUserByHandle(headers.get("handle"));
        if (currUser == null) {
            UserModel newUser = new UserModel();
            newUser.setHandle(headers.get("handle"));
            userServices.createUser(newUser);
        }

        String url = problemUrl + headers.get("tag");
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
            ResponseEntity<String> userinfo = restTemplate.getForEntity(
                "https://codeforces.com/api/user.info?handles=" + headers.get("handle"), 
                String.class
            );
            
            if (responseEntity.getStatusCode().is2xxSuccessful() && userinfo.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                
                // Use the full path to the nested class for ProblemsResponse
                SecureControllers.ProblemsResponse problemsResponse = objectMapper.readValue(
                    responseEntity.getBody(),
                    SecureControllers.ProblemsResponse.class
                );
                
                // Use the full path to the nested class for UserResponse
                SecureControllers.UserResponse userResponse = objectMapper.readValue(
                    userinfo.getBody(), 
                    SecureControllers.UserResponse.class
                );
                
                int currRating = userResponse.getResult().get(0).getRating();

                List<SecureControllers.Problem> problems = problemsResponse.getResult().getProblems();

                // get the list of solved problems
                Set<String> solved = currUser.getSolved();

                // Get filtered problems
                List<SecureControllers.Problem> filteredProblems = problems.stream()
                    .filter(problem -> problem.getRating() != null &&  // Add null check for rating
                            problem.getRating() > currRating + 100 && 
                            problem.getRating() < currRating + 300 &&
                            !solved.contains(problem.getContestId() + problem.getIndex()))
                    .limit(7)
                    .collect(Collectors.toList());
                
                logger.info("Filtered problems: " + filteredProblems);
                return ResponseEntity.ok("Week created");

            } else {
                return ResponseEntity.status(responseEntity.getStatusCode())
                    .body("Failed to fetch problems");
            }
        } catch (Exception e) {
            logger.error("Error fetching problems:", e);  // Add error logging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching problems: " + e.getMessage());
        }
    }
 


    public static class ProblemsResponse {
        private String status;
        private Result result;
    
        public String getStatus() {
            return status;
        }
    
        public void setStatus(String status) {
            this.status = status;
        }
    
        public Result getResult() {
            return result;
        }
    
        public void setResult(Result result) {
            this.result = result;
        }
    }
    
    public static class Result {
        private List<Problem> problems;
        private List<ProblemStatistics> problemStatistics;
    
        public List<Problem> getProblems() {
            return problems;
        }
    
        public void setProblems(List<Problem> problems) {
            this.problems = problems;
        }
    
        public List<ProblemStatistics> getProblemStatistics() {
            return problemStatistics;
        }
    
        public void setProblemStatistics(List<ProblemStatistics> problemStatistics) {
            this.problemStatistics = problemStatistics;
        }
    }
    
    public static class ProblemStatistics {
        private int contestId;
        private String index;
        private int solvedCount;
    
        public int getContestId() {
            return contestId;
        }
    
        public void setContestId(int contestId) {
            this.contestId = contestId;
        }
    
        public String getIndex() {
            return index;
        }
    
        public void setIndex(String index) {
            this.index = index;
        }
    
        public int getSolvedCount() {
            return solvedCount;
        }
    
        public void setSolvedCount(int solvedCount) {
            this.solvedCount = solvedCount;
        }
    
        @Override
        public String toString() {
            return "ProblemStatistics{" +
                    "contestId=" + contestId +
                    ", index='" + index + '\'' +
                    ", solvedCount=" + solvedCount +
                    '}';
        }
    }

    public static class Problem {
        private int contestId;
        private String index;
        private String name;
        private String type;
        private Double points; // Use Double to handle potential nulls
        private Integer rating; // Use Integer to handle potential nulls
        private List<String> tags;

        // Getters and Setters
        public int getContestId() {
            return contestId;
        }

        public void setContestId(int contestId) {
            this.contestId = contestId;
        }

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Double getPoints() {
            return points;
        }

        public void setPoints(Double points) {
            this.points = points;
        }

        public Integer getRating() {
            return rating;
        }

        public void setRating(Integer rating) {
            this.rating = rating;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        @Override
        public String toString() {
            return "Problem{" +
                    "contestId=" + contestId +
                    ", index='" + index + '\'' +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", points=" + points +
                    ", rating=" + rating +
                    ", tags=" + tags +
                    '}';
        }
    }

    public static class UserResponse {
        private String status;
        private List<infoResult> infoResult;
    
        // Getters and Setters
        public String getStatus() {
            return status;
        }
    
        public void setStatus(String status) {
            this.status = status;
        }
    
        public List<infoResult> getResult() {
            return infoResult;
        }
    
        public void setResult(List<infoResult> result) {
            this.infoResult = result;
        }
    
        @Override
        public String toString() {
            return "UserResponse{" +
                    "status='" + status + '\'' +
                    ", result=" + infoResult +
                    '}';
        }
    
        // Nested Result Class
        public static class infoResult {
            private String lastName;
            private String country;
            private long lastOnlineTimeSeconds;
            private String city;
            private int rating;
            private int friendOfCount;
            private String titlePhoto;
            private String handle;
            private String avatar;
            private String firstName;
            private int contribution;
            private String organization;
            private String rank;
            private int maxRating;
            private long registrationTimeSeconds;
            private String maxRank;
    
            // Getters and Setters
            public String getLastName() {
                return lastName;
            }
    
            public void setLastName(String lastName) {
                this.lastName = lastName;
            }
    
            public String getCountry() {
                return country;
            }
    
            public void setCountry(String country) {
                this.country = country;
            }
    
            public long getLastOnlineTimeSeconds() {
                return lastOnlineTimeSeconds;
            }
    
            public void setLastOnlineTimeSeconds(long lastOnlineTimeSeconds) {
                this.lastOnlineTimeSeconds = lastOnlineTimeSeconds;
            }
    
            public String getCity() {
                return city;
            }
    
            public void setCity(String city) {
                this.city = city;
            }
    
            public int getRating() {
                return rating;
            }
    
            public void setRating(int rating) {
                this.rating = rating;
            }
    
            public int getFriendOfCount() {
                return friendOfCount;
            }
    
            public void setFriendOfCount(int friendOfCount) {
                this.friendOfCount = friendOfCount;
            }
    
            public String getTitlePhoto() {
                return titlePhoto;
            }
    
            public void setTitlePhoto(String titlePhoto) {
                this.titlePhoto = titlePhoto;
            }
    
            public String getHandle() {
                return handle;
            }
    
            public void setHandle(String handle) {
                this.handle = handle;
            }
    
            public String getAvatar() {
                return avatar;
            }
    
            public void setAvatar(String avatar) {
                this.avatar = avatar;
            }
    
            public String getFirstName() {
                return firstName;
            }
    
            public void setFirstName(String firstName) {
                this.firstName = firstName;
            }
    
            public int getContribution() {
                return contribution;
            }
    
            public void setContribution(int contribution) {
                this.contribution = contribution;
            }
    
            public String getOrganization() {
                return organization;
            }
    
            public void setOrganization(String organization) {
                this.organization = organization;
            }
    
            public String getRank() {
                return rank;
            }
    
            public void setRank(String rank) {
                this.rank = rank;
            }
    
            public int getMaxRating() {
                return maxRating;
            }
    
            public void setMaxRating(int maxRating) {
                this.maxRating = maxRating;
            }
    
            public long getRegistrationTimeSeconds() {
                return registrationTimeSeconds;
            }
    
            public void setRegistrationTimeSeconds(long registrationTimeSeconds) {
                this.registrationTimeSeconds = registrationTimeSeconds;
            }
    
            public String getMaxRank() {
                return maxRank;
            }
    
            public void setMaxRank(String maxRank) {
                this.maxRank = maxRank;
            }
    
            @Override
            public String toString() {
                return "Result{" +
                        "lastName='" + lastName + '\'' +
                        ", country='" + country + '\'' +
                        ", lastOnlineTimeSeconds=" + lastOnlineTimeSeconds +
                        ", city='" + city + '\'' +
                        ", rating=" + rating +
                        ", friendOfCount=" + friendOfCount +
                        ", titlePhoto='" + titlePhoto + '\'' +
                        ", handle='" + handle + '\'' +
                        ", avatar='" + avatar + '\'' +
                        ", firstName='" + firstName + '\'' +
                        ", contribution=" + contribution +
                        ", organization='" + organization + '\'' +
                        ", rank='" + rank + '\'' +
                        ", maxRating=" + maxRating +
                        ", registrationTimeSeconds=" + registrationTimeSeconds +
                        ", maxRank='" + maxRank + '\'' +
                        '}';
            }
        }

}
}
