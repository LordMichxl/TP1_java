package net.michel.pres;

import net.michel.metier.IMetier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class PresSpringAnnotation {
    public static void main(String[] args) {
        ApplicationContext applicationContext =
                new AnnotationConfigApplicationContext("net.michel");
        IMetier metier = applicationContext.getBean(IMetier.class);
        System.out.println("RES="+metier.calcul());
    }
}
