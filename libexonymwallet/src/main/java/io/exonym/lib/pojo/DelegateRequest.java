package io.exonym.lib.pojo;

import java.net.URI;

public class DelegateRequest extends SsoConfiguration implements ExonymChallenge {


    public static DelegateRequest newDelegateRequest(URI accessDomain) throws Exception {
        DelegateRequest result = new DelegateRequest();
        result.setDomain(accessDomain);
        return result;

    }

    @Override
    public String universalLinkPrefix() {
        return Namespace.UNIVERSAL_LINK_DELEGATE_REQUEST;
    }
}
