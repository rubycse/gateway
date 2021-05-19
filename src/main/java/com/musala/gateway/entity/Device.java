package com.musala.gateway.entity;

import org.springframework.hateoas.RepresentationModel;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

/**
 * @author lutfun
 * @since 5/18/21
 */

@Entity
@Table(name = "device", uniqueConstraints = @UniqueConstraint(columnNames = "uid"))
public class Device extends RepresentationModel<Device> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    private Integer uid;

    @NotBlank(message = "Required")
    private String vendor;

    private ZonedDateTime created;

    @NotNull
    private DeviceStatus status;

    public Device() {
    }

    public Device(int uid, String vendor, ZonedDateTime created, DeviceStatus status) {
        this.uid = uid;
        this.vendor = vendor;
        this.created = created;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public void setCreated(ZonedDateTime created) {
        this.created = created;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        return getUid() != null ? getUid().equals(device.getUid()) : device.getUid() == null;
    }

    @Override
    public int hashCode() {
        return getUid() != null ? getUid().hashCode() : 0;
    }
}
