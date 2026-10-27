package com.supermercado.pos.ui;

import com.supermercado.pos.dominio.Pago;
import com.supermercado.pos.dominio.Venta;
import com.supermercado.pos.dominio.VentaDetalle;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class TicketDialog extends JDialog {

    public TicketDialog(Frame parent, Venta venta) {
        super(parent, "Ticket de Venta #" + venta.getId(), true);
        initUI(venta);
    }

    private void initUI(Venta v) {
        setSize(420, 520);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        JTextArea txtTicket = new JTextArea();
        txtTicket.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        txtTicket.setEditable(false);

        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("         SUPERMERCADO CENTRAL           \n");
        sb.append("=========================================\n");
        sb.append(String.format("Ticket Folio : #%d\n", v.getId()));
        sb.append(String.format("Fecha/Hora   : %s\n", v.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
        sb.append(String.format("Caja         : %d | Turno: #%d\n", v.getTurno().getCaja().getNumero(), v.getTurno().getId()));
        sb.append("-----------------------------------------\n");
        sb.append(String.format("%-18s %4s %8s %8s\n", "Producto", "Cant", "Precio", "Total"));
        sb.append("-----------------------------------------\n");

        for (VentaDetalle d : v.getDetalles()) {
            String nombre = d.getProducto().getNombre();
            if (nombre.length() > 18) nombre = nombre.substring(0, 15) + "...";
            sb.append(String.format("%-18s %4d %8.2f %8.2f\n",
                    nombre, d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal()));
        }

        sb.append("-----------------------------------------\n");
        sb.append(String.format("TOTAL A PAGAR:                Q %9.2f\n", v.getTotal()));
        sb.append("=========================================\n");
        sb.append("PAGOS REGISTRADOS:\n");
        for (Pago p : v.getPagos()) {
            sb.append(String.format(" - %-10s Aplicado: Q %.2f (Recibido: Q %.2f)\n",
                    p.getMetodo(), p.getMonto(), p.getRecibido()));
            if (p.getCambio().signum() > 0) {
                sb.append(String.format("   Cambio entregado: Q %.2f\n", p.getCambio()));
            }
        }
        sb.append("=========================================\n");
        sb.append("       ¡Gracias por su compra!          \n");

        txtTicket.setText(sb.toString());
        add(new JScrollPane(txtTicket), BorderLayout.CENTER);

        JButton btnCerrar = new JButton("Aceptar");
        btnCerrar.addActionListener(e -> dispose());
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlSur.add(btnCerrar);
        add(pnlSur, BorderLayout.SOUTH);
    }
}