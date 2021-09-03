package com.xxx.cloud.auth.service;

import com.xxx.cloud.auth.models.entity.CloudAuthAccount;
import com.xxx.cloud.auth.repository.CloudAuthAccountRepository;
import com.xxx.cloud.auth.security.model.CloudUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CloudUserDetailsService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudUserDetailsService.class);

    private final CloudAuthAccountRepository cloudAuthAccountRepository;

    public CloudUserDetailsService(CloudAuthAccountRepository cloudAuthAccountRepository) {
        this.cloudAuthAccountRepository = cloudAuthAccountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CloudAuthAccount account = cloudAuthAccountRepository.findFirstByUsername(username);
        if (Objects.isNull(account)) {
            LOGGER.debug("Account [{}] not found.", username);
            throw new UsernameNotFoundException(String.format("Account: [%s] not found.", username));
        }
        return CloudUserDetails.withId(account.getId())
                .username(account.getUsername())
                .password(account.getPassword())
                .roles(account.getRoles().toArray(String[]::new))
                .accountNonLocked(!account.accountLocked())
                .enabled(account.getEnabled())
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .build();
    }
}
