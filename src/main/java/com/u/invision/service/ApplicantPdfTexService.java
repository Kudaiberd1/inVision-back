package com.u.invision.service;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

/**
 * Loads applicant PDFs from S3 and returns their text wrapped in a LaTeX {@code verbatim} block for
 * frontend TeX renderers (e.g. MathJax full-document, react-latex-next).
 */
@Slf4j
@Service
public class ApplicantPdfTexService {

	private final S3Service s3Service;

	public ApplicantPdfTexService(S3Service s3Service) {
		this.s3Service = s3Service;
	}

	/**
	 * @return LaTeX source fragment: {@code \begin{verbatim}...\end{verbatim}} (UTF-8 text from PDF).
	 */
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

	/** LaTeX fragment {@code \\begin{verbatim}...\\end{verbatim}}. Avoids {@code \\r\\n} for consistent line breaks. */
	private static String wrapVerbatim(String inner) {
		String n = inner.replace("\r\n", "\n");
		return "\\begin{verbatim}\n" + n + "\n\\end{verbatim}";
	}

	private static String escapePercentLines(String s) {
		return s.replace("%", "\\%");
	}
}
