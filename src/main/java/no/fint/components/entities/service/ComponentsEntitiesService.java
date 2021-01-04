package no.fint.components.entities.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.components.entities.ComponentWithEntities;
import no.fint.components.entities.Entity;
import no.fint.components.entities.EntityResponse;
import no.fint.portal.model.component.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ComponentsEntitiesService {

    @Autowired
    PortalApiService portalApiService;

    public List<ComponentWithEntities> getComponentsWithEntities() {
        List<Component> components = portalApiService.getComponents();
        Mono<EntityResponse> entities = portalApiService.getEntities();
        EntityResponse entityList = entities.block();

        List<ComponentWithEntities> componentWithEntitiesList = new ArrayList<>();

        components.forEach(component -> {
            ComponentWithEntities componentWithEntities = new ComponentWithEntities();
            componentWithEntities.setDn(component.getDn());
            componentWithEntities.setBasePath(component.getBasePath());
            componentWithEntities.setDescription(component.getDescription());
            componentWithEntities.setName(component.getName());
            componentWithEntities.setOpenData(component.isOpenData());
            componentWithEntities.setCommon(component.isCommon());
            componentWithEntities.setEntities(getEntities(component, entityList));
            componentWithEntitiesList.add(componentWithEntities);
        });

        return componentWithEntitiesList;
    }

    private List<Entity> getEntities(Component component, EntityResponse entityList) {
        return entityList.get_embedded().get_entries().stream().filter(entity -> {
            String componentStringForMatch = "no.fint" + component.getBasePath().replace("/", ".");
            String entityIdForMatch = entity.getId().getIdentifikatorverdi().substring(0, entity.getId().getIdentifikatorverdi().lastIndexOf("."));
            return componentStringForMatch.equals(entityIdForMatch);
        })
                .collect(Collectors.toList());
    }
}
