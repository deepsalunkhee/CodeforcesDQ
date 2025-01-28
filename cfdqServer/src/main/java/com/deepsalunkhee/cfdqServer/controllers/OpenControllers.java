package com.deepsalunkhee.cfdqServer.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deepsalunkhee.cfdqServer.Services.UserServices;
import com.deepsalunkhee.cfdqServer.models.UserModel;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("api/v1")
public class OpenControllers {

    @Autowired
    private  UserServices userServices;

    @GetMapping("")
    public String home() {
        return "cfdq server is running";
    }

    @GetMapping("/latestWeek")
    public ResponseEntity<List<QueStatus>>latestWeek(HttpServletRequest request){

        String handle= request.getHeader("handle");
        UserModel currUser = userServices.getUserByHandle(handle);

        if(currUser == null){
            return ResponseEntity.badRequest().body(null);
        }


         if(currUser.getWeeks().size() == 0){
            return ResponseEntity.ok(null);
}
         UserModel.Week week = currUser.getWeeks().get(currUser.getWeeks().size()-1);

         //convert the list of questions to a list of  QueStatus
        List<QueStatus> questions = week.getQuestions().stream().map(question -> new QueStatus(question.getUrl(), question.getStatus())).toList();
        

        return ResponseEntity.ok(questions);
        
    }

    @GetMapping("/solvedWeeks")
    public ResponseEntity<List<SolvedWeek>> solvedWeeks(HttpServletRequest request){

        String handle= request.getHeader("handle");
        UserModel currUser = userServices.getUserByHandle(handle);

        if(currUser == null){
            return ResponseEntity.badRequest().body(null);
        }

        List<SolvedWeek> solvedWeeks = currUser.getWeeks().stream().filter(week -> week.isCompleted()).map(week -> {
            SolvedWeek solvedWeek = new SolvedWeek();
            solvedWeek.weekNo = week.getWeekNo();
            solvedWeek.questions = week.getQuestions().stream().map(question -> new SoledQue(question.getUrl(), question.getSubmission())).toList();
            return solvedWeek;
        }).toList();

        return ResponseEntity.ok(solvedWeeks);
    }

    private class QueStatus{
        private String url;
        private String  status;

        public QueStatus(String url, String status) {
            this.url = url;
            this.status = status;
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

        public SoledQue(String url, String submission) {
            this.url = url;
            this.submission = submission;
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
