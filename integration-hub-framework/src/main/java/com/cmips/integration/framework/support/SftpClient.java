package com.cmips.integration.framework.support;

import com.cmips.integration.framework.exception.ConnectionException;
import com.cmips.integration.framework.exception.SendException;
import com.cmips.integration.framework.exception.ReadException;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

/**
 * SFTP client wrapper around JSch.
 *
 * <p>This class provides a simplified interface for SFTP operations including
 * file upload, download, listing, and directory management.
 *
 * <p>Example usage:
 * <pre>
 * SftpConfig config = SftpConfig.builder()
 *     .host("sftp.example.com")
 *     .username("user")
 *     .password("secret")
 *     .build();
 *
 * try (SftpClient client = new SftpClient(config)) {
 *     client.connect();
 *     client.upload(localFile, "/uploads", "data.xml");
 *     List&lt;RemoteFile&gt; files = client.listFiles("/uploads");
 * }
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class SftpClient implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(SftpClient.class);

    private final SftpConfig config;
    private final JSch jsch;
    private Session session;
    private ChannelSftp channel;

    /**
     * Creates a new SFTP client with the given configuration.
     *
     * @param config the SFTP configuration
     */
    public SftpClient(SftpConfig config) {
        this.config = config;
        this.jsch = new JSch();
    }

    /**
     * Establishes a connection to the SFTP server.
     *
     * @throws ConnectionException if connection fails
     */
    public void connect() throws ConnectionException {
        try {
            log.debug("Connecting to SFTP server: {}:{}", config.getHost(), config.getPort());

            // Set up known hosts
            if (config.getKnownHostsFile() != null) {
                jsch.setKnownHosts(config.getKnownHostsFile().toString());
            }

            // Set up private key if configured
            if (config.usesPrivateKey()) {
                if (config.getPrivateKeyPassphrase() != null) {
                    jsch.addIdentity(config.getPrivateKeyFile().toString(),
                            config.getPrivateKeyPassphrase());
                } else {
                    jsch.addIdentity(config.getPrivateKeyFile().toString());
                }
            }

            // Create session
            session = jsch.getSession(config.getUsername(), config.getHost(), config.getPort());

            // Set password if configured
            if (config.getPassword() != null) {
                session.setPassword(config.getPassword());
            }

            // Configure session
            Properties sessionConfig = config.getSessionConfig();
            if (!config.isStrictHostKeyChecking()) {
                sessionConfig.put("StrictHostKeyChecking", "no");
            }
            session.setConfig(sessionConfig);

            // Connect session
            session.connect(config.getConnectionTimeout());

            // Open SFTP channel
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(config.getConnectionTimeout());

            log.info("Connected to SFTP server: {}", config.getHost());

        } catch (JSchException e) {
            throw new ConnectionException("Failed to connect to SFTP server: " + config.getHost(), e,
                    config.getHost(), config.getPort());
        }
    }

    /**
     * Checks if the client is connected.
     *
     * @return {@code true} if connected
     */
    public boolean isConnected() {
        return channel != null && channel.isConnected() && session != null && session.isConnected();
    }

    /**
     * Uploads a local file to the remote server.
     *
     * @param localFile the local file to upload
     * @param remoteDir the remote directory
     * @param remoteFileName the remote file name
     * @throws SendException if upload fails
     */
    public void upload(Path localFile, String remoteDir, String remoteFileName) throws SendException {
        ensureConnected();
        String remotePath = remoteDir + "/" + remoteFileName;

        try {
            log.debug("Uploading {} to {}", localFile, remotePath);

            // Ensure remote directory exists
            mkdirs(remoteDir);

            try (InputStream is = Files.newInputStream(localFile)) {
                channel.put(is, remotePath);
            }

            log.info("Uploaded file to {}", remotePath);

        } catch (SftpException | IOException e) {
            throw new SendException("Failed to upload file: " + localFile, e,
                    remotePath, null, 0, 1);
        }
    }

    /**
     * Downloads a remote file to a local directory.
     *
     * @param remoteFile the remote file path
     * @param localDir the local directory to save to
     * @return the path to the downloaded file
     * @throws ReadException if download fails
     */
    public Path download(String remoteFile, Path localDir) throws ReadException {
        ensureConnected();
        String fileName = remoteFile.substring(remoteFile.lastIndexOf('/') + 1);
        Path localFile = localDir.resolve(fileName);

        try {
            log.debug("Downloading {} to {}", remoteFile, localFile);

            Files.createDirectories(localDir);

            try (OutputStream os = Files.newOutputStream(localFile)) {
                channel.get(remoteFile, os);
            }

            log.info("Downloaded file to {}", localFile);
            return localFile;

        } catch (SftpException | IOException e) {
            throw new ReadException("Failed to download file: " + remoteFile, e,
                    remoteFile, -1, 0);
        }
    }

    /**
     * Lists files in a remote directory.
     *
     * @param remoteDir the remote directory
     * @return list of remote files
     * @throws ReadException if listing fails
     */
    public List<RemoteFile> listFiles(String remoteDir) throws ReadException {
        return listFiles(remoteDir, FilePattern.any());
    }

    /**
     * Lists files in a remote directory matching a pattern.
     *
     * @param remoteDir the remote directory
     * @param pattern the file pattern to match
     * @return list of matching remote files
     * @throws ReadException if listing fails
     */
    @SuppressWarnings("unchecked")
    public List<RemoteFile> listFiles(String remoteDir, FilePattern pattern) throws ReadException {
        ensureConnected();
        List<RemoteFile> files = new ArrayList<>();

        try {
            Vector<ChannelSftp.LsEntry> entries = channel.ls(remoteDir);

            for (ChannelSftp.LsEntry entry : entries) {
                String name = entry.getFilename();
                if (".".equals(name) || "..".equals(name)) {
                    continue;
                }

                if (pattern.matches(name)) {
                    files.add(RemoteFile.builder()
                            .name(name)
                            .path(remoteDir + "/" + name)
                            .size(entry.getAttrs().getSize())
                            .lastModified(Instant.ofEpochSecond(entry.getAttrs().getMTime()))
                            .directory(entry.getAttrs().isDir())
                            .permissions(entry.getAttrs().getPermissionsString())
                            .build());
                }
            }

            return files;

        } catch (SftpException e) {
            throw new ReadException("Failed to list files in: " + remoteDir, e, remoteDir, -1, 0);
        }
    }

    /**
     * Checks if a remote file exists.
     *
     * @param remotePath the remote file path
     * @return {@code true} if the file exists
     */
    public boolean exists(String remotePath) {
        ensureConnected();
        try {
            channel.stat(remotePath);
            return true;
        } catch (SftpException e) {
            return false;
        }
    }

    /**
     * Creates a remote directory and any necessary parent directories.
     *
     * @param remoteDir the remote directory path
     * @throws SendException if creation fails
     */
    public void mkdirs(String remoteDir) throws SendException {
        ensureConnected();
        String[] parts = remoteDir.split("/");
        StringBuilder path = new StringBuilder();

        try {
            for (String part : parts) {
                if (part.isEmpty()) {
                    path.append("/");
                    continue;
                }
                path.append(part);
                try {
                    channel.stat(path.toString());
                } catch (SftpException e) {
                    channel.mkdir(path.toString());
                }
                path.append("/");
            }
        } catch (SftpException e) {
            throw new SendException("Failed to create directory: " + remoteDir, e);
        }
    }

    /**
     * Deletes a remote file.
     *
     * @param remotePath the remote file path
     * @throws SendException if deletion fails
     */
    public void delete(String remotePath) throws SendException {
        ensureConnected();
        try {
            channel.rm(remotePath);
            log.info("Deleted remote file: {}", remotePath);
        } catch (SftpException e) {
            throw new SendException("Failed to delete file: " + remotePath, e);
        }
    }

    /**
     * Renames/moves a remote file.
     *
     * @param oldPath the current path
     * @param newPath the new path
     * @throws SendException if rename fails
     */
    public void rename(String oldPath, String newPath) throws SendException {
        ensureConnected();
        try {
            channel.rename(oldPath, newPath);
            log.info("Renamed {} to {}", oldPath, newPath);
        } catch (SftpException e) {
            throw new SendException("Failed to rename file: " + oldPath, e);
        }
    }

    /**
     * Disconnects from the SFTP server.
     */
    public void disconnect() {
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        log.info("Disconnected from SFTP server: {}", config.getHost());
    }

    @Override
    public void close() {
        disconnect();
    }

    private void ensureConnected() {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to SFTP server");
        }
    }
}
