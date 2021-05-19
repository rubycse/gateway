package com.musala.gateway.rest;

import com.musala.gateway.entity.Device;
import com.musala.gateway.entity.Gateway;
import com.musala.gateway.service.GatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * @author lutfun
 * @since 5/18/21
 */
@RestController
@RequestMapping("/gateways")
public class GatewayController {

    @Autowired
    GatewayService gatewayService;

    @GetMapping
    public ResponseEntity<Collection<Gateway>> getGateways() {
        List<Gateway> gateways = gatewayService.findAll();
        gateways.forEach(gateway -> addLink(gateway));

        return new ResponseEntity<>(gateways, HttpStatus.OK);
    }

    @GetMapping("/{serialNumber}")
    public ResponseEntity<Gateway> getGateway(@PathVariable String serialNumber) {
        Gateway gateway = gatewayService.findGateway(serialNumber);
        if (gateway == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(addLink(gateway));
    }

    @PostMapping
    public ResponseEntity<Void> addGateway(@Valid @RequestBody Gateway gateway, Errors errors) {
        if (errors.hasErrors() || gateway == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        gateway = gatewayService.save(gateway);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{serialNumber}")
                .buildAndExpand(gateway.getSerialNumber())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @DeleteMapping("/{serialNumber}")
    public ResponseEntity<Void> removeGateway(@PathVariable String serialNumber) {
        Gateway gateway = gatewayService.findGateway(serialNumber);
        if (gateway == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        gatewayService.deleteById(gateway.getId());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{serialNumber}/devices")
    public ResponseEntity<Collection<Device>> getDevices(@PathVariable String serialNumber) {
        Gateway gateway = gatewayService.findGateway(serialNumber);
        if (gateway == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        addLink(gateway);

        return ResponseEntity.ok(gateway.getDevices());
    }

    @GetMapping("/{serialNumber}/devices/{uid}")
    public ResponseEntity<Device> getDevice(@PathVariable String serialNumber,
                                            @PathVariable int uid) {

        Device device = gatewayService.getDevice(serialNumber, uid);
        if (device == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(addLink(device, serialNumber));
    }

    @PostMapping("/{serialNumber}/devices")
    public ResponseEntity<Void> addDevice(@PathVariable String serialNumber,
                                          @Valid @RequestBody Device device,
                                          Errors errors) {

        if (errors.hasErrors()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        boolean deviceAdded = gatewayService.addDevice(serialNumber, device);
        if (!deviceAdded) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{uid}")
                .buildAndExpand(device.getUid())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @DeleteMapping("/{serialNumber}/devices/{uid}")
    public ResponseEntity<Void> removeDevice(@PathVariable String serialNumber,
                                             @PathVariable int uid) {

        boolean removed = gatewayService.removeDevice(serialNumber, uid);
        if (!removed) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private Gateway addLink(Gateway gateway) {
        gateway.add(linkTo(methodOn(GatewayController.class).getGateway(gateway.getSerialNumber())).withSelfRel());
        gateway.getDevices().forEach(device -> addLink(device, gateway.getSerialNumber()));
        return gateway;
    }

    private Device addLink(Device device, String gatewaySerialNumber) {
        return device.add(linkTo(methodOn(GatewayController.class)
                .getDevice(gatewaySerialNumber, device.getUid())).withSelfRel());
    }
}
