package com.microbio.application.service;

import com.microbio.application.dto.ComercialAnaliseDTO;
import com.microbio.application.dto.ResponsavelRankingDTO;
import com.microbio.application.model.Orcamento;
import com.microbio.application.model.OrcamentoStatus;
import com.microbio.application.model.Usuario;
import com.microbio.application.repository.OrcamentoRepository;
import com.microbio.application.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class ComercialService {

    private static final String REGEX_APENAS_NUMEROS = "[^0-9]";

    private final OrcamentoRepository orcamentoRepository;
    private final ConfiguracaoService configuracaoService;
    private final UsuarioRepository usuarioRepository;

    public ComercialService(OrcamentoRepository orcamentoRepository,
                            ConfiguracaoService configuracaoService,
                            UsuarioRepository usuarioRepository) {
        this.orcamentoRepository = orcamentoRepository;
        this.configuracaoService = configuracaoService;
        this.usuarioRepository = usuarioRepository;
    }

    public ComercialAnaliseDTO obterAnaliseComercial(String username, String role, Long responsavelId) {
        List<Orcamento> allOrcamentos = orcamentoRepository.findAll();
        List<Orcamento> orcamentos = filtrarOrcamentos(allOrcamentos, role, responsavelId);

        String prazoStr = configuracaoService.getValor("prazo_acompanhamento_orcamentos", "48 horas");
        LocalDateTime limitTime = LocalDateTime.now().minusHours(parseDeadlineHours(prazoStr));

        List<Orcamento> finalizados = filtrarPorStatus(orcamentos, OrcamentoStatus.FINALIZADO);
        List<Orcamento> aceitos     = filtrarPorStatus(orcamentos, OrcamentoStatus.ACEITO);
        List<Orcamento> rejeitados  = filtrarPorStatus(orcamentos, OrcamentoStatus.REJEITADO);
        List<Orcamento> pendentes   = filtrarPorStatus(orcamentos, OrcamentoStatus.PENDENTE);

        List<Orcamento> convertidos = Stream.concat(finalizados.stream(), aceitos.stream()).toList();
        long totalDurationMinutes      = calcularDuracaoTotal(convertidos);
        long countConvertedWithDuration = contarComDuracao(convertidos);

        long leadsRecebidos  = orcamentos.size();
        long leadsConcluidos = finalizados.size();
        long leadsAceitos    = aceitos.size();
        long leadsConvertidos = finalizados.size() + aceitos.size();
        long leadsPerdidos   = rejeitados.size();
        long leadsRejeitados = rejeitados.size();
        long leadsPendentes  = pendentes.size();
        long leadsEmAberto   = leadsPendentes + leadsAceitos;
        long orcamentosAtrasados = calcularAtrasados(pendentes, limitTime);

        BigDecimal valorConvertido = somarPrecos(finalizados);
        BigDecimal valorPotencial  = somarPrecos(aceitos);
        BigDecimal valorPerdido    = somarPrecos(rejeitados);
        BigDecimal valorEmRisco    = somarPrecosAtrasados(pendentes, limitTime);

        double taxaConversao = leadsRecebidos > 0
                ? ((double) leadsConcluidos / leadsRecebidos) * 100.0 : 0.0;
        double tempoMedioConversaoHoras = countConvertedWithDuration > 0
                ? (double) totalDurationMinutes / 60.0 / countConvertedWithDuration : 0.0;

        return new ComercialAnaliseDTO(
                leadsRecebidos, leadsConvertidos, leadsPerdidos, taxaConversao,
                tempoMedioConversaoHoras, orcamentosAtrasados, leadsPendentes, leadsAceitos,
                leadsRejeitados, leadsConcluidos, leadsEmAberto,
                valorPotencial, valorEmRisco, valorConvertido, valorPerdido);
    }

    public List<ResponsavelRankingDTO> obterRankingResponsaveis(
            String periodo, LocalDate inicio, LocalDate fim) {

        List<Orcamento> orcamentos = orcamentoRepository.findAll();
        List<Usuario> admins = usuarioRepository.findByRole("ADMIN");

        LocalDateTime[] intervalo = resolverIntervalo(periodo, inicio, fim);
        LocalDateTime finalStart = intervalo[0];
        LocalDateTime finalEnd   = intervalo[1];

        Map<String, Long> rankingMap = new HashMap<>();
        for (Usuario admin : admins) {
            rankingMap.put(admin.getUsername(), 0L);
        }

        orcamentos.stream()
                .filter(o -> o.getResponsavel() != null)
                .filter(o -> o.getStatus() == OrcamentoStatus.FINALIZADO || o.getStatus() == OrcamentoStatus.ACEITO)
                .filter(o -> estaNoIntervalo(resolverDataMovimentacao(o), finalStart, finalEnd))
                .forEach(o -> {
                    String uname = o.getResponsavel().getUsername();
                    rankingMap.put(uname, rankingMap.getOrDefault(uname, 0L) + 1);
                });

        return rankingMap.entrySet().stream()
                .map(e -> new ResponsavelRankingDTO(e.getKey(), e.getValue()))
                .sorted((r1, r2) -> Long.compare(r2.conversoes(), r1.conversoes()))
                .toList();
    }

    private List<Orcamento> filtrarOrcamentos(List<Orcamento> todos, String role, Long responsavelId) {
        if (!"ADMIN".equals(role)) {
            return List.of();
        }
        if (responsavelId == null) {
            return todos;
        }
        return todos.stream()
                .filter(o -> o.getResponsavel() != null && responsavelId.equals(o.getResponsavel().getId()))
                .toList();
    }

    private List<Orcamento> filtrarPorStatus(List<Orcamento> orcamentos, OrcamentoStatus status) {
        return orcamentos.stream().filter(o -> o.getStatus() == status).toList();
    }

    private BigDecimal somarPrecos(List<Orcamento> orcamentos) {
        return orcamentos.stream()
                .map(o -> (o.getServico() != null && o.getServico().getPreco() != null)
                        ? o.getServico().getPreco() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long calcularDuracaoTotal(List<Orcamento> orcamentos) {
        return orcamentos.stream()
                .mapToLong(o -> {
                    LocalDateTime criacao = o.getDataCriacao() != null ? o.getDataCriacao() : LocalDateTime.now();
                    LocalDateTime end = o.getDataMovimentacao() != null ? o.getDataMovimentacao() : criacao;
                    long diff = Duration.between(criacao, end).toMinutes();
                    return diff > 0 ? diff : 0;
                })
                .sum();
    }

    private long contarComDuracao(List<Orcamento> orcamentos) {
        return orcamentos.stream()
                .filter(o -> {
                    LocalDateTime criacao = o.getDataCriacao() != null ? o.getDataCriacao() : LocalDateTime.now();
                    LocalDateTime end = o.getDataMovimentacao() != null ? o.getDataMovimentacao() : criacao;
                    return Duration.between(criacao, end).toMinutes() > 0;
                })
                .count();
    }

    private long calcularAtrasados(List<Orcamento> pendentes, LocalDateTime limitTime) {
        return pendentes.stream()
                .filter(o -> resolverDataMovimentacao(o).isBefore(limitTime))
                .count();
    }

    private BigDecimal somarPrecosAtrasados(List<Orcamento> pendentes, LocalDateTime limitTime) {
        return somarPrecos(pendentes.stream()
                .filter(o -> resolverDataMovimentacao(o).isBefore(limitTime))
                .toList());
    }

    private LocalDateTime resolverDataMovimentacao(Orcamento o) {
        LocalDateTime criacao = o.getDataCriacao() != null ? o.getDataCriacao() : LocalDateTime.now();
        return o.getDataMovimentacao() != null ? o.getDataMovimentacao() : criacao;
    }

    private LocalDateTime[] resolverIntervalo(String periodo, LocalDate inicio, LocalDate fim) {
        LocalDateTime now = LocalDateTime.now();
        if ("hoje".equalsIgnoreCase(periodo)) {
            return new LocalDateTime[]{LocalDate.now().atStartOfDay(), now};
        }
        if ("7dias".equalsIgnoreCase(periodo) || "7_dias".equalsIgnoreCase(periodo) || "7".equals(periodo)) {
            return new LocalDateTime[]{now.minusDays(7), now};
        }
        if ("30dias".equalsIgnoreCase(periodo) || "30_dias".equalsIgnoreCase(periodo) || "30".equals(periodo)) {
            return new LocalDateTime[]{now.minusDays(30), now};
        }
        if ("personalizado".equalsIgnoreCase(periodo)) {
            return new LocalDateTime[]{
                inicio != null ? inicio.atStartOfDay() : null,
                fim != null ? fim.atTime(LocalTime.MAX) : null
            };
        }
        return new LocalDateTime[]{null, null};
    }

    private boolean estaNoIntervalo(LocalDateTime data, LocalDateTime start, LocalDateTime end) {
        if (start != null && data.isBefore(start)) return false;
        if (end != null && data.isAfter(end)) return false;
        return true;
    }

    private int parseDeadlineHours(String deadlineStr) {
        if (deadlineStr == null || deadlineStr.isBlank()) {
            return 48;
        }
        deadlineStr = deadlineStr.toLowerCase().trim();
        try {
            if (deadlineStr.endsWith("h") || deadlineStr.contains("hora")) {
                return Integer.parseInt(deadlineStr.replaceAll(REGEX_APENAS_NUMEROS, ""));
            } else if (deadlineStr.endsWith("d") || deadlineStr.contains("dia")) {
                return Integer.parseInt(deadlineStr.replaceAll(REGEX_APENAS_NUMEROS, "")) * 24;
            } else {
                return Integer.parseInt(deadlineStr.replaceAll(REGEX_APENAS_NUMEROS, ""));
            }
        } catch (Exception e) {
            return 48;
        }
    }
}
