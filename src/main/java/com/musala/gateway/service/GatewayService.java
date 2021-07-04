package com.musala.gateway.service;

import com.musala.gateway.entity.Device;
import com.musala.gateway.entity.Gateway;
import com.musala.gateway.exceptions.NotFoundException;
import com.musala.gateway.repository.GatewayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author lutfun
 * @since 5/20/21
 */
@Service
public class GatewayService {

    @Autowired
    GatewayRepository gatewayRepository;

    public Gateway findGateway(String gatewaySerialNumber) {
        Optional<Gateway> gatewayOptional = gatewayRepository.findBySerialNumber(gatewaySerialNumber);
        if (!gatewayOptional.isPresent()) {
            throw new NotFoundException("Gateway not Found");
        }

        return gatewayOptional.get();
    }

    public List<Gateway> findAll() {
        Iterable<Gateway> gateways = gatewayRepository.findAll();
        List<Gateway> gatewayList = new ArrayList<>();
        gateways.forEach(gatewayList::add);

        return gatewayList;
    }

    public Device getDevice(String gatewaySerialNumber, int deviceUid) {
        Gateway gateway = findGateway(gatewaySerialNumber);

        Optional<Device> deviceOptional = gateway.getDevices()
                .stream()
                .filter(d -> d.getUid().equals(deviceUid))
                .findAny();

        if (!deviceOptional.isPresent()) {
            throw new NotFoundException("Device not found!");
        }

        return deviceOptional.get();
    }

    @Transactional
    public void addDevice(String gatewaySerialNumber, Device device) {
        Gateway gateway = findGateway(gatewaySerialNumber);
        if (gateway.deviceLimitReached()) {
            throw new RuntimeException("Already added 10 devices, no more is allowed.");
        }

        boolean deviceExists = gateway.getDevices()
                .stream()
                .anyMatch(device1 -> device1.equals(device));

        if (deviceExists) {
            throw new RuntimeException("Device already added.");
        }

        device.setCreated(ZonedDateTime.now());
        gateway.addDevice(device);
        gatewayRepository.save(gateway);
    }

    @Transactional
    public void removeDevice(String gatewaySerialNumber, int deviceUid) {
        Gateway gateway = findGateway(gatewaySerialNumber);

        boolean removed = gateway.getDevices()
                .remove(new Device(deviceUid, null, null, null));

        if (!removed) {
            throw new NotFoundException("Device not found!");
        }

        gatewayRepository.save(gateway);
    }

    @Transactional
    public Gateway save(Gateway gateway) {
        return gatewayRepository.save(gateway);
    }

    @Transactional
    public void deleteById(long id) {
        gatewayRepository.deleteById(id);
    }
}
