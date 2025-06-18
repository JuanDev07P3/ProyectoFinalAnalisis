/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

/**
 *
 * @author leonn
 */
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class LowStockRenderer extends DefaultTableCellRenderer {

    private static final int UMBRAL_STOCK_BAJO = 5; 
    private final int COLUMNA_STOCK;

    /**
     * Constructor que recibe el índice de la columna de Stock.
     */
    public LowStockRenderer(int columnaStock) {
        this.COLUMNA_STOCK = columnaStock;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                 boolean isSelected, boolean hasFocus,
                                                 int row, int column) {

       
        Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        try {

            int stock = Integer.parseInt(table.getValueAt(row, COLUMNA_STOCK).toString());

   
            if (stock < UMBRAL_STOCK_BAJO) {
    
                cellComponent.setBackground(new Color(255, 200, 200)); 
                cellComponent.setForeground(Color.BLACK); // Letra negra para contraste
            } else {
        
                cellComponent.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                cellComponent.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            }

        } catch (NumberFormatException e) {
   
            cellComponent.setBackground(table.getBackground());
            cellComponent.setForeground(table.getForeground());
        }

        return cellComponent;
    }
}