package cl.duoc.VentaCompra.model;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;

@Entity
@Table(name = "compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private double compraTotal;

    @Column(nullable = false)
    private Timestamp fechaCompra;

}
