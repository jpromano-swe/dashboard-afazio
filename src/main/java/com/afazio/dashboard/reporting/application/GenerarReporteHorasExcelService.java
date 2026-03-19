package com.afazio.dashboard.reporting.application;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
public class GenerarReporteHorasExcelService {

  private static final String TEMPLATE_PATH = "templates/excel/hg-horas-template.xlsx";
  private static final int DETAIL_START_ROW = 12; // Excel row 13
  private static final int TOTAL_ROW = 22;        // Excel row 23

  private final ConstruirReporteHorasExcelService construirReporteHorasExcelService;

  public GenerarReporteHorasExcelService(ConstruirReporteHorasExcelService construirReporteHorasExcelService) {
    this.construirReporteHorasExcelService = construirReporteHorasExcelService;
  }

  @Transactional(readOnly = true)
  public byte[] ejecutar(Long consultoraId, java.time.YearMonth periodo) {
    ReporteHorasExcelData data = construirReporteHorasExcelService.ejecutar(consultoraId, periodo);

    try (
      InputStream inputStream = new ClassPathResource(TEMPLATE_PATH).getInputStream();
      Workbook workbook = new XSSFWorkbook(inputStream);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
    ) {
      Sheet sheet = workbook.getSheetAt(0);

      escribirEncabezado(sheet, data);
      escribirDetalle(sheet, data.rows());
      escribirTotal(sheet, data.rows());

      FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
      evaluator.evaluateAll();

      workbook.write(outputStream);
      return outputStream.toByteArray();
    } catch (Exception e) {
      throw new IllegalStateException("No se pudo generar el archivo Excel", e);
    }
  }

  private void escribirEncabezado(Sheet sheet, ReporteHorasExcelData data) {
    getOrCreateCell(sheet, 6, 1).setCellValue("DOCENTE: " + data.docenteNombre()); // B7
    getOrCreateCell(sheet, 6, 5).setCellValue("MES: " + capitalizarMes(data.periodo())); // F7
    getOrCreateCell(sheet, 7, 1).setCellValue("CUIT:  " + data.docenteCuit()); // B8
  }

  private void escribirDetalle(Sheet sheet, List<ReporteHorasExcelRow> rows) {
    for (int i = 0; i < rows.size(); i++) {
      int rowIndex = DETAIL_START_ROW + i;
      Row row = getOrCreateRow(sheet, rowIndex);
      ReporteHorasExcelRow item = rows.get(i);

      getOrCreateCell(row, 1).setCellValue(item.empresa());                     // B
      getOrCreateCell(row, 2).setCellValue(item.grupo());                       // C
      getOrCreateCell(row, 3).setCellValue(item.horario());                     // D
      getOrCreateCell(row, 4).setCellValue(item.clases());                      // E
      getOrCreateCell(row, 5).setCellValue(item.duracionClaseHoras().doubleValue()); // F
      getOrCreateCell(row, 6).setCellValue(item.cantidadTotalHoras().doubleValue()); // G
      getOrCreateCell(row, 7).setCellValue(item.honorarios().doubleValue());         // H

      Cell subtotalCell = getOrCreateCell(row, 8); // I
      subtotalCell.setCellFormula("G" + (rowIndex + 1) + "*H" + (rowIndex + 1));
    }
  }

  private void escribirTotal(Sheet sheet, List<ReporteHorasExcelRow> rows) {
    Cell totalCell = getOrCreateCell(sheet, TOTAL_ROW, 8); // I23

    if (rows.isEmpty()) {
      totalCell.setCellValue(0);
      return;
    }

    int startExcelRow = DETAIL_START_ROW + 1; // 13
    int endExcelRow = DETAIL_START_ROW + rows.size(); // variable

    totalCell.setCellFormula("SUM(I" + startExcelRow + ":I" + endExcelRow + ")");
  }

  private String capitalizarMes(java.time.YearMonth periodo) {
    String mes = periodo.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "AR"));
    return mes.substring(0, 1).toUpperCase(new Locale("es", "AR")) + mes.substring(1);
  }

  private Row getOrCreateRow(Sheet sheet, int rowIndex) {
    Row row = sheet.getRow(rowIndex);
    return row != null ? row : sheet.createRow(rowIndex);
  }

  private Cell getOrCreateCell(Sheet sheet, int rowIndex, int colIndex) {
    return getOrCreateCell(getOrCreateRow(sheet, rowIndex), colIndex);
  }

  private Cell getOrCreateCell(Row row, int colIndex) {
    Cell cell = row.getCell(colIndex);
    return cell != null ? cell : row.createCell(colIndex);
  }
}
