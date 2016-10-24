package sk.xanion.routerconfig.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mkosik on 21. 10. 2016.
 */

public class WirelessStatus implements Serializable {
    public static final long serialVersionUID = -1;
    public Boolean active;
    public List<String> macAdresses;
    public String exception;
}
