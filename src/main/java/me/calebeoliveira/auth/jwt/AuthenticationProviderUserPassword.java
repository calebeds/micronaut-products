package me.calebeoliveira.auth.jwt;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationFailureReason;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.provider.AuthenticationProvider;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

@Singleton
public class AuthenticationProviderUserPassword implements AuthenticationProvider<HttpRequest<?>, String, String> {

    private final static Logger LOG = LoggerFactory.getLogger(AuthenticationProviderUserPassword.class);

    @Override
    public @NonNull AuthenticationResponse authenticate(@Nullable HttpRequest<?> requestContext,
                                                        @NonNull AuthenticationRequest<String, String> authRequest) {
        final String identity = authRequest.getIdentity();
        final String secret = authRequest.getSecret();
        LOG.debug("User {} tries to login...", identity);

        if (identity.equals("my-user") && secret.equals("secret")) {
            //pass
            return AuthenticationResponse.success(identity, new ArrayList<>());
        }

        throw AuthenticationResponse.exception(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH);
    }
}


