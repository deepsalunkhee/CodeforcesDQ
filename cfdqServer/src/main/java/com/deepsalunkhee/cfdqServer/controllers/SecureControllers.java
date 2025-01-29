package com.deepsalunkhee.cfdqServer.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SecureControllers {

    private static final String problemUrl = "https://codeforces.com/api/problemset.problems?tags=";
    private static final String problmebase = "https://codeforces.com/problemset/problem/";
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(SecureControllers.class);

    @Autowired
    private UserServices userServices;

    @PostMapping("/createweek")
    public ResponseEntity<List<QueStatus>> createWeek(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("handle", request.getHeader("handle"));
        headers.put("tag", request.getHeader("tag"));


        // check if user exists
        UserModel currUser = userServices.getUserByHandle(headers.get("handle"));
        if (currUser == null) {
            // Create a new user
            currUser = new UserModel();

            // Assign a unique ID if required (MongoDB will auto-generate one if left null)
            currUser.setId(UUID.randomUUID().toString());

            // Assign the handle for the new user
            currUser.setHandle(headers.get("handle"));

            // Initialize an empty list of weeks
            currUser.setWeeks(new ArrayList<>());

            // Initialize the solved set
            currUser.setSolved(new HashSet<>());

            // Save the user to the database
            userServices.createUser(currUser);

            System.out.println("New user created with handle: " + headers.get("handle"));
        }else{
            //check if previous week is completed if not then return
            if(currUser.getWeeks().size() > 0 && !currUser.getWeeks().get(currUser.getWeeks().size()-1).checkIfCompleted()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(List.of(new QueStatus("", "Previous week is not completed","","")));
            }

        }

        String url = problemUrl + headers.get("tag");
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
            ResponseEntity<String> userinfo = restTemplate.getForEntity(
                    "https://codeforces.com/api/user.info?handles=" + headers.get("handle"),
                    String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful() && userinfo.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();

                // Use the full path to the nested class for ProblemsResponse
                SecureControllers.ProblemsResponse problemsResponse = objectMapper.readValue(
                        responseEntity.getBody(),
                        SecureControllers.ProblemsResponse.class);

                // Use the full path to the nested class for UserResponse
                SecureControllers.UserResponse userResponse = objectMapper.readValue(
                        userinfo.getBody(),
                        SecureControllers.UserResponse.class);

                int currRating = userResponse.getResult().get(0).getRating();

                List<SecureControllers.Problem> problems = problemsResponse.getResult().getProblems();

                // get the list of solved problems
                Set<String> solved = currUser.getSolved();

                // Get filtered problems
                List<SecureControllers.Problem> filteredProblems = problems.stream()
                        .filter(problem -> problem.getRating() != null && // Add null check for rating
                                problem.getRating() > currRating + 100 &&
                                problem.getRating() < currRating + 300 &&
                                !solved.contains(problem.getContestId() + problem.getIndex()))
                        .limit(7)
                        .collect(Collectors.toList());

                //logger.info("Filtered problems: " + filteredProblems);
                // createing List of Links of problems
                List<String> links = filteredProblems.stream()
                        .map(problem -> problmebase + problem.getContestId() + "/" + problem.getIndex())
                        .collect(Collectors.toList());

                // create a new week
                UserModel.Week week = new UserModel.Week();
                week.setWeekNo(currUser.getWeeks().size() + 1);
                week.setTopic(headers.get("tag"));
                week.setCompleted(false);
                List<UserModel.Question> questions = new ArrayList<>();
                List<QueStatus> queStatus = new ArrayList<>();

                // add questions to the List
                for(String link:links){
                    UserModel.Question question = new UserModel.Question();
                    String code= link.substring(link.lastIndexOf("/", link.lastIndexOf("/") - 1) + 1).replace("/", "");
                    QueStatus que = new QueStatus(link, "unsolved",code,headers.get("tag"));
                    question.setUrl(link);
                    question.setStatus("unsolved");
                    question.setCode(code);
                    questions.add(question);
                    queStatus.add(que);
                }

                if(questions.size() == 0){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(List.of(new QueStatus("", "No problems found for the given tag","","")));
                }

                week.setQuestions(questions);

                // Add the week to the user
                currUser.getWeeks().add(week);


                // Update the user's solved list
                currUser.getSolved().addAll(
                        filteredProblems.stream()
                                .map(problem -> problem.getContestId() + problem.getIndex())
                                .collect(Collectors.toSet()));

                userServices.updateUser(currUser);

                return ResponseEntity.ok(queStatus);

            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(List.of(new QueStatus("", "Error fetching problems: ","","" + responseEntity.getStatusCode())));
            }
        } catch (Exception e) {
           // logger.error("Error fetching problems:", e); // Add error logging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of(new QueStatus("", "Error fetching problems: ","","" + e.getMessage())));
        }
    }

    @PostMapping("/markSolved")
    public ResponseEntity<String> markSolved(HttpServletRequest request){
        String questionUrl= request.getHeader("url");
        String handle = request.getHeader("handle");
        String submissionUrl= request.getHeader("submissionUrl");

        logger.info("Marking solved: " + questionUrl + " for user: " + handle+ " with submission: "+submissionUrl);

        UserModel currUser = userServices.getUserByHandle(handle);

        if(currUser == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User not found");
        }

        // get the ongoing week
        UserModel.Week week = currUser.getWeeks().get(currUser.getWeeks().size()-1);

        // get the question
        UserModel.Question question = week.getQuestions().stream()
                .filter(q -> q.getUrl().equals(questionUrl))
                .findFirst()
                .orElse(null);
        
        if(question == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Question not found");
        }

        question.setStatus("solved");
        question.setSubmission(submissionUrl);

        // check if the week is completed
        week.checkIfCompleted();

        userServices.updateUser(currUser);

        return ResponseEntity.ok("Marked Solved"); 
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

    private class QueStatus{
        private String url;
        private String  status;
        private String code;
        private String maintopic;

        public QueStatus(String url, String status, String code, String maintopic) {
            this.url = url;
            this.status = status;
            this.code = code;
            this.maintopic = maintopic; 
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMaintopic() {
            return maintopic;
        }

        public void setMaintopic(String maintopic) {
            this.maintopic = maintopic;
        }
    }
}
