package ch.dafo90.swissqrbillgenerator.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Configuration
public class RequestLoggingFilterConfig {

    private final String managementBasePath;

    public RequestLoggingFilterConfig(@Value("${management.endpoints.web.base-path:/actuator}") String managementBasePath) {
        this.managementBasePath = managementBasePath;
    }

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CustomRequestLoggingFilter(sanitizePath(managementBasePath));
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false);
        filter.setIncludeClientInfo(true);
        return filter;
    }

    protected String sanitizePath(String path) {
        if (!StringUtils.hasText(path)) {
            return null;
        }
        String newPath = Stream.of(path.trim().split("/")).filter(StringUtils::hasText).map(String::trim).collect(Collectors.joining("/"));
        if (StringUtils.hasText(newPath)) {
            return String.format("/%s/", newPath);
        }
        return "/";
    }

    protected static class CustomRequestLoggingFilter extends CommonsRequestLoggingFilter {

        private final Set<String> exclusions;

        public CustomRequestLoggingFilter(String... exclusions) {
            this.exclusions = filterEmptyString(Stream.of(exclusions));
        }

        protected final Set<String> filterEmptyString(Stream<String> exclusions) {
            return exclusions
                    .filter(StringUtils::hasText)
                    .map(String::trim).filter(str -> !str.equals("/"))
                    .collect(Collectors.toSet());
        }

        @Override
        protected boolean shouldLog(HttpServletRequest request) {
            return exclusions.stream()
                    .filter(exclusion -> request.getRequestURI().contains(exclusion))
                    .findFirst()
                    .map(exclusion -> false)
                    .orElseGet(log::isDebugEnabled);
        }

        @Override
        protected void beforeRequest(HttpServletRequest request, String message) {
            log.debug(message);
        }

        @Override
        protected void afterRequest(HttpServletRequest request, String message) {
            log.debug(message);
        }

    }

}
