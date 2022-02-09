package bio.overture.rollcall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.vault.config.SecretBackendConfigurer;
import org.springframework.cloud.vault.config.VaultConfigurer;
import org.springframework.context.annotation.Configuration;

/**
 * This configuration will override all the default vault paths generated by spring-cloud-vault
 * with just one secret path configured at "rollcall.vault.secretsPath". It is applied to the
 * bootstrap context via the META-INF/spring.factories. The config values come from the
 * bootstrap.yml or env configs.
 *
 * More info: https://docs.spring.io/spring-cloud-vault/docs/2.2.7.RELEASE/reference/html/#vault.config.backends.configurer
 *
 */
@Configuration
public class VaultConfig implements VaultConfigurer {
    @Value("${rollcall.vault.secretsPath}")
    String secretsPath;

    @Override
    public void addSecretBackends(SecretBackendConfigurer configurer) {
        configurer.add(secretsPath);
        configurer.registerDefaultGenericSecretBackends(false);
        configurer.registerDefaultDiscoveredSecretBackends(false);
    }
}
