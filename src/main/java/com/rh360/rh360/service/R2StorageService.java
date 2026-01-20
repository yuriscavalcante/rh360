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
            // Validar credenciais
            if (accessKeyId == null || accessKeyId.isEmpty()) {
                throw new RuntimeException("CLOUDFLARE_R2_ACCESS_KEY_ID não configurado");
            }
            if (secretAccessKey == null || secretAccessKey.isEmpty()) {
                throw new RuntimeException("CLOUDFLARE_R2_SECRET_ACCESS_KEY não configurado");
            }
            if (endpointUrl == null || endpointUrl.isEmpty()) {
                throw new RuntimeException("CLOUDFLARE_R2_ENDPOINT_URL não configurado");
            }
            if (bucketName == null || bucketName.isEmpty()) {
                throw new RuntimeException("CLOUDFLARE_R2_BUCKET_NAME não configurado");
            }
            
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
            
            try {
                System.out.println("Criando cliente S3 com endpoint: " + endpointUrl);
                s3Client = S3Client.builder()
                        .endpointOverride(java.net.URI.create(endpointUrl))
                        .credentialsProvider(StaticCredentialsProvider.create(credentials))
                        .region(Region.of("auto"))
                        .forcePathStyle(true) // Importante para R2
                        .build();
                System.out.println("Cliente S3 criado com sucesso para bucket: " + bucketName);
            } catch (Exception e) {
                System.err.println("ERRO ao criar cliente S3: " + e.getMessage());
                throw new RuntimeException("Erro ao criar cliente S3 para R2: " + e.getMessage(), e);
            }
        }
        return s3Client;
    }

    public String uploadPhoto(MultipartFile file, UUID userId) {
        // Log de todas as configurações do R2
        System.out.println("==========================================");
        System.out.println("CONFIGURAÇÕES R2 CARREGADAS:");
        System.out.println("==========================================");
        System.out.println("cloudflare.r2.account-id: " + (accountId != null && !accountId.isEmpty() ? accountId : "(VAZIO)"));
        System.out.println("cloudflare.r2.access-key-id: " + (accessKeyId != null && !accessKeyId.isEmpty() ? accessKeyId.substring(0, Math.min(8, accessKeyId.length())) + "..." : "(VAZIO)"));
        System.out.println("cloudflare.r2.secret-access-key: " + (secretAccessKey != null && !secretAccessKey.isEmpty() ? secretAccessKey.substring(0, Math.min(8, secretAccessKey.length())) + "..." : "(VAZIO)"));
        System.out.println("cloudflare.r2.bucket-name: " + (bucketName != null && !bucketName.isEmpty() ? bucketName : "(VAZIO)"));
        System.out.println("cloudflare.r2.endpoint-url: " + (endpointUrl != null && !endpointUrl.isEmpty() ? endpointUrl : "(VAZIO)"));
        System.out.println("cloudflare.r2.public-url: " + (publicUrl != null && !publicUrl.isEmpty() ? publicUrl : "(VAZIO)"));
        System.out.println("==========================================");
        
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
            String errorCode = e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "UNKNOWN";
            String errorMessage = e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage();
            String errorDetails = String.format(
                "Erro ao fazer upload para R2 - Status: %s, Code: %s, Message: %s",
                e.statusCode(), errorCode, errorMessage
            );
            
            // Mensagem mais específica para erro 403
            if (e.statusCode() == 403) {
                throw new RuntimeException(
                    "Acesso negado ao R2. Verifique: " +
                    "1) Se as credenciais (Access Key ID e Secret Access Key) estão corretas; " +
                    "2) Se o token R2 tem permissões de Object Read e Object Write; " +
                    "3) Se o bucket '" + bucketName + "' existe e está acessível. " +
                    "Detalhes: " + errorDetails, e
                );
            }
            
            throw new RuntimeException("Erro ao fazer upload para R2: " + errorDetails, e);
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
