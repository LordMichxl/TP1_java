package net.michel.metier;

import net.michel.dao.IDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("metier")
public class MetierImpl implements IMetier {
    @Autowired
    private IDao dao; //Couplage faible

    public MetierImpl( @Qualifier("d") IDao dao) {
        this.dao = dao;
    }


    @Override
    public double calcul() {
        double t = dao.getData();
        double res = t * 12 * Math.PI/2;
        return res;
    }



    public void setDao(IDao dao) {
        this.dao = dao;
    }
}
