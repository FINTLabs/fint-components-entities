package no.fint.components;

import no.fint.portal.model.component.ComponentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public ComponentService getComponentService(){
        return new ComponentService();
    }
}
