package com.codeoftheweb.salvo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface ShipRepository extends JpaRepository<Ship, Long>{

    Optional<List<Ship>> findByGamePlayer(GamePlayer gamePlayer);

}
