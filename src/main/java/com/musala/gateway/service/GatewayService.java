package com.musala.gateway.service;

import com.musala.gateway.entity.Device;
import com.musala.gateway.entity.Gateway;
import com.musala.gateway.repository.GatewayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lutfun
 * @since 5/20/21
 */
@Service
public class GatewayService {

    @Autowired
    GatewayRepository gatewayRepository;

    public Gateway findGateway(String gatewaySerialNumber) {
        return gatewayRepository.findBySerialNumber(gatewaySerialNumber).orElse(null);
    }

    public List<Gateway> findAll() {
        Iterable<Gateway> gateways = gatewayRepository.findAll();
        List<Gateway> gatewayList = new ArrayList<>();
        gateways.forEach(gatewayList::add);

        return gatewayList;
    }

    public Device getDevice(String gatewaySerialNumber, int deviceUid) {
        Gateway gateway = findGateway(gatewaySerialNumber);
        if (gateway == null) {
            return null;
        }

        return gateway.getDevices()
                .stream()
                .filter(d -> d.getUid().equals(deviceUid))
                .findAny()
                .orElse(null);
    }

    @Transactional
    public boolean addDevice(String gatewaySerialNumber, Device device) {
        if (device == null) {
            return false;
        }

        Gateway gateway = findGateway(gatewaySerialNumber);
        if (gateway == null || gateway.deviceLimitReached()) {
            return false;
        }

        boolean deviceExists = gateway.getDevices()
                .stream()
                .anyMatch(device1 -> device1.equals(device));

        if (deviceExists) {
            return false;
        }

        device.setCreated(ZonedDateTime.now());
        gateway.addDevice(device);
        gatewayRepository.save(gateway);

        return true;
    }

    @Transactional
    public boolean removeDevice(String gatewaySerialNumber, int deviceUid) {
        Gateway gateway = findGateway(gatewaySerialNumber);
        if (gateway == null) {
            return false;
        }

        boolean removed = gateway.getDevices()
                .remove(new Device(deviceUid, null, null, null));

        if (!removed) {
            return false;
        }

        gatewayRepository.save(gateway);

        return true;
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
