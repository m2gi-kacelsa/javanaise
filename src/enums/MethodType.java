package enums;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//declarer l'annotation @MethodType , permet de connaitre les types des méthodes invoqués read et write, ces 

@Retention(RetentionPolicy.RUNTIME)
//annotation applicable on methods
@Target(ElementType.METHOD)
public @interface MethodType {
	// attribut type reccuperer le type de la méthode appellée
	String type();
}
