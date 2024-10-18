package pe.edu.cibertec.patitas_frontend_wc_a.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.cibertec.patitas_frontend_wc_a.dto.LoginRequestDTO;
import pe.edu.cibertec.patitas_frontend_wc_a.dto.LoginResponseDTO;
import pe.edu.cibertec.patitas_frontend_wc_a.dto.RequestClose;
import pe.edu.cibertec.patitas_frontend_wc_a.dto.ResponseClose;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "http://localhost:5173")
public class LoginControllerAsync {

    @Autowired
    WebClient webClientAutenticacion;

    @PostMapping("/autenticar-async")
    public Mono<LoginResponseDTO> autenticar(@RequestBody LoginRequestDTO loginRequestDTO) {

        //Validando los campos
        if(loginRequestDTO.tipoDocumento() == null || loginRequestDTO.tipoDocumento().trim().length() == 0 ||
            loginRequestDTO.numeroDocumento() == null || loginRequestDTO.numeroDocumento().trim().length() == 0 ||
            loginRequestDTO.password() == null || loginRequestDTO.password().trim().length() == 0) {

            return Mono.just(new LoginResponseDTO("01", "Error: Debe completar correctamente sus credenciales", "", ""));

        }

        try {
            //Solicitud
            //se recibe la data response
            return webClientAutenticacion.post()
                    .uri("/login")
                    .body(Mono.just(loginRequestDTO), LoginRequestDTO.class)
                    .retrieve()
                    .bodyToMono(LoginResponseDTO.class)
                    .flatMap(response -> {

                        if(response.codigo().equals("00")) {
                            return Mono.just(new LoginResponseDTO("00", "", response.nombreUsuario(), ""));
                        } else {
                            return Mono.just(new LoginResponseDTO("02", "Error: Autenticación fallida", "", ""));
                        }

                    });

        } catch(Exception e) {

            System.out.println(e.getMessage());
            return Mono.just(new LoginResponseDTO("99", "Error: Ocurrió un problema en la autenticación", "", ""));
        }
        }
        @PostMapping("/close-async")
        public Mono<ResponseClose> cerrarSesion(@RequestBody RequestClose request){
            try{
                return webClientAutenticacion.post()
                        .uri("/close")
                        .body(Mono.just(request), RequestClose.class)
                        .retrieve()
                        .bodyToMono(ResponseClose.class)
                        .flatMap(response -> {
                            if(response.codigo().equals("00")){
                                return Mono.just(new ResponseClose("00","Sesión cerrada"));
                            }else {
                                return Mono.just(new ResponseClose("01","Se detecto un problema con el servicio"));
                            }
                        });
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return Mono.just(new ResponseClose("99",e.getMessage()));
            }

    }

}
