/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service; // O el paquete donde hayas creado la clase

import Models.Compra;
import java.io.File;
import java.io.IOException;
import java.awt.Desktop;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import javax.swing.JOptionPane;

public class GeneradorFacturaPDF {

    public void generar(Compra compra) {
        String nombreArchivo = "Factura_" + compra.getId() + ".pdf";

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4); // Usamos un tamaño de página estándar (A4)
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                
                // --- 1. ENCABEZADO ---
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
                contentStream.newLineAtOffset(220, 780); // Centrado aproximado
                contentStream.showText("FACTURA DE COMPRA");
                contentStream.endText();

                // --- 2. DATOS DE LA EMPRESA Y FACTURA (alineados a la izquierda y derecha) ---
                // Lado izquierdo: Tu empresa (ejemplo)
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 720);
                contentStream.showText("Mi Tienda S.A.");
                contentStream.newLineAtOffset(0, -15); // Bajar 15 puntos para la siguiente línea
                contentStream.showText("NIT: 1234567-8");
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("Tel: 5555-4444");
                contentStream.endText();

                // Lado derecho: Datos de la factura
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(400, 720);
                contentStream.showText("Factura No: " + compra.getId());
                contentStream.newLineAtOffset(0, -15);
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.showText("Fecha: " + compra.getFecha().toString());
                contentStream.endText();

                // --- 3. DATOS DEL CLIENTE ---
                // Línea divisora
                contentStream.setStrokingColor(java.awt.Color.GRAY);
                contentStream.moveTo(50, 680);
                contentStream.lineTo(550, 680);
                contentStream.stroke();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(50, 660);
                contentStream.showText("Cliente:");
                contentStream.newLineAtOffset(0, -15);
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.showText(compra.getNombreCliente()); // Nombre del cliente
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("NIT/ID: " + compra.getIdCliente()); // ID del cliente
                contentStream.endText();

                contentStream.moveTo(50, 620);
                contentStream.lineTo(550, 620);
                contentStream.stroke();

                // --- 4. TABLA DE PRODUCTOS (con columnas definidas) ---
                float yPosition = 600;
                // Encabezados de la tabla
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(60, yPosition);
                contentStream.showText("Producto");
                contentStream.newLineAtOffset(300, 0); // Posición X para Cantidad
                contentStream.showText("Cantidad");
                contentStream.newLineAtOffset(100, 0); // Posición X para Total
                contentStream.showText("Total");
                contentStream.endText();
                
                yPosition -= 15; // Bajar para el contenido

                // Contenido de la tabla
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(60, yPosition);
                contentStream.showText(compra.getNombreProducto());
                contentStream.newLineAtOffset(300, 0);
                contentStream.showText(String.valueOf(compra.getCantidad()));
                contentStream.newLineAtOffset(100, 0);
                contentStream.showText("Q " + compra.getTotalCompra().toString());
                contentStream.endText();
                
                // --- 5. TOTALES Y PIE DE PÁGINA ---
                yPosition = 120; // Posición para la línea final
                contentStream.moveTo(50, yPosition);
                contentStream.lineTo(550, yPosition);
                contentStream.stroke();
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.newLineAtOffset(350, yPosition - 30);
                contentStream.showText("TOTAL A PAGAR: Q " + compra.getTotalCompra().toString());
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
                contentStream.newLineAtOffset(240, 50);
                contentStream.showText("Gracias por su compra");
                contentStream.endText();

            } // El contentStream se cierra automáticamente

            document.save(nombreArchivo);
            
            // Abrir el archivo PDF generado
            File archivoPDF = new File(nombreArchivo);
            if (Desktop.isDesktopSupported() && archivoPDF.exists()) {
                Desktop.getDesktop().open(archivoPDF);
            } else {
                 JOptionPane.showMessageDialog(null, "La apertura automática de archivos no es soportada o el archivo no se encontró.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al generar o abrir el PDF: " + e.getMessage(), "Error de PDF", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}