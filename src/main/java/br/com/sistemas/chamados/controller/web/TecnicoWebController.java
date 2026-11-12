package br.com.sistemas.chamados.controller.web;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.sistemas.chamados.dto.TecnicoRequest;
import br.com.sistemas.chamados.dto.TecnicoResponse;
import br.com.sistemas.chamados.entity.Perfil;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.TecnicoService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/app/tecnicos")
public class TecnicoWebController {

    private final TecnicoService service;

    public TecnicoWebController(TecnicoService service) {
        this.service = service;
    }

    @ModelAttribute("perfis")
    public Perfil[] perfis() {
        return Perfil.values();
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("tecnicos", service.listar());
        return "tecnicos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        return form(model, new TecnicoRequest(null, null, Perfil.TECNICO), null, Map.of());
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") TecnicoRequest form, BindingResult result,
                        Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return form(model, form, null, Formularios.erros(result));
        }
        try {
            service.criar(form);
            redirect.addFlashAttribute("sucesso", "Técnico cadastrado com sucesso.");
            return "redirect:/app/tecnicos";
        } catch (RegraNegocioException ex) {
            return form(model, form, null, Map.of("email", ex.getMessage()));
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        TecnicoResponse t = service.buscar(id);
        return form(model, new TecnicoRequest(t.nome(), t.email(), t.perfil()), id, Map.of());
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") TecnicoRequest form, BindingResult result,
                            Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return form(model, form, id, Formularios.erros(result));
        }
        try {
            service.atualizar(id, form);
            redirect.addFlashAttribute("sucesso", "Técnico atualizado com sucesso.");
            return "redirect:/app/tecnicos";
        } catch (RegraNegocioException ex) {
            return form(model, form, id, Map.of("email", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        service.excluir(id);
        redirect.addFlashAttribute("sucesso", "Técnico excluído.");
        return "redirect:/app/tecnicos";
    }

    private String form(Model model, TecnicoRequest form, Long id, Map<String, String> erros) {
        model.addAttribute("form", form);
        model.addAttribute("id", id);
        model.addAttribute("erros", erros);
        return "tecnicos/form";
    }
}
