package io.exonym.example.sso;

import io.exonym.lib.actor.IdContainerExternal;
import io.exonym.lib.wallet.ExonymOwner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Date;
import java.time.Instant;
import java.util.UUID;

@WebFilter(filterName="authFilter", urlPatterns = {"/*"})
public class AuthenticationFilter implements Filter {

	private static final Logger logger = LogManager.getLogger(AuthenticationFilter.class);

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
				IdContainerExternal.loadSystemParams(lambdaXml);
//				testFileWrite();

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

	private void testFileWrite() throws IOException {
		Path writeLocation = Path.of("/var", "www", "html", "tokens");
		Files.createDirectories(writeLocation);
		Path filePath = writeLocation.resolve("test.json");
		Files.write(filePath,
				("{'test':'If you can read this, the server successfully wrote to the replication directory'," +
						"'time':'" + Date.from(Instant.now()) + "'}")
						.getBytes(StandardCharsets.UTF_8),
				StandardOpenOption.CREATE);
		logger.info("Written file" + filePath);

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