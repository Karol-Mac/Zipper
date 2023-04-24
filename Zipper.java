package Zipper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zipper extends JFrame {

    public Zipper() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(300, 300, 400, 300);

        Action akcjaDodawania = new Akcja("Dodaj", "Dodaj nowy wpis do archiwum", "ctrl D", new ImageIcon("src/Zipper/dodaj.png"));
        Action akcjaUsuwania = new Akcja("Usun", "Usuń zaznaczone wpisy z archium", "ctrl U", new ImageIcon("src/Zipper/usun.png"));
        Action akcjaZippowania = new Akcja("Zip", "Zippuj wybrane pliki", "ctrl S");

        setJMenuBar(pasekMenu);

        JMenu menuPlik = pasekMenu.add(new JMenu("Plik"));

        JMenuItem menuOtworz = menuPlik.add(akcjaDodawania);
        JMenuItem menuUsun = menuPlik.add(akcjaUsuwania);
        JMenuItem menuZip = menuPlik.add(akcjaZippowania);

        dodaj = new JButton(akcjaDodawania);
        usun = new JButton(akcjaUsuwania);
        zip = new JButton(akcjaZippowania);

        JScrollPane scroll = new JScrollPane(lista);

        lista.setBorder(BorderFactory.createEtchedBorder());

        GroupLayout layout = new GroupLayout(getContentPane());

        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        //tworzenie layoutu:

        //POZIOM
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addComponent(scroll, 100, 150, Short.MAX_VALUE)
                        .addContainerGap(0, Short.MAX_VALUE)
                        .addGroup(
                                layout.createParallelGroup()
                                        .addComponent(dodaj)
                                        .addComponent(usun)
                                        .addComponent(zip)
                        )
        );

        //PION
        layout.setVerticalGroup(
                layout.createParallelGroup()
                        .addComponent(scroll, GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(dodaj)
                                .addComponent(usun)
                                .addContainerGap(0, Short.MAX_VALUE)
                                .addComponent(zip)
                        )
        );

        getContentPane().setLayout(layout);
        pack();
    }


    DefaultListModel modelListy = new DefaultListModel(){

        @Override
        public void addElement(Object obj) {

            alist.add(obj);
            super.addElement(((File)obj).getName());
        }

        @Override
        public Object get(int index){
            return alist.get(index);
        }

        @Override
        public Object remove(int index) {
            alist.remove(index);
            return super.remove(index);
        }



        ArrayList<Object> alist = new ArrayList<>();

    };
    JList lista = new JList(modelListy);
    JButton dodaj, usun, zip;

    JFileChooser chooser = new JFileChooser();
    private JMenuBar pasekMenu = new JMenuBar();


    public static void main(String[] args) {
        new Zipper().setVisible(true);
    }

    private class Akcja extends AbstractAction {

        public Akcja(String nazwa, String opis, String skrotKla) {
            putValue(Action.NAME, nazwa);
            putValue(Action.SHORT_DESCRIPTION, opis);
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(skrotKla));
        }

        public Akcja(String nazwa, String opis, String skrotKla, Icon ikona) {

            this(nazwa, opis, skrotKla);
            putValue(Action.SMALL_ICON, ikona);

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("Dodaj")) {
                dodajWpisyDoArchiwum();
            } else if (e.getActionCommand().equals("Usun")) {
                usunWpisyzListy();
            } else if (e.getActionCommand().equals("Zip")) {
                stworzArchiwumZip();
            }

        }

        private void dodajWpisyDoArchiwum() {

            chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setMultiSelectionEnabled(true);
            int tmp = chooser.showDialog(rootPane, "Dodaj do archiwum");

            if (tmp == JFileChooser.APPROVE_OPTION) {
                File[] sciezki = chooser.getSelectedFiles();
                for (int i = 0; i < sciezki.length; i++) {

                    if(!czyPowtarza(sciezki[i].getPath()))
                        modelListy.addElement(sciezki[i]);
                }
            }

        }

        private void usunWpisyzListy(){
            int[] tmp = lista.getSelectedIndices();
            for (int i = 0; i < tmp.length; i++) {
                modelListy.remove(tmp[i]-i);
            }
        }

        private void stworzArchiwumZip(){

            chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            chooser.setSelectedFile(new File(System.getProperty("user.dir") + File.separator + "mojanazwa.zip"));
            int tmp = chooser.showDialog(rootPane, "Kompresuj");

            if(tmp == JFileChooser.APPROVE_OPTION){

                byte[] tmpData = new byte[BUFFOR];
                try {
                    ZipOutputStream zoutS = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(chooser.getSelectedFile()), BUFFOR));

                    //zapakowanie:

                    for (int i = 0; i < modelListy.getSize(); i++) {
                        if(!((File)modelListy.get(i)).isDirectory())
                            zipuj(zoutS, (File) modelListy.get(i), tmpData);
                        else{
                            writePath((File)modelListy.get(i) );
                            for (int j = 0; j < listaScierzek.size(); j++) {
                                zipuj(zoutS, (File)listaScierzek.get(j), tmpData);
                                listaScierzek.clear();
                            }
                        }

                    }
                    zoutS.close();

                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }

            }
        }

        void writePath(File pathName){
            String[] nazwyPlikow = pathName.list();

            for (int i = 0; i < (nazwyPlikow != null ? nazwyPlikow.length : 0); i++) {
                File p = new File(pathName.getPath(),nazwyPlikow[i] );

                if(p.isFile()){
                    listaScierzek.add(p);
                }

                if(p.isDirectory()){
                    writePath(new File(p.getPath()));
                }
            }
        }
        ArrayList<File> listaScierzek = new ArrayList<>();

        void zipuj(ZipOutputStream zoutS, File scierzkaPliku, byte[] tmpData) throws IOException {

            String scierzka = scierzkaPliku.getPath();

            BufferedInputStream inS = new BufferedInputStream(new FileInputStream(scierzkaPliku.getPath()), BUFFOR);

            zoutS.putNextEntry(new ZipEntry(scierzkaPliku.getPath().substring(scierzka.lastIndexOf(File.separator)+1)));
            //pomiędzy tym miejscem, a closeEntry - wykonujemy

            int counter;
            while ((counter = inS.read(tmpData, 0, BUFFOR)) != -1)
                zoutS.write(tmpData, 0, counter);

            zoutS.closeEntry();
            inS.close();
        }
        final int BUFFOR = 1024;

        boolean czyPowtarza(String testowanyWpis) {
            for (int i = 0; i < modelListy.getSize(); i++)
                if (((File) modelListy.get(i)).getPath().equals(testowanyWpis)) return true;

            return false;
        }
    }
}
