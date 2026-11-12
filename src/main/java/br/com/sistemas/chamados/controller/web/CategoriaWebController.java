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

import br.com.sistemas.chamados.dto.CategoriaRequest;
import br.com.sistemas.chamados.dto.CategoriaResponse;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.CategoriaService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/app/categorias")
public class CategoriaWebController {

    private final CategoriaService service;

    public CategoriaWebController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("categorias", service.listar());
        return "categorias/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        return form(model, new CategoriaRequest(null, null), null, Map.of());
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") CategoriaRequest form, BindingResult result,
                        Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return form(model, form, null, Formularios.erros(result));
        }
        try {
            service.criar(form);
            redirect.addFlashAttribute("sucesso", "Categoria cadastrada com sucesso.");
            return "redirect:/app/categorias";
        } catch (RegraNegocioException ex) {
            return form(model, form, null, Map.of("nome", ex.getMessage()));
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        CategoriaResponse c = service.buscar(id);
        return form(model, new CategoriaRequest(c.nome(), c.descricao()), id, Map.of());
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") CategoriaRequest form, BindingResult result,
                            Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return form(model, form, id, Formularios.erros(result));
        }
        try {
            service.atualizar(id, form);
            redirect.addFlashAttribute("sucesso", "Categoria atualizada com sucesso.");
            return "redirect:/app/categorias";
        } catch (RegraNegocioException ex) {
            return form(model, form, id, Map.of("nome", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        service.excluir(id);
        redirect.addFlashAttribute("sucesso", "Categoria excluída.");
        return "redirect:/app/categorias";
    }

    private String form(Model model, CategoriaRequest form, Long id, Map<String, String> erros) {
        model.addAttribute("form", form);
        model.addAttribute("id", id);
        model.addAttribute("erros", erros);
        return "categorias/form";
    }
}
