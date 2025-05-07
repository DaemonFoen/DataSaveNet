package com.nsu.datasavenet.model.server;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "encrypt_metadata")
public class EncryptMetadataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "metadata", nullable = false)
    private byte[] encryptMetadata;

    @Column(name = "login", nullable = false)
    private String login;

    public EncryptMetadataEntity(String login, byte[] encryptMetadata) {
        this.login = login;
        this.encryptMetadata = encryptMetadata;
    }

    public EncryptMetadataEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getEncryptMetadata() {
        return encryptMetadata;
    }

    public void setEncryptMetadata(byte[] encryptMetadata) {
        this.encryptMetadata = encryptMetadata;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}