package com.caremate.lifeguardian.report.service;

import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.config.ReportStorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 생성된 리포트 PDF를 객체 저장소에 업로드하고 접근 URL을 반환
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportStorageServiceImpl {

    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final ReportStorageProperties properties;

    /**
     * 고객별 고유 객체 키로 PDF를 업로드
     */
    public String uploadPdf(byte[] pdfBytes, Long customerId) {
        String objectKey = createObjectKey(customerId);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .contentType(PDF_CONTENT_TYPE)
                    .contentLength((long) pdfBytes.length)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(pdfBytes));
            return objectKey;
        } catch (S3Exception e) {
            String errorCode = e.awsErrorDetails() == null
                    ? "unknown"
                    : e.awsErrorDetails().errorCode();
            log.error(
                    "R2 upload rejected: status={}, errorCode={}, bucket={}, key={}",
                    e.statusCode(),
                    errorCode,
                    properties.getBucket(),
                    objectKey
            );
            throw new BaseException(
                    500,
                    "업로드가 거부되었습니다. 서버 로그의 상태 코드와 오류 코드를 확인하세요."
            );
        } catch (SdkClientException e) {
            log.error(
                    "R2 connection failed: endpoint={}, bucket={}, reason={}",
                    properties.getEndpoint(),
                    properties.getBucket(),
                    e.getMessage()
            );
            throw new BaseException(
                    500,
                    "연결에 실패했습니다. 네트워크 설정을 확인하세요."
            );
        } catch (Exception e) {
            log.error("Unexpected R2 upload failure", e);
            throw new BaseException(500, "리포트 PDF 객체 스토리지 업로드에 실패했습니다.");
        }
    }

    public PresignedReportUrl createReadUrl(String storedReportLocation) {
        String objectKey = resolveObjectKey(storedReportLocation);
        Duration signatureDuration = Duration.ofMinutes(properties.getPresignedUrlExpirationMinutes());

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(signatureDuration)
                .getObjectRequest(getObjectRequest)
                .build();

        return new PresignedReportUrl(
                s3Presigner.presignGetObject(presignRequest).url().toString(),
                OffsetDateTime.now().plus(signatureDuration)
        );
    }

    private String resolveObjectKey(String storedReportLocation) {
        if (!StringUtils.hasText(storedReportLocation)) {
            throw new BaseException(404, "리포트 파일 위치를 찾을 수 없습니다.");
        }

        String trimmed = storedReportLocation.trim();
        if (trimmed.startsWith("s3://")) {
            String bucketPrefix = "s3://" + properties.getBucket() + "/";
            if (trimmed.startsWith(bucketPrefix)) {
                return trimmed.substring(bucketPrefix.length());
            }
            return trimmed.substring("s3://".length()).replaceFirst("^[^/]+/", "");
        }

        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            String path = URI.create(trimmed).getPath();
            return path == null ? trimmed : path.replaceFirst("^/+", "");
        }

        return trimmed.replaceFirst("^/+", "");
    }

    private String createObjectKey(Long customerId) {
        LocalDate today = LocalDate.now();
        return "%s/%d/%d/%02d/%s.pdf".formatted(
                properties.getKeyPrefix(),
                customerId,
                today.getYear(),
                today.getMonthValue(),
                UUID.randomUUID()
        );
    }

    public record PresignedReportUrl(String url, OffsetDateTime expiresAt) {
    }
}
