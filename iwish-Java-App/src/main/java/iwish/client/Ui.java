package iwish.client;

import iwish.common.WishItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Window;


final class Ui {
    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("MMM d, HH:mm", Locale.ENGLISH).withZone(ZoneId.systemDefault());

    private Ui() {
    }

    static String formatTime(long epochMillis) {
        return TIME.format(Instant.ofEpochMilli(epochMillis));
    }

    static String money(BigDecimal price) {
        return String.format(Locale.US, "EGP %,.2f", price);
    }

   // leaving a note 
    static String wishSubtitle(WishItem w) {
        String text = (w.isBought() ? "BOUGHT | " : "")
                + money(w.getItem().getPrice()) + " | " + w.getItem().getCategory();
        return w.getNote().isEmpty() ? text : text + " | Note: " + w.getNote();
    }

    // including how much is already funded
    static String friendWishSubtitle(WishItem w) {
        String text;
        if (w.isBought()) {
            text = "FULLY FUNDED | " + money(w.getItem().getPrice()) + " | " + w.getItem().getCategory();
        } else {
            text = money(w.getItem().getPrice()) + " | " + w.getItem().getCategory();
            if (w.getFunded().signum() > 0) {
                text += " | funded " + money(w.getFunded()) + ", needs " + money(w.getRemaining());
            }
        }
        return w.getNote().isEmpty() ? text : text + " | Note: " + w.getNote();
    }

    // status labels 

    // A wrapping label that takes no space while it has no text
    static Label statusLabel() {
        Label label = new Label();
        label.setWrapText(true);
        label.managedProperty().bind(label.textProperty().isNotEmpty());
        label.visibleProperty().bind(label.textProperty().isNotEmpty());
        return label;
    }

    static void showError(Label label, String message) {
        label.getStyleClass().removeAll("success-label", "error-label");
        label.getStyleClass().add("error-label");
        label.setText(message);
    }

    static void showSuccess(Label label, String message) {
        label.getStyleClass().removeAll("success-label", "error-label");
        label.getStyleClass().add("success-label");
        label.setText(message);
    }

    // dialogs

    static void info(Window owner, String title, String message) {
        alert(Alert.AlertType.INFORMATION, owner, title, message).showAndWait();
    }

    static void warn(Window owner, String title, String message) {
        alert(Alert.AlertType.WARNING, owner, title, message).showAndWait();
    }

    static boolean confirm(Window owner, String title, String message) {
        Alert alert = alert(Alert.AlertType.CONFIRMATION, owner, title, message);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        return alert.showAndWait().filter(b -> b == ButtonType.YES).isPresent();
    }

    //return the text, or empty if the user cancelled 
    static Optional<String> askText(Window owner, String title, String prompt, String initial) {
        TextInputDialog dialog = new TextInputDialog(initial);
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(prompt);
        return dialog.showAndWait();
    }

    private static Alert alert(Alert.AlertType type, Window owner, String title, String message) {
        Alert alert = new Alert(type);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert;
    }

    // lists

    
    static <T> void twoLineCells(ListView<T> list, Function<T, String> title, Function<T, String> subtitle) {
        list.setCellFactory(lv -> new ListCell<T>() {
            private final Label titleLabel = new Label();
            private final Label subtitleLabel = new Label();
            private final VBox box = new VBox(2, titleLabel, subtitleLabel);

            {
                titleLabel.getStyleClass().add("cell-title");
                subtitleLabel.getStyleClass().add("cell-subtitle");
            }

            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    titleLabel.setText(title.apply(item));
                    subtitleLabel.setText(subtitle.apply(item));
                    setGraphic(box);
                }
            }
        });
    }

    
    static <T> void updateItems(ListView<T> list, List<T> items) {
        if (list.getItems().equals(items)) {
            return;
        }
        T selected = list.getSelectionModel().getSelectedItem();
        list.getItems().setAll(items);
        if (selected != null && items.contains(selected)) {
            list.getSelectionModel().select(selected);
        }
    }

    static Label placeholder(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("placeholder");
        label.setWrapText(true);
        label.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        return label;
    }
}
