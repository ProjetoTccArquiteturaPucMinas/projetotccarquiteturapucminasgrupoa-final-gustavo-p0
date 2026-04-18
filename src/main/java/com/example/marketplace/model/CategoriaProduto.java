package com.example.marketplace.model;

import java.math.BigDecimal;


public enum CategoriaProduto {
CAPINHA(BigDecimal.valueOf(3)),CARREGADOR(BigDecimal.valueOf(5)),FONE(BigDecimal.valueOf(3)),PELICULA(BigDecimal.valueOf(2)),SUPORTE(BigDecimal.valueOf(2));

public BigDecimal percentualDesconto;

public BigDecimal getDesconto() {
    return percentualDesconto;
}

CategoriaProduto(BigDecimal percentual) {
    percentualDesconto = percentual;
}
}