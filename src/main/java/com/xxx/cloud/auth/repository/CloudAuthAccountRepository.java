package com.xxx.cloud.auth.repository;

import com.xxx.cloud.auth.models.entity.CloudAuthAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CloudAuthAccountRepository extends JpaRepository<CloudAuthAccount, Long> {

    CloudAuthAccount findFirstByUsername(String username);

}
