package bio.overture.rollcall.controller;

import bio.overture.rollcall.Index.ResolvedIndex;
import bio.overture.rollcall.service.IndexService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/indices")
public class IndexController {

  private final IndexService service;

  public IndexController(IndexService service) {
    this.service = service;
  }

  @GetMapping()
  public String[] getIndices() {
    return service.getIndices();
  }

  @GetMapping("/resolved")
  public List<ResolvedIndex> getResolved() {
    return service.getResolved();
  }

}
