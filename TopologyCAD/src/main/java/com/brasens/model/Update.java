package com.brasens.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "application_update_topo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Update {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "app_version", length = 50)
    private String version;

    @Column(name = "download_url", length = 1024)
    private String URL;
}