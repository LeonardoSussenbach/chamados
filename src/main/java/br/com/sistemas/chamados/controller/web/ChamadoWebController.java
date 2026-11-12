package br.com.sistemas.chamados.controller.web;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.sistemas.chamados.dto.AlterarStatusRequest;
import br.com.sistemas.chamados.dto.AtribuirTecnicoRequest;
import br.com.sistemas.chamados.dto.ChamadoRequest;
import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.dto.ComentarioRequest;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.service.CategoriaService;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
import br.com.sistemas.chamados.service.ComentarioService;
import br.com.sistemas.chamados.service.TecnicoService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/app/chamados")
public class ChamadoWebController {

    private final ChamadoService service;
    private final ComentarioService comentarioService;
    private final ClienteService clienteService;
    private final CategoriaService categoriaService;
    private final TecnicoService tecnicoService;

    public ChamadoWebController(ChamadoService service, ComentarioService comentarioService,
                                ClienteService clienteService, CategoriaService categoriaService,
                                TecnicoService tecnicoService) {
        this.service = service;
        this.comentarioService = comentarioService;
        this.clienteService = clienteService;
        this.categoriaService = categoriaService;
        this.tecnicoService = tecnicoService;
    }

    @ModelAttribute("statusLista")
    public StatusChamado[] statusLista() {
        return StatusChamado.values();
    }

    @ModelAttribute("prioridades")
    public Prioridade[] prioridades() {
        return Prioridade.values();
    }

    @GetMapping
    public String lista(@RequestParam(required = false) StatusChamado status,
                        @RequestParam(required = false) Prioridade prioridade,
                        @RequestParam(required = false) Long clienteId,
                        @PageableDefault(size = 10, sort = "dataAbertura",
                            direction = Sort.Direction.DESC) Pageable pageable,
                        Model model) {
        model.addAttribute("pagina", service.filtrar(status, prioridade, clienteId, pageable));
        model.addAttribute("status", status);
        model.addAttribute("prioridade", prioridade);
        model.addAttribute("clienteId", clienteId);
        model.addAttribute("clientes", clienteService.listar());
        return "chamados/lista";
    }

    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) Long clienteId, Model model) {
        return form(model, new ChamadoRequest(null, null, Prioridade.MEDIA, clienteId, null, null),
            Map.of());
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") ChamadoRequest form, BindingResult result,
                        Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return form(model, form, Formularios.erros(result));
        }
        ChamadoResponse criado = service.criar(form);
        redirect.addFlashAttribute("sucesso", "Chamado #" + criado.id() + " aberto com sucesso.");
        return "redirect:/app/chamados/" + criado.id();
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        return detalhe(id, model, new ComentarioRequest(null, null), Map.of());
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id,
                                @RequestParam(required = false) StatusChamado status,
                                RedirectAttributes redirect) {
        if (status == null) {
            redirect.addFlashAttribute("erro", "Selecione um status.");
            return "redirect:/app/chamados/" + id;
        }
        service.alterarStatus(id, new AlterarStatusRequest(status));
        redirect.addFlashAttribute("sucesso", "Status alterado para " + status + ".");
        return "redirect:/app/chamados/" + id;
    }

    @PostMapping("/{id}/atribuir")
    public String atribuir(@PathVariable Long id,
                           @RequestParam(required = false) Long tecnicoId,
                           RedirectAttributes redirect) {
        if (tecnicoId == null) {
            redirect.addFlashAttribute("erro", "Selecione um técnico.");
            return "redirect:/app/chamados/" + id;
        }
        service.atribuirTecnico(id, new AtribuirTecnicoRequest(tecnicoId));
        redirect.addFlashAttribute("sucesso", "Técnico atribuído.");
        return "redirect:/app/chamados/" + id;
    }

    @PostMapping("/{id}/comentarios")
    public String comentar(@PathVariable Long id,
                           @Valid @ModelAttribute("comentarioForm") ComentarioRequest form,
                           BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return detalhe(id, model, form, Formularios.erros(result));
        }
        comentarioService.adicionar(id, form);
        redirect.addFlashAttribute("sucesso", "Comentário registrado.");
        return "redirect:/app/chamados/" + id + "#comentarios";
    }

    private String detalhe(Long id, Model model, ComentarioRequest comentarioForm,
                           Map<String, String> erros) {
        ChamadoResponse chamado = service.buscar(id);
        List<StatusChamado> proximos = Arrays.stream(StatusChamado.values())
            .filter(s -> s != StatusChamado.ABERTO && s != chamado.status())
            .toList();
        model.addAttribute("chamado", chamado);
        model.addAttribute("proximosStatus", proximos);
        model.addAttribute("tecnicos", tecnicoService.listar());
        model.addAttribute("comentarios", comentarioService.listar(id));
        model.addAttribute("comentarioForm", comentarioForm);
        model.addAttribute("erros", erros);
        return "chamados/detalhe";
    }

    private String form(Model model, ChamadoRequest form, Map<String, String> erros) {
        model.addAttribute("form", form);
        model.addAttribute("erros", erros);
        model.addAttribute("clientes", clienteService.listar());
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("tecnicos", tecnicoService.listar());
        return "chamados/form";
    }
}
