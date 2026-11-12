package br.com.sistemas.chamados.controller.web;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import br.com.sistemas.chamados.service.CategoriaService;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
import br.com.sistemas.chamados.service.TecnicoService;

@Controller
public class DashboardWebController {

    private final ChamadoService chamadoService;
    private final ClienteService clienteService;
    private final CategoriaService categoriaService;
    private final TecnicoService tecnicoService;

    public DashboardWebController(ChamadoService chamadoService, ClienteService clienteService,
                                  CategoriaService categoriaService, TecnicoService tecnicoService) {
        this.chamadoService = chamadoService;
        this.clienteService = clienteService;
        this.categoriaService = categoriaService;
        this.tecnicoService = tecnicoService;
    }

    @GetMapping("/")
    public String raiz() {
        return "redirect:/app";
    }

    @GetMapping("/app")
    public String dashboard(Model model) {
        model.addAttribute("porStatus", chamadoService.contarPorStatus());
        model.addAttribute("porPrioridade", chamadoService.contarPorPrioridade());
        model.addAttribute("recentes", chamadoService.filtrar(null, null, null,
            PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "dataAbertura"))).getContent());
        model.addAttribute("totalClientes", clienteService.listar().size());
        model.addAttribute("totalCategorias", categoriaService.listar().size());
        model.addAttribute("totalTecnicos", tecnicoService.listar().size());
        return "dashboard";
    }
}
