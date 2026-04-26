package net.microfalx.bootstrap.configuration;

import net.microfalx.bootstrap.configuration.annotation.ConfigurationMapping;
import net.microfalx.bootstrap.configuration.annotation.EnableConfigurationMapping;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Map;

public class ConfigurationPropertiesRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        Map<String, Object> attrs = metadata.getAnnotationAttributes(EnableConfigurationMapping.class.getName());
        if (attrs == null) return;
        String[] basePackages = (String[]) attrs.get("basePackages");
        ClassPathScanningCandidateComponentProvider scanner = new InterfaceAwareScanner();
        scanner.addIncludeFilter(new AnnotationTypeFilter(ConfigurationMapping.class));
        for (String basePackage : basePackages) {
            for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
                String className = bd.getBeanClassName();
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ConfigurationMappingFactoryBean.class);
                builder.addConstructorArgValue(className);
                //builder.addConstructorArgReference("configurationService");
                //builder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
                builder.setRole(BeanDefinition.ROLE_SUPPORT);
                registry.registerBeanDefinition(className, builder.getBeanDefinition());
            }
        }
    }

    private static class InterfaceAwareScanner extends ClassPathScanningCandidateComponentProvider {

        public InterfaceAwareScanner() {
            super(false);
        }

        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            return beanDefinition.getMetadata().isInterface();
        }
    }
}
