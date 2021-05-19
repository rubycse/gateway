package com.musala.gateway.entity;

import org.springframework.hateoas.RepresentationModel;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.HashSet;
import java.util.Set;

/**
 * @author lutfun
 * @since 5/18/21
 */
@Entity
@Table(name = "gateway", uniqueConstraints = @UniqueConstraint(columnNames = "serialNumber"))
public class Gateway extends RepresentationModel<Gateway> {

    public static final int MAX_DEVICE = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotBlank(message = "Required")
    private String serialNumber;

    @NotBlank(message = "Required")
    private String name;

    @Pattern(regexp = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$", message = "Invalid IPV4 address")
    private String ipAddress;

    @Valid
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "gateway_id")
    private Set<Device> devices;

    public Gateway() {
        devices = new HashSet<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Set<Device> getDevices() {
        return devices;
    }

    public void setDevices(Set<Device> devices) {
        if (devices.size() >= MAX_DEVICE) {
            throw new IllegalStateException();
        }
        this.devices = devices;
    }

    public boolean deviceLimitReached() {
        return devices.size() == MAX_DEVICE;
    }

    public void addDevice(Device device) {
        if (deviceLimitReached()) {
            throw new IllegalStateException();
        }

        getDevices().add(device);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Gateway gateway = (Gateway) o;

        return getSerialNumber() != null ? getSerialNumber().equals(gateway.getSerialNumber()) : gateway.getSerialNumber() == null;
    }

    @Override
    public int hashCode() {
        return getSerialNumber() != null ? getSerialNumber().hashCode() : 0;
    }
}
