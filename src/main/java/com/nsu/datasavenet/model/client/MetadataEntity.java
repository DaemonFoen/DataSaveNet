package com.nsu.datasavenet.model.client;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "metadata")
public class MetadataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "peer_address", nullable = false)
    private String peerAddress;

    // Абсолютный путь до файла
    @Column(name = "absolute_path", nullable = false)
    private String absolutePath;

    // Хэш файла
    @Column(name = "file_hash", nullable = false)
    private String fileHash;

    // Версия файла
    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "file_request_key", nullable = false)
    private String fileRequestKey;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;


    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getAbsolutePath() {
        return absolutePath;
    }
    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }
    public String getFileHash() {
        return fileHash;
    }
    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }
    public int getVersion() {
        return version;
    }
    public void setVersion(int version) {
        this.version = version;
    }

    public String getFileRequestKey() {
        return fileRequestKey;
    }

    public void setFileRequestKey(String fileRequestKey) {
        this.fileRequestKey = fileRequestKey;
    }

    public String getPeerAddress() {
        return peerAddress;
    }

    public void setPeerAddress(String peerAddress) {
        this.peerAddress = peerAddress;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MetadataEntity that)) {
            return false;
        }

        return version == that.version && peerAddress.equals(that.peerAddress) && absolutePath.equals(that.absolutePath)
                && fileHash.equals(that.fileHash) && fileRequestKey.equals(that.fileRequestKey);
    }

    @Override
    public int hashCode() {
        int result = peerAddress.hashCode();
        result = 31 * result + absolutePath.hashCode();
        result = 31 * result + fileHash.hashCode();
        result = 31 * result + version;
        result = 31 * result + fileRequestKey.hashCode();
        return result;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return "MetadataEntity{" +
                "id=" + id +
                ", peerAddress='" + peerAddress + '\'' +
                ", absolutePath='" + absolutePath + '\'' +
                ", fileHash='" + fileHash + '\'' +
                ", version=" + version +
                ", fileRequestKey='" + fileRequestKey + '\'' +
                ", creationDate=" + creationDate +
                '}';
    }
}
