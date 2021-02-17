# Program Words

Když se moje dcera začala ve škole učit angličtinu, tak jsem pro ní vyrobil jednoduchý slovníček, ve kterém si zadává slovíčka a zařazuje si je do různých kategorií. Slovník pro každé slovíčko nebo větu stáhne z Googlu správnou výslovnost. Dcera je tak v učení slovíček zcela soběstačná.

Třeba ten slovník pomůže i někomu dalšímu.

## 1. Jak aplikaci Words stáhnout a nainstalovat

Program Words je program napsaný v Javě a běží na následujících operačních systémech:

* MS Windows
* Linux
* MAC OS

### 1.1 MS Windows

Pokud na svém počítači používáte MS Windows, tak můžete stáhnout instalátor a program nainstalovat. [Stáhněte](https://github.com/berk76/words/releases/latest) si nejnovější verzi programu - např. __Words-1.11.0-RELEASE-Installer.exe__. Instalátor vytvoří ikonu na ploše a spustí program. 


### 1.2 Ostatní operační systémy

Aby bylo možné na počítači Words spustit tak je nutné nainstalovat Java Runtime. Pokud máte na počítači nainstalovaný Java Runtime, tak můžete stáhnout aplikaci Words. [Stáhněte](https://github.com/berk76/words/releases/latest) si nejnovější verzi programu - např. __Words-1.11.0-RELEASE.jar__.  

Na disku počítače vytvořte adresář __Words__ a do něj nakopírujte stažený soubor __Words-1.11.0-RELEASE.jar__. Program nyní spustíte poklepáním myší na soubor __Words-1.11.0-RELEASE.jar__ jako byste spouštěli jakýkoliv jiný spustitelný soubor. Případně z příkazového řádku spusťte `java -jar Words-1.11.0-RELEASE.jar`.


## 2. Použití

### 2.1 První spuštění programu

Při prvním spuštění se na obrazovce se objeví dialog, který vás požádá o výběr jazyka pro který budete chtít stahovat výslovnost. Vyberte například __English (United Kingdom)__. Pokaždé když zadáte do slovníku nové slovíčko, tak pro něj slovník bude hledat anglickou výslovnost.

![Language chooser](../gfx/LangChooser.png)

Následně se objeví hlavní okno slovníku, který zatím neobsahuje žádná slovíčka. Abyste zadali nové slovíčko, tak zvolte v levé horní části menu __Word__ a v roletovém menu vyberte __Add...__. Objeví se dialog pro zadání nového slovíčka. Do pole __Native word__ zadejte české slovo a do pole __Foreign word__ zadejte anglické slovíčko. Poté zvolte __Add Category...__ a vytvořte novou kategorii (například Lekce 1). Nakonec potvrďte tlačítkem __OK__. Program vás upozorní, že pro nové slovíčko nemá výslovnost a zeptá se zda jí má stáhnout. Potvrďte volbou __yes__.

![Edit word](../gfx/EditWord.png)

Tímto způsobem můžete zadávat další slovíčka a řadit je do různých kategorií.

### 2.2 Učení slovíček

Učení probíhá tak, že si student vybere kategorii kterou se chce učit. Na obrazovce se objeví české slovo a student ho řekne nahlas anglicky. Může a nemusí ho zkusit i napsat. Pak zmáčkne tlačítko __Show & Play__ a na obrazovce se objeví anglické slovíčko a zároveň se přehraje správná výslovnost. Student pak pokračuje na další slovíčko tlačítkem __Good__ pokud věděl správně, nebo tlačítkem __Wrong__ pokud udělal chybu. Slovník pak řadí slovíčka v kategoriích tak, aby na začátku byly slovíčka s horším skóre.

![Main window](../gfx/Words.png)
 
### 2.3 Další možnosti

Dále je možné:

* Vyhledávat slovíčka __Word - Find__
* Zadávat nová slovíčka __Word - Add...__
* Editovat slovíčka a měnit zařazení do kategorie __Word - Edit...__
* Smazat slovíčko __Word - Delete__
* Založit novou kategorii __Category - Add...__
* Přejmenovat kategorii __Category - Rename...__

## 3. Kontakt

Pokud narazíte na problém, se kterým si nebudete vědět rady, nebo na nějakou chybu programu, tak můžete kontaktovat autora na následující e-mailové adrese [jaroslav.beran@gmail.com](mailto:jaroslav.beran@gmail.com).  

## 4. Spolupráce

Pokud programujete a chtěli byste program rozšířit o nějakou vychytávku, tak jste vítání. Udělejte si __Fork__ projektu a program upravte a odlaďte. Nakonec udělejte __Pull request__ a svojí úpravu v něm popište.
