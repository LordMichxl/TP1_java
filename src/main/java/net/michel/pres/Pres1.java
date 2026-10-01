package net.michel.pres;

import net.michel.dao.DaoImpl;
import net.michel.metier.MetierImpl;

public class Pres1 {
    public static void main(String[] args) {
        DaoImpl d = new DaoImpl();
        MetierImpl metier = new MetierImpl(d);
        metier.setDao(d);//injection de dependances via le setter
        System.out.println("Res: "+ metier.calcul());
    }
}
