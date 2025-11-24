import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class SortingVisualizer extends JFrame {
    private JTextField inputField;
    private JButton startButton, randomButton;
    private JPanel mainPanel;
    private int[] originalArray;
    private List<SortPanel> sortPanels;
    private JTextArea codeArea;
    private JLabel rankingLabel;
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SortingVisualizer visualizer = new SortingVisualizer();
            visualizer.setVisible(true);
        });
    }
    
    public SortingVisualizer() {
        setTitle("Sorting Algorithm Visualizer");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Top panel with input
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.add(new JLabel("Enter Array (comma-separated):"));
        inputField = new JTextField("64,34,25,12,22,11,90,88,45,50,23,67", 30);
        topPanel.add(inputField);
        
        startButton = new JButton("Start Sorting");
        startButton.addActionListener(e -> startSorting());
        topPanel.add(startButton);
        
        randomButton = new JButton("Random Array");
        randomButton.addActionListener(e -> generateRandomArray());
        topPanel.add(randomButton);
        
        rankingLabel = new JLabel("Rankings will appear here after sorting");
        rankingLabel.setFont(new Font("Arial", Font.BOLD, 14));
        topPanel.add(rankingLabel);
        
        add(topPanel, BorderLayout.NORTH);
        
     
        mainPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        sortPanels = new ArrayList<>();
        
        String[] algorithms = {"Bubble Sort", "Selection Sort", "Insertion Sort", 
                               "Merge Sort", "Quick Sort", "Heap Sort"};
        
        for (String algo : algorithms) {
            SortPanel panel = new SortPanel(algo);
            sortPanels.add(panel);
            mainPanel.add(panel);
        }
        
        add(mainPanel, BorderLayout.CENTER);
        
   
        codeArea = new JTextArea(10, 50);
        codeArea.setEditable(false);
        codeArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        JScrollPane codeScroll = new JScrollPane(codeArea);
        codeScroll.setBorder(BorderFactory.createTitledBorder("Algorithm Code (Click on a sorting panel to view)"));
        add(codeScroll, BorderLayout.SOUTH);
        
        generateRandomArray();
    }
    
    private void generateRandomArray() {
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(rand.nextInt(100) + 1);
            if (i < 11) sb.append(",");
        }
        inputField.setText(sb.toString());
    }
    
    private void startSorting() {
        String input = inputField.getText().trim();
        try {
            String[] parts = input.split(",");
            originalArray = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                originalArray[i] = Integer.parseInt(parts[i].trim());
            }
            
            if (originalArray.length == 0) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers!");
                return;
            }
            
            startButton.setEnabled(false);
            randomButton.setEnabled(false);
            rankingLabel.setText("Sorting in progress...");
            
            for (SortPanel panel : sortPanels) {
                panel.startSorting(originalArray.clone());
            }
            
        
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    while (sortPanels.stream().anyMatch(p -> !p.isCompleted())) {
                        Thread.sleep(100);
                    }
                    SwingUtilities.invokeLater(this::displayRankings);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input! Please enter numbers separated by commas.");
        }
    }
    
    private void displayRankings() {
        List<SortPanel> ranked = new ArrayList<>(sortPanels);
        ranked.sort(Comparator.comparingLong(SortPanel::getExecutionTime));
        
        StringBuilder sb = new StringBuilder("Rankings: ");
        for (int i = 0; i < ranked.size(); i++) {
            sb.append((i + 1)).append(". ").append(ranked.get(i).getAlgorithmName())
              .append(" (").append(ranked.get(i).getExecutionTime()).append("ms)");
            if (i < ranked.size() - 1) sb.append(" | ");
        }
        rankingLabel.setText(sb.toString());
        
        startButton.setEnabled(true);
        randomButton.setEnabled(true);
    }
    
    class SortPanel extends JPanel {
        private String algorithmName;
        private int[] array;
        private int comparing1 = -1, comparing2 = -1;
        private int sorted = -1;
        private long executionTime = 0;
        private boolean completed = false;
        private JLabel statusLabel;
        
        public SortPanel(String algorithmName) {
            this.algorithmName = algorithmName;
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createTitledBorder(algorithmName));
            setBackground(Color.WHITE);
            
            statusLabel = new JLabel("Ready", SwingConstants.CENTER);
            statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            add(statusLabel, BorderLayout.SOUTH);
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showCode();
                }
            });
            
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        
        public void startSorting(int[] arr) {
            this.array = arr;
            this.comparing1 = -1;
            this.comparing2 = -1;
            this.sorted = -1;
            this.completed = false;
            this.executionTime = 0;
            
            Thread sortThread = new Thread(() -> {
                long startTime = System.currentTimeMillis();
                
                switch (algorithmName) {
                    case "Bubble Sort": bubbleSort(); break;
                    case "Selection Sort": selectionSort(); break;
                    case "Insertion Sort": insertionSort(); break;
                    case "Merge Sort": mergeSort(0, array.length - 1); break;
                    case "Quick Sort": quickSort(0, array.length - 1); break;
                    case "Heap Sort": heapSort(); break;
                }
                
                executionTime = System.currentTimeMillis() - startTime;
                completed = true;
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Completed in " + executionTime + "ms");
                    repaint();
                });
            });
            sortThread.start();
        }
        
        private void bubbleSort() {
            for (int i = 0; i < array.length - 1; i++) {
                for (int j = 0; j < array.length - i - 1; j++) {
                    comparing1 = j;
                    comparing2 = j + 1;
                    visualize();
                    
                    if (array[j] > array[j + 1]) {
                        swap(j, j + 1);
                    }
                }
                sorted = array.length - i - 1;
            }
            sorted = 0;
            comparing1 = comparing2 = -1;
            visualize();
        }
        
        private void selectionSort() {
            for (int i = 0; i < array.length - 1; i++) {
                int minIdx = i;
                for (int j = i + 1; j < array.length; j++) {
                    comparing1 = minIdx;
                    comparing2 = j;
                    visualize();
                    
                    if (array[j] < array[minIdx]) {
                        minIdx = j;
                    }
                }
                swap(i, minIdx);
                sorted = i;
            }
            sorted = 0;
            comparing1 = comparing2 = -1;
            visualize();
        }
        
        private void insertionSort() {
            for (int i = 1; i < array.length; i++) {
                int key = array[i];
                int j = i - 1;
                
                comparing1 = i;
                while (j >= 0) {
                    comparing2 = j;
                    visualize();
                    
                    if (array[j] <= key) break;
                    
                    array[j + 1] = array[j];
                    j--;
                }
                array[j + 1] = key;
                sorted = i;
            }
            sorted = 0;
            comparing1 = comparing2 = -1;
            visualize();
        }
        
        private void mergeSort(int left, int right) {
            if (left < right) {
                int mid = (left + right) / 2;
                mergeSort(left, mid);
                mergeSort(mid + 1, right);
                merge(left, mid, right);
            }
        }
        
        private void merge(int left, int mid, int right) {
            int n1 = mid - left + 1;
            int n2 = right - mid;
            
            int[] L = new int[n1];
            int[] R = new int[n2];
            
            System.arraycopy(array, left, L, 0, n1);
            System.arraycopy(array, mid + 1, R, 0, n2);
            
            int i = 0, j = 0, k = left;
            
            while (i < n1 && j < n2) {
                comparing1 = left + i;
                comparing2 = mid + 1 + j;
                visualize();
                
                if (L[i] <= R[j]) {
                    array[k++] = L[i++];
                } else {
                    array[k++] = R[j++];
                }
            }
            
            while (i < n1) array[k++] = L[i++];
            while (j < n2) array[k++] = R[j++];
        }
        
        private void quickSort(int low, int high) {
            if (low < high) {
                int pi = partition(low, high);
                quickSort(low, pi - 1);
                quickSort(pi + 1, high);
            }
        }
        
        private int partition(int low, int high) {
            int pivot = array[high];
            int i = low - 1;
            
            for (int j = low; j < high; j++) {
                comparing1 = j;
                comparing2 = high;
                visualize();
                
                if (array[j] < pivot) {
                    i++;
                    swap(i, j);
                }
            }
            swap(i + 1, high);
            return i + 1;
        }
        
        private void heapSort() {
            int n = array.length;
            
            for (int i = n / 2 - 1; i >= 0; i--) {
                heapify(n, i);
            }
            
            for (int i = n - 1; i > 0; i--) {
                swap(0, i);
                heapify(i, 0);
                sorted = i;
            }
            sorted = 0;
            comparing1 = comparing2 = -1;
            visualize();
        }
        
        private void heapify(int n, int i) {
            int largest = i;
            int left = 2 * i + 1;
            int right = 2 * i + 2;
            
            if (left < n) {
                comparing1 = left;
                comparing2 = largest;
                visualize();
                if (array[left] > array[largest]) {
                    largest = left;
                }
            }
            
            if (right < n) {
                comparing1 = right;
                comparing2 = largest;
                visualize();
                if (array[right] > array[largest]) {
                    largest = right;
                }
            }
            
            if (largest != i) {
                swap(i, largest);
                heapify(n, largest);
            }
        }
        
        private void swap(int i, int j) {
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
        
        private void visualize() {
            SwingUtilities.invokeLater(this::repaint);
            try {
                Thread.sleep(50); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (array == null) return;
            
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth() - 20;
            int height = getHeight() - 50;
            int barWidth = width / array.length;
            int maxValue = Arrays.stream(array).max().orElse(1);
            
            for (int i = 0; i < array.length; i++) {
                int barHeight = (int) ((double) array[i] / maxValue * height);
                int x = 10 + i * barWidth;
                int y = height - barHeight + 10;
                
                if (completed) {
                    g2d.setColor(new Color(46, 204, 113));
                } else if (i == comparing1 || i == comparing2) {
                    g2d.setColor(new Color(231, 76, 60)); 
                } else if (sorted >= 0 && i >= sorted) {
                    g2d.setColor(new Color(52, 152, 219)); 
                } else {
                    g2d.setColor(new Color(149, 165, 166));
                }
                
                g2d.fillRect(x, y, barWidth - 2, barHeight);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(x, y, barWidth - 2, barHeight);
                
              
                g2d.setFont(new Font("Arial", Font.PLAIN, 9));
                String value = String.valueOf(array[i]);
                int strWidth = g2d.getFontMetrics().stringWidth(value);
                g2d.drawString(value, x + (barWidth - strWidth) / 2, y - 2);
            }
        }
        
        private void showCode() {
            String code = getAlgorithmCode();
            codeArea.setText(code);
            codeArea.setCaretPosition(0);
        }
        
        private String getAlgorithmCode() {
            switch (algorithmName) {
                case "Bubble Sort":
                    return "// Bubble Sort - O(n²)\n" +
                           "void bubbleSort(int[] arr) {\n" +
                           "    for (int i = 0; i < arr.length - 1; i++) {\n" +
                           "        for (int j = 0; j < arr.length - i - 1; j++) {\n" +
                           "            if (arr[j] > arr[j + 1]) {\n" +
                           "                // Swap arr[j] and arr[j+1]\n" +
                           "                int temp = arr[j];\n" +
                           "                arr[j] = arr[j + 1];\n" +
                           "                arr[j + 1] = temp;\n" +
                           "            }\n" +
                           "        }\n" +
                           "    }\n" +
                           "}";
                           
                case "Selection Sort":
                    return "// Selection Sort - O(n²)\n" +
                           "void selectionSort(int[] arr) {\n" +
                           "    for (int i = 0; i < arr.length - 1; i++) {\n" +
                           "        int minIdx = i;\n" +
                           "        for (int j = i + 1; j < arr.length; j++) {\n" +
                           "            if (arr[j] < arr[minIdx]) {\n" +
                           "                minIdx = j;\n" +
                           "            }\n" +
                           "        }\n" +
                           "        // Swap arr[i] and arr[minIdx]\n" +
                           "        int temp = arr[i];\n" +
                           "        arr[i] = arr[minIdx];\n" +
                           "        arr[minIdx] = temp;\n" +
                           "    }\n" +
                           "}";
                           
                case "Insertion Sort":
                    return "// Insertion Sort - O(n²)\n" +
                           "void insertionSort(int[] arr) {\n" +
                           "    for (int i = 1; i < arr.length; i++) {\n" +
                           "        int key = arr[i];\n" +
                           "        int j = i - 1;\n" +
                           "        while (j >= 0 && arr[j] > key) {\n" +
                           "            arr[j + 1] = arr[j];\n" +
                           "            j--;\n" +
                           "        }\n" +
                           "        arr[j + 1] = key;\n" +
                           "    }\n" +
                           "}";
                           
                case "Merge Sort":
                    return "// Merge Sort - O(n log n)\n" +
                           "void mergeSort(int[] arr, int left, int right) {\n" +
                           "    if (left < right) {\n" +
                           "        int mid = (left + right) / 2;\n" +
                           "        mergeSort(arr, left, mid);\n" +
                           "        mergeSort(arr, mid + 1, right);\n" +
                           "        merge(arr, left, mid, right);\n" +
                           "    }\n" +
                           "}\n\n" +
                           "void merge(int[] arr, int l, int m, int r) {\n" +
                           "    // Create temp arrays and merge them\n" +
                           "    int[] L = Arrays.copyOfRange(arr, l, m + 1);\n" +
                           "    int[] R = Arrays.copyOfRange(arr, m + 1, r + 1);\n" +
                           "    int i = 0, j = 0, k = l;\n" +
                           "    while (i < L.length && j < R.length) {\n" +
                           "        arr[k++] = (L[i] <= R[j]) ? L[i++] : R[j++];\n" +
                           "    }\n" +
                           "    while (i < L.length) arr[k++] = L[i++];\n" +
                           "    while (j < R.length) arr[k++] = R[j++];\n" +
                           "}";
                           
                case "Quick Sort":
                    return "// Quick Sort - O(n log n) average\n" +
                           "void quickSort(int[] arr, int low, int high) {\n" +
                           "    if (low < high) {\n" +
                           "        int pi = partition(arr, low, high);\n" +
                           "        quickSort(arr, low, pi - 1);\n" +
                           "        quickSort(arr, pi + 1, high);\n" +
                           "    }\n" +
                           "}\n\n" +
                           "int partition(int[] arr, int low, int high) {\n" +
                           "    int pivot = arr[high];\n" +
                           "    int i = low - 1;\n" +
                           "    for (int j = low; j < high; j++) {\n" +
                           "        if (arr[j] < pivot) {\n" +
                           "            i++;\n" +
                           "            swap(arr, i, j);\n" +
                           "        }\n" +
                           "    }\n" +
                           "    swap(arr, i + 1, high);\n" +
                           "    return i + 1;\n" +
                           "}";
                           
                case "Heap Sort":
                    return "// Heap Sort - O(n log n)\n" +
                           "void heapSort(int[] arr) {\n" +
                           "    int n = arr.length;\n" +
                           "    // Build max heap\n" +
                           "    for (int i = n / 2 - 1; i >= 0; i--) {\n" +
                           "        heapify(arr, n, i);\n" +
                           "    }\n" +
                           "    // Extract elements from heap\n" +
                           "    for (int i = n - 1; i > 0; i--) {\n" +
                           "        swap(arr, 0, i);\n" +
                           "        heapify(arr, i, 0);\n" +
                           "    }\n" +
                           "}\n\n" +
                           "void heapify(int[] arr, int n, int i) {\n" +
                           "    int largest = i, left = 2*i + 1, right = 2*i + 2;\n" +
                           "    if (left < n && arr[left] > arr[largest]) largest = left;\n" +
                           "    if (right < n && arr[right] > arr[largest]) largest = right;\n" +
                           "    if (largest != i) {\n" +
                           "        swap(arr, i, largest);\n" +
                           "        heapify(arr, n, largest);\n" +
                           "    }\n" +
                           "}";
                           
                default:
                    return "Code not available";
            }
        }
        
        public String getAlgorithmName() { return algorithmName; }
        public long getExecutionTime() { return executionTime; }
        public boolean isCompleted() { return completed; }
    }
}