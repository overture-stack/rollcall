package bio.overture.rollcall.controller;

import bio.overture.rollcall.config.RollcallConfig.ConfiguredAlias;
import bio.overture.rollcall.service.AliasService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

}
