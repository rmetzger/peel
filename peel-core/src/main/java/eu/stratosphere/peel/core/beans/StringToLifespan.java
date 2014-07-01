package eu.stratosphere.peel.core.beans;

import eu.stratosphere.peel.core.beans.system.Lifespan;
import org.springframework.core.convert.converter.Converter;
import scala.Enumeration.Value;

public class StringToLifespan implements Converter<String, Value> {

    @Override
    public Value convert(String s) {
        switch (s) {
            case "PROVIDED":
                return Lifespan.PROVIDED();
            case "SUITE":
                return Lifespan.SUITE();
            case "EXPERIMENT":
                return Lifespan.EXPERIMENT();
            default:
                throw new IllegalArgumentException(s + " can not be converted to Scala Lifecycle Value!");
        }
    }

}