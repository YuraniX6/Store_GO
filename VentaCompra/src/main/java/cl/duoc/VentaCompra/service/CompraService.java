package cl.duoc.VentaCompra.service;

import org.springframework.stereotype.Service;

import cl.duoc.VentaCompra.repository.CompraRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompraService {

    private final CompraRepository compraRepository;
    
    public List<CompraDTO> 
}
