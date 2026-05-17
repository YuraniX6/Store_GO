package cl.duoc.StoreGo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "skinsarmas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GunSkin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String gunname;

    @Column(nullable = false)
    private String skinname;

    @Column(nullable = false)
    private String condicion_arma;

    @Column(nullable = false)
    private String collection;

    @Column(nullable = false)
    private Integer year;
}
