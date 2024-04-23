package me.patrzyk;

import java.util.Date;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

public class Detect {
    @JsonProperty("ts")
    private Date ts;

    @JsonProperty("last_change")
    private Date lastChange;

    @JsonProperty("diff")
    private String diff;
    
    public Detect(Date ts, Date lastChange, String diff) {
        this.ts = ts;
        this.lastChange = lastChange;
        this.diff = diff;
    }

    @Override
    public String toString() {
        return "Detect [ts=" + ts + ", lastChange=" + lastChange + ", diff=" + diff + "]";
    }

}
