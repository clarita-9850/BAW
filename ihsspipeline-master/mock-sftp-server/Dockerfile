FROM alpine:latest

# Install OpenSSH server
RUN apk add --no-cache openssh

# Create SFTP user
RUN adduser -D -s /bin/sh sftpuser
RUN echo "sftpuser:password" | chpasswd

# Create SFTP directories
RUN mkdir -p /home/sftpuser/reports
RUN mkdir -p /home/sftpuser/reports/daily
RUN chown -R sftpuser:sftpuser /home/sftpuser/reports

# Configure SSH for SFTP only
RUN echo "Match User sftpuser" >> /etc/ssh/sshd_config
RUN echo "    ChrootDirectory /home/sftpuser" >> /etc/ssh/sshd_config
RUN echo "    ForceCommand internal-sftp" >> /etc/ssh/sshd_config
RUN echo "    AllowTcpForwarding no" >> /etc/ssh/sshd_config
RUN echo "    X11Forwarding no" >> /etc/ssh/sshd_config

# Create startup script
RUN echo '#!/bin/sh' > /start-sftp.sh
RUN echo 'ssh-keygen -A' >> /start-sftp.sh
RUN echo 'exec /usr/sbin/sshd -D -e' >> /start-sftp.sh
RUN chmod +x /start-sftp.sh

EXPOSE 22

CMD ["/start-sftp.sh"]
