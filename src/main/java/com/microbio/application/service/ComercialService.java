package com.microbio.application.service;

import com.microbio.application.dto.ComercialAnaliseDTO;
import com.microbio.application.model.Orcamento;
import com.microbio.application.model.OrcamentoStatus;
import com.microbio.application.repository.OrcamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ComercialService {

    private final OrcamentoRepository orcamentoRepository;
    private final ConfiguracaoService configuracaoService;
    private final com.microbio.application.repository.UsuarioRepository usuarioRepository;

    public ComercialService(OrcamentoRepository orcamentoRepository, 
                            ConfiguracaoService configuracaoService,
                            com.microbio.application.repository.UsuarioRepository usuarioRepository) {
        this.orcamentoRepository = orcamentoRepository;
        this.configuracaoService = configuracaoService;
        this.usuarioRepository = usuarioRepository;
    }

    public ComercialAnaliseDTO obterAnaliseComercial(String username, String role, Long responsavelId) {
        List<Orcamento> allOrcamentos = orcamentoRepository.findAll();
        
        List<Orcamento> orcamentos;
        if ("ADMIN".equals(role)) {
            if (responsavelId != null) {
                orcamentos = allOrcamentos.stream()
                        .filter(o -> o.getResponsavel() != null && responsavelId.equals(o.getResponsavel().getId()))
                        .toList();
            } else {
                orcamentos = allOrcamentos;
            }
        } else {
            orcamentos = List.of();
        }
        
        long leadsConvertidos = 0;
        long leadsPerdidos = 0;
        long leadsPendentes = 0;
        long leadsAceitos = 0;
        long leadsRejeitados = 0;
        long leadsConcluidos = 0;
        long totalDurationMinutes = 0;
        long countConvertedWithDuration = 0;
        BigDecimal valorPotencial = BigDecimal.ZERO;
        BigDecimal valorEmRisco = BigDecimal.ZERO;
        BigDecimal valorConvertido = BigDecimal.ZERO;
        BigDecimal valorPerdido = BigDecimal.ZERO;

        // Buscar prazo configurado
        String prazoStr = configuracaoService.getValor("prazo_acompanhamento_orcamentos", "48 horas");
        int deadlineHours = parseDeadlineHours(prazoStr);
        LocalDateTime limitTime = LocalDateTime.now().minusHours(deadlineHours);

        long orcamentosAtrasados = 0;

        for (Orcamento o : orcamentos) {
            OrcamentoStatus status = o.getStatus();
            LocalDateTime criacao = o.getDataCriacao() != null ? o.getDataCriacao() : LocalDateTime.now();
            BigDecimal preco = (o.getServico() != null && o.getServico().getPreco() != null)
                    ? o.getServico().getPreco() : BigDecimal.ZERO;

            if (status == OrcamentoStatus.FINALIZADO) {
                leadsConcluidos++;
                leadsConvertidos++;
                valorConvertido = valorConvertido.add(preco);
                LocalDateTime end = o.getDataMovimentacao() != null ? o.getDataMovimentacao() : criacao;
                long diffMinutes = Duration.between(criacao, end).toMinutes();
                if (diffMinutes > 0) {
                    totalDurationMinutes += diffMinutes;
                    countConvertedWithDuration++;
                }
            } else if (status == OrcamentoStatus.ACEITO) {
                leadsAceitos++;
                leadsConvertidos++;
                valorPotencial = valorPotencial.add(preco);
                LocalDateTime end = o.getDataMovimentacao() != null ? o.getDataMovimentacao() : criacao;
                long diffMinutes = Duration.between(criacao, end).toMinutes();
                if (diffMinutes > 0) {
                    totalDurationMinutes += diffMinutes;
                    countConvertedWithDuration++;
                }
            } else if (status == OrcamentoStatus.REJEITADO) {
                leadsRejeitados++;
                leadsPerdidos++;
                valorPerdido = valorPerdido.add(preco);
            } else if (status == OrcamentoStatus.PENDENTE) {
                leadsPendentes++;
                LocalDateTime lastMove = o.getDataMovimentacao() != null ? o.getDataMovimentacao() : criacao;
                if (lastMove.isBefore(limitTime)) {
                    orcamentosAtrasados++;
                    valorEmRisco = valorEmRisco.add(preco);
                }
            }
        }

        long leadsRecebidos = orcamentos.size();
        long leadsEmAberto = leadsPendentes + leadsAceitos;

        double taxaConversao = leadsRecebidos > 0
                ? ((double) leadsConcluidos / leadsRecebidos) * 100.0
                : 0.0;

        double tempoMedioConversaoHoras = countConvertedWithDuration > 0
                ? (double) totalDurationMinutes / 60.0 / countConvertedWithDuration
                : 0.0;

        return new ComercialAnaliseDTO(
                leadsRecebidos,
                leadsConvertidos,
                leadsPerdidos,
                taxaConversao,
                tempoMedioConversaoHoras,
                orcamentosAtrasados,
                leadsPendentes,
                leadsAceitos,
                leadsRejeitados,
                leadsConcluidos,
                leadsEmAberto,
                valorPotencial,
                valorEmRisco,
                valorConvertido,
                valorPerdido
        );
    }

    public List<com.microbio.application.dto.ResponsavelRankingDTO> obterRankingResponsaveis(
            String periodo, java.time.LocalDate inicio, java.time.LocalDate fim) {
        
        List<Orcamento> orcamentos = orcamentoRepository.findAll();
        List<com.microbio.application.model.Usuario> admins = usuarioRepository.findByRole("ADMIN");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = null;
        LocalDateTime end = null;

        if ("hoje".equalsIgnoreCase(periodo)) {
            start = java.time.LocalDate.now().atStartOfDay();
            end = now;
        } else if ("7dias".equalsIgnoreCase(periodo) || "7_dias".equalsIgnoreCase(periodo) || "7".equals(periodo)) {
            start = now.minusDays(7);
            end = now;
        } else if ("30dias".equalsIgnoreCase(periodo) || "30_dias".equalsIgnoreCase(periodo) || "30".equals(periodo)) {
            start = now.minusDays(30);
            end = now;
        } else if ("personalizado".equalsIgnoreCase(periodo)) {
            if (inicio != null) {
                start = inicio.atStartOfDay();
            }
            if (fim != null) {
                end = fim.atTime(java.time.LocalTime.MAX);
            }
        }

        final LocalDateTime finalStart = start;
        final LocalDateTime finalEnd = end;

        java.util.Map<String, Long> rankingMap = new java.util.HashMap<>();
        for (com.microbio.application.model.Usuario admin : admins) {
            rankingMap.put(admin.getUsername(), 0L);
        }

        orcamentos.stream()
                .filter(o -> o.getResponsavel() != null)
                .filter(o -> o.getStatus() == OrcamentoStatus.FINALIZADO || o.getStatus() == OrcamentoStatus.ACEITO)
                .filter(o -> {
                    LocalDateTime criacao = o.getDataCriacao() != null ? o.getDataCriacao() : LocalDateTime.now();
                    LocalDateTime dataConversao = o.getDataMovimentacao() != null ? o.getDataMovimentacao() : criacao;
                    if (finalStart != null && dataConversao.isBefore(finalStart)) {
                        return false;
                    }
                    if (finalEnd != null && dataConversao.isAfter(finalEnd)) {
                        return false;
                    }
                    return true;
                })
                .forEach(o -> {
                    String username = o.getResponsavel().getUsername();
                    rankingMap.put(username, rankingMap.getOrDefault(username, 0L) + 1);
                });

        return rankingMap.entrySet().stream()
                .map(entry -> new com.microbio.application.dto.ResponsavelRankingDTO(entry.getKey(), entry.getValue()))
                .sorted((r1, r2) -> Long.compare(r2.conversoes(), r1.conversoes()))
                .toList();
    }

    private int parseDeadlineHours(String deadlineStr) {
        if (deadlineStr == null || deadlineStr.isBlank()) {
            return 48;
        }
        deadlineStr = deadlineStr.toLowerCase().trim();
        try {
            if (deadlineStr.endsWith("h") || deadlineStr.contains("hora")) {
                String numStr = deadlineStr.replaceAll("[^0-9]", "");
                return Integer.parseInt(numStr);
            } else if (deadlineStr.endsWith("d") || deadlineStr.contains("dia")) {
                String numStr = deadlineStr.replaceAll("[^0-9]", "");
                return Integer.parseInt(numStr) * 24;
            } else {
                return Integer.parseInt(deadlineStr.replaceAll("[^0-9]", ""));
            }
        } catch (Exception e) {
            return 48;
        }
    }
}
