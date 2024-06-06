//package example;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.regex.PatternSyntaxException;

public final class MainPanel extends JPanel {
  private final JTable table;
  private final RowDataModel model;
  private final JTextField searchField;

  private MainPanel() {
    super(new BorderLayout());
    model = new RowDataModel();
    table = new JTable(model) {
      private final Color evenColor = new Color(0xFA_FA_FA);
      @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
        Component c = super.prepareRenderer(tcr, row, column);
        if (isRowSelected(row)) {
          c.setForeground(getSelectionForeground());
          c.setBackground(getSelectionBackground());
        } else {
          c.setForeground(getForeground());
          c.setBackground(row % 2 == 0 ? evenColor : getBackground());
        }
        return c;
      }
    };

    searchField = new JTextField(20);
    searchField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        String text = searchField.getText();
        searchTable(text);
      }
    });

    TableColumn col = table.getColumnModel().getColumn(0);
    col.setMinWidth(60);
    col.setMaxWidth(60);
    col.setResizable(false);

    model.addRowData(new RowData("Name 1", "/Users/linchung/Downloads/image-downloader-0.1.2"));

    table.setAutoCreateRowSorter(true);
    table.setFillsViewportHeight(true);
    table.setComponentPopupMenu(new TablePopupMenu());

    JPanel searchPanel = new JPanel(new BorderLayout());
    searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
    searchPanel.add(searchField, BorderLayout.CENTER);

    add(searchPanel, BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);
    setPreferredSize(new Dimension(320, 240));
  }

  private void searchTable(String text) {
    TableRowSorter<? extends TableModel> sorter = (TableRowSorter<? extends TableModel>) table.getRowSorter();
    if (text.isEmpty()) {
      sorter.setRowFilter(null);
      table.clearSelection();
    } else {
      try {
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        if (sorter.getViewRowCount() > 0) {
          table.setRowSelectionInterval(0, 0);
          Rectangle rect = table.getCellRect(0, 0, true);
          table.scrollRectToVisible(rect);
        }
      } catch (PatternSyntaxException ex) {
        ex.printStackTrace();
      }
    }
  }

  public void ExportToCSVfile() {
    try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("table_data.csv"), "utf-8"))) {
      DefaultTableModel defaultTableModel = (DefaultTableModel) table.getModel();
      int Row = defaultTableModel.getRowCount();
      int Col = defaultTableModel.getColumnCount();

      StringBuffer bufferHeader = new StringBuffer();
      for (int j = 0; j < Col; j++) {
        bufferHeader.append(defaultTableModel.getColumnName(j));
        if (j != Col - 1) bufferHeader.append(", ");
      }
      writer.write(bufferHeader.toString() + "\r\n");

      for (int i = 0; i < Row; i++) {
        StringBuffer buffer = new StringBuffer();
        for (int j = 0; j < Col; j++) {
          buffer.append(defaultTableModel.getValueAt(i, j));
          if (j != Col - 1) buffer.append(", ");
        }
        writer.write(buffer.toString() + "\r\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void ImportFromCSVfile() {
    try (BufferedReader br = new BufferedReader(new FileReader("table_data.csv"))) {
      DefaultTableModel defaultTableModel = (DefaultTableModel) table.getModel();
      defaultTableModel.setRowCount(0); // Clear existing data
      String line;
      boolean isHeader = true;

      while ((line = br.readLine()) != null) {
        if (isHeader) {
          isHeader = false; // Skip header line
          continue;
        }
        String[] values = line.split(", ");
        defaultTableModel.addRow(values);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");

    JMenuItem exportItem = new JMenuItem("Export to CSV");
    exportItem.addActionListener(e -> ExportToCSVfile());
    fileMenu.add(exportItem);

    JMenuItem importItem = new JMenuItem("Import from CSV");
    importItem.addActionListener(e -> ImportFromCSVfile());
    fileMenu.add(importItem);

    menuBar.add(fileMenu);
    return menuBar;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
    }
    JFrame frame = new JFrame("files Notebook");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    MainPanel mainPanel = new MainPanel();
    frame.setJMenuBar(mainPanel.createMenuBar());
    frame.getContentPane().add(mainPanel);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class RowDataModel extends DefaultTableModel {
  private static final ColumnContext[] COLUMN_ARRAY = {
      new ColumnContext("No.", Integer.class, false),
      new ColumnContext("Name", String.class, true),
      new ColumnContext("Comment", String.class, true)
  };
  private int number;

  public void addRowData(RowData t) {
    Object[] obj = {number, t.getName(), t.getComment()};
    super.addRow(obj);
    number++;
  }

  @Override public boolean isCellEditable(int row, int col) {
    return COLUMN_ARRAY[col].isEditable;
  }

  @Override public Class<?> getColumnClass(int column) {
    return COLUMN_ARRAY[column].columnClass;
  }

  @Override public int getColumnCount() {
    return COLUMN_ARRAY.length;
  }

  @Override public String getColumnName(int column) {
    return COLUMN_ARRAY[column].columnName;
  }

  private static class ColumnContext {
    public final String columnName;
    public final Class<?> columnClass;
    public final boolean isEditable;

    protected ColumnContext(String columnName, Class<?> columnClass, boolean isEditable) {
      this.columnName = columnName;
      this.columnClass = columnClass;
      this.isEditable = isEditable;
    }
  }
}

class RowData {
  private final String name;
  private final String comment;

  protected RowData(String name, String comment) {
    this.name = name;
    this.comment = comment;
  }

  public String getName() {
    return name;
  }

  public String getComment() {
    return comment;
  }
}

final class TablePopupMenu extends JPopupMenu {
  private final JMenuItem delete;
  private final JMenuItem execute;

  TablePopupMenu() {
    super();
    add("add").addActionListener(e -> {
      /*JTable table = (JTable) getInvoker();
      RowDataModel model = (RowDataModel) table.getModel();
      model.addRowData(new RowData("New row", ""));
      Rectangle r = table.getCellRect(model.getRowCount() - 1, 0, true);
      table.scrollRectToVisible(r);
      */
      JTable table = (JTable) getInvoker();
      RowDataModel model = (RowDataModel) table.getModel();
    
      // Create a panel for the dialog
      JPanel panel = new JPanel(new BorderLayout(5, 5));
    
      // Name input field
      JTextField nameField = new JTextField(20);
      panel.add(new JLabel("Name: "), BorderLayout.WEST);
      panel.add(nameField, BorderLayout.CENTER);
    
      // File chooser
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      panel.add(fileChooser, BorderLayout.SOUTH);
    
      int result = JOptionPane.showConfirmDialog(null, panel, "Add Row", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    
      if (result == JOptionPane.OK_OPTION) {
        String name = nameField.getText();
        String comment = "";
        try {
          File selectedFile = fileChooser.getSelectedFile();
          if (selectedFile != null) {
            comment = selectedFile.getAbsolutePath();
          } else {
            throw new FileNotFoundException("No file selected.");
          }
          model.addRowData(new RowData(name, comment));
          Rectangle r = table.getCellRect(model.getRowCount() - 1, 0, true);
          table.scrollRectToVisible(r);
        } catch (FileNotFoundException ex) {
          ex.printStackTrace();
          JOptionPane.showMessageDialog(null, "File not found: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    addSeparator();
    delete = add("delete");
    delete.addActionListener(e -> {
      JTable table = (JTable) getInvoker();
      DefaultTableModel model = (DefaultTableModel) table.getModel();
      int[] selection = table.getSelectedRows();
      for (int i = selection.length - 1; i >= 0; i--) {
        model.removeRow(table.convertRowIndexToModel(selection[i]));
      }
    });
    addSeparator();
    execute = add("execute");
    execute.addActionListener(e -> {
      JTable table = (JTable) getInvoker();
      DefaultTableModel model = (DefaultTableModel) table.getModel();
      int[] selection = table.getSelectedRows();
      for (int i = selection.length - 1; i >= 0; i--) {
        int modelRow = table.convertRowIndexToModel(selection[i]);
        String name = (String) model.getValueAt(modelRow, 1);
        String comment = (String) model.getValueAt(modelRow, 2);
        System.out.println("Name: " + name + ", Comment: " + comment);
        try {
          StringBuilder sb = new StringBuilder("open ");
          sb.append(comment);
          String s = sb.toString();
          Process process = Runtime.getRuntime().exec(s);

          process.getOutputStream().close();

          Thread stdoutReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
              String line;
              while ((line = reader.readLine()) != null) {
                System.out.println(line);
              }
              process.getInputStream().close();
            } catch (IOException excp) {
              System.err.println("Error reading stdout: " + excp.getMessage());
              excp.printStackTrace();
            }
          });

          Thread stderrReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
              String line;
              while ((line = reader.readLine()) != null) {
                System.err.println(line);
              }
              process.getErrorStream().close();
            } catch (IOException excp) {
              System.err.println("Error reading stderr: " + excp.getMessage());
              excp.printStackTrace();
            }
          });

          stdoutReader.start();
          stderrReader.start();

          int exitCode = process.waitFor();
          System.out.println("Process exited with code: " + exitCode);

          stdoutReader.join();
          stderrReader.join();

        } catch (IOException excp) {
          System.err.println("Error executing command: " + excp.getMessage());
          excp.printStackTrace();
        } catch (InterruptedException excp) {
          System.err.println("Thread interrupted: " + excp.getMessage());
          excp.printStackTrace();
        }
      }
    });
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTable) {
      delete.setEnabled(((JTable) c).getSelectedRowCount() > 0);
      super.show(c, x, y);
    }
  }
}
