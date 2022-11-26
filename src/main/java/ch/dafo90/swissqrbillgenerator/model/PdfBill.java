package ch.dafo90.swissqrbillgenerator.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PdfBill {

    private final byte[] pdf;
    private final String fileName;

}
