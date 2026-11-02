package com.billingcopilot.contract;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class ContractTextExtractor {
  public String extract(byte[] pdf) throws IOException {
    try (PDDocument doc = Loader.loadPDF(pdf)) {
      if (doc.getNumberOfPages() > 100) throw new IllegalArgumentException("Contract exceeds 100-page MVP limit");
      return new PDFTextStripper().getText(doc);
    }
  }
}
