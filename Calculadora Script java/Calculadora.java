import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Calculadora Básica - soma, subtração, multiplicação e divisão.
 *
 * Compilar:  javac Calculadora.java
 * Executar:  java Calculadora
 */
public class Calculadora extends JFrame {

    private final JTextField visor = new JTextField("0");
    private final JLabel historico = new JLabel(" ");

    private BigDecimal acumulado = null;   // primeiro operando
    private String operador = null;        // + - × ÷
    private boolean novoNumero = true;     // próximo dígito inicia um novo número

    public Calculadora() {
        super("Calculadora Básica");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(6, 6));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().setBackground(Color.BLACK);

        historico.setHorizontalAlignment(SwingConstants.RIGHT);
        historico.setFont(new Font("SansSerif", Font.PLAIN, 13));
        historico.setForeground(new Color(160, 160, 160));

        visor.setEditable(false);
        visor.setHorizontalAlignment(JTextField.RIGHT);
        visor.setFont(new Font("SansSerif", Font.PLAIN, 48));
        visor.setBackground(Color.BLACK);
        visor.setForeground(Color.WHITE);
        visor.setCaretColor(Color.BLACK);
        visor.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 6));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(Color.BLACK);
        topo.add(historico, BorderLayout.NORTH);
        topo.add(visor, BorderLayout.CENTER);
        add(topo, BorderLayout.NORTH);

        String[] teclas = {
            "C", "⌫", "%", "÷",
            "7", "8", "9", "×",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "00", "0", ".", "="
        };

        // Cores da calculadora do iPhone
        Color corNumero   = new Color(0x33, 0x33, 0x33);
        Color corFuncao   = new Color(0xA5, 0xA5, 0xA5);
        Color corOperador = new Color(0xFF, 0x9F, 0x0A);

        JPanel grade = new JPanel(new GridLayout(5, 4, 10, 10));
        grade.setBackground(Color.BLACK);
        for (String t : teclas) {
            BotaoRedondo b;
            if ("÷×-+=".contains(t)) b = new BotaoRedondo(t, corOperador, Color.WHITE);
            else if (t.equals("C") || t.equals("⌫") || t.equals("%")) b = new BotaoRedondo(t, corFuncao, Color.BLACK);
            else b = new BotaoRedondo(t, corNumero, Color.WHITE);
            b.addActionListener(e -> tecla(t));
            grade.add(b);
        }
        add(grade, BorderLayout.CENTER);

        // Suporte ao teclado
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ev -> {
            if (ev.getID() != java.awt.event.KeyEvent.KEY_TYPED) return false;
            char c = ev.getKeyChar();
            if (Character.isDigit(c)) tecla(String.valueOf(c));
            else if (c == '+' || c == '-') tecla(String.valueOf(c));
            else if (c == '*') tecla("×");
            else if (c == '/') tecla("÷");
            else if (c == '.' || c == ',') tecla(".");
            else if (c == '%') tecla("%");
            else if (c == '\n' || c == '=') tecla("=");
            else if (c == '\b') tecla("⌫");
            else if (c == 27) tecla("C");
            else return false;
            return true;
        });

        setSize(340, 460);
        setMinimumSize(new Dimension(300, 420));
        setLocationRelativeTo(null);
    }

    private void tecla(String t) {
        switch (t) {
            case "C" -> limpar();
            case "⌫" -> apagar();
            case "±" -> inverterSinal();
            case "%" -> porcentagem();
            case "=" -> igual();
            case "+", "-", "×", "÷" -> operacao(t);
            case "." -> ponto();
            default -> digito(t);
        }
    }

    private void digito(String d) {
        if (novoNumero || visor.getText().equals("0") || visor.getText().equals("Erro")) {
            visor.setText(d.equals("00") ? "0" : d);
        } else {
            visor.setText(visor.getText() + d);
        }
        novoNumero = false;
    }

    private void ponto() {
        if (novoNumero || visor.getText().equals("Erro")) {
            visor.setText("0.");
            novoNumero = false;
        } else if (!visor.getText().contains(".")) {
            visor.setText(visor.getText() + ".");
        }
    }

    private void apagar() {
        if (novoNumero || visor.getText().equals("Erro")) return;
        String s = visor.getText();
        s = s.length() <= 1 || (s.length() == 2 && s.startsWith("-")) ? "0" : s.substring(0, s.length() - 1);
        visor.setText(s);
    }

    private void inverterSinal() {
        String s = visor.getText();
        if (s.equals("0") || s.equals("Erro")) return;
        visor.setText(s.startsWith("-") ? s.substring(1) : "-" + s);
    }

    private void porcentagem() {
        if (visor.getText().equals("Erro")) return;
        BigDecimal v = valorDoVisor().divide(BigDecimal.valueOf(100), MathContext.DECIMAL64);
        visor.setText(formatar(v));
        novoNumero = true;
    }

    private void operacao(String op) {
        if (visor.getText().equals("Erro")) return;
        if (operador != null && !novoNumero) {
            igual();   // encadeia: 2 + 3 + ...
            if (visor.getText().equals("Erro")) return;
        }
        acumulado = valorDoVisor();
        operador = op;
        novoNumero = true;
        historico.setText(formatar(acumulado) + " " + op);
    }

    private void igual() {
        if (operador == null || acumulado == null) return;
        BigDecimal b = valorDoVisor();
        BigDecimal r;
        try {
            r = switch (operador) {
                case "+" -> acumulado.add(b);
                case "-" -> acumulado.subtract(b);
                case "×" -> acumulado.multiply(b);
                case "÷" -> acumulado.divide(b, MathContext.DECIMAL64);
                default -> b;
            };
        } catch (ArithmeticException ex) {
            visor.setText("Erro");
            historico.setText("Divisão por zero");
            acumulado = null;
            operador = null;
            novoNumero = true;
            return;
        }
        historico.setText(formatar(acumulado) + " " + operador + " " + formatar(b) + " =");
        visor.setText(formatar(r));
        acumulado = null;
        operador = null;
        novoNumero = true;
    }

    private void limpar() {
        visor.setText("0");
        historico.setText(" ");
        acumulado = null;
        operador = null;
        novoNumero = true;
    }

    private BigDecimal valorDoVisor() {
        return new BigDecimal(visor.getText());
    }

    private String formatar(BigDecimal v) {
        v = v.round(new MathContext(15, RoundingMode.HALF_UP));
        String s = v.stripTrailingZeros().toPlainString();
        return s.equals("-0") ? "0" : s;
    }

    /** Botão redondo, sem borda, no estilo da calculadora do iPhone. */
    private static class BotaoRedondo extends JButton {
        private final Color fundo;

        BotaoRedondo(String texto, Color fundo, Color letra) {
            super(texto);
            this.fundo = fundo;
            setForeground(letra);
            setFont(new Font("SansSerif", Font.PLAIN, 26));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = getModel().isPressed() ? fundo.brighter() : fundo;
            g2.setColor(c);
            int arco = Math.min(getWidth(), getHeight());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arco, arco);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignorado) { }
            new Calculadora().setVisible(true);
        });
    }
}
