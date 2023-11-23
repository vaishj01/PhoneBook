import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

class TrieNode {
    HashMap<Character, TrieNode> children;
    boolean isEndOfName;

    public TrieNode() {
        this.children = new HashMap<>();
        this.isEndOfName = false;
    }
}

class Trie {
    TrieNode root;

    public Trie() {
        this.root = new TrieNode();
    }

    public void insert(String name) {
        TrieNode node = root;
        for (char c : name.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
        }
        node.isEndOfName = true;
    }

    public boolean search(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return false;
            }
            node = node.children.get(c);
        }
        return node.isEndOfName;
    }
}

class PhoneInfo implements Serializable {
    String name;
    String phoneNumber;

    public PhoneInfo(String name, String num) {
        this.name = name;
        phoneNumber = num;
    }

    public String getName() {
        return name;
    }

    public void showPhoneInfo() {
        System.out.println("name: " + name);
        System.out.println("phone: " + phoneNumber);
    }

    public String toString() {
        return "name: " + name + '\n' + "phone: " + phoneNumber + '\n';
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object obj) {
        PhoneInfo cmp = (PhoneInfo) obj;
        if (name.compareTo(cmp.name) == 0)
            return true;
        else
            return false;
    }
}

class PhoneBookManager {
    private final File dataFile = new File("PhoneBook.dat");
    HashSet<PhoneInfo> infoStorage = new HashSet<>();
    Trie trie;

    static PhoneBookManager inst = null;

    public static PhoneBookManager createManagerInst() {
        if (inst == null)
            inst = new PhoneBookManager();
        return inst;
    }

    private PhoneBookManager() {
        readFromFile();
        trie = new Trie();
        buildTrie();
    }

    private void buildTrie() {
        for (PhoneInfo info : infoStorage) {
            trie.insert(info.getName());
        }
    }

    public String searchData(String prefix) {
        StringBuilder result = new StringBuilder();
        searchTrie(trie.root, prefix, result);
        return result.toString();
    }

    private void searchTrie(TrieNode node, String prefix, StringBuilder result) {
        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return;
            }
            node = node.children.get(c);
        }
        searchContacts(node, result, prefix);
    }

   private void searchContacts(TrieNode node, StringBuilder result, String prefix) {
    if (node.isEndOfName) {
        for (PhoneInfo info : infoStorage) {
            if (info.getName().startsWith(prefix)) {
                result.append(info.toString()).append("\n");
            }
        }
    }
    for (char c : node.children.keySet()) {
        searchContacts(node.children.get(c), result, prefix + c);
    }
}


    private PhoneInfo search(TrieNode node) {
        String name = getNodeName(node);
        for (PhoneInfo info : infoStorage) {
            if (info.getName().equals(name)) {
                return info;
            }
        }
        return null;
    }

    private String getNodeName(TrieNode node) {
        StringBuilder name = new StringBuilder();
        while (node != null && !node.equals(trie.root)) {
            for (char c : node.children.keySet()) {
                if (node.children.get(c).equals(trie.root)) {
                    name.insert(0, c);
                    node = node.children.get(c);
                    break;
                }
            }
        }
        return name.toString();
    }

    private void readFromFile() {
        if (dataFile.exists() == false)
            return;
        try {
            FileInputStream file = new FileInputStream(dataFile);
            ObjectInputStream in = new ObjectInputStream(file);
            while (true) {
                PhoneInfo info = (PhoneInfo) in.readObject();
                if (info == null)
                    break;
                infoStorage.add(info);
            }
            in.close();
        } catch (IOException e) {
            return;
        } catch (ClassNotFoundException e) {
            return;
        }
    }

    public boolean deleteData(String name) {
        Iterator<PhoneInfo> itr = infoStorage.iterator();
        while (itr.hasNext()) {
            PhoneInfo curInfo = itr.next();
            if (name.compareTo(curInfo.name) == 0) {
                itr.remove();
                trie = new Trie(); // Rebuild the trie after deletion
                buildTrie();
                return true;
            }
        }
        return false;
    }

    public void storeToFile() {
        try {
            FileOutputStream file = new FileOutputStream(dataFile);
            ObjectOutputStream out = new ObjectOutputStream(file);
            Iterator<PhoneInfo> itr = infoStorage.iterator();
            while (itr.hasNext())
                out.writeObject(itr.next());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class SearchEventHandler implements ActionListener {
    JTextField searchField;
    JTextArea textArea;

    public SearchEventHandler(JTextField field, JTextArea area) {
        searchField = field;
        textArea = area;
    }

    public void actionPerformed(ActionEvent e) {
        String name = searchField.getText();
        PhoneBookManager manager = PhoneBookManager.createManagerInst();
        String srchResult = manager.searchData(name);
        if (srchResult.isEmpty()) {
            textArea.append("Search Failed: info does not exist.\n");
        } else {
            textArea.append("Search Completed:\n");
            textArea.append(srchResult);
            textArea.append("\n");
        }
    }
}

class AddEventHandler implements ActionListener {
    JTextField name;
    JTextField phone;
    JTextArea text;

    boolean isAdded;

    PhoneInfo info;

    public AddEventHandler(JTextField nameField, JTextField phoneField, JTextArea textArea) {
        name = nameField;
        phone = phoneField;
        text = textArea;
    }

    public void actionPerformed(ActionEvent e) {
        PhoneBookManager manager = PhoneBookManager.createManagerInst();
        info = new PhoneInfo(name.getText(), phone.getText());
        isAdded = manager.infoStorage.add(info);

        if (isAdded) {
            text.append("Update Completed.\n");
            manager.storeToFile(); // Save changes to file
        } else {
            text.append("Update Failed: info already exists.\n");
        }
    }
}

class DeleteEventHandler implements ActionListener {
    JTextField delField;
    JTextArea textArea;

    public DeleteEventHandler(JTextField field, JTextArea area) {
        delField = field;
        textArea = area;
    }

    public void actionPerformed(ActionEvent e) {
        String name = delField.getText();
        PhoneBookManager manager = PhoneBookManager.createManagerInst();
        boolean isDeleted = manager.deleteData(name);
        if (isDeleted) {
            textArea.append("Remove Completed.\n");
            manager.storeToFile(); // Save changes to file
        } else {
            textArea.append("Remove Failed: info does not exist.\n");
        }
    }
}

class MainFrame extends JFrame {
    JTextField srchField = new JTextField(15);
    JButton srchBtn = new JButton("SEARCH");

    JButton addBtn = new JButton("ADD");
    JLabel nameLabel = new JLabel("NAME");
    JTextField nameField = new JTextField(15);
    JLabel phoneLabel = new JLabel("PHONE NUMBER");
    JTextField phoneField = new JTextField(15);

    JTextField delField = new JTextField(15);
    JButton delBtn = new JButton("DEL");

    JTextArea textArea = new JTextArea(10, 25);

    public MainFrame(String title) {
        super(title);
        setBounds(100, 200, 330, 450);
        setSize(730, 350);
        setLayout(new GridLayout(0, 2, 0, 0));
        Border border = BorderFactory.createEtchedBorder();

        Border srchBorder = BorderFactory.createTitledBorder(border, "Search");
        JPanel srchPanel = new JPanel();
        srchPanel.setBorder(srchBorder);
        srchPanel.setLayout(new FlowLayout());
        srchPanel.add(srchField);
        srchPanel.add(srchBtn);

        Border addBorder = BorderFactory.createTitledBorder(border, "Add");
        JPanel addPanel = new JPanel();
        addPanel.setBorder(addBorder);
        addPanel.setLayout(new FlowLayout());

        JPanel addInputPanel = new JPanel();
        addInputPanel.setLayout(new GridLayout(0, 2, 5, 5));

        addPanel.add(addBtn);

        addInputPanel.add(nameLabel);
        addInputPanel.add(nameField);
        addInputPanel.add(phoneLabel);
        addInputPanel.add(phoneField);

        addPanel.add(addInputPanel);

        Border delBorder = BorderFactory.createTitledBorder(border, "Delete");
        JPanel delPanel = new JPanel();
        delPanel.setBorder(delBorder);
        delPanel.setLayout(new FlowLayout());
        delPanel.add(delField);
        delPanel.add(delBtn);

        JScrollPane scrollTextArea = new JScrollPane(textArea);
        Border textBorder = BorderFactory.createTitledBorder(border, "Information Board");
        scrollTextArea.setBorder(textBorder);

        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BorderLayout());
        actionPanel.add(srchPanel, BorderLayout.NORTH);
        actionPanel.add(addPanel, BorderLayout.CENTER);
        actionPanel.add(delPanel, BorderLayout.SOUTH);

        add(actionPanel);
        add(scrollTextArea);

        srchBtn.addActionListener(new SearchEventHandler(srchField, textArea));
        addBtn.addActionListener(new AddEventHandler(nameField, phoneField, textArea));
        delBtn.addActionListener(new DeleteEventHandler(delField, textArea));

        setVisible(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }
}

class PhoneBook {
    public static void main(String[] args) {
        PhoneBookManager manager = PhoneBookManager.createManagerInst();
        MainFrame winFrame = new MainFrame("Phone Book by Vaishnavi");
    }
}