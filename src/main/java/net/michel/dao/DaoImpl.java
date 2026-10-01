package net.michel.dao;

public class DaoImpl implements IDao{

    @Override
    public double getData() {
        System.out.println("Version Base de données");
        double t =34;
        return t;
    }
}

