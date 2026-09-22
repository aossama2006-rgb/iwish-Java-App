package iwish.common;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

// One entry on a wish list: a catalog item, the owner's optional note, and how much is funded
public class WishItem implements Serializable {
    private static final long serialVersionUID = 2L;

    private final int id;
    private final Item item;
    private final String note;
    private final BigDecimal funded;

    public WishItem(int id, Item item, String note, BigDecimal funded) {
        this.id = id;
        this.item = item;
        this.note = note == null ? "" : note;
        this.funded = funded == null ? BigDecimal.ZERO : funded;
    }

    
    public int getId() {
        return id;
    }

    public Item getItem() {
        return item;
    }

    public String getNote() {
        return note;
    }

    
    //How much friends have contributed. On your own list 
   
    public BigDecimal getFunded() {
        return funded;
    }

    public BigDecimal getRemaining() {
        return item.getPrice().subtract(funded).max(BigDecimal.ZERO);
    }

    public boolean isBought() {
        return funded.compareTo(item.getPrice()) >= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WishItem)) {
            return false;
        }
        WishItem w = (WishItem) o;
        return id == w.id && Objects.equals(item, w.item) && Objects.equals(note, w.note)
                && funded.compareTo(w.funded) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, item, note, funded.stripTrailingZeros());
    }
}
