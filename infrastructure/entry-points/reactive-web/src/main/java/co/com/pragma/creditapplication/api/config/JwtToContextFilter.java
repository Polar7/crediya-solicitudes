package co.com.pragma.creditapplication.api.config;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtToContextFilter implements WebFilter {

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html",
            "/swagger-ui/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Saltar swagger y api-docs
        if (EXCLUDED_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        return ReactiveSecurityContextHolder.getContext()
                .flatMap(context -> {
                    if (context.getAuthentication() instanceof JwtAuthenticationToken jwtToken) {
                        String tokenValue = jwtToken.getToken().getTokenValue();
                        return chain.filter(exchange)
                                .contextWrite(ctx -> ctx.put("jwt", tokenValue));
                    }
                    return chain.filter(exchange);
                });
    }

}
