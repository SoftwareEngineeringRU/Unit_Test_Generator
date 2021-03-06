package GraphicalUserInterface;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The Preferences Frame class incorporates a stretch goal feature that enables a user to modify certain application
 * preferences from the File Menu.
 *
 * @author Aanchal Chaturvedi, Gianluca Solari, Thomas Soistmann Jr., Timothy McClintock
 * @version 2017.12.12
 */
public class PreferencesFrame extends JFrame {

    private Container container;
    private JComboBox<String> directoryInput;
    private JTextField makeNameInput;
    private String[] userDirectories;
    private ArrayList<String> filePaths;
    private ArrayList<String> storedDirectories;

    /**
     * Constructor for the PreferencesFrame class.
     * Calls the methods responsible for populating the GUI with its content and sets it to be visible on screen.
     */
    public PreferencesFrame() {
        super("Preferences");
        container = getContentPane();
        filePaths = new ArrayList<>();
        loadDirectoryPreferences();
        createFrame();
        buildApplication();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            setDefaultLookAndFeelDecorated(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    /**
     * Creates the frame for Preferences GUI; sets its size and location.
     */
    private void createFrame() {
        setPreferredSize(new Dimension(800, 400));
        pack();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /**
     * Adds all of the content included in the Preferences Menu and sets its location.
     * Adds action listeners to GUI components where appropriate.
     */
    private void buildApplication() {
        JPanel pane = new JPanel(new GridBagLayout());
        pane.setPreferredSize(new Dimension(700, 500));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;

        JLabel directoryLabel = new JLabel("Set a default directory:");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 3;
        pane.add(directoryLabel, constraints);

        directoryInput = new JComboBox<>(userDirectories);
        directoryInput.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXX");
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 3;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        directoryInput.setEditable(true);
        pane.add(directoryInput, constraints);

        JButton applyDirectory = new JButton("Apply");
        constraints.gridx = 3;
        constraints.gridy = 1;
        pane.add(applyDirectory, constraints);

        JLabel makeFileLabel = new JLabel("Set a name for the makefile executable:");
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 3;
        constraints.insets = new Insets(20, 0, -20, 0);
        pane.add(makeFileLabel, constraints);

        makeNameInput = new JTextField();
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 3;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        makeNameInput.setEditable(true);
        pane.add(makeNameInput, constraints);

        JButton applyMakeName = new JButton("Apply");
        constraints.gridx = 3;
        constraints.gridy = 3;
        constraints.weighty = 1;
        pane.add(applyMakeName, constraints);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setSize(new Dimension(600, 300));

        JButton save = new JButton("Save");
        JButton saveAndClose = new JButton("Save and Close");
        buttonPanel.add(save);
        buttonPanel.add(saveAndClose);

        container.add(pane);
        container.add(buttonPanel, BorderLayout.SOUTH);

        applyDirectory.addActionListener(e -> setDirectory());
        applyMakeName.addActionListener(e -> setMakeName());
        save.addActionListener(e -> saveDirectoryPreferences());
        saveAndClose.addActionListener(e -> saveAndClose());

        setVisible(true);
    }

    /**
     * Used upon clocking 'Apply' when entering a directory. First checks to see if the directory exists,
     * then checks to see if the entered directory is not already stored. If these conditions are met,
     * it is added to the drop down menu abd sent to the main GUI to be implemented in the browse method.
     */
    private void setDirectory() {
        if (directoryInput.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(null, "Please enter a file path.");
        } else {
            String filePath = (String) directoryInput.getSelectedItem();
            if (Files.exists(Paths.get(filePath))) {
                if (!storedDirectories.contains(filePath) && (!filePaths.contains(filePath))) {
                    directoryInput.addItem(filePath);
                    filePaths.add(filePath);
                }
                GraphicalUserInterface.Frame.userDirectory = filePath;
            } else {
                JOptionPane.showMessageDialog(null, "The directory you entered does not exist.");
            }
        }
    }

    /**
     * Used upon clicking 'Apply' when entering a make executable name. Passes this value along to the Frame
     * class to eventually be used for makefile generation.
     */
    private void setMakeName() {
        if (makeNameInput.getText() == null) {
            JOptionPane.showMessageDialog(null, "Please enter a replacement makefile executable name.");
        } else {
            GraphicalUserInterface.Frame.makeExecutableName = makeNameInput.getText();
        }
    }

    /**
     * Upon clocking either of save buttons, this method stores any newly entered and applied directories
     * into an external file so that they can be loaded into future sessions.
     */
    private void saveDirectoryPreferences() {
        if (filePaths.isEmpty()) {
            JOptionPane.showMessageDialog(null, "There are no new file paths to be saved.");
        } else {
            new File(System.getProperty("user.dir") + "/preferences").mkdir();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter("preferences/directories.txt", true))) {
                for (String directory : filePaths) {
                    if (!storedDirectories.contains(directory)) {
                        bw.write(directory);
                        bw.write("\n");
                    } else {
                        JOptionPane.showMessageDialog(null, "Warning: " + directory + " is already a saved directory.");
                    }
                }
                bw.flush();
                bw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads any previously saved custom directories into the JComboBox drop down menu (for the directory
     * preference) so that the user doesn't have to re-type them each time they run the program.
     */
    private void loadDirectoryPreferences() {
        File f = new File("preferences/directories.txt");
        String line;
        storedDirectories = new ArrayList<>();

        if (f.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                while ((line = br.readLine()) != null) {
                    storedDirectories.add(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        userDirectories = storedDirectories.toArray(new String[0]);
    }

    /**
     * Uses the save method to store any newly entered directories and closes the window.
     */
    private void saveAndClose() {
        saveDirectoryPreferences();
        dispose();
    }
}
