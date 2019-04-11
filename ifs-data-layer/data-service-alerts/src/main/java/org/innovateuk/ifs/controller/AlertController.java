package org.innovateuk.ifs.controller;

import org.innovateuk.ifs.alert.resource.AlertResource;
import org.innovateuk.ifs.alert.resource.AlertType;
import org.innovateuk.ifs.commons.ZeroDowntime;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.domain.Alert;
import org.innovateuk.ifs.transactional.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exposes CRUD operations through a REST API to manage {@link Alert} related data.
 */
@RestController
@RequestMapping("/alert")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @ZeroDowntime(reference = "IFS-430", description = "remove camelCase mapping in h2020 sprint 6")
    @GetMapping({"/findAllVisible", "find-all-visible"})
    public RestResult<List<AlertResource>> findAllVisible() {
        return alertService.findAllVisible().toGetResponse();
    }

    @GetMapping("/findAllVisible/{type}")
    public RestResult<List<AlertResource>> findAllVisibleByType(@PathVariable("type") AlertType type) {
        return alertService.findAllVisibleByType(type).toGetResponse();
    }

    @GetMapping("/{id}")
    public RestResult<AlertResource> findById(@PathVariable("id") Long id) {
        return alertService.findById(id).toGetResponse();
    }

    @PostMapping("/")
    public RestResult<AlertResource> create(@RequestBody AlertResource alertResource) {
        return alertService.create(alertResource).toPostCreateResponse();
    }

    @DeleteMapping("/{id}")
    public RestResult<Void> delete(@PathVariable("id") Long id) {
        return alertService.delete(id).toDeleteResponse();
    }

    @DeleteMapping("/delete/{type}")
    public RestResult<Void> deleteAllByType(@PathVariable("type") AlertType type) {
        return alertService.deleteAllByType(type).toDeleteResponse();
    }
}
