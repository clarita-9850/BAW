package com.cmips.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {
    
    @Value("${encryption.algorithm:AES}")
    private String encryptionAlgorithm;
    
    @Value("${encryption.key-size:256}")
    private int keySize;
    
    @Value("${encryption.key-file:encryption.key}")
    private String keyFilePath;
    
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    
    public EncryptionService() {
        System.out.println("🔧 EncryptionService: Initializing encryption service");
        System.out.println("🔐 Algorithm: " + encryptionAlgorithm);
        System.out.println("🔑 Key size: " + keySize + " bits");
    }
    
    /**
     * Encrypt a file using AES encryption
     */
    public String encryptFile(String filePath) {
        System.out.println("🔒 EncryptionService: Encrypting file: " + filePath);
        
        try {
            // Read the original file
            byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
            System.out.println("📄 Original file size: " + fileContent.length + " bytes");
            
            // Get or generate encryption key
            SecretKey secretKey = getOrGenerateSecretKey();
            
            // Encrypt the file content
            byte[] encryptedContent = encryptData(fileContent, secretKey);
            System.out.println("🔒 Encrypted content size: " + encryptedContent.length + " bytes");
            
            // Create encrypted file path
            String encryptedFilePath = generateEncryptedFilePath(filePath);
            
            // Write encrypted content to file
            Files.write(Paths.get(encryptedFilePath), encryptedContent);
            System.out.println("💾 Encrypted file saved: " + encryptedFilePath);
            
            // Clean up original file (optional - for security)
            // Files.deleteIfExists(Paths.get(filePath));
            
            return encryptedFilePath;
            
        } catch (Exception e) {
            System.err.println("❌ EncryptionService: Error encrypting file: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to encrypt file", e);
        }
    }
    
    /**
     * Decrypt a file (for testing purposes)
     */
    public String decryptFile(String encryptedFilePath) {
        System.out.println("🔓 EncryptionService: Decrypting file: " + encryptedFilePath);
        
        try {
            // Read the encrypted file
            byte[] encryptedContent = Files.readAllBytes(Paths.get(encryptedFilePath));
            System.out.println("🔒 Encrypted file size: " + encryptedContent.length + " bytes");
            
            // Get encryption key
            SecretKey secretKey = getOrGenerateSecretKey();
            
            // Decrypt the file content
            byte[] decryptedContent = decryptData(encryptedContent, secretKey);
            System.out.println("📄 Decrypted content size: " + decryptedContent.length + " bytes");
            
            // Create decrypted file path
            String decryptedFilePath = generateDecryptedFilePath(encryptedFilePath);
            
            // Write decrypted content to file
            Files.write(Paths.get(decryptedFilePath), decryptedContent);
            System.out.println("💾 Decrypted file saved: " + decryptedFilePath);
            
            return decryptedFilePath;
            
        } catch (Exception e) {
            System.err.println("❌ EncryptionService: Error decrypting file: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to decrypt file", e);
        }
    }
    
    /**
     * Encrypt data using AES
     */
    private byte[] encryptData(byte[] data, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }
    
    /**
     * Decrypt data using AES
     */
    private byte[] decryptData(byte[] encryptedData, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(encryptedData);
    }
    
    /**
     * Get existing key or generate new one
     */
    private SecretKey getOrGenerateSecretKey() {
        try {
            // Try to load existing key
            if (Files.exists(Paths.get(keyFilePath))) {
                return loadSecretKey();
            } else {
                return generateAndSaveSecretKey();
            }
        } catch (Exception e) {
            System.err.println("❌ Error with encryption key: " + e.getMessage());
            return generateAndSaveSecretKey();
        }
    }
    
    /**
     * Generate new secret key and save it
     */
    private SecretKey generateAndSaveSecretKey() {
        try {
            System.out.println("🔑 Generating new encryption key");
            
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
            keyGenerator.init(keySize, new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            
            // Save key to file
            saveSecretKey(secretKey);
            
            System.out.println("✅ New encryption key generated and saved");
            return secretKey;
            
        } catch (Exception e) {
            System.err.println("❌ Error generating encryption key: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate encryption key", e);
        }
    }
    
    /**
     * Save secret key to file
     */
    private void saveSecretKey(SecretKey secretKey) throws IOException {
        byte[] keyBytes = secretKey.getEncoded();
        String keyString = Base64.getEncoder().encodeToString(keyBytes);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(keyFilePath))) {
            writer.println(keyString);
        }
        
        System.out.println("💾 Encryption key saved to: " + keyFilePath);
    }
    
    /**
     * Load secret key from file
     */
    private SecretKey loadSecretKey() throws IOException {
        System.out.println("🔑 Loading existing encryption key from: " + keyFilePath);
        
        String keyString = Files.readString(Paths.get(keyFilePath)).trim();
        byte[] keyBytes = Base64.getDecoder().decode(keyString);
        
        return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }
    
    /**
     * Generate encrypted file path
     */
    private String generateEncryptedFilePath(String originalFilePath) {
        Path originalPath = Paths.get(originalFilePath);
        String fileName = originalPath.getFileName().toString();
        String encryptedFileName = fileName.replace(".csv", ".encrypted.csv");
        
        // Handle case where file is in current directory (no parent)
        Path parent = originalPath.getParent();
        if (parent == null) {
            return encryptedFileName;
        }
        
        return parent.resolve(encryptedFileName).toString();
    }
    
    /**
     * Generate decrypted file path
     */
    private String generateDecryptedFilePath(String encryptedFilePath) {
        Path encryptedPath = Paths.get(encryptedFilePath);
        String fileName = encryptedPath.getFileName().toString();
        String decryptedFileName = fileName.replace(".encrypted.csv", ".decrypted.csv");
        
        // Handle case where file is in current directory (no parent)
        Path parent = encryptedPath.getParent();
        if (parent == null) {
            return decryptedFileName;
        }
        
        return parent.resolve(decryptedFileName).toString();
    }
    
    
    /**
     * Get encryption status
     */
    public String getEncryptionStatus() {
        return String.format(
            "Encryption Service Status:\n" +
            "Algorithm: %s\n" +
            "Key Size: %d bits\n" +
            "Key File: %s\n" +
            "Key Exists: %s",
            encryptionAlgorithm,
            keySize,
            keyFilePath,
            Files.exists(Paths.get(keyFilePath)) ? "Yes" : "No"
        );
    }
}
