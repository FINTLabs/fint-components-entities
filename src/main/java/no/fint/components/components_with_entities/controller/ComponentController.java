package no.fint.components.components_with_entities.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import no.fint.components.components_with_entities.ComponentWithEntities;
import no.fint.components.components_with_entities.Entity;
import no.fint.components.components_with_entities.EntityResponse;
import no.fint.components.components_with_entities.service.PortalApiService;
import no.fint.portal.exceptions.EntityFoundException;
import no.fint.portal.exceptions.EntityNotFoundException;
import no.fint.portal.exceptions.UpdateEntityMismatchException;
import no.fint.portal.model.ErrorResponse;
import no.fint.portal.model.component.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@Api(tags = "Components")
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/components/entities")
public class ComponentController {

    @Autowired
    PortalApiService portalApiService;

    @ApiOperation("Get all components with entities")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity getComponentsWithEntities() {
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

        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(componentWithEntitiesList);
    }

    private List<Entity> getEntities(Component component, EntityResponse entityList) {
        return entityList.get_embedded().get_entries().stream().filter(entity -> {
                String componentStringForMatch = "no.fint" + component.getBasePath().replace("/", ".");
                String entityIdForMatch = entity.getId().getIdentifikatorverdi().substring(0, entity.getId().getIdentifikatorverdi().lastIndexOf("."));
                return componentStringForMatch.equals(entityIdForMatch);
        })
                .collect(Collectors.toList());
    }

    @ExceptionHandler(UpdateEntityMismatchException.class)
    public ResponseEntity handleUpdateEntityMismatch(Exception e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity handleEntityNotFound(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(EntityFoundException.class)
    public ResponseEntity handleEntityFound(Exception e) {
        return ResponseEntity.status(HttpStatus.FOUND).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(NameNotFoundException.class)
    public ResponseEntity handleNameNotFound(Exception e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(UnknownHostException.class)
    public ResponseEntity handleUnkownHost(Exception e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorResponse(e.getMessage()));
    }


}
