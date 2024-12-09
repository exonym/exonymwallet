package io.exonym.lib.api;

import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.helpers.UIDHelper;
import io.exonym.lib.helpers.UrlHelper;
import io.exonym.lib.pojo.Rulebook;
import io.exonym.lib.pojo.RulebookDescription;
import io.exonym.lib.pojo.RulebookItem;
import io.exonym.lib.standard.CryptoUtils;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RulebookVerifier {

    private Rulebook rulebook = null;

    public RulebookVerifier(String rulebookPath) throws Exception {
        Path path = Paths.get(rulebookPath);
        Rulebook rulebook = JaxbHelper.jsonFileToClass(path, Rulebook.class);
        verifyRulebook(rulebook);

    }

    public RulebookVerifier(Rulebook rulebook) throws Exception {
        this.rulebook = rulebook;
        verifyRulebook(rulebook);

    }

    public RulebookVerifier(URL rulebookUrl) throws Exception {
        String r = new String(UrlHelper.read(rulebookUrl), StandardCharsets.UTF_8);
        this.rulebook = JaxbHelper.jsonToClass(r, Rulebook.class);
        verifyRulebook(rulebook);

    }


    private void verifyRulebook(Rulebook rulebook) throws UxException {
        StringBuilder builder = new StringBuilder();
        RulebookDescription d =  rulebook.getDescription();
        builder.append(d.isProduction());
        builder.append(d.getName());
        builder.append(d.getSimpleDescriptionEN());

        for (RulebookItem item : rulebook.getRules()){
            String id = item.getId();
            String parts[] = id.split(":");
            String hash = parts[4];
            String compare = CryptoUtils.computeSha256HashAsHex(item.getDescription());
            if (!hash.equals(compare)){
                throw new UxException(ErrorMessages.FAILED_TO_AUTHORIZE,
                        compare, hash, "The rule has been changed and is invalid.");

            }
            builder.append(id);

        }
        String rulebookId = CryptoUtils.computeSha256HashAsHex(builder.toString());
        String id = UIDHelper.computeRulebookHashUid(URI.create(rulebook.getRulebookId()));

        if (!id.equals(rulebookId)){
            throw new UxException(ErrorMessages.FAILED_TO_AUTHORIZE, "The Rulebook ID is invalid.");

        }
    }

    public Rulebook getRulebook() {
        return rulebook;
    }
}
