package com.intuit.yappa.apiclientes.repositories;

import com.intuit.yappa.apiclientes.models.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    Optional<Cliente> findByCuit(String cuit);

    Optional<Cliente> findByEmail(String email);

    // Call to Stored Procedure
    @Query(value = "SELECT * FROM search_clientes_by_name(:name)", nativeQuery = true)
    List<Cliente> searchClientesByName(@Param("name") String name);
}
