package cl.duoc.StoreGo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class gunDTO {

    @NotBlank(message = "El nombre del arma es obligatorio")
    @Size(min = 3, max = 20, message = "El nombre del arma debe tener entre 3 y 20 caracteres")
    private String gunname;

    @NotBlank(message = "El nombre de la skin es obligatorio")
    @Size(min = 10, max = 50, message = "El nombre de la skin debe tener entre 10 y 50 caracteres")
    private String skinname;

    @NotBlank(message = "El estado del arma es obligatoria")
    @Size(min = 10, max = 30, message = "El estado del arma debe tener entre 10 y 30 caracteres")
    private String condicion_arma;

    @NotBlank(message = "La colección a la que pertenece el arma es obligatoria")
    @Size(min = 10, max = 50, message = "La colección debe tener entre 10 y 50 caracteres")
    private String collection;

    @NotNull(message = "El año de salida del arma es obligatorio")
    @Min(value = 2012, message = "El año de salida del arma debe ser mayor o igual a 2012")
    @Max(value = 2028, message = "El año de salida del arma debe ser menor o igual a 2028")
    private Integer year;
}
