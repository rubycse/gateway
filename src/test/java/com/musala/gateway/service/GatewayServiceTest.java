package com.musala.gateway.service;

import com.musala.gateway.boot.GatewayApplication;
import com.musala.gateway.entity.Device;
import com.musala.gateway.entity.DeviceStatus;
import com.musala.gateway.entity.Gateway;
import com.musala.gateway.repository.GatewayRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.TransactionSystemException;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * @author lutfun
 * @since 5/21/21
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = GatewayApplication.class)
public class GatewayServiceTest {

    @Autowired
    GatewayRepository gatewayRepository;

    GatewayService gatewayService;

    Gateway gateway;

    Device device1001, device1002;

    @Before
    public void setup() {
        gatewayService = new GatewayService();
        gatewayService.gatewayRepository = gatewayRepository;

        gateway = new Gateway();
        gateway.setSerialNumber("SL100");
        gateway.setName("Gateway100");
        gateway.setIpAddress("192.168.0.1");
        device1001 = createDevice(1001);
        device1002 = createDevice(1002);
        gateway.setDevices(new HashSet<>(Arrays.asList(device1001, device1002)));

        gatewayService.save(gateway);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void test_addGatewayWithDuplicareSerialNumber() {
        Gateway gateway = new Gateway();
        gateway.setSerialNumber("SL100");
        gateway.setName("Whatever");
        gateway.setIpAddress("192.168.0.0");
        gatewayService.save(gateway);
    }

    @Test
    public void test_addGatewayInvalidIP() {
        gateway.setIpAddress("InvalidIP!");

        TransactionSystemException exception = assertThrows(TransactionSystemException.class, () -> {
            gatewayService.save(gateway);
        });

        assertTrue(exception.getCause().getCause() instanceof ConstraintViolationException);
    }

    @Test
    @Transactional
    public void test_removeDevice() {
        assertFalse(gatewayService.removeDevice(gateway.getSerialNumber(), 1003));
        assertTrue(gatewayService.removeDevice(gateway.getSerialNumber(), 1001));
    }

    @Test
    @Transactional
    public void test_addEmptyDevice() {
        assertFalse(gatewayService.addDevice(gateway.getSerialNumber(), null));
    }

    @Test
    @Transactional
    public void test_addDeviceWithDuplicateUid() {
        assertFalse(gatewayService.addDevice(gateway.getSerialNumber(), device1001));
    }

    @Test
    @Transactional
    public void test_addNewDevice() {
        Device newDevice = createDevice(1003);
        assertTrue(gatewayService.addDevice(gateway.getSerialNumber(), newDevice));
    }

    @Test
    @Transactional
    public void test_addMoreThanMaxAllowedDevice() {
        IntStream.rangeClosed(1, remainingAllowedDevice()).forEach(uid -> {
            Device newDevice = createDevice(uid);
            assertTrue(gatewayService.addDevice(gateway.getSerialNumber(), newDevice));
        });

        Device newDevice = createDevice(remainingAllowedDevice() + 1);
        assertFalse(gatewayService.addDevice(gateway.getSerialNumber(), newDevice));
    }

    private Device createDevice(int uid) {
        return new Device(uid, "Whatever", ZonedDateTime.now(), DeviceStatus.offline);
    }

    private int remainingAllowedDevice() {
        return Gateway.MAX_DEVICE - gateway.getDevices().size();
    }

    @After
    public void tearDown() {
        gatewayService.deleteById(gateway.getId());
    }
}
