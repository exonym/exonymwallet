package io.exonym.lib.pojo;

import eu.abc4trust.xml.PresentationToken;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.helpers.UIDHelper;

import java.net.URI;

public class EndonymToken {

    private URI endonym;
    private String xmlPresentationToken;
    private URI moderatorUid;

    public static EndonymToken build(URI endonym, PresentationToken pt) throws UxException {
        try {
            EndonymToken result = new EndonymToken();
            result.setXmlPresentationToken(IdContainer.convertObjectToXml(pt));
            result.setEndonym(endonym);
            return result;

        } catch (Exception e) {
            throw new UxException(ErrorMessages.TOKEN_INVALID + ":Forward Serialization Error");

        }
    }

    public URI getEndonym() {
        return endonym;
    }

    public void setEndonym(URI endonym) {
        this.endonym = endonym;
    }

    public String getXmlPresentationToken() {
        return xmlPresentationToken;
    }

    public void setXmlPresentationToken(String xmlPresentationToken) {
        this.xmlPresentationToken = xmlPresentationToken;
    }

    public URI getModeratorUid() {
        return moderatorUid;
    }

    public void setModeratorUid(URI moderatorUid) {
        this.moderatorUid = moderatorUid;
    }

    public String computeIndex() throws Exception {
        String r = UIDHelper.computeShortRulebookHashUid(moderatorUid);
        String fn = IdContainer.uidToFileName(endonym);

        String modName = UIDHelper.computeModNameFromModUid(moderatorUid);
        String leadName = UIDHelper.computeLeadNameFromModOrLeadUid(moderatorUid);

        fn = fn.replace("urn", leadName);
        fn = fn.replace("endonym", modName);

        StringBuilder builder = new StringBuilder();
        builder.append(r)
                .append(".")
                .append(fn);
        return builder.toString();

    }
}
