package iwish.client;

import iwish.common.Action;
import iwish.common.FriendRequest;
import iwish.common.Notification;
import iwish.common.Request;
import iwish.common.Response;
import iwish.common.User;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Duration;

/** Main screen: friends, incoming friend requests, and my wish list. */
class MainView {
    private final ServerConnection server = ServerConnection.get();
    private final BorderPane root = new BorderPane();

    private final ListView<User> friendsList = new ListView<>();
    private final ListView<FriendRequest> requestsList = new ListView<>();
    private final Tab requestsTab = new Tab("Requests");
    private final ListView<Notification> notificationsList = new ListView<>();
    private final Tab notificationsTab = new Tab("Notifications");
    private final TabPane tabs = new TabPane();
    private final Label banner = Ui.statusLabel();
    private final PauseTransition hideBanner = new PauseTransition(Duration.seconds(8));
    private final Label statusBar = Ui.statusLabel();
    private final WishlistView wishlistView = new WishlistView(this::sessionEnded);

    private final Button addButton = new Button("Add friend");
    private final Button viewWishesButton = new Button("View wish list");
    private final Button removeButton = new Button("Remove");
    private final Button acceptButton = new Button("Accept");
    private final Button declineButton = new Button("Decline");

    private final Timeline poller = new Timeline(new KeyFrame(Duration.seconds(5), e -> refresh()));
    private boolean refreshing;
    private boolean leaving;
    private int lastNotifiedId;

    MainView(User me) {
        // header
        Label greeting = new Label("Hi, " + me.getName() + "!");
        greeting.getStyleClass().add("greeting");
        Button signOut = new Button("Sign out");
        signOut.getStyleClass().add("secondary-button");
        signOut.setOnAction(e -> signOut());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(greeting, spacer, signOut);
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);

        // banner for new notifications (click to dismiss, hides itself after a few seconds)
        banner.getStyleClass().add("banner");
        banner.setMaxWidth(Double.MAX_VALUE);
        banner.setOnMouseClicked(e -> banner.setText(""));
        hideBanner.setOnFinished(e -> banner.setText(""));

        // lists
        Ui.twoLineCells(friendsList, User::getName, User::getEmail);
        friendsList.setPlaceholder(Ui.placeholder("No friends yet.\nClick \"Add friend\" to send a request."));
        Ui.twoLineCells(requestsList, r -> r.getSender().getName(),
                r -> r.getSender().getEmail() + " wants to be your friend");
        requestsList.setPlaceholder(Ui.placeholder("No pending friend requests."));

        // buttons
        addButton.getStyleClass().add("primary-button");
        viewWishesButton.getStyleClass().add("secondary-button");
        removeButton.getStyleClass().add("danger-button");
        acceptButton.getStyleClass().add("primary-button");
        declineButton.getStyleClass().add("danger-button");
        viewWishesButton.disableProperty().bind(friendsList.getSelectionModel().selectedItemProperty().isNull());
        removeButton.disableProperty().bind(friendsList.getSelectionModel().selectedItemProperty().isNull());
        acceptButton.disableProperty().bind(requestsList.getSelectionModel().selectedItemProperty().isNull());
        declineButton.disableProperty().bind(requestsList.getSelectionModel().selectedItemProperty().isNull());

        addButton.setOnAction(e -> addFriend());
        viewWishesButton.setOnAction(e -> viewFriendWishlist());
        removeButton.setOnAction(e -> removeFriend());
        acceptButton.setOnAction(e -> answerRequest(true));
        declineButton.setOnAction(e -> answerRequest(false));
        friendsList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && friendsList.getSelectionModel().getSelectedItem() != null) {
                viewFriendWishlist();
            }
        });

        // tabs: Friends, Requests, My Wish List
        Tab friendsTab = new Tab("Friends", listTab(friendsList, addButton, viewWishesButton, removeButton));
        requestsTab.setContent(listTab(requestsList, acceptButton, declineButton));
        Tab wishesTab = new Tab("My Wish List", wishlistView.getRoot());
        configureNotificationList();
        notificationsTab.setContent(listTab(notificationsList));
        tabs.getTabs().addAll(friendsTab, requestsTab, wishesTab, notificationsTab);
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getSelectionModel().selectedItemProperty().addListener((obs, old, tab) -> {
            if (tab == notificationsTab) {
                markNotificationsRead();
            }
        });

        statusBar.getStyleClass().add("error-label");
        statusBar.setPadding(new Insets(6, 16, 10, 16));

        root.setTop(new VBox(header, banner));
        root.setCenter(tabs);
        root.setBottom(statusBar);

        poller.setCycleCount(Animation.INDEFINITE);
        refresh();
        wishlistView.load();
        poller.play();
    }

    Parent getRoot() {
        return root;
    }

    // ---- actions -------------------------------------------------------

    private void addFriend() {
        Ui.askText(window(), "Add friend", "Your friend's email address:", "").ifPresent(email -> {
            if (email.isBlank()) {
                return;
            }
            server.sendAsync(new Request(Action.ADD_FRIEND).with("email", email.trim()), r -> {
                if (sessionEnded(r)) {
                    return;
                }
                if (r.isSuccess()) {
                    Ui.info(window(), "Request sent", r.getMessage());
                } else {
                    Ui.warn(window(), "Couldn't add friend", r.getMessage());
                }
            });
        });
    }

    private void viewFriendWishlist() {
        User friend = friendsList.getSelectionModel().getSelectedItem();
        if (friend != null) {
            new FriendWishlistWindow(window(), friend, this::sessionEnded).showAndWait();
        }
    }

    private void removeFriend() {
        User friend = friendsList.getSelectionModel().getSelectedItem();
        if (friend == null
                || !Ui.confirm(window(), "Remove friend", "Remove " + friend.getName() + " from your friends?")) {
            return;
        }
        server.sendAsync(new Request(Action.REMOVE_FRIEND).with("friendId", friend.getId()), r -> {
            if (sessionEnded(r)) {
                return;
            }
            if (!r.isSuccess()) {
                Ui.warn(window(), "Couldn't remove friend", r.getMessage());
            }
            refresh();
        });
    }

    private void answerRequest(boolean accept) {
        FriendRequest request = requestsList.getSelectionModel().getSelectedItem();
        if (request == null) {
            return;
        }
        Action action = accept ? Action.ACCEPT_REQUEST : Action.DECLINE_REQUEST;
        server.sendAsync(new Request(action).with("requestId", request.getId()), r -> {
            if (sessionEnded(r)) {
                return;
            }
            if (!r.isSuccess()) {
                Ui.warn(window(), "Couldn't update request", r.getMessage());
            }
            refresh();
        });
    }

    private void signOut() {
        poller.stop();
        server.sendAsync(new Request(Action.LOGOUT), r -> {
            leaving = true;
            IWishApp.showLogin();
        });
    }

    // ---- refreshing ----------------------------------------------------

    @SuppressWarnings("unchecked")
    private void refresh() {
        if (refreshing || leaving) {
            return;
        }
        refreshing = true;
        server.sendAsync(new Request(Action.GET_FRIENDS), friendsResponse -> {
            if (!stepOk(friendsResponse)) {
                return;
            }
            Ui.updateItems(friendsList, (List<User>) friendsResponse.getData());
            server.sendAsync(new Request(Action.GET_INCOMING_REQUESTS), requestsResponse -> {
                if (!stepOk(requestsResponse)) {
                    return;
                }
                List<FriendRequest> requests = (List<FriendRequest>) requestsResponse.getData();
                Ui.updateItems(requestsList, requests);
                requestsTab.setText(requests.isEmpty() ? "Requests" : "Requests (" + requests.size() + ")");
                server.sendAsync(new Request(Action.GET_NOTIFICATIONS), notificationsResponse -> {
                    refreshing = false;
                    if (sessionEnded(notificationsResponse)) {
                        return;
                    }
                    if (notificationsResponse.isSuccess()) {
                        applyNotifications((List<Notification>) notificationsResponse.getData());
                        statusBar.setText("");
                    } else {
                        statusBar.setText(notificationsResponse.getMessage());
                    }
                });
            });
        });
    }

    /** One step of the refresh chain: false means "stop here" (session ended or an error was shown). */
    private boolean stepOk(Response response) {
        if (sessionEnded(response)) {
            return false;
        }
        if (!response.isSuccess()) {
            refreshing = false;
            statusBar.setText(response.getMessage());
            return false;
        }
        return true;
    }

    // ---- notifications -------------------------------------------------

    private void applyNotifications(List<Notification> all) {
        Ui.updateItems(notificationsList, all);
        int unread = 0;
        List<Notification> fresh = new ArrayList<>();
        for (Notification n : all) {
            if (!n.isRead()) {
                unread++;
                if (n.getId() > lastNotifiedId) {
                    fresh.add(n);
                }
            }
        }
        notificationsTab.setText(unread == 0 ? "Notifications" : "Notifications (" + unread + ")");

        if (!fresh.isEmpty()) {
            // 'all' is newest first, so the first one is the newest
            lastNotifiedId = fresh.get(0).getId();
            showBanner(fresh.size() == 1 ? fresh.get(0).getMessage()
                    : "You have " + fresh.size() + " new notifications. Open the Notifications tab.");
            wishlistView.load(); // a gift may have just been bought
        }
        if (unread > 0 && tabs.getSelectionModel().getSelectedItem() == notificationsTab) {
            markNotificationsRead();
        }
    }

    private void showBanner(String message) {
        banner.setText(message);
        hideBanner.playFromStart();
    }

    private void markNotificationsRead() {
        server.sendAsync(new Request(Action.MARK_NOTIFICATIONS_READ), r -> {
            if (!sessionEnded(r)) {
                refresh();
            }
        });
    }

    private void configureNotificationList() {
        notificationsList.setPlaceholder(Ui.placeholder("No notifications yet.\nYou'll be told when a gift is fully funded."));
        notificationsList.setCellFactory(lv -> new ListCell<Notification>() {
            private final Label message = new Label();
            private final Label time = new Label();
            private final VBox box = new VBox(3, message, time);

            {
                setPrefWidth(0);
                message.setWrapText(true);
                message.prefWidthProperty().bind(lv.widthProperty().subtract(52));
                message.getStyleClass().add("notif-message");
                time.getStyleClass().add("cell-subtitle");
            }

            @Override
            protected void updateItem(Notification n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null) {
                    setGraphic(null);
                    return;
                }
                message.setText(n.getMessage());
                time.setText(Ui.formatTime(n.getCreatedAt()));
                message.getStyleClass().remove("notif-unread");
                if (!n.isRead()) {
                    message.getStyleClass().add("notif-unread");
                }
                setGraphic(box);
            }
        });
    }

    /** Returns true if the response means the session is gone (and takes the user back to login). */
    private boolean sessionEnded(Response response) {
        if (!response.isSessionExpired()) {
            return false;
        }
        if (!leaving) {
            leaving = true;
            poller.stop();
            Ui.info(window(), "Signed out", "Your session has ended. Please sign in again.");
            IWishApp.showLogin();
        }
        return true;
    }

    // ---- helpers -------------------------------------------------------

    private Window window() {
        return root.getScene() == null ? null : root.getScene().getWindow();
    }

    private static Node listTab(ListView<?> list, Button... actions) {
        HBox buttons = new HBox(8);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.getChildren().addAll(actions);
        VBox box = new VBox(10, list, buttons);
        box.setPadding(new Insets(12));
        VBox.setVgrow(list, Priority.ALWAYS);
        return box;
    }
}
