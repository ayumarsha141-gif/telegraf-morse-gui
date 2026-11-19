import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Telegraf Morse Mini - Tema Neon Cyberpunk
 * Top-level container : JFrame
 */
public class TelegrafMorse extends JFrame {

    // ====== Palet warna tema ======
    private static final Color BG        = new Color(14, 14, 28);
    private static final Color PANEL     = new Color(26, 26, 50);
    private static final Color NEON_CYAN = new Color(0, 229, 255);
    private static final Color NEON_PINK = new Color(255, 64, 129);
    private static final Color TEKS      = new Color(224, 224, 245);
    private static final Color LAMP_ON   = new Color(0, 255, 200);
    private static final Color LAMP_OFF  = new Color(48, 48, 72);

    // ====== Data kamus Morse ======
    private static final Map<Character, String> MORSE = new HashMap<>();
    private static final Map<String, Character> REVERSE = new HashMap<>();

    static {
        String[] huruf = {".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..",
                ".---", "-.-", ".-..", "--", "-.", "---", ".--.", "--.-", ".-.", "...",
                "-", "..-", "...-", ".--", "-..-", "-.--", "--.."};
        String[] angka = {"-----", ".----", "..---", "...--", "....-",
                ".....", "-....", "--...", "---..", "----."};
        for (int i = 0; i < 26; i++) {
            MORSE.put((char) ('A' + i), huruf[i]);
            REVERSE.put(huruf[i], (char) ('A' + i));
        }
        for (int i = 0; i < 10; i++) {
            MORSE.put((char) ('0' + i), angka[i]);
            REVERSE.put(angka[i], (char) ('0' + i));
        }
    }

    // ====== Komponen yang perlu diakses antar-method ======
    private final JTextArea inputArea = new JTextArea(5, 20);      // Atomic
    private final JTextArea morseArea = new JTextArea(5, 20);      // Atomic
    private final JLabel bufferLabel = new JLabel("Buffer: -");    // Atomic
    private final JLabel decodedLabel = new JLabel("Hasil: ");     // Atomic
    private final JSlider speedSlider = new JSlider(60, 300, 150); // Atomic
    private final LampPanel lamp = new LampPanel();                // Custom component

    private final StringBuilder buffer = new StringBuilder();
    private final StringBuilder decoded = new StringBuilder();
    private long pressedAt;
    private Timer idleTimer;     // jeda antar-huruf pada kunci telegraf
    private Timer playTimer;     // pemutar lampu

    public TelegrafMorse() {
        super("Telegraf Morse Mini");                                // JFrame
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));                         // Layout 1: BorderLayout
        getContentPane().setBackground(BG);

        gayaArea(inputArea, false);
        gayaArea(morseArea, true);
        gayaLabel(bufferLabel, NEON_PINK);
        gayaLabel(decodedLabel, NEON_CYAN);
        decodedLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));

        speedSlider.setBackground(PANEL);
        speedSlider.setForeground(NEON_CYAN);

        add(buatHeader(), BorderLayout.NORTH);
        add(buatKonten(), BorderLayout.CENTER);
        add(buatFooter(), BorderLayout.SOUTH);

        // Event Adapter: WindowAdapter -> konfirmasi sebelum keluar
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int pilih = JOptionPane.showConfirmDialog(TelegrafMorse.this,
                        "Matikan telegraf dan keluar?", "Konfirmasi",
                        JOptionPane.YES_NO_OPTION);
                if (pilih == JOptionPane.YES_OPTION) System.exit(0);
            }
        });

        // Timer: kalau 800 ms tidak ada ketukan, buffer dianggap 1 huruf
        idleTimer = new Timer(800, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Character c = REVERSE.get(buffer.toString());
                decoded.append(c == null ? '?' : c);
                decodedLabel.setText("Hasil: " + decoded);
                buffer.setLength(0);
                bufferLabel.setText("Buffer: -");
            }
        });
        idleTimer.setRepeats(false);

        pack();
        setMinimumSize(new Dimension(780, 500));
        setLocationRelativeTo(null);
    }

    // ---------- Helper styling ----------
    private JPanel pnl(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(PANEL);
        return p;
    }

    private JLabel lbl(String teks) {
        JLabel l = new JLabel(teks);
        l.setForeground(TEKS);
        return l;
    }

    private void gayaLabel(JLabel l, Color warna) {
        l.setForeground(warna);
        l.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
    }

    private void gayaArea(JTextArea a, boolean morse) {
        a.setLineWrap(true);
        a.setBackground(new Color(8, 8, 18));
        a.setForeground(morse ? NEON_PINK : NEON_CYAN);
        a.setCaretColor(NEON_CYAN);
        a.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        a.setMargin(new Insets(6, 8, 6, 8));
    }

    private JScrollPane scroll(JTextArea a) {                        // Intermediate container
        JScrollPane sp = new JScrollPane(a);
        sp.setBorder(BorderFactory.createLineBorder(NEON_CYAN.darker()));
        return sp;
    }

    private TitledBorder judulBorder(String judul) {
        TitledBorder tb = new TitledBorder(BorderFactory.createLineBorder(NEON_CYAN), judul);
        tb.setTitleColor(NEON_CYAN);
        tb.setTitleFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        return tb;
    }

    private void gayaTombol(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createLineBorder(fg, 2));
    }

    // ---------- Header ----------
    private JPanel buatHeader() {                                    // Intermediate container
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));    // Layout 2: FlowLayout
        p.setBackground(new Color(8, 8, 18));
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, NEON_PINK));
        JLabel judul = new JLabel("▌▌ TELEGRAF MORSE MINI ▐▐");
        judul.setForeground(NEON_CYAN);
        judul.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        p.add(judul);
        return p;
    }

    // ---------- Konten tengah: 2 kolom ----------
    private JPanel buatKonten() {
        JPanel grid = new JPanel(new GridLayout(1, 2, 12, 0));       // Layout 3: GridLayout
        grid.setBackground(BG);
        grid.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        grid.add(buatPanelEncoder());
        grid.add(buatPanelKunci());
        return grid;
    }

    // ---------- Kolom kiri: Teks -> Morse ----------
    private JPanel buatPanelEncoder() {
        JPanel p = pnl(new BorderLayout(6, 6));
        p.setBorder(judulBorder("Teks → Morse"));

        JPanel atas = pnl(new BorderLayout());
        atas.add(lbl("Ketik pesan:"), BorderLayout.NORTH);
        atas.add(scroll(inputArea), BorderLayout.CENTER);

        // Event Adapter: KeyAdapter -> terjemahan live saat tombol dilepas
        inputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                morseArea.setText(keMorse(inputArea.getText()));
            }
        });

        JPanel tengah = pnl(new GridLayout(2, 1, 0, 6));
        tengah.add(atas);
        JPanel bawahMorse = pnl(new BorderLayout());
        bawahMorse.add(lbl("Kode Morse:"), BorderLayout.NORTH);
        bawahMorse.add(scroll(morseArea), BorderLayout.CENTER);
        tengah.add(bawahMorse);

        // Kontrol pemutar lampu
        JPanel kontrol = pnl(new FlowLayout(FlowLayout.LEFT));
        JButton mainkan = new JButton("▶ Kedipkan Lampu");
        gayaTombol(mainkan, new Color(8, 8, 18), NEON_CYAN);
        kontrol.add(mainkan);
        kontrol.add(lbl("Tempo (ms/unit):"));
        speedSlider.setPreferredSize(new Dimension(110, 24));
        kontrol.add(speedSlider);

        // Event Listener Interface: ActionListener
        mainkan.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                putarLampu(morseArea.getText());
            }
        });

        p.add(tengah, BorderLayout.CENTER);
        p.add(kontrol, BorderLayout.SOUTH);
        return p;
    }

    // ---------- Kolom kanan: Kunci telegraf -> Teks ----------
    private JPanel buatPanelKunci() {
        JPanel p = pnl(new BorderLayout(6, 6));
        p.setBorder(judulBorder("Kunci Telegraf → Teks"));

        JPanel lampWrap = pnl(new FlowLayout(FlowLayout.CENTER));
        lamp.setPreferredSize(new Dimension(100, 100));
        lampWrap.add(lamp);

        JButton kunci = new JButton("TEKAN AKU");
        kunci.setFont(new Font(Font.MONOSPACED, Font.BOLD, 22));
        gayaTombol(kunci, new Color(120, 0, 60), NEON_PINK);

        // Event Adapter: MouseAdapter -> ukur durasi tekan
        kunci.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                pressedAt = System.currentTimeMillis();
                idleTimer.stop();
                lamp.setOn(true);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                long durasi = System.currentTimeMillis() - pressedAt;
                buffer.append(durasi < 250 ? '.' : '-');
                bufferLabel.setText("Buffer: " + buffer);
                lamp.setOn(false);
                idleTimer.restart();
            }
        });

        JButton hapus = new JButton("Hapus Hasil");
        gayaTombol(hapus, new Color(8, 8, 18), NEON_CYAN);
        hapus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                decoded.setLength(0);
                buffer.setLength(0);
                decodedLabel.setText("Hasil: ");
                bufferLabel.setText("Buffer: -");
            }
        });

        JPanel info = pnl(new GridLayout(3, 1, 0, 4));
        info.add(bufferLabel);
        info.add(decodedLabel);
        info.add(hapus);

        p.add(lampWrap, BorderLayout.NORTH);
        p.add(kunci, BorderLayout.CENTER);
        p.add(info, BorderLayout.SOUTH);
        return p;
    }

    // ---------- Footer ----------
    private JPanel buatFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setBackground(new Color(8, 8, 18));
        JLabel l = lbl("Tekan < 250 ms = titik (.)   |   ≥ 250 ms = garis (-)   |   Diam 0,8 detik = huruf selesai");
        l.setForeground(NEON_PINK);
        p.add(l);
        return p;
    }

    // ====== Logika ======
    private String keMorse(String teks) {
        StringBuilder sb = new StringBuilder();
        for (char c : teks.toUpperCase().toCharArray()) {
            if (c == ' ') sb.append(" / ");
            else if (MORSE.containsKey(c)) sb.append(MORSE.get(c)).append(' ');
        }
        return sb.toString().trim();
    }

    /** Ubah string Morse menjadi daftar langkah {nyala(1/0), lamaUnit}. */
    private void putarLampu(String morse) {
        if (playTimer != null) playTimer.stop();
        final List<int[]> langkah = new ArrayList<>();
        for (char c : morse.toCharArray()) {
            if (c == '.') { langkah.add(new int[]{1, 1}); langkah.add(new int[]{0, 1}); }
            else if (c == '-') { langkah.add(new int[]{1, 3}); langkah.add(new int[]{0, 1}); }
            else if (c == ' ' || c == '/') langkah.add(new int[]{0, 2});
        }
        if (langkah.isEmpty()) return;

        final int[] idx = {0};
        playTimer = new Timer(0, null);
        playTimer.setRepeats(false);
        playTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (idx[0] >= langkah.size()) { lamp.setOn(false); return; }
                int[] s = langkah.get(idx[0]++);
                lamp.setOn(s[0] == 1);
                playTimer.setInitialDelay(s[1] * speedSlider.getValue());
                playTimer.restart();
            }
        });
        playTimer.start();
    }

    // ====== Komponen kustom: lampu sinyal neon ======
    private static class LampPanel extends JPanel {
        private boolean on = false;

        LampPanel() { setOpaque(false); }

        void setOn(boolean on) { this.on = on; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (on) {   // efek glow
                g2.setColor(new Color(0, 255, 200, 60));
                g2.fillOval(0, 0, w, h);
                g2.setColor(new Color(0, 255, 200, 110));
                g2.fillOval(8, 8, w - 16, h - 16);
            }
            g2.setColor(on ? LAMP_ON : LAMP_OFF);
            g2.fillOval(16, 16, w - 32, h - 32);
            g2.setColor(NEON_CYAN);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(16, 16, w - 32, h - 32);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelegrafMorse().setVisible(true));
    }
}