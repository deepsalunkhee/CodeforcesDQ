package com.deepsalunkhee.cfdqServer.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.deepsalunkhee.cfdqServer.Services.UserServices;
import com.deepsalunkhee.cfdqServer.models.UserModel;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("api/v1")
public class OpenControllers {

    private static final Logger logger = LoggerFactory.getLogger(OpenControllers.class);

    @Autowired
    private  UserServices userServices;

    // Explicitly handle OPTIONS preflight requests for CORS
    @RequestMapping(method = RequestMethod.OPTIONS, value = "/**")
    public ResponseEntity<Void> handleOptionsRequest() {
        return ResponseEntity.status(HttpStatus.OK).build(); // Responds with 200 OK
    }

    @GetMapping("")
    public String home() {
        return "cfdq server is running";
    }

    @GetMapping("/latestWeek")
    public ResponseEntity<List<QueStatus>>latestWeek(HttpServletRequest request){

        String handle= request.getHeader("handle");
        UserModel currUser = userServices.getUserByHandle(handle);

        if(currUser == null){
            return ResponseEntity.ok().body(null);
        }


         if(currUser.getWeeks().size() == 0){
            return ResponseEntity.ok(null);
}
         UserModel.Week week = currUser.getWeeks().get(currUser.getWeeks().size()-1);

         //convert the list of questions to a list of  QueStatus
        List<QueStatus> questions = week.getQuestions().stream().map(question -> new QueStatus(question.getUrl(), question.getStatus(),question.getCode(),week.getTopic())).toList();
        

        return ResponseEntity.ok(questions);
        
    }

    @GetMapping("/allweeks")
    public ResponseEntity<List<SolvedWeek>> solvedWeeks(HttpServletRequest request){

        String handle= request.getParameter("handle");
        logger.info("Handle: " + handle);
        if(handle == null){
            return ResponseEntity.badRequest().body(null);
        }

        UserModel currUser = userServices.getUserByHandle(handle);

        if(currUser == null){
            return ResponseEntity.badRequest().body(null);
        }

        List<SolvedWeek> solvedWeeks = currUser.getWeeks().stream().map(week -> {
            SolvedWeek solvedWeek = new SolvedWeek();
            solvedWeek.weekNo = week.getWeekNo();
            solvedWeek.questions = week.getQuestions().stream().map(question -> new SoledQue(question.getUrl(), question.getSubmission(),question.getCode(),week.getTopic())).toList();
            return solvedWeek;
        }).toList();

        return ResponseEntity.ok(solvedWeeks);
    }

    private class QueStatus{
        private String url;
        private String  status;
        private String code;
        private String mainTopic;

        public QueStatus(String url, String status, String code, String mainTopic) {
            this.url = url;
            this.status = status;
            this.code = code;
            this.mainTopic = mainTopic;
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

        public String getMainTopic() {
            return mainTopic;
        }

        public void setMainTopic(String mainTopic) {
            this.mainTopic = mainTopic;
        }
    }

    private class SolvedWeek{
        private int weekNo;
        private List<SoledQue> questions;

        public int getWeekNo() {
            return weekNo;
        }

        public void setWeekNo(int weekNo) {
            this.weekNo = weekNo;
        }

        public List<SoledQue> getQuestions() {
            return questions;
        }

        public void setQuestions(List<SoledQue> questions) {
            this.questions = questions;
        }

        


    }

    private class SoledQue{
        private String url;
        private String submission;
        private String code;
        private String mainTopic;

        public SoledQue(String url, String submission, String code, String mainTopic) {
            this.url = url;
            this.submission = submission;
            this.code = code;
            this.mainTopic = mainTopic;
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

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMainTopic() {
            return mainTopic;
        }

        public void setMainTopic(String mainTopic) {
            this.mainTopic = mainTopic;
        }


    }
}
