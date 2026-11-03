package com.billingcopilot.entity;
import jakarta.persistence.*;
@Entity @Table(name="tenants")
public class Tenant {
 @Id private Long id;
 @Column(nullable=false) private String name;
 protected Tenant(){}
 public Tenant(Long id,String name){this.id=id;this.name=name;}
 public Long getId(){return id;} public String getName(){return name;}
}
