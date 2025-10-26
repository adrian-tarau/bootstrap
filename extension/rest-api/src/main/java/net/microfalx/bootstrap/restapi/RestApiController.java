package net.microfalx.bootstrap.restapi;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.lang.annotation.Module;
import net.microfalx.threadpool.ThreadPool;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for all REST API controllers.
 */
@SecurityRequirement(name = "bearer")
@SecurityRequirement(name = "apiKey")
@ApiResponse(responseCode = "200", description = "OK")
@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = RestApiError.class)))
@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = RestApiError.class)))
@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = RestApiError.class)))
@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = RestApiError.class)))
@ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content(schema = @Schema(implementation = RestApiError.class)))
@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = RestApiError.class)))
@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content(schema = @Schema(implementation = RestApiError.class)))
@Module("REST API")
public abstract class RestApiController {

    @Autowired
    private DataSetService dataSetService;

    @Autowired
    private ThreadPool threadPool;

    /**
     * Returns the dataset service.
     *
     * @return a non-null instance
     */
    protected final DataSetService getDataSetService() {
        return dataSetService;
    }

    /**
     * Returns the thread pool for various asynchronous operations.
     *
     * @return a non-null instance
     */
    protected final ThreadPool getThreadPool() {
        return threadPool;
    }
}
