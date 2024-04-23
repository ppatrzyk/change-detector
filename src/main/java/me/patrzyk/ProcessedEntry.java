package me.patrzyk;

import java.util.Date;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

public class ProcessedEntry {
    @JsonProperty("ts")
    private Date ts;

    @JsonProperty("previous_ts")
    private Date previousTs;

    @JsonProperty("content")
    private String content;

    @JsonProperty("previous_content")
    private String previousContent;

    @JsonProperty("diff")
    private String diff;

    public ProcessedEntry(Date ts, Date previousTs, String content, String previousContent, String diff) {
        this.ts = ts;
        this.previousTs = previousTs;
        this.content = content;
        this.previousContent = previousContent;
        this.diff = diff;
    }

    @Override
    public String toString() {
        return "ProcessedEntry [ts=" + ts + ", previousTs=" + previousTs + ", content=" + content + ", previousContent="
                + previousContent + ", diff=" + diff + "]";
    }

}
