package ch.dafo90.swissqrbillgenerator.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Builder
public class Document {

    private String senderName;
    private String senderStreet;
    private String senderLocality;
    private String senderCountry;
    private String senderEmail;
    private String senderWebsite;
    private String senderPhoneNumber;

    private String recipientTitle;
    private String recipientOrganization;
    private String recipientName;
    private String recipientStreet;
    private String recipientLocality;
    private String recipientCountry;

    private String salutation;
    private String text;
    private String closure;
    private String signatureTitle1;
    private String signatureName1;
    private String signatureTitle2;
    private String signatureName2;

    public String oneLineSenderAddress() {
        return Stream.of(senderStreet, senderLocality, senderCountry).filter(StringUtils::hasText).collect(Collectors.joining(" "));
    }

    public boolean showSignatureSection() {
        return Stream.of(signatureTitle1, signatureName1, signatureTitle2, signatureName2).anyMatch(StringUtils::hasText);
    }

    public boolean showAllSignatures() {
        return Stream.of(signatureTitle1, signatureName1, signatureTitle2, signatureName2).allMatch(StringUtils::hasText);
    }

    public boolean showSignature1() {
        return Stream.of(signatureTitle1, signatureName1).anyMatch(StringUtils::hasText);
    }

    public boolean showSignature2() {
        return Stream.of(signatureTitle2, signatureName2).anyMatch(StringUtils::hasText);
    }

}
