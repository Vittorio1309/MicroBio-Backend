package com.microbio.application.repository;

import com.microbio.application.model.Configuracao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ConfiguracaoRepository extends JpaRepository<Configuracao, Long> {
    Optional<Configuracao> findByChave(String chave);
}
