# TP1 – Injection de dépendances en Java

Ce projet reprend les exemples du TP sur l’injection de dépendances, d’abord sans Spring, puis avec Spring en XML et avec des annotations. Les captures suivantes montrent les différentes étapes réalisées dans IntelliJ ; certaines correspondent donc à un état intermédiaire du code.

## Organisation du projet

```text
net.michel
├── dao       IDao et DaoImpl (source « Base de données »)
├── ext       DaoImplV2 (source « Capteurs »)
├── metier    IMetier et MetierImpl
└── pres      présentation

src/main/resources/config.xml   configuration Spring en XML
config.txt                      configuration utilisée par Pres2
```

## 1. Sans Spring

### 1.1 Instanciation directe

Dans `Pres1`, le programme crée lui-même les objets. Il connaît donc la classe concrète `DaoImpl`.

![Pres1 instancie directement DaoImpl](<captures/Capture d'écran 2026-10-07 141343.png>)

Le DAO est passé au constructeur de `MetierImpl`, puis aussi au setter dans cet exemple. En pratique, une seule de ces deux injections suffit. L’exécution affiche `Version Base de données` et un résultat d’environ `640,88`.

### 1.2 Charger le DAO depuis `config.txt`

`Pres2` lit le nom de la classe du DAO dans un fichier et la charge avec la réflexion. Le code dépend de l’interface `IDao`, plutôt que de `DaoImpl` directement.

![Pres2 lit le nom du DAO dans config.txt](<captures/Capture d'écran 2026-10-07 141504.png>)

```java
Scanner scanner = new Scanner(new File("config.txt"));
String daoClassName = scanner.nextLine();
Class cDao = Class.forName(daoClassName);
IDao dao = (IDao) cDao.newInstance();
System.out.println(dao.getData());
```

À ce stade, le programme affiche la valeur brute du DAO (`34.0`), sans passer par le calcul métier. `Class.newInstance()` est dépréciée ; on peut la remplacer par `cDao.getDeclaredConstructor().newInstance()`.

### 1.3 Ajouter une seconde implémentation

`DaoImplV2` implémente la même interface, mais représente une autre source de données, ici des capteurs.

![Implémentation DaoImplV2 pour les capteurs](<captures/Capture d'écran 2026-10-07 142334.png>)

Si on veut l’utiliser en l’instanciant directement, il faut modifier `Pres1` pour importer et créer `DaoImplV2` :

![Pres1 modifié pour utiliser DaoImplV2](<captures/Capture d'écran 2026-10-07 142718.png>)

Le résultat change bien (environ `226,19`), mais il faut alors modifier et recompiler le programme à chaque changement de DAO.

### 1.4 Choisir le DAO dans le fichier de configuration

Avec `Pres2`, le choix peut se faire dans `config.txt` sans modifier le code Java. Le fichier contient le nom du DAO, puis celui de la classe métier :

```text
net.michel.ext.DaoImplV2
net.michel.metier.MetierImpl
```

La capture ci-dessous montre une ancienne valeur erronée (`net.michel.dao.DaoImplV2`). Le bon package est `net.michel.ext` ; c’est le nom utilisé dans le `config.txt` actuel.

![Ancienne configuration avec un package incorrect](<captures/Capture d'écran 2026-10-07 142553.png>)

### 1.5 Instancier le métier et lui injecter le DAO

On peut également charger la classe métier depuis le fichier et lui transmettre le DAO par son constructeur :

```java
String metierClassName = scanner.nextLine();
Class cMetier = Class.forName(metierClassName);
IMetier metier =
        (IMetier) cMetier.getConstructor(IDao.class).newInstance(dao);
System.out.println("RES = " + metier.calcul());
```

![Pres2 charge le métier et injecte le DAO par le constructeur](<captures/Capture d'écran 2026-10-07 142729.png>)

Une autre possibilité consiste à créer le métier sans argument, puis à appeler son setter par réflexion :

```java
IMetier metier = (IMetier) cMetier.getConstructor().newInstance();
Method setDao = cMetier.getDeclaredMethod("setDao", IDao.class);
setDao.invoke(metier, dao);
System.out.println("RES = " + metier.calcul());
```

![Pres2 injecte le DAO avec setDao](<captures/Capture d'écran 2026-10-07 162213.png>)

## 2. Avec Spring et un fichier XML

### 2.1 Ajouter les dépendances

Le projet utilise `spring-core`, `spring-beans` et `spring-context` en version `6.2.3`.

![Dépendances Spring dans pom.xml](<captures/Capture d'écran 2026-10-07 145426.png>)

![Dépendances et alertes affichées dans IntelliJ](<captures/Capture d'écran 2026-10-07 152905.png>)

IntelliJ signale des alertes de vulnérabilité sur cette version. Pour un exercice, elles sont simplement à noter ; pour un vrai projet, il faudrait choisir une version corrigée.

### 2.2 Déclarer les objets dans `config.xml`

Spring crée le DAO et le métier à partir de ce fichier. La propriété `dao` correspond au setter `setDao` de `MetierImpl`.

```xml
<bean id="d" class="net.michel.dao.DaoImpl"></bean>
<bean id="metier" class="net.michel.metier.MetierImpl">
    <property name="dao" ref="d"></property>
</bean>
```

![Déclaration des beans dans config.xml](<captures/Capture d'écran 2026-10-12 183240.png>)

Pour choisir l’autre DAO dans cette configuration, il faut remplacer sa classe par `net.michel.ext.DaoImplV2`.

### 2.3 Lancer l’application

`PresSpringXML` charge `config.xml`, récupère le bean `metier` et lance le calcul :

```java
ApplicationContext springContext =
        new ClassPathXmlApplicationContext("config.xml");
IMetier metier = (IMetier) springContext.getBean("metier");
System.out.println("RES=" + metier.calcul());
```

![Lancement de PresSpringXML](<captures/Capture d'écran 2026-10-07 145518.png>)

![Autre capture de l’exécution de PresSpringXML](<captures/Capture d'écran 2026-10-07 161040.png>)

Le résultat affiché dans cette démonstration est d’environ `640,88`.

## 3. Avec les annotations Spring

`MetierImpl` est déclaré comme composant Spring. Son DAO est injecté par le constructeur ; `@Qualifier("d")` précise quel bean `IDao` utiliser dans la version actuelle.

![Annotations Spring dans MetierImpl](<captures/Capture d'écran 2026-10-12 183517.png>)

Pour lancer l’application, `PresSpringAnnotation` crée un contexte Spring et récupère le métier par son interface :

```java
ApplicationContext applicationContext =
        new AnnotationConfigApplicationContext("net.michel");
IMetier metier = applicationContext.getBean(IMetier.class);
System.out.println("RES=" + metier.calcul());
```

La capture suivante montre l’essai avec les packages `dao` et `metier` scannés explicitement :

![Démarrage Spring avec le package dao](<captures/Capture d'écran 2026-10-07 161620.png>)

Une autre capture montre l’essai en scannant `ext` à la place de `dao` :

![Démarrage Spring avec le package ext](<captures/Capture d'écran 2026-10-12 002420.png>)

Ces deux captures correspondent à des étapes différentes du TP. Dans le code actuel, le contexte scanne `net.michel` et `MetierImpl` demande le bean nommé `d` ; `DaoImplV2` porte le nom `d2`. Le choix du DAO dans ces essais dépend donc aussi du nom du bean, pas seulement du package scanné.
