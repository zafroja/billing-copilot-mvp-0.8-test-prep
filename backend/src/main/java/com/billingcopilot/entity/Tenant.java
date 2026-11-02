package com.billingcopilot.entity;
import jakarta.persistence.*;
import java.util.UUID;
@Entity @Table(name="tenants")
public class Tenant {
 @Id private UUID id=UUID.randomUUID();
 @Column(nullable=false) private String name;
 protected Tenant(){} public Tenant(String name){this.name=name;}
 public UUID getId(){return id;} public String getName(){return name;}
}
