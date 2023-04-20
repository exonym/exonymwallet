package io.exonym.lib.pojo;

import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.standard.QrCode;

import java.net.URI;

public interface ExonymChallenge {


    public String universalLinkPrefix();

    public URI getDomain();

}
