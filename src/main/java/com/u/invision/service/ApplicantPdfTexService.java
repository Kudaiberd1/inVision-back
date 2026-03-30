package com.u.invision.service;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicantPdfTexService {

	private final S3Service s3Service;

	public String pdfPublicUrlToLatexVerbatim(String publicUrl) {
		if (publicUrl == null || publicUrl.isBlank()) {
			return wrapVerbatim("% (no document URL)");
		}
		try {
			byte[] pdf = s3Service.downloadBytesFromPublicUrl(publicUrl);
			String plain = extractUtf8Text(pdf);
			return wrapVerbatim(plain.isBlank() ? "% (no extractable text in PDF)" : plain);
		} catch (Exception e) {
			log.warn("PDF load/extract failed: {}", e.toString());
			String oneLine = e.getMessage() != null ? e.getMessage().replaceAll("\\s+", " ") : e.toString();
			return wrapVerbatim("% PDF could not be loaded\n% " + escapePercentLines(oneLine));
		}
	}

	private static String extractUtf8Text(byte[] pdfBytes) throws IOException {
		try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
			PDFTextStripper stripper = new PDFTextStripper();
			stripper.setSortByPosition(true);
			return stripper.getText(doc);
		}
	}

	private static String wrapVerbatim(String inner) {
		String n = inner.replace("\r\n", "\n");
		return "\\begin{verbatim}\n" + n + "\n\\end{verbatim}";
	}

	private static String escapePercentLines(String s) {
		return s.replace("%", "\\%");
	}
}
