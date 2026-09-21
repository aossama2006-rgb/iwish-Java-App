package iwish.client;

import iwish.common.Action;
import iwish.common.Request;
import iwish.common.Response;
import iwish.common.User;
import iwish.common.WishItem;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/** A friend's wish list. You can chip in towards any item that isn't fully funded yet. */
class FriendWishlistWindow {
    private final ServerConnection server = ServerConnection.get();
    private final Predicate<Response> sessionEnded;
    private final User friend;

    private final ListView<WishItem> list = new ListView<>();
    private final Label summary = new Label("Loading...");
    private final Label placeholder = Ui.placeholder("Loading...");
    private final Button contributeButton = new Button("Contribute");
    private final Stage stage = new Stage();

    FriendWishlistWindow(Window owner, User friend, Predicate<Response> sessionEnded) {
        this.friend = friend;
        this.sessionEnded = sessionEnded;

        Ui.twoLineCells(list, w -> w.getItem().getName(), Ui::friendWishSubtitle);
        list.setPlaceholder(placeholder);
        summary.getStyleClass().add("summary-label");

        contributeButton.getStyleClass().add("primary-button");
        contributeButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            WishItem selected = list.getSelectionModel().getSelectedItem();
            return selected == null || selected.isBought();
        }, list.getSelectionModel().selectedItemProperty()));
        contributeButton.setOnAction(e -> contribute());

        Button close = new Button("Close");
        close.getStyleClass().add("secondary-button");
        close.setOnAction(e -> stage.close());
        HBox buttons = new HBox(8, close, contributeButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Label hint = new Label("Pick an item and click Contribute to pay part (or all) of its price.");
        hint.getStyleClass().add("hint-label");
        hint.setWrapText(true);

        VBox root = new VBox(10, summary, list, hint, buttons);
        root.setPadding(new Insets(14));
        VBox.setVgrow(list, Priority.ALWAYS);

        if (owner != null) {
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
        } else {
            stage.initModality(Modality.APPLICATION_MODAL);
        }
        stage.setTitle(friend.getName() + "'s wish list");
        stage.setScene(IWishApp.createScene(root, 480, 560));
        load();
    }

    void showAndWait() {
        stage.showAndWait();
    }

    private void contribute() {
        WishItem w = list.getSelectionModel().getSelectedItem();
        if (w == null || w.isBought()) {
            return;
        }
        Ui.askText(stage, "Contribute",
                "How much do you want to contribute to \"" + w.getItem().getName() + "\"?\n"
                        + "Still needed: " + Ui.money(w.getRemaining()),
                "").ifPresent(amount -> {
            if (amount.isBlank()) {
                return;
            }
            server.sendAsync(new Request(Action.CONTRIBUTE)
                    .with("wishId", w.getId()).with("amount", amount.trim()), r -> {
                if (sessionEnded.test(r)) {
                    stage.close();
                    return;
                }
                if (r.isSuccess()) {
                    Ui.info(stage, "Thank you!", r.getMessage());
                } else {
                    Ui.warn(stage, "Couldn't contribute", r.getMessage());
                }
                load();
            });
        });
    }

    @SuppressWarnings("unchecked")
    private void load() {
        server.sendAsync(new Request(Action.GET_FRIEND_WISHLIST).with("friendId", friend.getId()), r -> {
            if (sessionEnded.test(r)) {
                stage.close();
                return;
            }
            if (!r.isSuccess()) {
                summary.setText(r.getMessage());
                placeholder.setText("");
                return;
            }
            List<WishItem> items = (List<WishItem>) r.getData();
            WishItem selected = list.getSelectionModel().getSelectedItem();
            list.getItems().setAll(items);
            if (selected != null) {
                for (WishItem w : items) {
                    if (w.getId() == selected.getId()) {
                        list.getSelectionModel().select(w);
                    }
                }
            }
            BigDecimal total = BigDecimal.ZERO;
            for (WishItem w : items) {
                total = total.add(w.getItem().getPrice());
            }
            placeholder.setText(friend.getName() + " hasn't added anything to their wish list yet.");
            int n = items.size();
            summary.setText(n == 0 ? friend.getName() + "'s wish list"
                    : n + (n == 1 ? " item" : " items") + " | total " + Ui.money(total));
        });
    }
}
