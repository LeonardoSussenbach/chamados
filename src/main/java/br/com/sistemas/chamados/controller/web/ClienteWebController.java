package br.com.sistemas.chamados.controller.web;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.sistemas.chamados.dto.ClienteRequest;
import br.com.sistemas.chamados.dto.ClienteResponse;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/app/clientes")
public class ClienteWebController {

    private final ClienteService service;
    private final ChamadoService chamadoService;

    public ClienteWebController(ClienteService service, ChamadoService chamadoService) {
        this.service = service;
        this.chamadoService = chamadoService;
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("clientes", service.listar());
        return "clientes/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        return form(model, new ClienteRequest(null, null, null), null, Map.of());
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") ClienteRequest form, BindingResult result,
                        Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return form(model, form, null, Formularios.erros(result));
        }
        try {
            ClienteResponse criado = service.criar(form);
            redirect.addFlashAttribute("sucesso", "Cliente cadastrado com sucesso.");
            return "redirect:/app/clientes/" + criado.id();
        } catch (RegraNegocioException ex) {
            return form(model, form, null, Map.of("email", ex.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model,
                          @PageableDefault(size = 10, sort = "dataAbertura",
                              direction = Sort.Direction.DESC) Pageable pageable) {
        model.addAttribute("cliente", service.buscar(id));
        model.addAttribute("chamados", chamadoService.listarPorCliente(id, pageable));
        return "clientes/detalhe";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        ClienteResponse c = service.buscar(id);
        return form(model, new ClienteRequest(c.nome(), c.email(), c.telefone()), id, Map.of());
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") ClienteRequest form, BindingResult result,
                            Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return form(model, form, id, Formularios.erros(result));
        }
        try {
            service.atualizar(id, form);
            redirect.addFlashAttribute("sucesso", "Cliente atualizado com sucesso.");
            return "redirect:/app/clientes/" + id;
        } catch (RegraNegocioException ex) {
            return form(model, form, id, Map.of("email", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        service.excluir(id);
        redirect.addFlashAttribute("sucesso", "Cliente excluído.");
        return "redirect:/app/clientes";
    }

    private String form(Model model, ClienteRequest form, Long id, Map<String, String> erros) {
        model.addAttribute("form", form);
        model.addAttribute("id", id);
        model.addAttribute("erros", erros);
        return "clientes/form";
    }
}
