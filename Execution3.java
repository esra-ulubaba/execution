package execution3;

import java.io.*;
import java.util.*;

public class Execution3{

    static int AX = 0, BX = 0, CX = 0, DX = 0;
    static int[] RAM = new int[256];

    static int BayrakSifir = 0;
    static int BayrakIsaret = 0;
    static int BayrakTasma = 0;

    static List<String[]> program = new ArrayList<>();
    static Map<String, Integer> labels = new HashMap<>();

    static int pc = 0;

    public static void main(String[] args) {

        String filePath = "C:\\Users\\WINUSER\\Desktop\\Program Yapısı ve Anlamı-Dosyalar\\program.txt";

        loadProgram(filePath);
        executeProgram();
    }

    static void loadProgram(String file) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String satir;

            while ((satir = br.readLine()) != null) {
                satir = satir.trim();
                if (satir.equals("")) continue;

                
                if (satir.contains(":")) {
                    String label = satir.substring(0, satir.indexOf(":")).trim();

                   
                    labels.put(label, program.size());

                    satir = satir.substring(satir.indexOf(":") + 1).trim();

                    if (satir.equals("")) {
                        program.add(new String[]{"NOP"});
                    } else {
                        program.add(satir.split("\\s+"));
                    }
                }
                else {
                    program.add(satir.split("\\s+"));
                }
            }

            br.close();
        } catch (IOException e) {
            System.out.println("HATA: " + e.getMessage());
        }
    }

    static void executeProgram() {
        pc = 0;

        while (pc < program.size()) {

            String[] k = program.get(pc);
            String komut = k[0];

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

                case "SON": 
                    System.out.println("Program SON komutu ile bitti.");
                    System.out.println("AX = " + AX);
                    System.out.println("BX = " + BX);
                    System.out.println("CX = " + CX);
                    System.out.println("DX = " + DX);
                    return;

                default: 
                    System.out.println("Bilinmeyen komut: " + komut);
                    pc++;
                    break;
            }
        }
    }

    static void oku(String[] k, int idx) {
        Scanner sc = new Scanner(System.in);
        String hedef = k[idx + 1];

        System.out.print(hedef + " giriniz: ");
        int val = sc.nextInt();

        if (val < -128 || val > 127) {
            BayrakTasma = 1;
            val = (byte) val;
        } else {
            BayrakTasma = 0;
        }

        if (hedef.equals("AX")) AX = val;
        else if (hedef.equals("BX")) BX = val;
        else if (hedef.equals("CX")) CX = val;
        else if (hedef.equals("DX")) DX = val;

        pc++;
    }

    static void toplam(String[] k) {
        String hedef = k[1];
        String kaynak = k[2];
        int sonuc = 0;

        if(hedef.equals("AX")) {
            if(kaynak.equals("AX")) sonuc = AX + AX;
            else if(kaynak.equals("BX")) sonuc = AX + BX;
            else if(kaynak.equals("CX")) sonuc = AX + CX;
            else if(kaynak.equals("DX")) sonuc = AX + DX;
            else sonuc = AX + Integer.parseInt(kaynak);

            if(sonuc < -128 || sonuc > 127) {
                BayrakTasma = 1;
                sonuc = (byte) sonuc;
            } else BayrakTasma = 0;
            BayrakSifir = (sonuc == 0) ? 1 : 0;
            BayrakIsaret = (sonuc < 0) ? 1 : 0;
            AX = sonuc;
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

        pc++;
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
        String hedef = k[1];
        int sonuc = 0;

        if (hedef.equals("AX")) {
            if (AX == 0)
                sonuc = 1;
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


        if (hedef.equals("AX")) AX = sonuc;
        else if (hedef.equals("BX")) BX = sonuc;
        else if (hedef.equals("CX")) CX = sonuc;
        else if (hedef.equals("DX")) DX = sonuc;
        
        BayrakSifir = (sonuc == 0) ? 1 : 0;
        BayrakIsaret = 0;         
        BayrakTasma = 0;

        pc++;
    }

    static void atama(String[] k) {
        String hedef = k[1];
        String kaynak = k[2];

        boolean hedefBellek = hedef.startsWith("[") && hedef.endsWith("]");
        boolean kaynakBellek = kaynak.startsWith("[") && kaynak.endsWith("]");

        int value = 0;

        if (kaynakBellek) {
            String inner = kaynak.substring(1, kaynak.length() - 1);

            if (inner.equals("AX")) value = RAM[AX];
            else if (inner.equals("BX")) value = RAM[BX];
            else if (inner.equals("CX")) value = RAM[CX];
            else if (inner.equals("DX")) value = RAM[DX];
            else value = RAM[Integer.parseInt(inner)];

        } else if (kaynak.equals("AX")) value = AX;
        else if (kaynak.equals("BX")) value = BX;
        else if (kaynak.equals("CX")) value = CX;
        else if (kaynak.equals("DX")) value = DX;
        else value = Integer.parseInt(kaynak);

        if (hedefBellek) {
            String inner = hedef.substring(1, hedef.length() - 1);

            if (inner.equals("AX")) RAM[AX] = value;
            else if (inner.equals("BX")) RAM[BX] = value;
            else if (inner.equals("CX")) RAM[CX] = value;
            else if (inner.equals("DX")) RAM[DX] = value;
            else RAM[Integer.parseInt(inner)] = value;

        } else if (hedef.equals("AX")) AX = value;
        else if (hedef.equals("BX")) BX = value;
        else if (hedef.equals("CX")) CX = value;
        else if (hedef.equals("DX")) DX = value;

        pc++;
    }

    
    static void d_git(String[] k) {
        gotoLabel(k[1]);
    }


    static void de_git(String[] k) {
        if (BayrakSifir == 1) gotoLabel(k[1]);
        else pc++;
    }


    static void ded_git(String[] k) {
        if (BayrakSifir == 0) gotoLabel(k[1]);
        else pc++;
    }


    static void db_git(String[] k) {
        if (BayrakIsaret == 0 && BayrakSifir == 0) gotoLabel(k[1]);
        else pc++;
    }


    static void dbe_git(String[] k) {
        if (BayrakIsaret == 0 || BayrakSifir == 1) {
            gotoLabel(k[1]);
        } else {
            pc++;
        }
    }



    static void dk_git(String[] k) {
        if (BayrakIsaret == 1) gotoLabel(k[1]);
        else pc++;
    }


    static void dke_git(String[] k) {
        if (BayrakIsaret == 1 || BayrakSifir == 1) gotoLabel(k[1]);
        else pc++;
    }


    static void yaz(String[] k) {
        String hedef = k[1];
        int sonuc = 0;

        try {
            if (hedef.startsWith("[") && hedef.endsWith("]")) {
                String reg = hedef.substring(1, hedef.length() - 1);
                int addr = 0;
                if (reg.equals("AX")) addr = AX;
                else if (reg.equals("BX")) addr = BX;
                else if (reg.equals("CX")) addr = CX;
                else if (reg.equals("DX")) addr = DX;
                else addr = Integer.parseInt(reg);
                sonuc = RAM[addr];
            } else if (hedef.equals("AX")) sonuc = AX;
            else if (hedef.equals("BX")) sonuc = BX;
            else if (hedef.equals("CX")) sonuc = CX;
            else if (hedef.equals("DX")) sonuc = DX;
            else sonuc = Integer.parseInt(hedef); 
        } catch (Exception e) {
            System.out.println("HATA (yaz): " + e.getMessage());
        }

        System.out.println(sonuc);
        pc++;
    }
    
    static boolean gotoLabel(String label) {
    if (!labels.containsKey(label)) {
        System.out.println("HATA: Label bulunamadı → " + label);
        pc++; 
        return false;
    }
        pc = labels.get(label);
        return true;
    }

}