package iwish.client;

import iwish.common.Action;
import iwish.common.Request;
import iwish.common.Response;
import iwish.common.WishItem;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

// The "My Wish List" tab: add items from the catalog, edit their notes, remove them
class WishlistView {
    private final ServerConnection server = ServerConnection.get();
    private final Predicate<Response> sessionEnded;
    private final VBox root = new VBox(10);

    private final ListView<WishItem> list = new ListView<>();
    private final Label summary = new Label(" ");
    private final Button addButton = new Button("Add item");
    private final Button editButton = new Button("Edit note");
    private final Button removeButton = new Button("Remove");

    
    WishlistView(Predicate<Response> sessionEnded) {
        this.sessionEnded = sessionEnded;

        Ui.twoLineCells(list, w -> w.getItem().getName(), Ui::wishSubtitle);
        list.setPlaceholder(Ui.placeholder("Your wish list is empty.\nClick \"Add item\" to pick gifts you'd love."));
        summary.getStyleClass().add("summary-label");

        addButton.getStyleClass().add("primary-button");
        editButton.getStyleClass().add("secondary-button");
        removeButton.getStyleClass().add("danger-button");
        editButton.disableProperty().bind(list.getSelectionModel().selectedItemProperty().isNull());
        removeButton.disableProperty().bind(list.getSelectionModel().selectedItemProperty().isNull());
        addButton.setOnAction(e -> openCatalog());
        editButton.setOnAction(e -> editNote());
        removeButton.setOnAction(e -> removeSelected());

        HBox buttons = new HBox(8, addButton, editButton, removeButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        root.setPadding(new Insets(12));
        root.getChildren().addAll(summary, list, buttons);
        VBox.setVgrow(list, Priority.ALWAYS);
    }

    Parent getRoot() {
        return root;
    }

    @SuppressWarnings("unchecked")
    void load() {
        server.sendAsync(new Request(Action.GET_MY_WISHLIST), r -> {
            if (sessionEnded.test(r)) {
                return;
            }
            if (!r.isSuccess()) {
                summary.setText(r.getMessage());
                return;
            }
            List<WishItem> items = (List<WishItem>) r.getData();
            BigDecimal total = BigDecimal.ZERO;
            for (WishItem w : items) {
                total = total.add(w.getItem().getPrice());
            }
            Ui.updateItems(list, items);
            int n = items.size();
            summary.setText(n == 0 ? " " : n + (n == 1 ? " item" : " items") + " | total " + Ui.money(total));
        });
    }

    private Window window() {
        return root.getScene() == null ? null : root.getScene().getWindow();
    }

    private void openCatalog() {
        List<Integer> onList = new ArrayList<>();
        for (WishItem w : list.getItems()) {
            onList.add(w.getItem().getId());
        }
        new CatalogWindow(window(), sessionEnded, onList, this::load).showAndWait();
    }

    private void editNote() {
        WishItem w = list.getSelectionModel().getSelectedItem();
        if (w == null) {
            return;
        }
        Ui.askText(window(), "Edit note",
                "Note for " + w.getItem().getName() + " (optional, max 200 characters):", w.getNote())
                .ifPresent(note -> server.sendAsync(new Request(Action.UPDATE_WISHLIST_ITEM)
                        .with("wishId", w.getId()).with("note", note.trim()), r -> {
                    if (sessionEnded.test(r)) {
                        return;
                    }
                    if (!r.isSuccess()) {
                        Ui.warn(window(), "Couldn't update note", r.getMessage());
                    }
                    load();
                }));
    }

    private void removeSelected() {
        WishItem w = list.getSelectionModel().getSelectedItem();
        if (w == null || !Ui.confirm(window(), "Remove item",
                "Remove " + w.getItem().getName() + " from your wish list?")) {
            return;
        }
        server.sendAsync(new Request(Action.REMOVE_FROM_WISHLIST).with("wishId", w.getId()), r -> {
            if (sessionEnded.test(r)) {
                return;
            }
            if (!r.isSuccess()) {
                Ui.warn(window(), "Couldn't remove item", r.getMessage());
            }
            load();
        });
    }
}
