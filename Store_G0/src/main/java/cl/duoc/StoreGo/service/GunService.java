package cl.duoc.StoreGo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import cl.duoc.StoreGo.dto.gunDTO;
import cl.duoc.StoreGo.repository.GunRepository;
import lombok.RequiredArgsConstructor;

/*import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
*/

@Service
@RequiredArgsConstructor
public class GunService {

    private final GunRepository gunRepository;

    //listar todas las armas
    public List<gunDTO> getAllGunsDTO() {
        return gunRepository.findAll().stream()
                .map(gun -> new gunDTO(
                        gun.getGunname(),
                        gun.getSkinname(),
                        gun.getCondicion_arma(),
                        gun.getCollection(),
                        gun.getYear()))
                .toList();
    }
    //buscar armas por collecion
    public List<gunDTO> getGunsByCollection(String collection) {
        return gunRepository.findAll().stream()
                .filter(gun -> gun.getCollection().equalsIgnoreCase(collection))
                .map(gun -> new gunDTO(
                        gun.getGunname(),
                        gun.getSkinname(),
                        gun.getCondicion_arma(),
                        gun.getCollection(),
                        gun.getYear()))
                .toList();
    }
    //buscar armas por nombre del arma
    public List<gunDTO> getGunsByGunname(String gunname) {
        return gunRepository.findAll().stream()
                .filter(gun -> gun.getGunname().equalsIgnoreCase(gunname))
                .map(gun -> new gunDTO(
                        gun.getGunname(),
                        gun.getSkinname(),
                        gun.getCondicion_arma(),
                        gun.getCollection(),
                        gun.getYear()))
                .toList();
    }
    //buscar armas por nombre de la skin
    public List<gunDTO> getGunsBySkinname(String skinname) {
        return gunRepository.findAll().stream()
                .filter(gun -> gun.getSkinname().equalsIgnoreCase(skinname))
                .map(gun -> new gunDTO(
                        gun.getGunname(),
                        gun.getSkinname(),
                        gun.getCondicion_arma(),
                        gun.getCollection(),
                        gun.getYear()))
                .toList();
    }
    //buscar armas por año de salida
    public List<gunDTO> getGunsByYear(Integer year) {
        return gunRepository.findAll().stream()
                .filter(gun -> gun.getYear().equals(year))
                .map(gun -> new gunDTO(
                        gun.getGunname(),
                        gun.getSkinname(),
                        gun.getCondicion_arma(),
                        gun.getCollection(),
                        gun.getYear()))
                .toList();
    }

    // cuestionable, preguntar al profe si lo dejamos asi o lo quitamos
    /* 
    public String verifyOtherService() {
        // URL del otro servicio
        String otherServiceUrl = "http://localhost:8081/api/v1/other-service/verify";

        try {
            // Crear cliente HTTP
            RestTemplate restTemplate = new RestTemplate();

            // Realizar la solicitud GET
            ResponseEntity<String> response = restTemplate.getForEntity(otherServiceUrl, String.class);

            // Verificar el estado de la respuesta
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody(); // Retornar el cuerpo de la respuesta
            } else {
                throw new RuntimeException("Error al conectar con el servicio: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al conectar con el otro servicio", e);
        }
    }*/
}
