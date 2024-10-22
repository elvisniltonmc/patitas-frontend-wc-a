package pe.edu.cibertec.patitas_frontend_wc_a.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.cibertec.patitas_frontend_wc_a.clients.AutenticacionCliente;
import pe.edu.cibertec.patitas_frontend_wc_a.dto.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "http://localhost:5173")
public class LoginControllerAsync {

    @Autowired
    WebClient webClientAutenticacion;

    @Autowired
    AutenticacionCliente autenticacionClient;

    @PostMapping("/autenticar-async")
    public Mono<LoginResponseDTO> autenticar(@RequestBody LoginRequestDTO loginRequestDTO) {

        //Validando los campos
        if(loginRequestDTO.tipoDocumento() == null || loginRequestDTO.tipoDocumento().trim().length() == 0 ||
            loginRequestDTO.numeroDocumento() == null || loginRequestDTO.numeroDocumento().trim().length() == 0 ||
            loginRequestDTO.password() == null || loginRequestDTO.password().trim().length() == 0) {

            return Mono.just(new LoginResponseDTO("01", "Error: Debe completar correctamente sus credenciales", "", ""));

        }

        try {
            //hacemos la solicitud

            //y recibimos la data (response)
            return webClientAutenticacion.post()
                    .uri("/login")
                    .body(Mono.just(loginRequestDTO), LoginRequestDTO.class)
                    .retrieve()
                    .bodyToMono(LoginResponseDTO.class)
                    .flatMap(response -> {
                        //manipulación... cambia el "return" que le daremos
                        if(response.codigo().equals("00")){
                            return Mono.just(new LoginResponseDTO(
                                    "00","",response.nombreUsuario(), response.correoUsuario()));
                        }else {
                            return Mono.just(new LoginResponseDTO(
                                    "02","Autenticación fallida","",""));
                        }

                    });

        }catch (Exception e){
            System.out.println(e.getMessage());
            return Mono.just(new LoginResponseDTO("99",e.getMessage(),"",""));
        }

    }

    @PostMapping("/close-async")
    public Mono<ResponseClose> cerrarSesion(@RequestBody RequestClose request){
        try{
            return webClientAutenticacion.post()
                    .uri("/close")
                    .body(Mono.just(request),RequestClose.class)
                    .retrieve()
                    .bodyToMono(ResponseClose.class)
                    .flatMap(response -> {
                        if(response.codigo().equals("00")){
                            return Mono.just(new ResponseClose("00","Sesión cerrada"));
                        }else {
                            return Mono.just(new ResponseClose("01","Hubo un problema en el servicio"));
                        }
                    });
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Mono.just(new ResponseClose("99",e.getMessage()));
        }
    }

    //Como es síncrono no se devuelve Mono, solo el Response
    @PostMapping("/close-ef")
    public ResponseClose2 cerrarSesion(@RequestBody RequestClose2 request){
        try {
            //consumimos servicio con Feign Client
            ResponseEntity<ResponseClose2> response = autenticacionClient.closeEF(request);
            System.out.println("Cerrando sesión con Feign :D");
            if(response.getStatusCode().is2xxSuccessful()){
                //recuperamos y retornamos response
                return response.getBody();
            } else {
                return new ResponseClose2("99","Ocurrió un problema con el servicio");
            }

        }catch (Exception e){
            return new ResponseClose2("99",e.getMessage());
        }

    }

}

