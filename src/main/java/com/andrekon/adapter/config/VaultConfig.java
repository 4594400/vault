package com.andrekon.adapter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.VaultException;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.support.VaultToken;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Configuration
public class VaultConfig {
    static Logger LOGGER = LoggerFactory.getLogger(VaultConfig.class);
    public static final String LOGIN_PATH = "v1/auth/userpass/login";

    @Bean
    public ClientAuthentication clientAuthentication(@Value("${VAULT_USERNAME}") String username,
                                                     @Value("${VAULT_PASSWORD}") String password,
                                                     @Value("${spring.cloud.vault.scheme}") String scheme,
                                                     @Value("${VAULT_HOST}") String host) {
        System.out.println("create ClientAuthentication" + username);
        LOGGER.info("$$$$$ Creating ClientAuthentication bean");
        return new UserPassAuthentication(scheme, host, LOGIN_PATH, username, password);
    }

    public static class UserPassAuthentication implements ClientAuthentication {
        private RestOperations restOperations = new RestTemplate();

        private String url;
        private String password;

        public UserPassAuthentication(String scheme, String host, String path, String user, String password){
            this.url = UriComponentsBuilder.newInstance()
                    .scheme(scheme)
                    .host(host)
                    .pathSegment(path, user)
                    .build()
                    .toUriString();
            this.password = password;
        }

        @Override
        public VaultToken login() throws VaultException {
            VaultToken token = VaultToken.of(
                    ((Map<String,String>) restOperations.postForEntity(url, new Password(password), Map.class)
                    .getBody().get("auth")).get("client_token"));
            return token;
        }

        static class Password{
            private String password;

            public Password(String pasword) {
                this.password = pasword;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }
        }
    }
}
