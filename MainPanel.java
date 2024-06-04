// javac -d . MainPanel.java 
// java -cp . example.MainPanel

package example;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.PatternSyntaxException;
import javax.swing.*;
import javax.swing.table.*;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

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
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
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
      JTable table = (JTable) getInvoker();
      RowDataModel model = (RowDataModel) table.getModel();
      model.addRowData(new RowData("New row", ""));
      Rectangle r = table.getCellRect(model.getRowCount() - 1, 0, true);
      table.scrollRectToVisible(r);
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