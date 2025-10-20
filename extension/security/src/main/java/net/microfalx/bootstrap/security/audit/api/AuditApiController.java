package net.microfalx.bootstrap.security.audit.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.restapi.RestApiDataSetController;
import net.microfalx.bootstrap.restapi.RestApiMapper;
import net.microfalx.bootstrap.security.audit.jpa.Audit;
import net.microfalx.bootstrap.security.user.api.UserDto;
import net.microfalx.bootstrap.security.user.jpa.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audits")
@DataSet(model = Audit.class, trend = true, trendFieldNames = {"action", "module", "category", "client_info"})
@Tag(name = "Audits", description = "Audit Management API")
public class AuditApiController extends RestApiDataSetController<Audit, AuditDTO, Long> {

    @Operation(summary = "List audits", description = "Returns a list of audits with search and paging.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AuditDTO.class)))
    @GetMapping
    public List<AuditDTO> list(
            @Parameter(description = "The query used to filter by various model fields", name = "action")
            @RequestParam(name = "query", required = false) String query,

            @Parameter(description = "The sorting desired for the result set", name = "createdAt=desc")
            @RequestParam(name = "sort", required = false) String sort,

            @Parameter(description = "The page to return for the result set", example = "0")
            @RequestParam(name = "page", required = false) int page,

            @Parameter(description = "The page size for the result set", example = "20")
            @RequestParam(name = "page-size", required = false) int pageSize
    ) {
        return doList(null, query, sort, page, pageSize);
    }

    @Operation(summary = "Get audi", description = "Returns a single audit by its unique identifier.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AuditDTO.class)))
    @GetMapping("/{id}")
    public AuditDTO get(@Parameter(description = "The audit identifier", example = "42") @PathVariable Long id) {
        return doFind(id);
    }


    @Override
    protected Class<? extends RestApiMapper<Audit, AuditDTO>> getMapperClass() {
        return AuditMapper.class;
    }
}
