package net.michel.ext;

import net.michel.dao.IDao;

public class DaoImplV2 implements IDao{
    @Override
    public double getData() {
        System.out.println("Version Capteurs");
        double t = 12;

        return t;
    }
}
