package iwish.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Action action;
    private final Map<String, Object> params = new HashMap<>();

    public Request(Action action) {
        this.action = action;
    }

    public Request with(String key, Object value) {
        params.put(key, value);
        return this;
    }

    public Action getAction() {
        return action;
    }

    public String getString(String key) {
        Object v = params.get(key);
        return v == null ? null : v.toString();
    }

    public int getInt(String key) {
        Object v = params.get(key);
        if (v instanceof Integer) {
            return (Integer) v;
        }
        throw new IllegalArgumentException("Missing or invalid parameter: " + key);
    }
}
