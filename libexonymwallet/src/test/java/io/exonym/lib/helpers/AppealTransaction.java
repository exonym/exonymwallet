package io.exonym.lib.helpers;


public class AppealTransaction implements Comparable<AppealTransaction> {

    public static final String ACTOR_PRODUCER = "PRODUCER";
    public static final String ACTOR_CONSUMER = "CONSUMER";
    public static final String ACTOR_MODERATOR = "MODERATOR";
    public static final String ACTOR_LEAD = "LEAD";

    private long timestamp;
    private String dateTime;

    private String actor;

    private String comment;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


    @Override
    public int compareTo(AppealTransaction other) {
        return Long.compare(this.timestamp, other.timestamp);
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
