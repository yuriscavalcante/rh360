package com.rh360.rh360.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class QrCodeService {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.qrcode.width:300}")
    private int qrCodeWidth;

    @Value("${app.qrcode.height:300}")
    private int qrCodeHeight;

    /**
     * Gera um QR code em formato Base64 (PNG) para uma URL específica
     * 
     * @param url URL que será codificada no QR code
     * @return String Base64 da imagem PNG do QR code
     */
    public String generateQrCodeBase64(String url) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, qrCodeWidth, qrCodeHeight, hints);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] qrCodeBytes = outputStream.toByteArray();

            return Base64.getEncoder().encodeToString(qrCodeBytes);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar QR code: " + e.getMessage(), e);
        }
    }

    /**
     * Gera a URL que será usada no QR code para bater ponto no mobile
     * 
     * @param qrToken Token temporário para autenticação via QR code
     * @return URL completa para acessar via mobile
     */
    public String generateQrCodeUrl(String qrToken) {
        return frontendUrl + "/timeclock/mobile?token=" + qrToken;
    }

    /**
     * Gera a URL que será usada no QR code com ID e path customizados
     * 
     * @param id ID que será incluído na URL
     * @param path Path para onde o QR code será redirecionado
     * @param qrToken Token temporário para autenticação via QR code (opcional)
     * @return URL completa para acessar via mobile
     */
    public String generateQrCodeUrl(String id, String path, String qrToken) {
        // Garantir que o path começa com /
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        
        // Construir URL base
        String baseUrl = frontendUrl.endsWith("/") 
            ? frontendUrl.substring(0, frontendUrl.length() - 1) 
            : frontendUrl;
        
        // Construir URL completa com id e path
        String url = baseUrl + normalizedPath + "?id=" + id;
        
        // Adicionar token se fornecido
        if (qrToken != null && !qrToken.isEmpty()) {
            url += "&token=" + qrToken;
        }
        
        return url;
    }
}
