package iwish.common;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

// A catalog item users can put on their wish list
public class Item implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private final String category;
    private final BigDecimal price;

    public Item(int id, String name, String category, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Item)) {
            return false;
        }
        Item i = (Item) o;
        return id == i.id && Objects.equals(name, i.name)
                && Objects.equals(category, i.category) && Objects.equals(price, i.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, category, price);
    }
}
