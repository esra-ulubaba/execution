# 🤖 EXECUTION3: El-Ceziri 8-Bit İşlemci Simülasyonu

Bu proje, verilen Assembly komutları ve mimari kısıtlamaları temel alarak 8-bitlik El-Ceziri işlemcisinin yürütme (Execution) mantığını simüle etmektedir.

## 🎯 Proje Amacı

Hazırlanan program, El-Ceziri Assembly dilinde yazılmış bir programın kaynak dosyasını girdi olarak alır ve komutları satır satır işleyerek işlemcinin yaptığı tüm aritmetik, lojik ve kontrol akışı işlemlerini taklit eder. 

## ⚙️ Mimarinin Simülasyonu

Simülasyon, özellikle 8-bitlik işaretli tamsayı kısıtlamasını ve bellek yönetimini titizlikle uygulamaktadır.

### 1. Kayıtçılar ve Bellek

| Bileşen | Tanım | Açıklama |
| :--- | :--- | :--- |
| **Kayıtçılar** | `int AX, BX, CX, DX` | İşlemcinin 4 adet 8-bitlik kayıtçısının taklidi. Değerler [-128, 127] aralığında kalır. |
| **RAM** | `int RAM[256]` |256 elemanlı belleğin taklidi. |
| **Bayraklar** | `BayrakSifir, BayrakIsaret, BayrakTasma` |İşlemlerin sonuç durumunu tutar. |

### 2. Adresleme Modları ve Veri Akışı (`ATM` Komutu)

`ATM Hedef Kaynak` kuralına göre veri transferi yapılır. 

* **Veri Akışı:** Program, **Kaynaktan** değeri okur ve **Hedef** konuma yazar.
* **Dolaylı Adresleme:** `ATM` komutu, `ATM AX 50` (Sabit Değerden Kayıtçıya) ve `ATM [BX] AX` (Kayıtçı Dolaylı Adreslemeyle RAM'e yazma) gibi tüm adresleme modlarını destekler.

### 3. Aritmetik Mantık Birimi (ALU) Kuralları

Tüm aritmetik ve lojik komutlar, **8-bit işaretli tamsayı** kurallarına uyarak çalışır:

* **Aritmetik İşlemler (TOP, CIK, CRP, BOL):** Sonuçlar, Java'nın 32-bit `int`'inde hesaplanır. Eğer sonuç $\mathbf{[-128, 127]}$ aralığını aşarsa:
    * `BayrakTasma` (OF) $\mathbf{1}$ olarak ayarlanır.
    * Sonuç, `(byte) sonuc` işlemi ile $\mathbf{8-bit'lik taşma değerine indirgenir}$ ve kayıtçıya bu değer atanır.
* **Lojik İşlemler (VE, VEY, DEG):** Bu işlemler işaretli aritmetik taşmaya neden olmadığından, $\mathbf{BayrakTasma}$ daima $\mathbf{0}$'dır.

### 4. Kontrol Akışı ve Etiket Yönetimi

Program, kaynak kodu yüklerken dallanma hedeflerini yönetmek için bir `labels` Haritası kullanır:

1.  **Etiket Yükleme:** `loadProgram` metodu, `ETIKET1:` gibi ifadeleri bulur ve bu etiketi, program listesindeki karşılık gelen satır numarasına (`program.size()`) atar (`labels.put()`).
2.  **Dallanma (D, DE, DK):** Atlama komutları, `labels.get(etiket_adı)` ile hedef adresi bulur ve Program Sayacını (`pc`) anında o adrese ayarlar.
3.  **Hata Kontrolü:** Eğer dallanma komutunda etiket bulunamazsa, kullanıcıya hata mesajı basılır ve `pc++` ile program akışı devam ettirilir.

## 🚀 Örnek Çalıştırma

Aşağıdaki örnek kaynak kod, kullanıcıdan alınan tamsayıya kadar olan sayıları RAM'e yazar ve ardından ekrana basar.

### Kaynak Kod (`ornek.txt`)

```assembly
OKU CX           ; Kullanıcıdan N değerini al (Örn: 7)
ATM DX CX        ; DX'e N değerini yedekle
ATM BX 1         ; RAM adresi sayacını 1'den başlat
ATM AX 0         ; Yazılacak değeri 0'dan başlat
ETIKET1: CIK CX BX  ; CX = CX - BX yap (Döngü kontrolü)
DE ETIKET2       ; Sonuç sıfırsa (CX = BX) döngüden çık
TOP AX 1
ATM [BX] AX      ; AX'i RAM[BX] adresine yaz
ATM CX DX        ; CX'i yedeğinden (N) geri yükle
TOP BX 1         ; Adresi/Sayacı artır
D ETIKET1
ETIKET2: ATM CX DX
ATM BX 1
ETIKET3: CIK CX BX
DE ETIKET4
YAZ [BX]         ; RAM[BX]'deki değeri yazdır
TOP BX 1
ATM CX DX
D ETIKET3
ETIKET4: SON
