package tools.ceac.ai.modules.qbid.interfaces.api;

import org.springframework.stereotype.Component;
import tools.ceac.ai.modules.qbid.application.auth.QbidRuntimeCredentials;
import tools.ceac.ai.modules.qbid.application.service.SesionCache;
import tools.ceac.ai.modules.qbid.domain.exception.SessionExpiredException;

/**
 * Resolves the qBid operator session for the local REST API using the runtime credentials already
 * loaded in the embedded process.
 */
@Component
public class QbidApiSessionProvider {

    private final QbidRuntimeCredentials runtimeCredentials;
    private final SesionCache sesionCache;

    public QbidApiSessionProvider(QbidRuntimeCredentials runtimeCredentials, SesionCache sesionCache) {
        this.runtimeCredentials = runtimeCredentials;
        this.sesionCache = sesionCache;
    }

    public String currentSession() throws Exception {
        String basicAuth = runtimeCredentials.buildBasicAuthHeader();
        try {
            return sesionCache.resolveSession(basicAuth);
        } catch (SessionExpiredException ex) {
            return sesionCache.renewSession(basicAuth);
        }
    }
}
