package com.musala.gateway.rest;

import com.musala.gateway.entity.Device;
import com.musala.gateway.entity.Gateway;
import com.musala.gateway.service.GatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        gateways.forEach(this::addLink);

        return new ResponseEntity<>(gateways, HttpStatus.OK);
    }

    @GetMapping("/{serialNumber}")
    public ResponseEntity<Gateway> getGateway(@PathVariable String serialNumber) {
        Gateway gateway = gatewayService.findGateway(serialNumber);

        return ResponseEntity.ok(addLink(gateway));
    }

    @PostMapping
    public ResponseEntity<Void> addGateway(@Valid @RequestBody Gateway gateway) {

        gateway = gatewayService.save(gateway);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{serialNumber}")
                .buildAndExpand(gateway.getSerialNumber())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @DeleteMapping("/{serialNumber}")
    public void deleteGateway(@PathVariable String serialNumber) {
        Gateway gateway = gatewayService.findGateway(serialNumber);
        gatewayService.deleteById(gateway.getId());
    }

    @GetMapping("/{serialNumber}/devices")
    public ResponseEntity<Collection<Device>> getDevices(@PathVariable String serialNumber) {
        Gateway gateway = gatewayService.findGateway(serialNumber);
        addLink(gateway);

        return ResponseEntity.ok(gateway.getDevices());
    }

    @GetMapping("/{serialNumber}/devices/{uid}")
    public ResponseEntity<Device> getDevice(@PathVariable String serialNumber,
                                            @PathVariable int uid) {

        Device device = gatewayService.getDevice(serialNumber, uid);

        return ResponseEntity.ok(addLink(device, serialNumber));
    }

    @PostMapping("/{serialNumber}/devices")
    public ResponseEntity<Void> addDevice(@PathVariable String serialNumber,
                                          @Valid @RequestBody Device device) {

        gatewayService.addDevice(serialNumber, device);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{uid}")
                .buildAndExpand(device.getUid())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @DeleteMapping("/{serialNumber}/devices/{uid}")
    public void removeDevice(@PathVariable String serialNumber, @PathVariable int uid) {
        gatewayService.removeDevice(serialNumber, uid);
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
