package com.xxx.cloud.auth.repository;

import com.xxx.cloud.auth.entity.CloudRegisteredClient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CloudRegisteredClientRepository extends JpaRepository<CloudRegisteredClient, Long> {

    CloudRegisteredClient findFirstByClientId(String clientId);
}
