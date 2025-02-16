package me.patrzyk;

import java.util.Date;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

public class Entry {
    @JsonProperty("key")
    private String key;

    @JsonProperty("ts")
    private Date ts;

    @JsonProperty("content")
    private String content;

    @Override
    public String toString() {
        return "Entry [key=" + key + ", ts=" + ts + ", content=" + content + "]";
    }

    public Date getTs() {
        return ts;
    }

    public String getContent() {
        return content;
    }

    public String getKey() {
        return key;
    }

}
