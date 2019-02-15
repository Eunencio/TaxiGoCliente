package tovele.eunencio.taxigocliente.Model;

/**
 * Created by Eunencio Tovele on 9/3/2018.
 */

public class Result {
    public String massage_id;

    public Result() {
    }

    public Result(String massage_id) {
        this.massage_id = massage_id;
    }

    public String getMassage_id() {
        return massage_id;
    }

    public void setMassage_id(String massage_id) {
        this.massage_id = massage_id;
    }
}
