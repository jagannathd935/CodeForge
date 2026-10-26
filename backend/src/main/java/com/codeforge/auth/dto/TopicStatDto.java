package com.codeforge.auth.dto;

public class TopicStatDto {

    private String topic;
    private long totalSubmissions;
    private long acceptedSubmissions;
    private long uniqueSolved;
    private double accuracy;

    public TopicStatDto() {
    }

    public TopicStatDto(String topic, long totalSubmissions, long acceptedSubmissions, long uniqueSolved, double accuracy) {
        this.topic = topic;
        this.totalSubmissions = totalSubmissions;
        this.acceptedSubmissions = acceptedSubmissions;
        this.uniqueSolved = uniqueSolved;
        this.accuracy = accuracy;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(long totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public long getAcceptedSubmissions() {
        return acceptedSubmissions;
    }

    public void setAcceptedSubmissions(long acceptedSubmissions) {
        this.acceptedSubmissions = acceptedSubmissions;
    }

    public long getUniqueSolved() {
        return uniqueSolved;
    }

    public void setUniqueSolved(long uniqueSolved) {
        this.uniqueSolved = uniqueSolved;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }
}
