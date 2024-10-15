package io.exonym.lib.pojo;



import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;

import java.net.URI;

public class OverrideRequest implements VioIndexable {

    public static final String TYPE_CLEAR = "CLEAR";

    public static final String TYPE_PLAIN = "PLAIN";

    private String kid, key;
    private String type;
    private String nibble6;
    private String x0Hash;
    private URI modOfVioUid;
    private String timeOfViolation;

    public void validate() throws UxException {
        StringBuilder builder = new StringBuilder();
        builder.append("The following attributes were required, but were null: ");

        if (kid==null || key==null){
            throw new UxException(ErrorMessages.FAILED_TO_AUTHORIZE);

        } if (type==null){
            builder.append("type: ");

        } else {
            if (!type.equals(TYPE_PLAIN)){
                throw new UxException("ONLY_PLAIN_CURRENTLY_IMPL");

            }
        }
        if (nibble6 ==null){
            builder.append("n6: ");

        } if (x0Hash==null){
            builder.append("x0Hash: ");

        } if (modOfVioUid ==null){
            builder.append("modOfVioUid: ");

        } if (timeOfViolation ==null){
            builder.append("timeOfVio: ");

        }
        if (type==null || nibble6 ==null || x0Hash==null || timeOfViolation == null || modOfVioUid ==null){
            throw new UxException(ErrorMessages.INCORRECT_PARAMETERS, builder.toString());

        }
    }

    public String getKid() {
        return kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNibble6() {
        return nibble6;
    }

    public void setNibble6(String nibble6) {
        this.nibble6 = nibble6;
    }

    public String getX0Hash() {
        return x0Hash;
    }

    public void setX0Hash(String x0Hash) {
        this.x0Hash = x0Hash;
    }

    public String getTimeOfViolation() {
        return timeOfViolation;
    }

    public void setTimeOfViolation(String timeOfViolation) {
        this.timeOfViolation = timeOfViolation;
    }

    public URI getModOfVioUid() {
        return modOfVioUid;
    }

    public void setModOfVioUid(URI modOfVioUid) {
        this.modOfVioUid = modOfVioUid;
    }

}
