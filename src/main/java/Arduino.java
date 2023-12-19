
import com.fazecast.jSerialComm.SerialPort;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;


public class Arduino {

    static SerialPort choosenPort;
    static int x = 0;


    public static void main(String[] args) {
        // Ana pencere oluşturuluyor
        JFrame window = new JFrame();
        window.setTitle("POT DEĞERİ");
        window.setSize(1000, 600);
        window.setLayout(new BorderLayout());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Port listesi ve bağlantı butonu oluşturuluyor
        JComboBox<String> portList = new JComboBox<>();
        JButton connectButton = new JButton("Connect");
        JPanel topPanel = new JPanel();
        topPanel.add(connectButton);
        topPanel.add(portList);
        window.add(topPanel, BorderLayout.NORTH);

        // Bilgisayarın bağlı olduğu seri portlar listeye ekleniyor
        SerialPort[] portNames = SerialPort.getCommPorts();
        for (int i = 0; i < portNames.length; i++) {
            portList.addItem(portNames[i].getSystemPortName());
        }

        // Grafik oluşturuluyor
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // grafik çizgisini oluşturma
        JFreeChart chart = ChartFactory.createLineChart3D("Anlık POT Grafiği", "Zaman", "Pot Değeri", dataset);

        // tablo özelliklerini belirleme
        chart.setBackgroundPaint(Color.white);
        CategoryPlot catPlot = chart.getCategoryPlot();
        catPlot.setRangeGridlinePaint(Color.white);
        chart.getPlot().setBackgroundPaint(Color.darkGray);

        window.add(new ChartPanel(chart), BorderLayout.CENTER);

        // Bağlanma butonuna tıklanınca yapılacak işlemler
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (connectButton.getText().equals("Connect")) {

                    // Seçilen portun alınması ve bağlantının açılması
                    choosenPort = SerialPort.getCommPort(portList.getSelectedItem().toString());
                    choosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
                    if (choosenPort.openPort()) {
                        connectButton.setText("Disconnect");
                        portList.setEnabled(false);
                    }

                    //  seri porttan yeni thread ile aynı anda okuma yapılıyor
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            Scanner scanner = new Scanner(choosenPort.getInputStream());
                            while (scanner.hasNextLine()) {
                                try {

                                    // Gelen veriyi okuma ve grafik dataset'ine ekleme
                                    String line = scanner.nextLine();
                                    int number = Integer.parseInt(line);
                                    dataset.addValue(number, "Pot okuma", String.valueOf(x++));
                                    System.out.println(x + " = " + number);
                                    window.repaint();
                                } catch (Exception e) {
                                    System.out.println(" Have Problem ! ");
                                }
                            }
                            scanner.close();
                        }
                    };
                    thread.start();
                } else {

                    // Bağlantıyı kapatır
                    choosenPort.closePort();
                    portList.setEnabled(true);
                    connectButton.setText("Connect");
                    dataset.clear();
                    x = 0;
                }
            }
        });
        window.setVisible(true);
    }
}
