package com.cmips.integration.framework.util;

import com.cmips.integration.framework.exception.ConnectionException;
import com.cmips.integration.framework.exception.ReadException;
import com.cmips.integration.framework.exception.SendException;
import com.cmips.integration.framework.model.UploadResult;
import com.cmips.integration.framework.support.FilePattern;
import com.cmips.integration.framework.support.RemoteFile;
import com.cmips.integration.framework.support.SftpClient;
import com.cmips.integration.framework.support.SftpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

/**
 * Utility class for SFTP operations.
 *
 * <p>This class provides static utility methods for common SFTP operations
 * such as uploading, downloading, and listing files.
 *
 * <p>Example usage:
 * <pre>
 * SftpConfig config = SftpConfig.builder()
 *     .host("sftp.example.com")
 *     .username("user")
 *     .password("secret")
 *     .build();
 *
 * // Create a reusable client
 * SftpClient client = SftpUtil.createClient(config);
 *
 * // Upload a file
 * SftpUtil.upload(client, localFile, "/uploads", "data.xml");
 *
 * // Download a file
 * Path downloaded = SftpUtil.download(client, "/uploads/data.xml", localDir);
 *
 * // List files
 * List&lt;RemoteFile&gt; files = SftpUtil.listFiles(client, "/uploads", "*.xml");
 *
 * // Upload with verification
 * UploadResult result = SftpUtil.uploadWithVerification(client, localFile, "/uploads/data.xml");
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SftpUtil {

    private static final Logger log = LoggerFactory.getLogger(SftpUtil.class);

    private SftpUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a new SFTP client with the given configuration.
     *
     * <p>Note: The caller is responsible for connecting and closing the client.
     *
     * @param config the SFTP configuration
     * @return a new SftpClient instance
     */
    public static SftpClient createClient(SftpConfig config) {
        return new SftpClient(config);
    }

    /**
     * Creates and connects an SFTP client.
     *
     * @param config the SFTP configuration
     * @return a connected SftpClient instance
     * @throws ConnectionException if connection fails
     */
    public static SftpClient createConnectedClient(SftpConfig config) throws ConnectionException {
        SftpClient client = new SftpClient(config);
        client.connect();
        return client;
    }

    /**
     * Uploads a local file to the remote server.
     *
     * @param client the SFTP client
     * @param localFile the local file to upload
     * @param remoteDir the remote directory
     * @param remoteFileName the remote file name
     * @throws SendException if upload fails
     */
    public static void upload(SftpClient client, Path localFile, String remoteDir,
                               String remoteFileName) throws SendException {
        ensureConnected(client);
        client.upload(localFile, remoteDir, remoteFileName);
    }

    /**
     * Uploads a local file to the remote server with the same file name.
     *
     * @param client the SFTP client
     * @param localFile the local file to upload
     * @param remoteDir the remote directory
     * @throws SendException if upload fails
     */
    public static void upload(SftpClient client, Path localFile, String remoteDir)
            throws SendException {
        String fileName = localFile.getFileName().toString();
        upload(client, localFile, remoteDir, fileName);
    }

    /**
     * Downloads a remote file to the local directory.
     *
     * @param client the SFTP client
     * @param remoteFile the remote file path
     * @param localDir the local directory
     * @return the path to the downloaded file
     * @throws ReadException if download fails
     */
    public static Path download(SftpClient client, String remoteFile, Path localDir)
            throws ReadException {
        ensureConnected(client);
        return client.download(remoteFile, localDir);
    }

    /**
     * Lists files in a remote directory.
     *
     * @param client the SFTP client
     * @param remoteDir the remote directory
     * @param globPattern the glob pattern to match
     * @return list of matching remote files
     * @throws ReadException if listing fails
     */
    public static List<RemoteFile> listFiles(SftpClient client, String remoteDir,
                                               String globPattern) throws ReadException {
        ensureConnected(client);
        FilePattern pattern = FilePattern.glob(globPattern);
        return client.listFiles(remoteDir, pattern);
    }

    /**
     * Lists all files in a remote directory.
     *
     * @param client the SFTP client
     * @param remoteDir the remote directory
     * @return list of all remote files
     * @throws ReadException if listing fails
     */
    public static List<RemoteFile> listFiles(SftpClient client, String remoteDir)
            throws ReadException {
        return listFiles(client, remoteDir, "*");
    }

    /**
     * Uploads a file with verification.
     *
     * <p>After uploading, verifies that the file exists on the remote server
     * and checks the file size matches.
     *
     * @param client the SFTP client
     * @param localFile the local file to upload
     * @param remotePath the full remote path (including file name)
     * @return the upload result
     */
    public static UploadResult uploadWithVerification(SftpClient client, Path localFile,
                                                        String remotePath) {
        long startTime = System.currentTimeMillis();
        String remoteDir = remotePath.substring(0, remotePath.lastIndexOf('/'));
        String remoteFileName = remotePath.substring(remotePath.lastIndexOf('/') + 1);

        try {
            ensureConnected(client);

            // Get local file size
            long localSize = java.nio.file.Files.size(localFile);

            // Upload file
            client.upload(localFile, remoteDir, remoteFileName);

            // Verify upload
            boolean exists = client.exists(remotePath);
            long durationMs = System.currentTimeMillis() - startTime;

            if (exists) {
                log.info("Upload verified: {} ({} bytes in {}ms)",
                        remotePath, localSize, durationMs);

                return UploadResult.builder()
                        .success(true)
                        .localPath(localFile.toString())
                        .remotePath(remotePath)
                        .fileSize(localSize)
                        .verified(true)
                        .durationMs(durationMs)
                        .build();
            } else {
                return UploadResult.builder()
                        .success(false)
                        .localPath(localFile.toString())
                        .remotePath(remotePath)
                        .verified(false)
                        .errorMessage("File not found after upload")
                        .durationMs(durationMs)
                        .build();
            }

        } catch (Exception e) {
            log.error("Upload failed: {} -> {}", localFile, remotePath, e);
            return UploadResult.builder()
                    .success(false)
                    .localPath(localFile.toString())
                    .remotePath(remotePath)
                    .errorMessage(e.getMessage())
                    .durationMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    /**
     * Checks if a remote file exists.
     *
     * @param client the SFTP client
     * @param remotePath the remote file path
     * @return {@code true} if the file exists
     */
    public static boolean exists(SftpClient client, String remotePath) {
        ensureConnected(client);
        return client.exists(remotePath);
    }

    /**
     * Deletes a remote file.
     *
     * @param client the SFTP client
     * @param remotePath the remote file path
     * @throws SendException if deletion fails
     */
    public static void delete(SftpClient client, String remotePath) throws SendException {
        ensureConnected(client);
        client.delete(remotePath);
    }

    /**
     * Renames/moves a remote file.
     *
     * @param client the SFTP client
     * @param oldPath the current path
     * @param newPath the new path
     * @throws SendException if rename fails
     */
    public static void rename(SftpClient client, String oldPath, String newPath)
            throws SendException {
        ensureConnected(client);
        client.rename(oldPath, newPath);
    }

    /**
     * Creates a remote directory and any necessary parent directories.
     *
     * @param client the SFTP client
     * @param remoteDir the remote directory path
     * @throws SendException if creation fails
     */
    public static void mkdirs(SftpClient client, String remoteDir) throws SendException {
        ensureConnected(client);
        client.mkdirs(remoteDir);
    }

    private static void ensureConnected(SftpClient client) {
        if (client == null) {
            throw new IllegalArgumentException("SFTP client cannot be null");
        }
        if (!client.isConnected()) {
            throw new IllegalStateException("SFTP client is not connected");
        }
    }
}
