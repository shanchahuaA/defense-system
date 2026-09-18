package com.defense3.demo.utils;

import org.apache.poi.xwpf.usermodel.*;

import java.io.OutputStream;

public class TemplateGenerator {

    public static void createStandardTemplate(OutputStream out) throws Exception {
        try (XWPFDocument doc = new XWPFDocument()) {
            // 1. 标题
            XWPFParagraph titlePara = doc.createParagraph();
            titlePara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText("福建农林大学本科毕业论文答辩成绩表（可以转为 EXCEL 表）");
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            titleRun.setFontFamily("宋体");

            // 2. 头部信息（系别、组号、日期、地点）
            XWPFParagraph headerPara = doc.createParagraph();
            headerPara.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun headerRun = headerPara.createRun();
            headerRun.setText(
                    "系别： {{deptName}}    第 {{groupIndex}} 小组    {{year}} 年 {{month}} 月 {{day}} 日    答辩地点： {{location}}");
            headerRun.setFontFamily("宋体");
            headerRun.setFontSize(12);

            // 3. 表格 (2行，7列)
            XWPFTable table = doc.createTable(2, 7);

            // 表头
            setCellText(table.getRow(0).getCell(0), "学号");
            setCellText(table.getRow(0).getCell(1), "姓名");
            setCellText(table.getRow(0).getCell(2), "题目");
            setCellText(table.getRow(0).getCell(3), "评价指标(45分)");
            setCellText(table.getRow(0).getCell(4), "答辩自述(25分)");
            setCellText(table.getRow(0).getCell(5), "问答情况(30分)");
            setCellText(table.getRow(0).getCell(6), "总分");

            // 数据行 (Row 1) - 关键：使用正确的 {{#students}} 和 {{/students}}
            XWPFTableRow dataRow = table.getRow(1);

            // Cell 0: {{#students}}{{studentNo}}
            XWPFParagraph cell0Para = dataRow.getCell(0).getParagraphs().get(0);
            clearRuns(cell0Para);

            XWPFRun startTagRun = cell0Para.createRun();
            startTagRun.setText("{{#students}}");
            XWPFRun snoRun = cell0Para.createRun();
            snoRun.setText("{{studentNo}}");

            setCellText(dataRow.getCell(1), "{{name}}");
            setCellText(dataRow.getCell(2), "{{thesisTitle}}");
            setCellText(dataRow.getCell(3), "{{score1}}");
            setCellText(dataRow.getCell(4), "{{score2}}");
            setCellText(dataRow.getCell(5), "{{score3}}");

            // Cell 6: {{totalScore}}{{/students}}
            XWPFParagraph cell6Para = dataRow.getCell(6).getParagraphs().get(0);
            clearRuns(cell6Para);

            XWPFRun totalRun = cell6Para.createRun();
            totalRun.setText("{{totalScore}}");
            XWPFRun endTagRun = cell6Para.createRun();
            endTagRun.setText("{{/students}}");

            // 4. 底部签名
            XWPFParagraph footerPara = doc.createParagraph();
            footerPara.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun footerRun = footerPara.createRun();
            footerRun.setText("评委签名： {{judgeName}}");
            footerRun.setFontFamily("宋体");
            footerRun.setFontSize(12);

            doc.write(out);
        }
    }

    private static void setCellText(XWPFTableCell cell, String text) {
        if (!cell.getParagraphs().isEmpty()) {
            XWPFParagraph p = cell.getParagraphs().get(0);
            clearRuns(p);
            p.createRun().setText(text);
        } else {
            cell.addParagraph().createRun().setText(text);
        }
    }

    private static void clearRuns(XWPFParagraph p) {
        for (int i = p.getRuns().size() - 1; i >= 0; i--) {
            p.removeRun(i);
        }
    }
}
