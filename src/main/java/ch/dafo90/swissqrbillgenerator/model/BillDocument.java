package ch.dafo90.swissqrbillgenerator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.codecrete.qrbill.generator.Bill;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillDocument {

    private Bill bill;
    private Document document;

}
