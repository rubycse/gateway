package com.musala.gateway.repository;

import com.musala.gateway.entity.Gateway;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author lutfun
 * @since 5/18/21
 */
@Repository
public interface GatewayRepository extends CrudRepository<Gateway, Long> {

    Optional<Gateway> findBySerialNumber(String serialNumber);
}
