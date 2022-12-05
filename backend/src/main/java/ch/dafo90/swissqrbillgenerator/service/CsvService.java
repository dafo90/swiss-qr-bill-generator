package ch.dafo90.swissqrbillgenerator.service;

import ch.dafo90.swissqrbillgenerator.exception.ValidationException;
import ch.dafo90.swissqrbillgenerator.model.csv.Csv;
import ch.dafo90.swissqrbillgenerator.util.CsvReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.Charset;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvService {

    private static String CSV_MEDIA_TYPE = "text/csv";

    public Csv convert(MultipartFile file, char separator) {
        if (!file.getContentType().toLowerCase().contains(CSV_MEDIA_TYPE)) {
            throw new ValidationException("Invalid media type of file");
        }

        try {
            Charset charset = Charset.forName(UniversalDetector.detectCharset(file.getInputStream()));
            return CsvReader.read(file.getInputStream(), separator, charset);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot read CSV input stream!", ex);
        }
    }

}
