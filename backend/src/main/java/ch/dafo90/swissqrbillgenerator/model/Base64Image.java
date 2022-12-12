package ch.dafo90.swissqrbillgenerator.model;

import ch.dafo90.swissqrbillgenerator.util.MediaTypeUtils;
import jakarta.xml.bind.DatatypeConverter;
import org.apache.tika.Tika;
import org.springframework.util.StringUtils;


public class Base64Image {

    private static final String DEFAULT_MEDIA_TYPE = "text/plain";
    private static final String DEFAULT_CHARSET = "US-ASCII";
    private final String mediaType;
    private final String charset;
    private final String base64;

    /**
     * @param imageBase64Url Syntax: data:[<mediatype>][;base64],<data>
     *                       The mediatype is a MIME type string, such as 'image/jpeg' for a JPEG image file. If omitted, defaults to text/plain;charset=US-ASCII
     */
    public static Base64Image of(String imageBase64Url) {
        if (!StringUtils.hasText(imageBase64Url)) {
            return new Base64Image(null, null, null);
        }

        imageBase64Url = imageBase64Url.trim();
        if (!imageBase64Url.startsWith("data:")) {
            throw new RuntimeException("Invalid image: doesn't start with 'data:' (must be a string containing the requested data URL, see https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/Data_URLs)");
        }

        String[] commaSplit = imageBase64Url.split(",");
        if (commaSplit.length != 2) {
            throw new RuntimeException(String.format("Invalid image: found %d comma separated token, expected 2 (must be a string containing the requested data URL, see https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/Data_URLs)", commaSplit.length));
        }

        String data = commaSplit[0].trim();
        String base64Image = commaSplit[1].trim();

        String[] dataSplit = data.split(":");
        if (dataSplit.length != 2) {
            throw new RuntimeException(String.format("Invalid image data: found %d colon separated token, expected 2 (must be a string containing the requested data URL, see https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/Data_URLs)", dataSplit.length));
        }

        String[] dataParams = dataSplit[1].trim().split(";");
        String mediaType = dataParams.length >= 1 ? dataParams[0].trim().toLowerCase() : DEFAULT_MEDIA_TYPE;
        String charset = dataParams.length >= 2 && dataParams[1].trim().toLowerCase().startsWith("charset") ? parseCharset(dataParams[1].trim()) : DEFAULT_CHARSET;

        if (!mediaType.startsWith("image/")) {
            throw new RuntimeException(String.format("Invalid image: unsupported media type '%s', 'image/*' only are supported", mediaType));
        }

        String detectedMediaType = new Tika().detect(DatatypeConverter.parseBase64Binary(base64Image));
        if (!MediaTypeUtils.check(mediaType, detectedMediaType)) {
            throw new RuntimeException(String.format("Invalid image: defined media type '%s', detected '%s'", mediaType, detectedMediaType));
        }

        return new Base64Image(mediaType, charset, base64Image);
    }

    private static String parseCharset(String charsetParam) {
        String[] charsetParams = charsetParam.split("=");
        return charsetParams.length == 2 ? charsetParams[1].trim().toUpperCase() : DEFAULT_CHARSET;
    }

    private Base64Image(String mediaType, String charset, String base64) {
        this.mediaType = mediaType;
        this.charset = charset;
        this.base64 = base64;
    }

    public String getDataUrl() {
        return StringUtils.hasText(base64) ? String.format("data:%s;charset=%s,%s", mediaType, charset, base64) : null;
    }

}
