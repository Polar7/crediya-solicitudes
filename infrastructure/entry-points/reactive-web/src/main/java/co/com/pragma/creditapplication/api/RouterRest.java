package co.com.pragma.creditapplication.api;

import co.com.pragma.creditapplication.api.config.ApplicationPath;
import co.com.pragma.creditapplication.api.dto.CreateCreditApplicationDTO;
import co.com.pragma.creditapplication.api.dto.FilterSelectCreditApplicationDTO;
import co.com.pragma.creditapplication.api.dto.GenericResponseDto;
import co.com.pragma.creditapplication.api.dto.ProcessApplicationDecisionDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final ApplicationPath applicationPath;

    @Bean
    @RouterOperations({
            @RouterOperation(path = "/api/v1/solicitudes",
                    produces = "application/json",
                    method = POST,
                    operation = @Operation(
                            operationId = "createCreditApplication",
                            summary = "Crea una nueva solicitud de crédito",
                            tags = {"Solicitudes de Crédito"},
                            requestBody = @RequestBody(
                                    description = "Datos de la solicitud de crédito para crear",
                                    required = true,
                                    content = @Content(
                                            schema = @Schema(implementation = CreateCreditApplicationDTO.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Solicitud de crédito creada exitosamente",
                                            content = @Content(
                                                    mediaType = "application/json",
                                                    schema = @Schema(implementation = GenericResponseDto.class)
                                            )
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            ),
            @RouterOperation(path = "/api/v1/solicitudes/pendientes",
                    produces = "application/json",
                    method = POST,
                    beanClass = Handler.class,
                    beanMethod = "listenPOSTFindAllPendingUseCase",
                    operation = @Operation(
                            operationId = "getCreditApplicationsPending",
                            summary = "Obtiene solicitudes de crédito pendientes con filtros de paginación",
                            tags = {"Solicitudes de Crédito"},
                            requestBody = @RequestBody(
                                    description = "Filtros de paginación para la solicitud",
                                    required = true,
                                    content = @Content(
                                            schema = @Schema(implementation = FilterSelectCreditApplicationDTO.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Lista de solicitudes de crédito",
                                            content = @Content(
                                                    mediaType = "application/json",
                                                    schema = @Schema(implementation = GenericResponseDto.class)
                                            )
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Parámetros de filtro inválidos"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            ),
            @RouterOperation(path = "/api/v1/solicitudes",
                    produces = "application/json",
                    method = PUT,
                    beanClass = Handler.class,
                    beanMethod = "listenPUTApproveRejectCreditApplicationUseCase",
                    operation = @Operation(
                            operationId = "decideApplicationStatus",
                            summary = "Aprueba o rechaza una solicitud de crédito",
                            tags = {"Solicitudes de Crédito"},
                            requestBody = @RequestBody(
                                    description = "Datos para aprobar o rechazar la solicitud",
                                    required = true,
                                    content = @Content(
                                            schema = @Schema(implementation = ProcessApplicationDecisionDTO.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Estado de la solicitud actualizado exitosamente",
                                            content = @Content(
                                                    mediaType = "application/json",
                                                    schema = @Schema(implementation = GenericResponseDto.class)
                                            )
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
                                    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST(applicationPath.getApplications()), handler::listenPOSTCreateCreditApplicationUseCase)
                .andRoute(PUT(applicationPath.getApplications()), handler::listenPUTApproveRejectManuallyCreditApplicationUseCase)
                .andRoute(POST(applicationPath.getApplicationsPending()), handler::listenPOSTFindAllPendingUseCase);
    }

}
