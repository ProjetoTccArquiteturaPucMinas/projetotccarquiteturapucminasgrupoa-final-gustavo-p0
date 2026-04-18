package com.example.marketplace.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.marketplace.model.CategoriaProduto;
import com.example.marketplace.model.ItemCarrinho;
import com.example.marketplace.model.Produto;
import com.example.marketplace.model.ResumoCarrinho;
import com.example.marketplace.model.SelecaoCarrinho;
import com.example.marketplace.repository.ProdutoRepository;

@Service
public class ServicoCarrinho {

    private static final BigDecimal PERCENTUAL_MAXIMO_DESCONTO = BigDecimal.valueOf(25);
    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final ProdutoRepository repositorioProdutos;

    public ServicoCarrinho(ProdutoRepository repositorioProdutos) {
        this.repositorioProdutos = repositorioProdutos;
    }

    public ResumoCarrinho construirResumo(List<SelecaoCarrinho> selecoes) {

        List<ItemCarrinho> itens = new ArrayList<>();

        // =========================
        // Monta os itens do carrinho
        // =========================
        for (SelecaoCarrinho selecao : selecoes) {
            Produto produto = repositorioProdutos.buscarPorId(selecao.getProdutoId())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Produto não encontrado: " + selecao.getProdutoId()));

            itens.add(new ItemCarrinho(produto, selecao.getQuantidade()));
        }

        // =========================
        // Calcula subtotal
        // =========================
        BigDecimal subtotal = itens.stream()
                .map(ItemCarrinho::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int quantidadeTotalItens = itens.stream()
                .mapToInt(ItemCarrinho::getQuantidade)
                .sum();

        BigDecimal descontoQuantidade = calcularDescontoPorQuantidade(quantidadeTotalItens);
        BigDecimal descontoCategoria = calcularDescontoPorCategoria(itens);
        BigDecimal percentualDesconto = normalizarDesconto(descontoQuantidade.add(descontoCategoria));

        BigDecimal valorDesconto = subtotal
                .multiply(percentualDesconto)
                .divide(CEM, 2, RoundingMode.HALF_UP);

        BigDecimal total = subtotal.subtract(valorDesconto);

        return new ResumoCarrinho(itens, subtotal, percentualDesconto, valorDesconto, total);
    }

    private BigDecimal calcularDescontoPorQuantidade(int quantidadeTotalItens) {
        if (quantidadeTotalItens <= 1) {
            return BigDecimal.ZERO;
        }
        if (quantidadeTotalItens == 2) {
            return BigDecimal.valueOf(5);
        }
        if (quantidadeTotalItens == 3) {
            return BigDecimal.valueOf(7);
        }

        return BigDecimal.valueOf(10);
    }

    private BigDecimal calcularDescontoPorCategoria(List<ItemCarrinho> itens) {
        BigDecimal desconto = BigDecimal.ZERO;

        for (ItemCarrinho item : itens) {
            CategoriaProduto categoria = item.getProduto().getCategoria();
            BigDecimal percentualCategoria = categoria.getDesconto();
            desconto = desconto.add(percentualCategoria.multiply(BigDecimal.valueOf(item.getQuantidade())));
        }

        return desconto;
    }

    private BigDecimal normalizarDesconto(BigDecimal desconto) {
        if (desconto.compareTo(PERCENTUAL_MAXIMO_DESCONTO) > 0) {
            return PERCENTUAL_MAXIMO_DESCONTO;
        }

        return desconto;
    }
}
