package iwish.client;

import iwish.common.Action;
import iwish.common.Item;
import iwish.common.Request;
import iwish.common.Response;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/** Lets the user browse the catalog and add items to their wish list. Stays open to add several. */
class CatalogWindow {
    private final ServerConnection server = ServerConnection.get();
    private final Predicate<Response> sessionEnded;
    private final Runnable onChanged;
    private final Set<Integer> alreadyOnList;

    private final ObservableList<Item> available = FXCollections.observableArrayList();
    private final FilteredList<Item> filtered = new FilteredList<>(available, i -> true);
    private final ListView<Item> list = new ListView<>(filtered);
    private final TextField search = new TextField();
    private final TextField note = new TextField();
    private final Label placeholder = Ui.placeholder("Loading...");
    private final Label status = Ui.statusLabel();
    private final Button addButton = new Button("Add to wish list");
    private final Stage stage = new Stage();
    private boolean loaded;
    private boolean busy;

    CatalogWindow(Window owner, Predicate<Response> sessionEnded, Collection<Integer> alreadyOnList,
                  Runnable onChanged) {
        this.sessionEnded = sessionEnded;
        this.onChanged = onChanged;
        this.alreadyOnList = new HashSet<>(alreadyOnList);

        search.setPromptText("Search by name or category");
        note.setPromptText("Note (optional), e.g. blue color");
        Ui.twoLineCells(list, Item::getName, i -> Ui.money(i.getPrice()) + " | " + i.getCategory());
        list.setPlaceholder(placeholder);

        Button done = new Button("Done");
        done.getStyleClass().add("secondary-button");
        done.setOnAction(e -> stage.close());
        addButton.getStyleClass().add("primary-button");
        addButton.setOnAction(e -> addSelected());
        HBox buttons = new HBox(8, done, addButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(10, search, list, note, status, buttons);
        root.setPadding(new Insets(14));
        VBox.setVgrow(list, Priority.ALWAYS);

        search.textProperty().addListener((obs, old, text) -> {
            String q = text.trim().toLowerCase(Locale.ROOT);
            filtered.setPredicate(i -> q.isEmpty()
                    || i.getName().toLowerCase(Locale.ROOT).contains(q)
                    || i.getCategory().toLowerCase(Locale.ROOT).contains(q));
            updatePlaceholder();
        });
        list.getSelectionModel().selectedItemProperty().addListener((obs, old, item) -> updateButtons());
        note.setOnAction(e -> addSelected());

        if (owner != null) {
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
        } else {
            stage.initModality(Modality.APPLICATION_MODAL);
        }
        stage.setTitle("Add to your wish list");
        stage.setScene(IWishApp.createScene(root, 460, 580));
        updateButtons();
        loadCatalog();
    }

    void showAndWait() {
        stage.showAndWait();
    }

    @SuppressWarnings("unchecked")
    private void loadCatalog() {
        server.sendAsync(new Request(Action.GET_CATALOG), r -> {
            if (sessionEnded.test(r)) {
                stage.close();
                return;
            }
            if (!r.isSuccess()) {
                Ui.showError(status, r.getMessage());
                return;
            }
            for (Item item : (List<Item>) r.getData()) {
                if (!alreadyOnList.contains(item.getId())) {
                    available.add(item);
                }
            }
            loaded = true;
            updatePlaceholder();
        });
    }

    private void addSelected() {
        Item item = list.getSelectionModel().getSelectedItem();
        if (item == null || busy) {
            return;
        }
        busy = true;
        updateButtons();
        server.sendAsync(new Request(Action.ADD_TO_WISHLIST)
                .with("itemId", item.getId()).with("note", note.getText().trim()), r -> {
            busy = false;
            if (sessionEnded.test(r)) {
                stage.close();
                return;
            }
            if (r.isSuccess()) {
                available.remove(item);
                note.clear();
                Ui.showSuccess(status, r.getMessage());
                onChanged.run();
            } else {
                Ui.showError(status, r.getMessage());
            }
            updatePlaceholder();
            updateButtons();
        });
    }

    private void updatePlaceholder() {
        if (!loaded) {
            placeholder.setText("Loading...");
        } else if (available.isEmpty()) {
            placeholder.setText("You've added everything in the catalog!");
        } else {
            placeholder.setText("No items match your search.");
        }
    }

    private void updateButtons() {
        addButton.setDisable(busy || list.getSelectionModel().getSelectedItem() == null);
    }
}
