package com.codeclash.contracts.auth.session.remote;

import com.codeclash.contracts.auth.session.BearerSessionFilter;
import com.codeclash.contracts.auth.session.SessionPrincipal;
import com.codeclash.contracts.auth.session.SessionResolver;
import com.codeclash.contracts.auth.session.exception.AuthUnavailableException;
import java.net.http.HttpClient;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ApiVersionInserter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@RequiredArgsConstructor
public class RemoteSessionResolver implements SessionResolver {

  private static final int API_VERSION_SEGMENT = 1;
  private static final String API_VERSION_PREFIX = "v";

  private final SessionClient sessionClient;

  public static RemoteSessionResolver create(
      RestClient.Builder builder, AuthClientProperties properties) {
    JdkClientHttpRequestFactory requestFactory =
        new JdkClientHttpRequestFactory(
            HttpClient.newBuilder().connectTimeout(properties.connectTimeout()).build());
    requestFactory.setReadTimeout(properties.readTimeout());
    RestClient restClient =
        builder
            .clone()
            .baseUrl(properties.baseUrl().toString())
            .requestFactory(requestFactory)
            .apiVersionInserter(
                ApiVersionInserter.builder()
                    .usePathSegment(RemoteSessionResolver.API_VERSION_SEGMENT)
                    .withVersionFormatter(
                        version -> RemoteSessionResolver.API_VERSION_PREFIX + version)
                    .build())
            .build();
    SessionClient sessionClient =
        HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
            .build()
            .createClient(SessionClient.class);
    return new RemoteSessionResolver(sessionClient);
  }

  @Override
  public Optional<SessionPrincipal> resolve(UUID sessionId) {
    try {
      SessionResponse session =
          this.sessionClient.findById(sessionId, BearerSessionFilter.SCHEME + " " + sessionId);
      return Optional.of(new SessionPrincipal(session.userId(), Set.copyOf(session.permissions())));
    } catch (HttpClientErrorException.Unauthorized
        | HttpClientErrorException.Forbidden
        | HttpClientErrorException.NotFound
        | HttpClientErrorException.UnprocessableContent exception) {
      return Optional.empty();
    } catch (RestClientException exception) {
      throw new AuthUnavailableException(exception);
    }
  }
}
