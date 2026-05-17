package cl.duoc.VentaCompra.dto;

import java.sql.Timestamp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CompraDTO {
    
    @NotBlank(message = "obligatorio declarar el estado de la compra")
    private String estado;
    
    @NotNull(message = "obligatorio declarar el total de la compra")
    private double compraTotal;

    @NotNull(message = "obligatorio declarar la fecha de la compra")
    private Timestamp fechaCompra;

}
