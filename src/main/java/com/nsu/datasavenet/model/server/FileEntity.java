package com.nsu.datasavenet.model.server;


import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "files")
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login", nullable = false)
    private String login;

    @Column(name = "absolute_path", nullable = false)
    private String absolutePath;

    @Column(name = "file_hash", nullable = false)
    private String fileHash;

    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "file_request_key", nullable = false)
    private String fileRequestKey;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "file_content", nullable = false)
    private byte[] file;

    public FileEntity(String login, String absolutePath, String fileHash, int version, String fileRequestKey,
            byte[] file) {
        this.login = login;
        this.absolutePath = absolutePath;
        this.fileHash = fileHash;
        this.version = version;
        this.fileRequestKey = fileRequestKey;
        this.file = file;
    }

    public FileEntity() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
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

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }
}
