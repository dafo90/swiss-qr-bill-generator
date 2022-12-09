package ch.dafo90.swissqrbillgenerator.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.xml.bind.DatatypeConverter;
import org.apache.tika.Tika;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Stream;


public class Base64Image {

    private static final String DEFAULT_MEDIA_TYPE = "text/plain";
    private static final String DEFAULT_CHARSET = "US-ASCII";

    private final boolean valid;

    private final boolean empty;
    private final String mediaType;
    private final String charset;
    private final String base64;

    /**
     * @param dataBase64Image Syntax: data:[<mediatype>][;base64],<data>
     *                        The mediatype is a MIME type string, such as 'image/jpeg' for a JPEG image file. If omitted, defaults to text/plain;charset=US-ASCII
     */
    @Valid
    public static Base64Image of(@NotBlank String dataBase64Image) {
        if (!StringUtils.hasText(dataBase64Image)) {
            return new Base64Image(true, true, null, null, null);
        }

        dataBase64Image = dataBase64Image.trim();
        if (!dataBase64Image.startsWith("data:")) {
            return new Base64Image(false, false, null, null, null);
        }

        String[] commaSplit = dataBase64Image.split(",");
        if (commaSplit.length != 2) {
            return new Base64Image(false, false, null, null, null);
        }

        String data = commaSplit[0].trim();
        String base64Image = commaSplit[1].trim();

        String[] dataSplit = data.split(":");
        if (dataSplit.length != 2) {
            return new Base64Image(false, false, null, null, null);
        }

        String[] dataParams = dataSplit[1].trim().split(";");
        String mediaType = dataParams.length >= 1 ? dataParams[0].trim().toLowerCase() : DEFAULT_MEDIA_TYPE;
        String charset = dataParams.length >= 2 && dataParams[1].trim().toLowerCase().startsWith("charset") ? parseCharset(dataParams[1].trim()) : DEFAULT_CHARSET;

        return mediaType.startsWith("image/") && checkBase64(mediaType, base64Image)
                ? new Base64Image(true, false, mediaType, charset, base64Image)
                : new Base64Image(false, false, null, null, null);
    }

    private static String parseCharset(String charsetParam) {
        String[] charsetParams = charsetParam.split("=");
        return charsetParams.length == 2 ? charsetParams[1].trim().toUpperCase() : DEFAULT_CHARSET;
    }

    private static boolean checkBase64(String mediaType, String base64Image) {
        String detectedMediaType = new Tika().detect(DatatypeConverter.parseBase64Binary(base64Image));
        if (Stream.of("jpg", "jpeg").anyMatch(mediaType::contains)) {
            return List.of("image/jpg", "image/jpeg").contains(detectedMediaType);
        }
        return mediaType.equals(detectedMediaType);
    }

    private Base64Image(boolean valid, boolean empty, String mediaType, String charset, String base64) {
        this.valid = valid;
        this.empty = empty;
        this.mediaType = mediaType;
        this.charset = charset;
        this.base64 = base64;
    }

    public boolean isValid() {
        return valid;
    }

    protected boolean isEmpty() {
        return empty;
    }

    public String getDataUrl() {
        if (!valid || empty) {
            return null;
        }
        return String.format("data:%s;charset=%s,%s", mediaType, charset, base64);
    }

}
