package execution;

import java.io.*;
import java.util.*;

public class Execution{
/*
static Kullanımı: Java'da static anahtar kelimesi, bu değişkenlerin tüm Execution3 sınıfı için tek bir kopyasının olduğunu belirtir.
8-bit Bellek Taklidi: Bu ifade, El-Ceziri işlemcisinin 256 elemanlı ana belleğini (RAM) simüle eder.    
    
List<String[]> program = new ArrayList<>():
Kullanım Amacı: Kaynak dosyadaki tüm Assembly komutlarını sırasıyla tutmak için.   
Neden ArrayList? Assembly programları, Program Sayacına (pc) göre sırayla çalıştırılır. 
ArrayList, elemanların eklenme sırasını korur ve indeksi (satır numarası) ile komutlara hızlı erişim sağlar. Bu, programın "kayıtlı komut" belleğini temsil eder.    
    
Map<String, Integer> labels = new HashMap<>():
Kullanım Amacı: Programdaki tüm etiketlerin (örneğin ETIKET1, ETIKET2) hangi satır numarasına (indekse) karşılık geldiğini saklamak.
Neden HashMap? Dallanma komutları (D, DE, DB vb.), etiket adını kullanarak anında o satır numarasına atlamak zorundadır. 
HashMap, etiket adını (String) anahtar olarak kullanıp satır numarasını (Integer) değer olarak saklayarak, atlama hedeflerini çok hızlı bir şekilde bulmamızı sağlar. 
Bu, program akışını yöneten temel yapıdır.    
    
    */
    
    
    static int AX = 0, BX = 0, CX = 0, DX = 0; 
// İşlemcinin 4 adet 8-bitlik kayıtçısının taklidi. Java'da 'int' kullanılmasına rağmen, değerler 8-bit aralığında tutulur.
    
    // --- Bellek (RAM)  ---
    static int[] RAM = new int[256];// İşlemcideki 256 elemanlı belleğin taklidi. 8-bit adresleme ile ulaşılabilecek 2^8 = 256 adresi temsil eder.

    static int BayrakSifir = 0;
    static int BayrakIsaret = 0;
    static int BayrakTasma = 0;

    static List<String[]> program = new ArrayList<>(); // Assembly komutlarını satır satır tutan liste.
    static Map<String, Integer> labels = new HashMap<>(); // Etiketleri programdaki satır numaralarıyla eşleştiren harita.

    static int pc = 0;  // Program Sayacı (Program Counter): Şu anda çalıştırılacak komutun indeksini (satır numarasını) tutar.

    public static void main(String[] args) {

        String filePath = "C:\\Users\\WINUSER\\Desktop\\Program Yapısı ve Anlamı-Dosyalar\\program.txt";

        loadProgram(filePath); // Kaynak dosyayı okuyup komutları ve etiketleri yükler.
        executeProgram(); // Yüklenen komutları çalıştırmaya başlar.
    }

    static void loadProgram(String file) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String satir;
            
            // Dosyanın sonuna gelene kadar her satırı oku.
            while ((satir = br.readLine()) != null) {
                // Amaç: Komutları ve etiketleri temiz bir şekilde ayrıştırmak.
                satir = satir.trim();  // trim() Metodu: Satırın başındaki ve sonundaki tüm boşlukları (space, tab, yeni satır vb.) temizler.
                if (satir.equals("")) continue;  // Boş satır kontrolü: Eğer satır boşsa (veya trim sonrası boş kaldıysa), bu satırı atla ve bir sonraki satıra geç.

                
                if (satir.contains(":")) {
                    // Etiket varsa, etiket adını al.
                    //substring Metodu: Satırın başlangıcından (0) ':' işaretinin bulunduğu indekse kadar olan kısmı kesip alır.
                    //Bu, etiket adını (örneğin "ETIKET1") verir.
                    String label = satir.substring(0, satir.indexOf(":")).trim();

                // labels.put(label, program.size());
                // labels HashMap'ine etiketi ve adresini kaydet.
                // program.size(): Bu, List'e yeni eklenecek olan komutun indisini, yani satır numarasını (Program Counter değerini) temsil eder.
                    labels.put(label, program.size());

                    // Satırın etiket kısmını (ETIKET1:) attıktan sonra geri kalan komut kısmını al.
                    satir = satir.substring(satir.indexOf(":") + 1).trim();

                    if (satir.equals("")) {
                        // Etiket var, ancak komut yok (Örn: ETIKET1:)                
                        // Amaç: Etiketin bir satır numarasını (adresi) işgal etmesini ve bu adrese atlama yapılabilmesini sağlamak.
                        program.add(new String[]{"NOP"}); // NOP (No Operation) Komutu: Boş döngü veya etiket tanımı için komut içermeyen satırlara "Hiçbir Şey Yapma" komutunu ekler.
                    } else {
                        // Etiket var VE komut var (Örn: ETIKET2: ATM DX AX)
                        // Amaç: Komutu ve operandları ayrı String[] elemanları olarak ayırmak (Örn: {"ATM", "DX", "AX"}).
                        program.add(satir.split("\\s+")); // split("\\s+") Metodu: Komut satırını bir veya daha fazla boşluk karakterine göre ayırır. 
                    // \\s+: Bu, bir regex (düzenli ifade) olup, bir veya daha fazla boşluk karakteri (space, tab vb.) anlamına gelir.
                    }
                }
                else {
                    // Etiket içermeyen normal komut satırı (Örn: TOP AX 10)
                    program.add(satir.split("\\s+"));
                }
            }

            br.close();
        } catch (IOException e) {
            System.out.println("HATA: " + e.getMessage());
        }
    }

    //Bu metot, Assembly programının çalıştırılmasından sorumlu olan İşlemci Kontrol Ünitesini (Control Unit) taklit eder. 
    //Her döngüde bir komut alınır, çözülür (decode edilir) ve çalıştırılır (execute edilir).
    static void executeProgram() {
        pc = 0; // Program Sayacı (Program Counter - PC) 0'a sıfırlanır. Programın ilk komutundan başlanacağı anlamına gelir.

// Program Sayacı, yüklenen komut listesinin (program ArrayList'inin) boyutundan küçük olduğu sürece döngü devam eder.
    // PC, List'in geçerli bir indeksine işaret ettiği sürece program çalışmaya devam eder.
        while (pc < program.size()) {

            // Komut Getirme (Fetch)
            String[] k = program.get(pc);// program.get(pc): 'program' ArrayList'indeki PC tarafından gösterilen indeksteki elemanı (yani komut satırını) alır.
            // get() Metodu: ArrayList'in belirli bir indeksindeki öğeyi döndürme işlevidir.
            // Bu öğe, komutu ve operandları içeren bir String dizisidir (String[] k).
            
            String komut = k[0];
            // k[0] Nedir?: Komut dizisinin (String[] k) 0. indeksindeki elemanı (ilk kelimeyi) alır.
            // Assembly'de bu, her zaman komutun adıdır (Örn: "ATM", "CIK", "DKE").
            // Örneğin k = {"ATM", "AX", "10"} ise, k[0] = "ATM" olacaktır

            // Komut Çözümleme ve Yürütme (Decode and Execute)
            // switch (komut) bloğu, komutun adını kontrol ederek hangi fonksiyonun çağrılacağını belirler.
            switch (komut) {

                case "OKU": oku(k, 0); break;

                case "TOP": toplam(k); break;
                case "CIK": cikar(k); break;
                case "CRP": carp(k); break;
                case "BOL": bol(k); break;

                case "VE": ve(k); break;
                case "VEY": vey(k); break;
                case "DEG": deg(k); break;

                case "ATM": atama(k); break;
                case "YAZ": yaz(k); break;

                case "D": d_git(k); break;
                case "DE": de_git(k); break;
                case "DB": db_git(k); break;
                case "DK": dk_git(k); break;
                case "DBE": dbe_git(k); break;
                case "DKE": dke_git(k); break;
                case "DED": ded_git(k); break;

                case "NOP": pc++; break;
            // NOP'da Neden pc++ dedik?: NOP (No Operation), "Hiçbir İşlem Yapma" anlamına gelir.
            // Bu komut, sadece zaman geçirmek veya etiketler için bir adres tutmak amacıyla kullanılır.
            // Bu nedenle, yapılması gereken tek şey Program Sayacını bir sonraki komuta ilerletmektir (pc++).
                
                case "SON": 
                    System.out.println("Program SON komutu ile bitti.");
                    System.out.println("AX = " + AX);
                    System.out.println("BX = " + BX);
                    System.out.println("CX = " + CX);
                    System.out.println("DX = " + DX);
                    return; // return; ifadesi ile metottan hemen çıkılarak program sonlandırılır.

                default: 
                    System.out.println("Bilinmeyen komut: " + komut);
                    pc++; // Hata olsa bile döngünün takılmaması için PC'yi artır.
                    break;
                    // NOT: Çoğu komut fonksiyonu (Örn: TOP, CIK, OKU), komutu yürüttükten sonra kendi içinde pc++ işlemini yapar.
                    // Ancak Dallanma komutları (D, DE vb.) başarılı olursa PC'yi etiket adresine ayarlar, başarısız olursa pc++ yapar.
            }
        }
    }

    static void oku(String[] k, int idx) {
        Scanner sc = new Scanner(System.in);
        
        // Hedef Kayıtçıyı Belirleme: k[idx + 1] -> OKU komutundan sonra gelen ilk ve tek operandı (AX, BX, CX, DX) alır.
        // Örn: OKU AX komutunda "AX" string'ini alır.
        String hedef = k[idx + 1];  //k[idx + 1] ifadesi, çalışan komutun hedef kayıtçısının adını (AX, BX, CX, DX) veya operandını almak için kullanılır.
        // k : komut dizisi, idx: başlangıç indeksi, +1: operand ofseti (Komut adından hemen sonraki kelimeye (operand) geçişi sağlar.)
        
        System.out.print(hedef + " giriniz: ");
        int val = sc.nextInt(); // sc.nextInt(): Kullanıcının girdiği tamsayıyı okur. Bu değer başlangıçta 32-bit'lik 'int' değişkenine atanır.

        if (val < -128 || val > 127) {
            BayrakTasma = 1;
            val = (byte) val;
        } else {
            BayrakTasma = 0;
        }

        // Hedef Kayıtçının Güncellenmesi
        // Elde edilen (ve muhtemelen 8-bit'e indirgenmiş) val değeri, doğru kayıtçıya atanır.
        if (hedef.equals("AX")) AX = val;
        else if (hedef.equals("BX")) BX = val;
        else if (hedef.equals("CX")) CX = val;
        else if (hedef.equals("DX")) DX = val;

        pc++;  // OKU komutu tamamlandığı için Program Sayacı (PC) bir sonraki komuta ilerletilir.
    }

    static void toplam(String[] k) { // TOP AX BX 
        String hedef = k[1]; // Hedef kayıtçı (AX, BX, CX, DX)
        String kaynak = k[2]; // Kaynak operand (kayıtçı veya sabit sayı)
        int sonuc = 0; // Toplamanın sonucunu geçici olarak tutacak değişken

        if(hedef.equals("AX")) { // Kaynak operandın tipine göre toplama işlemi yapılır.
            if(kaynak.equals("AX")) sonuc = AX + AX;
            else if(kaynak.equals("BX")) sonuc = AX + BX;
            else if(kaynak.equals("CX")) sonuc = AX + CX;
            else if(kaynak.equals("DX")) sonuc = AX + DX;
            // Sabit Değer (Immediate) Kontrolü
            else sonuc = AX + Integer.parseInt(kaynak);// Integer.parseInt(kaynak) Nedir?: 'kaynak' değişkenindeki String ifadeyi (örneğin "10" veya "127") alıp
        // onu bir tamsayıya (int) dönüştürür. Bu, sabit (immediate) değerlerin aritmetik işlemlere dahil edilmesini sağlar.

            if(sonuc < -128 || sonuc > 127) {
                BayrakTasma = 1;
                // Bu indirgenmiş değer, kayıtçıya yazılacak nihai değerdir.
                sonuc = (byte) sonuc; // Sonuç, 8-bitlik taşma karşılığına indirgenir (byte cast).
            } else BayrakTasma = 0;
            //Bu, Java'da "Ternary Operator" (Üçlü Operatör) olarak bilinir. Bir koşul (sonuc == 0) doğruysa ilk değeri (1), yanlışsa ikinci değeri (0) atar.
            BayrakSifir = (sonuc == 0) ? 1 : 0;  // Amaç: İşlemin nihai sonucu (8-bit'e indirgenmiş) sıfır ise BayrakSifir (ZF) 1 olur.
            BayrakIsaret = (sonuc < 0) ? 1 : 0;  // Amaç: İşlemin nihai sonucu negatif ise BayrakIsaret (SF) 1 olur.
            AX = sonuc; // Kayıtçı, 8-bit'e indirgenmiş nihai sonuçla güncellenir.
        }
        else if(hedef.equals("BX")) {
            if(kaynak.equals("AX")) sonuc = BX + AX;
            else if(kaynak.equals("BX")) sonuc = BX + BX;
            else if(kaynak.equals("CX")) sonuc = BX + CX;
            else if(kaynak.equals("DX")) sonuc = BX + DX;
            else sonuc = BX + Integer.parseInt(kaynak);

            if(sonuc < -128 || sonuc > 127) {
                BayrakTasma = 1;
                sonuc = (byte) sonuc;
            } else BayrakTasma = 0;
            BayrakSifir = (sonuc == 0) ? 1 : 0;
            BayrakIsaret = (sonuc < 0) ? 1 : 0;           
            BX = sonuc;
        }
        else if(hedef.equals("CX")) {
            if(kaynak.equals("AX")) sonuc = CX + AX;
            else if(kaynak.equals("BX")) sonuc = CX + BX;
            else if(kaynak.equals("CX")) sonuc = CX + CX;
            else if(kaynak.equals("DX")) sonuc = CX + DX;
            else sonuc = CX + Integer.parseInt(kaynak);

            if(sonuc < -128 || sonuc > 127) {
                BayrakTasma = 1;
                sonuc = (byte) sonuc;
            } else BayrakTasma = 0;
            BayrakSifir = (sonuc == 0) ? 1 : 0;
            BayrakIsaret = (sonuc < 0) ? 1 : 0;
            CX = sonuc;
        }
        else if(hedef.equals("DX")) {
            if(kaynak.equals("AX")) sonuc = DX + AX;
            else if(kaynak.equals("BX")) sonuc = DX + BX;
            else if(kaynak.equals("CX")) sonuc = DX + CX;
            else if(kaynak.equals("DX")) sonuc = DX + DX;
            else sonuc = DX + Integer.parseInt(kaynak);

            if(sonuc < -128 || sonuc > 127) {
                BayrakTasma = 1;
                sonuc = (byte) sonuc;
            } else BayrakTasma = 0;

            BayrakSifir = (sonuc == 0) ? 1 : 0;
            BayrakIsaret = (sonuc < 0) ? 1 : 0;
            DX = sonuc;

            
        }

        pc++; // Komut başarılı bir şekilde yürütüldüğü için PC, bir sonraki komuta ilerletilir.
    }
    
    static void cikar(String[] k) {
        String hedef = k[1];
        String kaynak = k[2];
        int sonuc = 0;

        if(hedef.equals("AX")) {
            if(kaynak.equals("AX")) sonuc = AX - AX;
            else if(kaynak.equals("BX")) sonuc = AX - BX;
            else if(kaynak.equals("CX")) sonuc = AX - CX;
            else if(kaynak.equals("DX")) sonuc = AX - DX;
            else sonuc = AX - Integer.parseInt(kaynak);

            if(sonuc < -128 || sonuc > 127) {
                        BayrakTasma = 1;
                        sonuc = (byte) sonuc; 
                    } else {
                        BayrakTasma = 0;
                    }

                    BayrakSifir = (sonuc == 0) ? 1 : 0; 
                    BayrakIsaret = (sonuc < 0) ? 1 : 0; 
                    AX = sonuc;
        }
        else if(hedef.equals("BX")) {
            if(kaynak.equals("AX")) sonuc = BX - AX;
            else if(kaynak.equals("BX")) sonuc = BX - BX;
            else if(kaynak.equals("CX")) sonuc = BX - CX;
            else if(kaynak.equals("DX")) sonuc = BX - DX;
            else sonuc = BX - Integer.parseInt(kaynak);

            if(sonuc < -128 || sonuc > 127) {
                        BayrakTasma = 1;
                        sonuc = (byte) sonuc; 
                    } else {
                        BayrakTasma = 0;
                    }

                    BayrakSifir = (sonuc == 0) ? 1 : 0; 
                    BayrakIsaret = (sonuc < 0) ? 1 : 0; 
                    BX = sonuc;
        }
        else if(hedef.equals("CX")) {
            if(kaynak.equals("AX")) sonuc = CX - AX;
            else if(kaynak.equals("BX")) sonuc = CX - BX;
            else if(kaynak.equals("CX")) sonuc = CX - CX;
            else if(kaynak.equals("DX")) sonuc = CX - DX;
            else sonuc = CX - Integer.parseInt(kaynak);

            if(sonuc < -128 || sonuc > 127) {
                        BayrakTasma = 1;
                        sonuc = (byte) sonuc; 
                    } else {
                        BayrakTasma = 0;
                    }

                    BayrakSifir = (sonuc == 0) ? 1 : 0; 
                    BayrakIsaret = (sonuc < 0) ? 1 : 0; 
                    CX = sonuc;
        }
        else if(hedef.equals("DX")) {
            if(kaynak.equals("AX")) sonuc = DX - AX;
            else if(kaynak.equals("BX")) sonuc = DX - BX;
            else if(kaynak.equals("CX")) sonuc = DX - CX;
            else if(kaynak.equals("DX")) sonuc = DX - DX;
            else sonuc = DX - Integer.parseInt(kaynak);

            if(sonuc < -128 || sonuc > 127) {
                        BayrakTasma = 1;
                        sonuc = (byte) sonuc; 
                    } else {
                        BayrakTasma = 0;
                    }

                    BayrakSifir = (sonuc == 0) ? 1 : 0; 
                    BayrakIsaret = (sonuc < 0) ? 1 : 0; 
                    DX = sonuc;
        }


        pc++;
    }

    static void carp(String[] k) {
        String hedef = k[1];
        String kaynak = k[2];
        int sonuc = 0;

        if(hedef.equals("AX")) {
            if(kaynak.equals("AX")) sonuc = AX * AX;
            else if(kaynak.equals("BX")) sonuc = AX * BX;
            else if(kaynak.equals("CX")) sonuc = AX * CX;
            else if(kaynak.equals("DX")) sonuc = AX * DX;
            else sonuc = AX * Integer.parseInt(kaynak);

            if(sonuc < -128 || sonuc > 127) {
                        BayrakTasma = 1;
                        sonuc = (byte) sonuc; 
                    } else {
                        BayrakTasma = 0;
                    }

                    BayrakSifir = (sonuc == 0) ? 1 : 0; 
                    BayrakIsaret = (sonuc < 0) ? 1 : 0; 
                    AX = sonuc;
        }
        else if(hedef.equals("BX")) {
            if(kaynak.equals("AX")) sonuc = BX * AX;
            else if(kaynak.equals("BX")) sonuc = BX * BX;
            else if(kaynak.equals("CX")) sonuc = BX * CX;
            else if(kaynak.equals("DX")) sonuc = BX * DX;
            else sonuc = BX * Integer.parseInt(kaynak);

            if(sonuc < -128 || sonuc > 127) {
                        BayrakTasma = 1;
                        sonuc = (byte) sonuc; 
                    } else {
                        BayrakTasma = 0;
                    }

                    BayrakSifir = (sonuc == 0) ? 1 : 0; 
                    BayrakIsaret = (sonuc < 0) ? 1 : 0; 
                    BX = sonuc;
        }
        else if(hedef.equals("CX")) {
            if(kaynak.equals("AX")) sonuc = CX * AX;
            else if(kaynak.equals("BX")) sonuc = CX * BX;
            else if(kaynak.equals("CX")) sonuc = CX * CX;
            else if(kaynak.equals("DX")) sonuc = CX * DX;
            else sonuc = CX * Integer.parseInt(kaynak);

            if(sonuc < -128 || sonuc > 127) {
                        BayrakTasma = 1;
                        sonuc = (byte) sonuc; 
                    } else {
                        BayrakTasma = 0;
                    }

                    BayrakSifir = (sonuc == 0) ? 1 : 0; 
                    BayrakIsaret = (sonuc < 0) ? 1 : 0; 
                    CX = sonuc;
        }
        else if(hedef.equals("DX")) {
            if(kaynak.equals("AX")) sonuc = DX * AX;
            else if(kaynak.equals("BX")) sonuc = DX * BX;
            else if(kaynak.equals("CX")) sonuc = DX * CX;
            else if(kaynak.equals("DX")) sonuc = DX * DX;
            else sonuc = DX * Integer.parseInt(kaynak);
            
            if(sonuc < -128 || sonuc > 127) {
                        BayrakTasma = 1;
                        sonuc = (byte) sonuc; 
                    } else {
                        BayrakTasma = 0;
                    }

                    BayrakSifir = (sonuc == 0) ? 1 : 0; 
                    BayrakIsaret = (sonuc < 0) ? 1 : 0; 
                    DX = sonuc;
        }

        pc++;
    }

    static void bol(String[] k) {
        String hedef = k[1];
        String kaynak = k[2];
        int sonuc = 0;

        if(hedef.equals("AX")) {
            if(kaynak.equals("AX")) sonuc = AX / AX;
            else if(kaynak.equals("BX")) sonuc = AX / BX;
            else if(kaynak.equals("CX")) sonuc = AX / CX;
            else if(kaynak.equals("DX")) sonuc = AX / DX;
            else sonuc = AX / Integer.parseInt(kaynak);

            if(sonuc < -128 || sonuc > 127) {
                        BayrakTasma = 1;
                        sonuc = (byte) sonuc; 
                    } else {
                        BayrakTasma = 0;
                    }

                    BayrakSifir = (sonuc == 0) ? 1 : 0; 
                    BayrakIsaret = (sonuc < 0) ? 1 : 0; 
                    AX = sonuc;
        }
        else if(hedef.equals("BX")) {
            if(kaynak.equals("AX")) sonuc = BX / AX;
            else if(kaynak.equals("BX")) sonuc = BX / BX;
            else if(kaynak.equals("CX")) sonuc = BX / CX;
            else if(kaynak.equals("DX")) sonuc = BX / DX;
            else sonuc = BX / Integer.parseInt(kaynak);
            
            if(sonuc < -128 || sonuc > 127) {
                        BayrakTasma = 1;
                        sonuc = (byte) sonuc; 
                    } else {
                        BayrakTasma = 0;
                    }

                    BayrakSifir = (sonuc == 0) ? 1 : 0; 
                    BayrakIsaret = (sonuc < 0) ? 1 : 0; 
                    BX = sonuc;
        }
        else if(hedef.equals("CX")) {
            if(kaynak.equals("AX")) sonuc = CX / AX;
            else if(kaynak.equals("BX")) sonuc = CX / BX;
            else if(kaynak.equals("CX")) sonuc = CX / CX;
            else if(kaynak.equals("DX")) sonuc = CX / DX;
            else sonuc = CX / Integer.parseInt(kaynak);

            if(sonuc < -128 || sonuc > 127) {
                        BayrakTasma = 1;
                        sonuc = (byte) sonuc; 
                    } else {
                        BayrakTasma = 0;
                    }

                    BayrakSifir = (sonuc == 0) ? 1 : 0; 
                    BayrakIsaret = (sonuc < 0) ? 1 : 0; 
                    CX = sonuc;
        }
        else if(hedef.equals("DX")) {
            if(kaynak.equals("AX")) sonuc = DX / AX;
            else if(kaynak.equals("BX")) sonuc = DX / BX;
            else if(kaynak.equals("CX")) sonuc = DX / CX;
            else if(kaynak.equals("DX")) sonuc = DX / DX;
            else sonuc = DX / Integer.parseInt(kaynak);

            if(sonuc < -128 || sonuc > 127) {
                        BayrakTasma = 1;
                        sonuc = (byte) sonuc; 
                    } else {
                        BayrakTasma = 0;
                    }

                    BayrakSifir = (sonuc == 0) ? 1 : 0; 
                    BayrakIsaret = (sonuc < 0) ? 1 : 0; 
                    DX = sonuc;
        }

        pc++;
    }

    //"Lojik komutlarda (VE ve VEY), Taşma Bayrağı (BayrakTasma) her zaman sıfır (0) olarak atanır.
    static void ve(String[] k) {
        String hedef = k[1];
        String kaynak = k[2];
        int sonuc = 0;

        if (hedef.equals("AX")) {
            if (kaynak.equals("AX")) sonuc = AX & AX;
            else if (kaynak.equals("BX")) sonuc = AX & BX;
            else if (kaynak.equals("CX")) sonuc = AX & CX;
            else if (kaynak.equals("DX")) sonuc = AX & DX;
            else sonuc = AX & Integer.parseInt(kaynak);
            AX = sonuc;
            BayrakSifir = (sonuc == 0) ? 1 : 0;
            BayrakIsaret = (sonuc < 0) ? 1 : 0;
            BayrakTasma = 0; 
            
        } else if (hedef.equals("BX")) {
            if (kaynak.equals("AX")) sonuc = BX & AX;
            else if (kaynak.equals("BX")) sonuc = BX & BX;
            else if (kaynak.equals("CX")) sonuc = BX & CX;
            else if (kaynak.equals("DX")) sonuc = BX & DX;
            else sonuc = BX & Integer.parseInt(kaynak);
            BX = sonuc;
            BayrakSifir = (sonuc == 0) ? 1 : 0;
            BayrakIsaret = (sonuc < 0) ? 1 : 0;
            BayrakTasma = 0; 
            
        } else if (hedef.equals("CX")) {
            if (kaynak.equals("AX")) sonuc = CX & AX;
            else if (kaynak.equals("BX")) sonuc = CX & BX;
            else if (kaynak.equals("CX")) sonuc = CX & CX;
            else if (kaynak.equals("DX")) sonuc = CX & DX;
            else sonuc = CX & Integer.parseInt(kaynak);
            CX = sonuc;
            BayrakSifir = (sonuc == 0) ? 1 : 0;
            BayrakIsaret = (sonuc < 0) ? 1 : 0;
            BayrakTasma = 0; 
            
        } else if (hedef.equals("DX")) {
            if (kaynak.equals("AX")) sonuc = DX & AX;
            else if (kaynak.equals("BX")) sonuc = DX & BX;
            else if (kaynak.equals("CX")) sonuc = DX & CX;
            else if (kaynak.equals("DX")) sonuc = DX & DX;
            else sonuc = DX & Integer.parseInt(kaynak);
            DX = sonuc;
            BayrakSifir = (sonuc == 0) ? 1 : 0;
            BayrakIsaret = (sonuc < 0) ? 1 : 0;
            BayrakTasma = 0;             
        }

        pc++;
    }

//Lojik İşlem: VE ve VEY gibi komutlar ise aritmetik değil, bitsel (bitwise) işlemlerdir. 
//Bu işlemler, sayıların matematiksel değerini değil, her bir bitin durumunu değiştirir. Bu nedenle, lojik işlemler işaretli aritmetik taşmaya neden olmaz.    

 //"Lojik komutlarda (VE ve VEY), Taşma Bayrağı (BayrakTasma) her zaman sıfır (0) olarak atanır.
    static void vey(String[] k) {
        String hedef = k[1];
        String kaynak = k[2];
        int sonuc = 0;

        if (hedef.equals("AX")) {
            if (kaynak.equals("AX")) sonuc = AX | AX;
            else if (kaynak.equals("BX")) sonuc = AX | BX;
            else if (kaynak.equals("CX")) sonuc = AX | CX;
            else if (kaynak.equals("DX")) sonuc = AX | DX;
            else sonuc = AX | Integer.parseInt(kaynak);
            AX = sonuc;
            BayrakSifir = (sonuc == 0) ? 1 : 0;
            BayrakIsaret = (sonuc < 0) ? 1 : 0;
            BayrakTasma = 0; 
            
        } else if (hedef.equals("BX")) {
            if (kaynak.equals("AX")) sonuc = BX | AX;
            else if (kaynak.equals("BX")) sonuc = BX | BX;
            else if (kaynak.equals("CX")) sonuc = BX | CX;
            else if (kaynak.equals("DX")) sonuc = BX | DX;
            else sonuc = BX | Integer.parseInt(kaynak);
            BX = sonuc;
            BayrakSifir = (sonuc == 0) ? 1 : 0;
            BayrakIsaret = (sonuc < 0) ? 1 : 0;
            BayrakTasma = 0; 
            
        } else if (hedef.equals("CX")) {
            if (kaynak.equals("AX")) sonuc = CX | AX;
            else if (kaynak.equals("BX")) sonuc = CX | BX;
            else if (kaynak.equals("CX")) sonuc = CX | CX;
            else if (kaynak.equals("DX")) sonuc = CX | DX;
            else sonuc = CX | Integer.parseInt(kaynak);
            CX = sonuc;
            BayrakSifir = (sonuc == 0) ? 1 : 0;
            BayrakIsaret = (sonuc < 0) ? 1 : 0;
            BayrakTasma = 0; 
            
        } else if (hedef.equals("DX")) {
            if (kaynak.equals("AX")) sonuc = DX | AX;
            else if (kaynak.equals("BX")) sonuc = DX | BX;
            else if (kaynak.equals("CX")) sonuc = DX | CX;
            else if (kaynak.equals("DX")) sonuc = DX | DX;
            else sonuc = DX | Integer.parseInt(kaynak);
            DX = sonuc;
            BayrakSifir = (sonuc == 0) ? 1 : 0;
            BayrakIsaret = (sonuc < 0) ? 1 : 0;
            BayrakTasma = 0;             
        }

        pc++;
    }

    static void deg(String[] k) {
        String hedef = k[1]; // Hedef kayıtçı adını alır (Örn: AX). Tek operand olduğu için sadece k[1] kullanılır.
        int sonuc = 0; // İşlem sonucunu tutacak geçici değişken.

        if (hedef.equals("AX")) {
            // Mantık: Eğer AX'in değeri 0 ise, sonuc 1 olur (DEĞİL 0, yani TRUE).
            if (AX == 0) 
                sonuc = 1;
            // Eğer AX'in değeri 0 değilse (yani herhangi bir değerse), sonuc 0 olur (DEĞİL TRUE, yani 0).
            else
                sonuc = 0;
        }
        else if (hedef.equals("BX")) {
            if (BX == 0)
                sonuc = 1;
            else
                sonuc = 0;
        }
        else if (hedef.equals("CX")) {
            if (CX == 0)
                sonuc = 1;
            else
                sonuc = 0;
        }
        else if (hedef.equals("DX")) {
            if (DX == 0)
                sonuc = 1;
            else
                sonuc = 0;
        }

        // Kayıtçının Güncellenmesi
        // Hesaplanan nihai sonuç (0 veya 1), hedef kayıtçıya atanır.
        if (hedef.equals("AX")) AX = sonuc;
        else if (hedef.equals("BX")) BX = sonuc;
        else if (hedef.equals("CX")) CX = sonuc;
        else if (hedef.equals("DX")) DX = sonuc;
        
        //BayrakSifir: Sonucun değeri 0 veya 1 olabilir. Eğer sonuç 0 ise (yani kayıtçı daha önce 0'dan farklıydı), ZF=1 olur.
        BayrakSifir = (sonuc == 0) ? 1 : 0;
        // BayrakIsaret: Sonuc ya 0 ya da 1 olacağı için, negatif olma durumu yoktur (SF=0).
        BayrakIsaret = 0;  
        // BayrakTasma: Lojik bir işlem olduğu için taşma olmaz (OF=0).
        BayrakTasma = 0;

        pc++; // Program Akışının İlerletilmesi
    }

    static void atama(String[] k) { // ATM AX 10 , AX= hedef 10= kaynak
        String hedef = k[1]; // Hedef konum (AX, [BX], 10, [100] vb.)
        String kaynak = k[2]; // Kaynak değer (AX, 50, [CX], [200] vb.)

    // Bellek Adresleme Kontrolü
    // Hedef veya kaynağın köşeli parantez ([...]) içerip içermediğini kontrol eder.
    // Bu, dolaylı adreslemeyi veya sabit bellek adresini temsil eder.
        boolean hedefBellek = hedef.startsWith("[") && hedef.endsWith("]");
        boolean kaynakBellek = kaynak.startsWith("[") && kaynak.endsWith("]");

        int value = 0; // Kaynaktan çekilen değeri tutacak geçici değişken.

        //1. KAYNAK DEĞERİNİ OKUMA (SOURCE FETCH)
        if (kaynakBellek) { // Kaynak bir bellek adresi ise (Örn: ATM AX [BX] veya ATM AX [100])
            String inner = kaynak.substring(1, kaynak.length() - 1); // Köşeli parantezler içindeki kısmı al (adres).
            // Örn: Kaynak "[BX]" ise, inner = "BX" olur. Kaynak "[100]" ise, inner = "100" olur.
            
            // Kayıtçı Dolaylı Adresleme (Örn: RAM[AX] değerini al)
            if (inner.equals("AX")) value = RAM[AX];
            else if (inner.equals("BX")) value = RAM[BX];
            else if (inner.equals("CX")) value = RAM[CX];
            else if (inner.equals("DX")) value = RAM[DX];
            else value = RAM[Integer.parseInt(inner)]; // Sabit Adresleme (Örn: RAM[100] değerini al)

            // Sabit (Immediate) Değer Kaynağı (Örn: ATM AX 50)
        } else if (kaynak.equals("AX")) value = AX;
        else if (kaynak.equals("BX")) value = BX;
        else if (kaynak.equals("CX")) value = CX;
        else if (kaynak.equals("DX")) value = DX;
        else value = Integer.parseInt(kaynak); // ATM AX 50 ise value = 50

        //2. HEDEF KONUMUNA YAZMA (DESTINATION WRITE)
        if (hedefBellek) { // Hedef bir bellek adresi ise (Örn: ATM [BX] AX veya ATM [100] 50)
            String inner = hedef.substring(1, hedef.length() - 1); // Köşeli parantez içindeki adresi al.

            // Kayıtçı Dolaylı Adresleme (Örn: RAM[AX] = value)
            if (inner.equals("AX")) RAM[AX] = value;
            else if (inner.equals("BX")) RAM[BX] = value;
            else if (inner.equals("CX")) RAM[CX] = value;
            else if (inner.equals("DX")) RAM[DX] = value;
            else RAM[Integer.parseInt(inner)] = value; // Sabit Adresleme (Örn: RAM[100] = value)

            // Sabit (Immediate) Değer Kaynağı (Örn: ATM AX 50)
        } else if (hedef.equals("AX")) AX = value;
        else if (hedef.equals("BX")) BX = value;
        else if (hedef.equals("CX")) CX = value;
        else if (hedef.equals("DX")) DX = value;

        pc++; // Program Akışının İlerletilmesi
    }

    
    static void d_git(String[] k) {
        // Koşulsuz olduğu için direkt atlama metodunu çağırır.
        gotoLabel(k[1]);
    }


    static void de_git(String[] k) {
        // BayrakSifir (ZF) 1 ise atla (Önceki işlem sonucu sıfır demektir).
        if (BayrakSifir == 1) gotoLabel(k[1]);
        else pc++; // Koşul sağlanmazsa, PC normal akışta bir sonraki komuta ilerler.
    }


    static void ded_git(String[] k) {
        // BayrakSifir (ZF) 0 ise atla (Önceki işlem sonucu sıfır değil demektir).
        if (BayrakSifir == 0) gotoLabel(k[1]);
        else pc++;
    }


    static void db_git(String[] k) {
        // İşaret Bayrağı 0 (Pozitif) VE Sıfır Bayrağı 0 (Sıfır değil) ise atla.
        if (BayrakIsaret == 0 && BayrakSifir == 0) gotoLabel(k[1]);
        else pc++;
    }


    static void dbe_git(String[] k) {
        // İşaret Bayrağı 0 (Pozitif) VEYA Sıfır Bayrağı 1 (Eşit) ise atla.
        if (BayrakIsaret == 0 || BayrakSifir == 1) {
            gotoLabel(k[1]);
        } else {
            pc++;
        }
    }



    static void dk_git(String[] k) {
        // İşaret Bayrağı 1 (Negatif) ise atla.
    if (BayrakIsaret == 1) gotoLabel(k[1]);
    else pc++;
}


    static void dke_git(String[] k) {
        // İşaret Bayrağı 1 (Negatif) VEYA Sıfır Bayrağı 1 (Eşit) ise atla.
        if (BayrakIsaret == 1 || BayrakSifir == 1) gotoLabel(k[1]);
        else pc++;
    }


    static void yaz(String[] k) {
        String hedef = k[1]; // Hedef (yazdırılacak) operandı alır (Örn: AX, [BX], 50).
        int sonuc = 0; // Yazdırılacak nihai değeri tutar.

        // try-catch bloğu: Özellikle Integer.parseInt veya RAM'e hatalı indeksleme (adrese erişim hatası) durumlarını yakalar.
        try {
            // Bellek Adresinden Okuma Kontrolü (Örn: YAZ [BX] veya YAZ [10])
            if (hedef.startsWith("[") && hedef.endsWith("]")) {
                String reg = hedef.substring(1, hedef.length() - 1); // Köşeli parantez içindeki kısmı ayırır (adresin kendisi).
                
                int addr = 0; // RAM içindeki indeksi (adresi) tutar.
 
                // Adres Hesaplama (Kayıtçı Dolaylı Adresleme)
                if (reg.equals("AX")) addr = AX;
                else if (reg.equals("BX")) addr = BX;
                else if (reg.equals("CX")) addr = CX;
                else if (reg.equals("DX")) addr = DX;
                else addr = Integer.parseInt(reg); // Adres Hesaplama (Sabit Adresleme)
                sonuc = RAM[addr]; // Belirlenen adresteki değeri alır.
              
                // Kayıtçıdan veya Sabit Değerden Okuma Kontrolü (Örn: YAZ AX veya YAZ 50)
            } else if (hedef.equals("AX")) sonuc = AX;
            else if (hedef.equals("BX")) sonuc = BX;
            else if (hedef.equals("CX")) sonuc = CX;
            else if (hedef.equals("DX")) sonuc = DX;
            // Sabit Değer Yazdırma (Örn: YAZ 50)
            else sonuc = Integer.parseInt(hedef);  // Operandı doğrudan tamsayıya çevirir.
        } catch (Exception e) {
            System.out.println("HATA (yaz): " + e.getMessage());
        }

        System.out.println(sonuc); // Hesaplanan/bulunan nihai değeri ekrana yazdırır.
        pc++; // Program Akışının İlerletilmesi
    }
    
    //Bu metot, koşullu veya koşulsuz dallanma komutlarının (Jump Instructions) Program
    //Sayacını (pc) doğru hedefe ayarlamasını sağlayan temel yardımcı fonksiyondur.
    
    static boolean gotoLabel(String label) { // Etiket Kontrolü: Yükleme sırasında (loadProgram) oluşturulan 'labels' HashMap'inde etiket var mı?
    if (!labels.containsKey(label)) {
        // Hata Durumu (Etiket Bulunamadı): Etiket programda tanımlanmamışsa.
        System.out.println("HATA: Label bulunamadı → " + label);
        pc++; // PC'yi Artır: Atlamak istenen hedef bulunamadığı için Program Sayacı bir sonraki satıra ilerletilir. 
        return false; // İşlem başarısız oldu.
    }
    // Başarı Durumu (Etiket Bulundu): Etiket varsa, hedef satır numarasını HashMap'ten al.
    // pc = labels.get(label);
    // Program Sayacı (PC), etiket adıyla eşleşen satır numarasına (index) ayarlanır. Bu, programın akışını anında o satıra taşır.
    pc = labels.get(label);
    return true; // İşlem başarılı oldu.
}


}
