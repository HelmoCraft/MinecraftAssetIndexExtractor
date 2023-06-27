package dev.helmocraft.mcassetindexextractor.gui;

import dev.helmocraft.mcassetindexextractor.main.Index;
import dev.helmocraft.mcassetindexextractor.util.MarkedAssets;
import dev.helmocraft.mcassetindexextractor.util.PathPreset;
import dev.helmocraft.mcassetindexextractor.util.PathPresets;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.Objects;

public class MainWindow extends JFrame implements ActionListener, DocumentListener {

    private static boolean debugEnabled = false;

    private static final MainWindow instance = new MainWindow();
    private final Index index;
    private final MarkedAssets markedAssets;

    public static MainWindow getInstance() {return instance;}

    // Calling the getInstance() method once will initialize everything.
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void initialize(boolean debugEnabled) {
        MainWindow.debugEnabled = debugEnabled;
        if(debugEnabled) System.out.println("Debug messages enabled.");
        getInstance();
    }

    private final Dimension windowSize = new Dimension(900, 540);

    // Items of a menu
    private final JMenuItem[] indexMenuItems = new JMenuItem[3];
    private final JMenuItem[] fileMenuItems = new JMenuItem[2];

    //private JFileChooser fileChooser;
    public static final FileNameExtensionFilter jsonExtensionFilter = new FileNameExtensionFilter("JSON Files", "json");

    private String[] assetSearchResults = new String[]{""};
    private final JList<String> assetSearchResultList;


    public MainWindow() {
        index = new Index();
        markedAssets = new MarkedAssets();

        initializeWindow();
        Dimension shortFitToWidth = new Dimension(this.getWidth() - 10, 30);
        initializeMenus();
        initializeFileChooser();

        JPanel mainPanel = new JPanel();

        JLabel searchInfoLabel = new JLabel("Search for assets by path...");
        searchInfoLabel.setFont(searchInfoLabel.getFont().deriveFont(Font.ITALIC, 12));
        searchInfoLabel.setPreferredSize(new Dimension((int) shortFitToWidth.getWidth(), 20));

        JTextField assetSearchInput = new JTextField();
        assetSearchInput.getDocument().addDocumentListener(this);
        assetSearchInput.setPreferredSize(shortFitToWidth);

        assetSearchResultList = new JList<>(new DefaultListModel<>());
        assetSearchResultList.setListData(assetSearchResults);
        JScrollPane scrollableList = new JScrollPane(assetSearchResultList);
        scrollableList.setPreferredSize(new Dimension((int) shortFitToWidth.getWidth(), (this.getHeight() - 130)));

        mainPanel.add(searchInfoLabel);
        mainPanel.add(assetSearchInput); mainPanel.add(scrollableList);

        setContentPane(mainPanel);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeMenus() {
        // The menu bar
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenuItems[0] = new JMenuItem("Save resources as...");
        fileMenuItems[1] = new JMenuItem("Exit");
        for(JMenuItem item : fileMenuItems) {
            fileMenu.add(item);
            item.addActionListener(this);
        }

        JMenu indexMenu = new JMenu("Index");
        indexMenuItems[0] = new PathPreset("Load...", new File(System.getProperty("user.home")));
        indexMenuItems[0].addActionListener(PathPresets.getInstance());
        indexMenuItems[1] = PathPresets.getInstance().getLoadFromMenu();

        indexMenuItems[2] = new JMenuItem("Reload");
        indexMenuItems[2].addActionListener(this);
        for(JMenuItem item : indexMenuItems) {
            indexMenu.add(item);
        }

        menuBar.add(fileMenu);
        menuBar.add(indexMenu);
        setJMenuBar(menuBar);
    }

    private void initializeWindow() {
        // NOTE:  getResourceAsStream only works if the path starts with a / apparently...
        ImageIcon image;
        try {
            image = new ImageIcon(ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png"))));
            setIconImage(image.getImage());
        } catch (Exception e) {
            System.err.println("Unable to read icon! Continuing with default icon.");
            e.printStackTrace();
        }

        setTitle("Minecraft Asset Index Extractor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize((int) windowSize.getWidth(), (int) windowSize.getHeight());


    }

    private void initializeFileChooser() {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if(source.equals(fileMenuItems[1])) { // Exit
            System.exit(0);
        }
        else if(source.equals(fileMenuItems[0])) { // Save resources
            if(!assetSearchResultList.getSelectedValuesList().isEmpty()) {
                List<String> selected = assetSearchResultList.getSelectedValuesList();

                JFileChooser fileChooser =  new JFileChooser();
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                fileChooser.setFileHidingEnabled(false);

                if(selected.size() == 1) {
                    String assetPath = selected.get(0);
                    String assetFileName = Index.fileNameOfAsset(assetPath);

                    fileChooser.setDialogTitle("Save asset");
                    fileChooser.setApproveButtonText("Save");

                    fileChooser.setSelectedFile(new File(fileChooser.getCurrentDirectory() + "/" + assetFileName));

                    // Get the file extension
                    try {
                        String fileExtension = assetPath.split("\\.")[1];
                        FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter(
                                fileExtension.toUpperCase() + " Files", fileExtension);
                        fileChooser.setFileFilter(extensionFilter);
                    } catch (Exception x) {
                        showErrorMessage("Unable to detect extension", "There was an error getting the file" +
                                " extension for the asses.");
                    }

                    int selection = fileChooser.showOpenDialog(this);
                    if(selection == JFileChooser.APPROVE_OPTION) {
                        // Save the file
                        if(fileChooser.getSelectedFile() != null) {
                            MainWindow.getInstance().getIndex().saveAsset(assetPath, fileChooser.getSelectedFile());
                        }
                    }
                }
                else {
                    fileChooser.setDialogTitle("Select folder for saving");
                    fileChooser.setApproveButtonText("Select");
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                    int selection = fileChooser.showOpenDialog(this);
                    File selectedFolder = fileChooser.getSelectedFile();
                    if(selection == JFileChooser.APPROVE_OPTION && selectedFolder != null) {
                        if(selectedFolder.exists() && selectedFolder.isDirectory()) {
                            selected.forEach(asset -> MainWindow.getInstance().getIndex().saveAsset(asset, new File(selectedFolder.getAbsolutePath() + "/" + asset.replace("/", "_"))));
                        }
                    }
                }
            }
        }
        else if(source.equals(indexMenuItems[2])) { // Reload
            MainWindow.getInstance().getIndex().reload();
        }
    }

    public File showFileChooser(File startingPath, FileNameExtensionFilter[] filters) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileHidingEnabled(false);
        fileChooser.setFileFilter(jsonExtensionFilter);
        fileChooser.addChoosableFileFilter(jsonExtensionFilter);

        //noinspection ReplaceNullCheck
        if(startingPath == null) {
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        } else {
            fileChooser.setCurrentDirectory(startingPath);
        }

        fileChooser.setDialogTitle("Open index");
        fileChooser.setApproveButtonText("Open");
        fileChooser.resetChoosableFileFilters();
        for(FileNameExtensionFilter filter : filters) {
            fileChooser.addChoosableFileFilter(filter);
        }

        int selection = fileChooser.showOpenDialog(this);
        if(selection == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }

        return null;
    }

    public void showErrorMessage(String title, String message) {
        JOptionPane.showMessageDialog(this, message,
                title, JOptionPane.ERROR_MESSAGE);
    }

    public void setAssetSearchResults(String[] assetSearchResults) {
        this.assetSearchResults = assetSearchResults;
        assetSearchResultList.setListData(assetSearchResults);

    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        searchAssetName(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        searchAssetName(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    private void searchAssetName(DocumentEvent event) {
        try {
            String query = event.getDocument().getText(0, event.getDocument().getLength());
            assetSearchResultList.setListData(MainWindow.getInstance().getIndex().search(query));
        } catch (BadLocationException e) {
            assetSearchResults = new String[]{"(There was an error searching)"};
        }
    }

    public Index getIndex() {
        return index;
    }

    public static void printDebug(String message) {
        if(debugEnabled) System.out.println(message);
    }
}
