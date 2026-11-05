package br.com.sistemas.chamados.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "comentarios")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String texto;

    @Column(nullable = false, updatable = false)
    private LocalDateTime data;

    @Column(nullable = false, length = 100)
    private String autor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chamado_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Chamado chamado;

    public Comentario() { }

    public Comentario(String texto, String autor, Chamado chamado) {
        this.texto = texto;
        this.autor = autor;
        this.chamado = chamado;
    }

    @PrePersist
    void aoCriar() {
        this.data = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getTexto() { return texto; }
    public LocalDateTime getData() { return data; }
    public String getAutor() { return autor; }
    public Chamado getChamado() { return chamado; }
}
