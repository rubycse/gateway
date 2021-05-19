package com.musala.gateway.rest;

import com.musala.gateway.boot.GatewayApplication;
import com.musala.gateway.entity.Device;
import com.musala.gateway.entity.DeviceStatus;
import com.musala.gateway.entity.Gateway;
import com.musala.gateway.repository.GatewayRepository;
import com.musala.gateway.service.GatewayService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author lutfun
 * @since 5/19/21
 */

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = GatewayApplication.class)
@WebAppConfiguration
@WebMvcTest(value = GatewayController.class)
public class GatewayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GatewayService gatewayService;

    @MockBean
    private GatewayRepository gatewayRepository;

    private Gateway gateway;

    @Before
    public void setup() {
        gateway = new Gateway();
        gateway.setSerialNumber("SL100");
        gateway.setName("Gateway100");
        gateway.setIpAddress("192.168.0.1");
        Device device1001 = new Device(1001, "Netgear", ZonedDateTime.now(), DeviceStatus.offline);
        Device device1002 = new Device(1002, "Cisco", ZonedDateTime.now(), DeviceStatus.online);
        gateway.setDevices(new HashSet<>(Arrays.asList(device1001, device1002)));

        when(gatewayService.findGateway(anyString())).thenReturn(gateway);
        when(gatewayService.findAll()).thenReturn(Collections.singletonList(gateway));
        when(gatewayService.getDevice("SL100", 1001)).thenReturn(device1001);
        when(gatewayService.getDevice("SL100", 1002)).thenReturn(device1002);
    }

    @Test
    public void test_gatewayGet() throws Exception {
        String expected = "{serialNumber:SL100,name:Gateway100,ipAddress:192.168.0.1}";
        mockMvc.perform(get("/gateways/1"))
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(content().json(expected, false))
                .andReturn();
    }

    @Test
    public void test_gatewayAdd() throws Exception {
        when(gatewayService.save(any())).thenReturn(gateway);
        String gatewayJson = "{\"serialNumber\":\"SL100\",\"name\":\"Gateway100\",\"ipAddress\":\"192.168.0.1\"}";
        MockHttpServletRequestBuilder requestBuilder = post("/gateways")
                .accept(MediaType.APPLICATION_JSON)
                .content(gatewayJson)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andReturn();
    }

    @Test
    public void test_deviceGet() throws Exception {
        String expected = "{uid:1001,vendor:Netgear,status:offline}";
        mockMvc.perform(get("/gateways/SL100/devices/1001"))
                .andExpect(content().json(expected, false))
                .andReturn();
    }

    //TODO: Test all methods
}
