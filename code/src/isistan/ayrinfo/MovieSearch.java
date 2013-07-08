/*
 * Class MovieSearch
 * Created on 01/03/2013
 */
package isistan.ayrinfo;

import java.awt.EventQueue;

import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.JTextField;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;
import javax.swing.ButtonGroup;
import javax.swing.JScrollPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;
import java.awt.Toolkit;
import javax.swing.JTextArea;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MovieSearch {

    private JFrame frmMovieSearch;
    private JFrame frmShowMovie;
    private JTextField indexPathToIndex;
    private JTextField docsPathToIndex;
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private JTextField indexPathToSearch;
    private JTextField queryToSearch;
    private JRadioButton createNewButton;
    private JRadioButton updateIndexButton;
    private JComboBox searchFieldComboBox;
    JTextArea indexLog;
    IndexFiles index;
    SearchFiles searcher;
    String lastDir = System.getProperty("user.dir");
    private JTable table;
    JLabel statusLabel;
    JEditorPane jEditorPane;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MovieSearch window = new MovieSearch();
                    window.frmMovieSearch.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public MovieSearch() {
        initialize();
        index = new IndexFiles(this);
        searcher = new SearchFiles(this);
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frmMovieSearch = new JFrame();
        frmMovieSearch.setIconImage(Toolkit.getDefaultToolkit().getImage(MovieSearch.class.getResource("search.png")));
        frmMovieSearch.setTitle("Movie Search");
        frmMovieSearch.setBounds(100, 100, 560, 400);
        frmMovieSearch.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        frmMovieSearch.getContentPane().add(tabbedPane, BorderLayout.CENTER);
        
        JPanel indexPanel = new JPanel();
        tabbedPane.addTab("", new ImageIcon(MovieSearch.class.getResource("archive-folder-index.png")), indexPanel, "Index");
        indexPanel.setLayout(new FormLayout(new ColumnSpec[] {
                FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC,
                ColumnSpec.decode("6dlu"),
                ColumnSpec.decode("default:grow"),
                FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC,
                FormFactory.RELATED_GAP_COLSPEC,},
            new RowSpec[] {
                FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                RowSpec.decode("10dlu"),
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC,
                RowSpec.decode("default:grow"),
                FormFactory.RELATED_GAP_ROWSPEC,}));
        
        JLabel lblIndexPath = new JLabel("Carpeta del índice: ");
        indexPanel.add(lblIndexPath, "2, 2, right, default");
        
        indexPathToIndex = new JTextField();
        indexPathToIndex.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                indexPathToIndex.setText(selectDirectoryDialog());
            }
        });
        indexPathToIndex.setEditable(false);
        indexPanel.add(indexPathToIndex, "4, 2, fill, default");
        indexPathToIndex.setColumns(10);
        
        JButton btnSelect = new JButton("Seleccionar...");
        btnSelect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                indexPathToIndex.setText(selectDirectoryDialog());
            }
        });
        indexPanel.add(btnSelect, "6, 2");
        
        JLabel lblDocumentsPath = new JLabel("Carpeta de documentos: ");
        indexPanel.add(lblDocumentsPath, "2, 4, right, default");
        
        docsPathToIndex = new JTextField();
        docsPathToIndex.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                docsPathToIndex.setText(selectDirectoryDialog());
            }
        });
        docsPathToIndex.setEditable(false);
        indexPanel.add(docsPathToIndex, "4, 4, fill, default");
        docsPathToIndex.setColumns(10);
        
        JButton btnSelect_1 = new JButton("Seleccionar...");
        btnSelect_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                docsPathToIndex.setText(selectDirectoryDialog());
            }
        });
        indexPanel.add(btnSelect_1, "6, 4");
        
        JPanel panel = new JPanel();
        indexPanel.add(panel, "4, 6, fill, fill");
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        createNewButton = new JRadioButton("Crear un nuevo índice");
        buttonGroup.add(createNewButton);
        panel.add(createNewButton);
        
        updateIndexButton = new JRadioButton("Actualizar un índice (si existe)");
        buttonGroup.add(updateIndexButton);
        panel.add(updateIndexButton);
        
        JButton btnStartIndexing = new JButton("¡Comenzar a indexar!");
        btnStartIndexing.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showIndexingConfirmDialog();
            }
        });
        indexPanel.add(btnStartIndexing, "4, 8, center, top");
        
        JScrollPane scrollPane_1 = new JScrollPane();
        indexPanel.add(scrollPane_1, "2, 10, 5, 1, fill, fill");
        
        indexLog = new JTextArea();
        indexLog.setEditable(false);
        scrollPane_1.setViewportView(indexLog);
        
        JPanel searchPanel = new JPanel();
        tabbedPane.addTab("", new ImageIcon(MovieSearch.class.getResource("/isistan/ayrinfo/search.png")), searchPanel, "Search");
        searchPanel.setLayout(new FormLayout(new ColumnSpec[] {
                FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC,
                FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"),
                FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC,
                FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC,
                FormFactory.RELATED_GAP_COLSPEC,},
            new RowSpec[] {
                FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC,
                RowSpec.decode("default:grow"),
                FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.UNRELATED_GAP_ROWSPEC,}));
        
        JLabel lblIndexPath_1 = new JLabel("Carpeta del índice:");
        searchPanel.add(lblIndexPath_1, "2, 2, right, default");
        
        indexPathToSearch = new JTextField();
        indexPathToSearch.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                indexPathToSearch.setText(selectDirectoryDialog());
            }
        });
        indexPathToSearch.setEditable(false);
        searchPanel.add(indexPathToSearch, "4, 2, 3, 1, fill, default");
        indexPathToSearch.setColumns(10);
        
        JButton btnSelect_2 = new JButton("Seleccionar...");
        btnSelect_2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                indexPathToSearch.setText(selectDirectoryDialog());
            }
        });
        searchPanel.add(btnSelect_2, "8, 2");
        
        JLabel lblQuery = new JLabel("Búsqueda:");
        searchPanel.add(lblQuery, "2, 4, right, default");
        
        JPanel panel_1 = new JPanel();
        searchPanel.add(panel_1, "4, 4, fill, fill");
        panel_1.setLayout(new FormLayout(new ColumnSpec[] {
                ColumnSpec.decode("default:grow"),},
            new RowSpec[] {
                FormFactory.DEFAULT_ROWSPEC,}));
        
        queryToSearch = new JTextField();
        queryToSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showSarchingConfirmDialog();
            }
        });
        panel_1.add(queryToSearch, "1, 1, fill, default");
        queryToSearch.setColumns(10);
        
        searchFieldComboBox = new JComboBox();
        searchFieldComboBox.setModel(new DefaultComboBoxModel(new String[] {"contents", "path"}));
        searchPanel.add(searchFieldComboBox, "6, 4, fill, default");
        
        JButton btnSearch = new JButton("Buscar");
        btnSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showSarchingConfirmDialog();
            }
        });
        searchPanel.add(btnSearch, "8, 4");
        
        JScrollPane scrollPane = new JScrollPane();
        searchPanel.add(scrollPane, "2, 8, 7, 1, fill, fill");
        
        table = new JTable() {
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;   //Disallow the editing of any cell
            }
        };
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable target = (JTable) e.getSource();
                    int row = target.getSelectedRow();
                    //int column = target.getSelectedColumn();
                    displayMovie(((DefaultTableModel) target.getModel()).getValueAt(row, 1).toString());
                    }
            }
        });
        table.setModel(new DefaultTableModel(
            new Object[][] {
            },
            new String[] {
                "Score", "Path"
            }
        ));
        TableColumn column = null;
        for (int i = 0; i < 2; i++) {
            column = table.getColumnModel().getColumn(i);
            if (i == 1) {
                column.setPreferredWidth(10); //third column is bigger
            } else {
                column.setPreferredWidth(3);
            }
        }
        scrollPane.setViewportView(table);
        
        statusLabel = new JLabel("");
        searchPanel.add(statusLabel, "2, 10, 7, 1");
        
        jEditorPane = new JEditorPane();
        jEditorPane.setEditable(false);

        //HTMLEditorKit kit = new HTMLEditorKit();
        //jEditorPane.setEditorKit(kit);

        JScrollPane htmlScrollPane = new JScrollPane(jEditorPane);
        
        frmShowMovie = new JFrame();
        frmShowMovie.setSize(500, 700);
        frmShowMovie.getContentPane().add(htmlScrollPane, BorderLayout.CENTER);
        
    }
 

    protected void displayMovie(String fileName) {
        
        File file = new File(fileName);
        String absolutePath = file.getAbsolutePath();
        String url = "file:///" + absolutePath;

        try {
          // Si no cambió la página, no la recargo
          URL newURL = new URL(url);
          URL loadedURL = jEditorPane.getPage();
          if (loadedURL != null && loadedURL.sameFile(newURL)) {
            return;
          }
          
        jEditorPane.setPage(url);
        frmShowMovie.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(jEditorPane, new String[] {
                "Unable to open file", url }, "File Open Error",
                JOptionPane.ERROR_MESSAGE);
            
          }
        
    }

    protected String selectDirectoryDialog() {
        JFileChooser fc = new JFileChooser(lastDir);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showOpenDialog(frmMovieSearch);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            lastDir = file.getPath();
            return file.getPath();            
        } else 
            return null;
        
    }
    
protected void showIndexingConfirmDialog() {
        
        if (indexPathToIndex.getText().equals("")) {
            JOptionPane.showMessageDialog(frmMovieSearch,
                    "Por favor, seleccione la carpeta del índice",
                    "Índice no seleccionado",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (docsPathToIndex.getText().equals("")){    
            JOptionPane.showMessageDialog(frmMovieSearch,
                    "Por favor, seleccione la carpeta a indexar",
                    "Carpeta de documentos no seleccionada",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!createNewButton.isSelected() && !updateIndexButton.isSelected()) {
            JOptionPane.showMessageDialog(frmMovieSearch,
                    "Por favor, seleccione si desea crear un nuevo índice\n"
                    +"o actualizar un índice existente (en caso de que exista)",
                    "¿Crear o actualizar?",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int selectedOption = JOptionPane.CANCEL_OPTION;
        if (createNewButton.isSelected())
            
            selectedOption = JOptionPane.showConfirmDialog(
                    frmMovieSearch,
                    "Desea crear un nuevo índice en\n"
                    + indexPathToIndex.getText()
                    +"\ny agregar todos los documentos de"
                    + docsPathToIndex.getText()
                    + "?",
                    "Comenzar a indexar",
                    JOptionPane.YES_NO_OPTION);
        
        else 
            
            selectedOption = JOptionPane.showConfirmDialog(
                    frmMovieSearch,
                    "Desea actualizar el índice en\n"
                    + indexPathToIndex.getText()
                    +"\ny agregar todos los documentos de"
                    + docsPathToIndex.getText()
                    + "?",
                    "Comenzar a indexar",
                    JOptionPane.YES_NO_OPTION);
        
        if (selectedOption == JOptionPane.YES_OPTION) {
            
            indexLog.setText("Comenzando a indexar");
            index.index(docsPathToIndex.getText(), indexPathToIndex.getText(), createNewButton.isSelected());
        }
        return;

    }

    protected void showSarchingConfirmDialog() {
    
        if (indexPathToSearch.getText().equals("")) {
            JOptionPane.showMessageDialog(frmMovieSearch,
                    "Por favor, seleccione la carpeta del índice donde desea realizar las búsquedas",
                    "Índice no seleccionado",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        

        if (queryToSearch.getText().equals("")) {
            JOptionPane.showMessageDialog(frmMovieSearch,
                    "Por favor, ingrese la consulta que desea realizar",
                    "Consulta vacía",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        searcher.search(indexPathToSearch.getText(), queryToSearch.getText(), searchFieldComboBox.getSelectedItem().toString());
        
        return;
    
    }
    
    public void indexLogAppend(String textToLog){
        indexLog.append("\n"+textToLog);
    }
    
    public void newResult(double score, String path){
        ((DefaultTableModel) table.getModel()).addRow(new Object[] {score,path});
    }
    
    public void setStatus(String status){
        statusLabel.setText(status);
    }

    public void clearResults() {
        DefaultTableModel dm = (DefaultTableModel) table.getModel();
        for (int i = dm.getRowCount() - 1; i >= 0; i--) {
            dm.removeRow(i);
        }
        
    }

}
