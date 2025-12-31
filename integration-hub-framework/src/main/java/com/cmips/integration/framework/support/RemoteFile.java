package com.cmips.integration.framework.support;

import java.time.Instant;

/**
 * Represents a file on a remote server.
 *
 * <p>This class provides metadata about files retrieved from SFTP or other
 * remote file systems.
 *
 * <p>Example usage:
 * <pre>
 * List&lt;RemoteFile&gt; files = sftpClient.listFiles("/uploads");
 * for (RemoteFile file : files) {
 *     if (!file.isDirectory() &amp;&amp; file.getSize() &gt; 0) {
 *         sftpClient.download(file.getPath(), localDir);
 *     }
 * }
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class RemoteFile {

    private final String name;
    private final String path;
    private final long size;
    private final Instant lastModified;
    private final boolean directory;
    private final String permissions;
    private final String owner;
    private final String group;

    private RemoteFile(Builder builder) {
        this.name = builder.name;
        this.path = builder.path;
        this.size = builder.size;
        this.lastModified = builder.lastModified;
        this.directory = builder.directory;
        this.permissions = builder.permissions;
        this.owner = builder.owner;
        this.group = builder.group;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public boolean isDirectory() {
        return directory;
    }

    public boolean isFile() {
        return !directory;
    }

    public String getPermissions() {
        return permissions;
    }

    public String getOwner() {
        return owner;
    }

    public String getGroup() {
        return group;
    }

    /**
     * Returns the file extension, or empty string if none.
     *
     * @return the file extension
     */
    public String getExtension() {
        if (name == null || directory) {
            return "";
        }
        int dotIndex = name.lastIndexOf('.');
        return dotIndex > 0 ? name.substring(dotIndex + 1) : "";
    }

    @Override
    public String toString() {
        return "RemoteFile{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", size=" + size +
                ", directory=" + directory +
                ", lastModified=" + lastModified +
                '}';
    }

    public static class Builder {
        private String name;
        private String path;
        private long size = 0;
        private Instant lastModified;
        private boolean directory = false;
        private String permissions;
        private String owner;
        private String group;

        private Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder size(long size) {
            this.size = size;
            return this;
        }

        public Builder lastModified(Instant lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder directory(boolean directory) {
            this.directory = directory;
            return this;
        }

        public Builder permissions(String permissions) {
            this.permissions = permissions;
            return this;
        }

        public Builder owner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder group(String group) {
            this.group = group;
            return this;
        }

        public RemoteFile build() {
            return new RemoteFile(this);
        }
    }
}
