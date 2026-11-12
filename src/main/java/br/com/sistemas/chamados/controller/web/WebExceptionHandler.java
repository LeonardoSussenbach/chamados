package br.com.sistemas.chamados.controller.web;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice(basePackages = "br.com.sistemas.chamados.controller.web")
public class WebExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ModelAndView naoEncontrado(RecursoNaoEncontradoException ex) {
        return paginaErro(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView tipoInvalido(MethodArgumentTypeMismatchException ex) {
        return paginaErro(HttpStatus.BAD_REQUEST,
            "Valor inválido para o parâmetro '" + ex.getName() + "'");
    }

    @ExceptionHandler(RegraNegocioException.class)
    public String regraNegocio(RegraNegocioException ex, HttpServletRequest request) {
        return voltarComErro(request, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String integridade(DataIntegrityViolationException ex, HttpServletRequest request) {
        return voltarComErro(request,
            "Não é possível excluir: o registro está sendo usado por algum chamado.");
    }

    private ModelAndView paginaErro(HttpStatus status, String mensagem) {
        ModelAndView mv = new ModelAndView("erro");
        mv.setStatus(status);
        mv.addObject("status", status.value());
        mv.addObject("mensagem", mensagem);
        return mv;
    }

    private String voltarComErro(HttpServletRequest request, String mensagem) {
        RequestContextUtils.getOutputFlashMap(request).put("erro", mensagem);
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/app");
    }
}
