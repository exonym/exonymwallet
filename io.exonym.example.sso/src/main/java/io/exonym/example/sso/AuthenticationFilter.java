package io.exonym.example.sso;

import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;
import eu.abc4trust.xml.SystemParameters;
import io.exonym.lib.actor.XContainerExternal;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.pojo.XContainerSchema;
import io.exonym.lib.wallet.ExonymOwner;
import org.apache.http.HttpRequest;
import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@WebFilter(filterName="authFilter", urlPatterns = {"/*"})
public class AuthenticationFilter implements Filter {

	private static Logger logger = Logger.getLogger(AuthenticationFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		try {
			// lambda.xml is on the class path, but you need to access it
			// load it into the external container.  The class loader in libexonymwallet
			// will fail.
			try (InputStream stream = ExonymAuthenticate.class.getClassLoader()
					.getResourceAsStream("lambda.xml")){

				byte[] in = new byte[stream.available()];
				stream.read(in);
				String lambdaXml = new String(in, StandardCharsets.UTF_8);
				XContainerExternal.loadSystemParams(lambdaXml);

			} catch (Exception e) {
				throw e;

			}
			// this instantiates the memory containers.
			// Tip: it's better to implement a local version of the network map using your database.
			ExonymOwner owner = ExonymOwner.verifierOnly();

		} catch (Exception e) {
			logger.error("", e);

		}
		logger.debug("Filter Active");

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		logger.debug("Filter Processing " + request.getRemoteAddr());
			chain.doFilter(request, response);

	}

	@Override
	public void destroy() {
		try {
			ExonymAuthenticate.getInstance().close();

		} catch (Exception e) {
			throw new RuntimeException(e);

		}
	}
}