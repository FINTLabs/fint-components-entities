package no.fint.components.componententities.service;

import lombok.Synchronized;
import no.fint.components.componententities.EntityResponse;
import no.fint.components.componententities.exception.InvalidResourceException;
import no.fint.portal.model.component.Component;
import no.fint.portal.model.component.ComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;


@Service
public class PortalApiService {
    @Autowired
    private ComponentService componentService;

    @Retryable(
            backoff = @Backoff(delay = 200L),
            value = {InvalidResourceException.class},
            maxAttempts = 5
    )
    @Synchronized
    public List<Component> getComponents() {
        List<Component> components = componentService.getComponents();

        if (components.size() == 0) return Collections.emptyList();
        if (components.get(0).getName() == null) throw new InvalidResourceException("Invalid component");
        return components;
    }

    public Mono<EntityResponse> getEntities(){
        WebClient weclient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .defaultHeader("x-org-id", "fintlabs.no")
                .defaultHeader("x-client", "fintlabs.no")
                .baseUrl("https://beta.felleskomponent.no/fint/")
                .build();
        return weclient.get().uri("metamodell/klasse/")
                .retrieve()
                .bodyToMono(EntityResponse.class);
    }
}
