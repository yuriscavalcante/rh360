package com.rh360.rh360.service;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class R2StorageService {

    @Value("${cloudflare.r2.account-id:}")
    private String accountId;

    @Value("${cloudflare.r2.access-key-id:}")
    private String accessKeyId;

    @Value("${cloudflare.r2.secret-access-key:}")
    private String secretAccessKey;

    @Value("${cloudflare.r2.bucket-name:}")
    private String bucketName;

    @Value("${cloudflare.r2.endpoint-url:}")
    private String endpointUrl;

    @Value("${cloudflare.r2.public-url:}")
    private String publicUrl;

    private S3Client s3Client;

    private S3Client getS3Client() {
        if (s3Client == null) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
            
            s3Client = S3Client.builder()
                    .endpointOverride(java.net.URI.create(endpointUrl))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .region(Region.of("auto"))
                    .build();
        }
        return s3Client;
    }

    public String uploadPhoto(MultipartFile file, UUID userId) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Arquivo não pode ser vazio");
        }

        // Validar tipo de arquivo (apenas imagens)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Apenas arquivos de imagem são permitidos");
        }

        try {
            // Gerar nome único para o arquivo
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = "users/" + userId + "/photo" + extension;

            // Upload para R2
            InputStream inputStream = file.getInputStream();
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(contentType)
                    .build();

            getS3Client().putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));

            // Retornar URL pública
            if (publicUrl != null && !publicUrl.isEmpty()) {
                return publicUrl + "/" + fileName;
            } else {
                // Fallback: construir URL a partir do endpoint
                return endpointUrl.replace("/" + accountId, "") + "/" + bucketName + "/" + fileName;
            }
        } catch (S3Exception e) {
            throw new RuntimeException("Erro ao fazer upload para R2: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar arquivo: " + e.getMessage(), e);
        }
    }

    public void deletePhoto(String photoUrl) {
        if (photoUrl == null || photoUrl.isEmpty()) {
            return;
        }

        try {
            // Extrair a chave do objeto da URL
            final String key;
            if (photoUrl.contains("users/")) {
                int usersIndex = photoUrl.indexOf("users/");
                key = photoUrl.substring(usersIndex);
            } else if (photoUrl.contains("/")) {
                key = photoUrl.substring(photoUrl.lastIndexOf("/") + 1);
            } else {
                key = photoUrl;
            }

            getS3Client().deleteObject(builder -> builder
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (Exception e) {
            // Log do erro mas não falha se não conseguir deletar
            System.err.println("Erro ao deletar foto do R2: " + e.getMessage());
        }
    }
}
