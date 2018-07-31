package bio.overture.rollcall.controller;

import bio.overture.rollcall.config.RollcallConfig.ConfiguredAlias;
import bio.overture.rollcall.model.AliasRequest;
import bio.overture.rollcall.service.AliasService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/aliases")
public class AliasController {

  private final AliasService service;

  public AliasController(AliasService service) {
    this.service = service;
  }

  @GetMapping("config")
  public List<ConfiguredAlias> getConfigured() {
    return service.getConfigured();
  }

  @GetMapping("candidates")
  public List<AliasService.AliasCandidates> getCandidates() {
    return service.getCandidates();
  }

  @PostMapping(path = "release", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  public boolean release(@RequestBody AliasRequest aliasRequest) {
    return service.release(aliasRequest);
  }

  @PostMapping(path = "remove", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  public boolean remove(@RequestBody AliasRequest aliasRequest) {
    return service.remove(aliasRequest);
  }

}
