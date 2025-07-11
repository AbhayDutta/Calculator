import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class AdvancedCalculatorGUI {
    private static double memory = 0;
    private static List<String> history = new ArrayList<>();
    private static String expression = "";
    private static JTextField display;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Advanced Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 600);
        frame.setLayout(new BorderLayout());

        // Set a modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create the display text field
        display = new JTextField();
        display.setFont(new Font("Segoe UI", Font.BOLD, 28)); // Modern font
        display.setEditable(false);
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setPreferredSize(new Dimension(400, 80)); // Increased height of the input area
        display.setBackground(new Color(30, 30, 30)); // Dark background
        display.setForeground(Color.WHITE); // White text for better contrast
        display.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding
        frame.add(display, BorderLayout.CENTER);

        // Create a panel for the calculator buttons
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 5, 10, 10)); // 6 rows and 5 columns grid for buttons
        panel.setBackground(new Color(45, 45, 45)); // Dark background for the button panel
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding
        frame.add(panel, BorderLayout.SOUTH);

        // Buttons for digits and operations
        String[] buttons = {
            "7", "8", "9", "/", "sqrt",
            "4", "5", "6", "*", "sin",
            "1", "2", "3", "-", "cos",
            "0", ".", "=", "+", "tan",
            "(", ")", "^", "C", "exp",
            "log", "ln", "mem", "history"
        };

        // Add buttons to the panel
        for (String text : buttons) {
            JButton button = new GradientButton(text);
            button.setFont(new Font("Segoe UI", Font.BOLD, 18));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false); // Remove focus border
            button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding
            button.addActionListener(new ButtonClickListener());

            // Customize button colors based on their function
            if (text.matches("[0-9.]")) {
                button.setBackground(new Color(70, 70, 70)); // Dark gray for numbers
            } else if (text.matches("[+\\-*/^=]")) {
                button.setBackground(new Color(255, 140, 0)); // Orange for operators
            } else if (text.equals("C")) {
                button.setBackground(new Color(220, 20, 60)); // Red for clear
            } else {
                button.setBackground(new Color(50, 50, 50)); // Default gray for functions
            }

            panel.add(button);
        }

        // Make the frame visible
        frame.setVisible(true);
    }

    // Custom JButton with gradient background and rounded corners
    static class GradientButton extends JButton {
        public GradientButton(String text) {
            super(text);
            setContentAreaFilled(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Gradient background
            GradientPaint gradient = new GradientPaint(0, 0, getBackground(), getWidth(), getHeight(), getBackground().darker());
            g2.setPaint(gradient);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // Rounded corners

            // Add a subtle border
            g2.setColor(getBackground().darker());
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

            super.paintComponent(g2);
            g2.dispose();
        }
    }

    // Listener for button clicks
    static class ButtonClickListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();

            // Handle special commands like memory, history, clear, and evaluate
            if (command.equals("mem")) {
                display.setText("Memory: " + memory);
                return;
            } else if (command.equals("history")) {
                showHistory();
                return;
            } else if (command.equals("=")) {
                try {
                    double result = evaluateExpression(expression);
                    display.setText(String.valueOf(result));
                    history.add(expression + " = " + result);
                    memory = result;
                    expression = ""; // Reset the expression after evaluation
                } catch (Exception ex) {
                    display.setText("Invalid Expression");
                }
                return;
            } else if (command.equals("C")) {
                // Clear the expression
                expression = "";
                display.setText(expression);
                return;
            }

            // Handle parentheses and concatenate the clicked button's text to the expression
            if (command.equals("(") || command.equals(")")) {
                expression += command;
            } else {
                expression += command;
            }

            display.setText(expression);
        }
    }

    // Function to evaluate the expression with BODMAS and Power
    private static double evaluateExpression(String expression) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                while (true) {
                    if (eat('+')) x += parseTerm(); // Addition
                    else if (eat('-')) x -= parseTerm(); // Subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                while (true) {
                    if (eat('*')) x *= parseFactor(); // Multiplication
                    else if (eat('/')) {
                        double divisor = parseFactor();
                        if (divisor == 0) throw new ArithmeticException("Division by Zero");
                        x /= divisor;
                    }
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // Unary plus
                if (eat('-')) return -parseFactor(); // Unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { 
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // Numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expression.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') {  // Functions like sin, cos, log, etc.
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = expression.substring(startPos, this.pos);
                    x = parseFactor();
                    switch (func) { // Trigo, logarithm, expo, etc.
                        case "sqrt": 
                            if (x < 0) throw new ArithmeticException("Square root of negative number");
                            x = Math.sqrt(x); 
                            break;
                        case "sin": x = Math.sin(Math.toRadians(x)); break;
                        case "cos": x = Math.cos(Math.toRadians(x)); break;
                        case "tan": x = Math.tan(Math.toRadians(x)); break;
                        case "log": 
                            if (x <= 0) throw new ArithmeticException("Logarithm of non-positive number");
                            x = Math.log10(x); 
                            break;
                        case "ln": 
                            if (x <= 0) throw new ArithmeticException("Natural log of non-positive number");
                            x = Math.log(x); 
                            break;
                        case "exp": x = Math.exp(x); break;
                        default: throw new RuntimeException("Unknown function: " + func);
                    }
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }
                
                // Power Operation
                if (eat('^')) {
                    double exponent = parseFactor();
                    x = Math.pow(x, exponent);
                }
                
                return x;
            }
        }.parse();
    }

    // Display History Log
    private static void showHistory() {
        if (history.isEmpty()) {
            display.setText("History is empty.");
        } else {
            StringBuilder historyText = new StringBuilder();
            for (String record : history) {
                historyText.append(record).append("\n");
            }
            display.setText(historyText.toString());
        }
    }
}