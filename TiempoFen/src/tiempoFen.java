import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class tiempoFen extends JFrame {
	private JTable table;
	private DefaultTableModel model;
	private Font font;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		tiempoFen frame = new tiempoFen();
		frame.setVisible(true);
	}

	/**
	 * Create the frame.
	 */
	public tiempoFen() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(tiempoFen.class.getResource("/javax/swing/plaf/metal/icons/ocean/warning.png")));
		setBackground(Color.WHITE);
		setTitle("TFEN v1.0");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1100, 490);
		getContentPane().setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 1064, 430);
		getContentPane().add(scrollPane);

		table = new JTable(new DefaultTableModel(new Object[] { "ID", "Duración Evento", "Fen", "Hora Inicio", "Fecha Inicio", "Evento/Falla", "Servicio" }, 0));
		model = (DefaultTableModel) table.getModel();

		miRender ft = new miRender(5);
		table.setDefaultRenderer(Object.class, ft);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableColumnModel colmodel = table.getColumnModel();

		colmodel.removeColumn(colmodel.getColumn(0)); // remover el ID (desplaza el indice de las columnas)

		try {
			InputStream is = tiempoFen.class.getResourceAsStream("DS-DIGII.TTF");
			font = Font.createFont(Font.TRUETYPE_FONT, is);
			font = font.deriveFont(Font.PLAIN, 20);
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(font);
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}

		miCellRender renderer = new miCellRender(font);
		table.getColumnModel().getColumn(0).setCellRenderer(renderer);

		colmodel.getColumn(0).setPreferredWidth(100);
		colmodel.getColumn(1).setPreferredWidth(68);
		colmodel.getColumn(2).setPreferredWidth(88);
		colmodel.getColumn(3).setPreferredWidth(80);
		colmodel.getColumn(4).setPreferredWidth(655);
		colmodel.getColumn(5).setPreferredWidth(70);
		scrollPane.setViewportView(table);

		conectar();
		hardWorkThread worker = new hardWorkThread();
		worker.execute();

	}

	public void conectar() {
		try {
			Connection connect = DriverManager.getConnection("jdbc:mysql://172.17.37.79:3306/fens?zeroDateTimeBehavior=convertToNull", "test", "test"); // 172.17.37.79/ 
			Statement sttmnt = connect.createStatement();
			ResultSet result = null;
			result = sttmnt.executeQuery("SELECT * FROM tiempofen");
			while (result.next()) {
				String detalle = result.getString(7);
				byte[] utf8 = detalle.getBytes("cp1252");
				detalle = new String(utf8, "UTF-8");

				model.addRow(new Object[] { result.getInt(1), "----", result.getInt(2), result.getTime(3), result.getDate(4), detalle, result.getString(8) });

			}
			result.close();
			sttmnt.close();
			connect.close();
		} catch (SQLException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	public class hardWorkThread extends SwingWorker<String, String> {
		protected String doInBackground() throws InterruptedException, ParseException {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			long lastTime = System.currentTimeMillis();
			while (true) {
				if (System.currentTimeMillis() - lastTime > (1000 * 30)) {
					model.setRowCount(0);
					conectar();
					lastTime = System.currentTimeMillis();
				}

				for (int i = 0; i < model.getRowCount(); i++) {
					Date inicioTime = formatter.parse(model.getValueAt(i, 4).toString() + " " + model.getValueAt(i, 3).toString());
					long millis = (System.currentTimeMillis() - inicioTime.getTime()); // Horario normal GMT-4 ahora // - (1 * 60 * 60 * 1000); // menos 1 hora por la zona horaria mal puesta!
					String elapsedtime = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis), TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
					model.setValueAt(elapsedtime, i, 1);
				}
			}
		}
	}

	public class miRender extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;
		private int columna_patron;

		public miRender(int Colpatron) {
			this.columna_patron = Colpatron;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
			if (model.getRowCount() > 0) {
				setBackground(Color.white);//color de fondo
				setForeground(Color.black);//color de texto

				if (table.getValueAt(row, columna_patron).toString().toLowerCase().contains("internet")) {
					setForeground(Color.black);
					setBackground(Color.yellow);
				}
				if (table.getValueAt(row, columna_patron).toString().toLowerCase().contains("telefonia")) {
					setForeground(Color.black);
					setBackground(Color.orange);
				}
				if (table.getValueAt(row, columna_patron).toString().toLowerCase().contains("television")) {
					setForeground(Color.black);
					setBackground(Color.CYAN);
				}
				if (table.getValueAt(row, columna_patron).toString().toLowerCase().contains("hfc")) {
					setForeground(Color.black);
					setBackground(Color.green);
				}
				if (table.getValueAt(row, columna_patron).toString().contains("Tv+Int")) {
					setForeground(Color.white);
					setBackground(Color.darkGray);
				}
				if (table.getValueAt(row, columna_patron).toString().contains("Tv+Int+Tel")) {
					setForeground(Color.white);
					setBackground(Color.red);
				}
				if (table.getValueAt(row, columna_patron).toString().contains("Int+Telef")) {
					setForeground(Color.black);
					setBackground(Color.LIGHT_GRAY);
				}
			}
			super.getTableCellRendererComponent(table, value, selected, focused, row, column);
			return this;
		}

	}

	public class miCellRender extends JLabel implements TableCellRenderer {
		private Font fuente;

		public miCellRender(Font font) {
			this.fuente = font;
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (model.getRowCount() > 0) {
				setFont(fuente);
				setHorizontalAlignment(JLabel.CENTER);
				setForeground(Color.RED);
				setText(String.valueOf(value));
			}
			return this;
		}
	}
}
