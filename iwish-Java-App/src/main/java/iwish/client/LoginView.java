package iwish.client;

import iwish.common.Action;
import iwish.common.Request;
import iwish.common.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Sign-in and registration screen
class LoginView {
    private final StackPane root = new StackPane();

    private final TextField signInEmail = new TextField();
    private final PasswordField signInPassword = new PasswordField();
    private final Button signInButton = new Button("Sign in");
    private final Label signInStatus = Ui.statusLabel();

    private final TextField regName = new TextField();
    private final TextField regEmail = new TextField();
    private final PasswordField regPassword = new PasswordField();
    private final PasswordField regConfirm = new PasswordField();
    private final Button regButton = new Button("Create account");
    private final Label regStatus = Ui.statusLabel();

    LoginView() {
        Label title = new Label("i-Wish");
        title.getStyleClass().add("app-title");
        Label tagline = new Label("Make your friends happy, one gift at a time");
        tagline.getStyleClass().add("tagline");

        signInEmail.setPromptText("you@example.com");
        signInPassword.setPromptText("Password");
        regName.setPromptText("Your name");
        regEmail.setPromptText("you@example.com");
        regPassword.setPromptText("At least 6 characters");
        regConfirm.setPromptText("Repeat the password");

        VBox signInForm = form(
                new String[]{"Email", "Password"},
                new Control[]{signInEmail, signInPassword},
                signInButton, signInStatus);
        VBox registerForm = form(
                new String[]{"Name", "Email", "Password", "Confirm password"},
                new Control[]{regName, regEmail, regPassword, regConfirm},
                regButton, regStatus);

        // Segmented switch between the two forms (the card always fits the visible form)
        ToggleButton signInToggle = segment("Sign in", "segment-left");
        ToggleButton registerToggle = segment("Create account", "segment-right");
        ToggleGroup group = new ToggleGroup();
        signInToggle.setToggleGroup(group);
        registerToggle.setToggleGroup(group);
        signInToggle.setSelected(true);
        HBox segments = new HBox(signInToggle, registerToggle);
        VBox.setMargin(segments, new Insets(18, 0, 0, 0));

        VBox formHolder = new VBox(signInForm);
        group.selectedToggleProperty().addListener((obs, old, selected) -> {
            if (selected == null) {
                old.setSelected(true); // one of the two is always selected
            } else {
                formHolder.getChildren().setAll(selected == signInToggle ? signInForm : registerForm);
            }
        });

        VBox card = new VBox(4, title, tagline, segments, formHolder);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.TOP_CENTER);
        card.setMaxSize(380, Region.USE_PREF_SIZE);

        root.getStyleClass().add("login-root");
        root.setPadding(new Insets(24));
        root.getChildren().add(card);

        signInButton.setOnAction(e -> signIn());
        signInEmail.setOnAction(e -> signInPassword.requestFocus());
        signInPassword.setOnAction(e -> signIn());
        regButton.setOnAction(e -> register());
        regConfirm.setOnAction(e -> register());
    }

    Parent getRoot() {
        return root;
    }

    private void signIn() {
        String email = signInEmail.getText().trim();
        String password = signInPassword.getText();
        if (email.isEmpty() || password.isEmpty()) {
            Ui.showError(signInStatus, "Enter your email and password.");
            return;
        }
        submit(new Request(Action.LOGIN).with("email", email).with("password", password),
                signInButton, signInStatus);
    }

    private void register() {
        String name = regName.getText().trim();
        String email = regEmail.getText().trim();
        String password = regPassword.getText();
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Ui.showError(regStatus, "Please fill in all the fields.");
        } else if (password.length() < 6) {
            Ui.showError(regStatus, "Password must be at least 6 characters.");
        } else if (!password.equals(regConfirm.getText())) {
            Ui.showError(regStatus, "Passwords don't match.");
        } else {
            submit(new Request(Action.REGISTER)
                    .with("name", name).with("email", email).with("password", password),
                    regButton, regStatus);
        }
    }

    private void submit(Request request, Button button, Label status) {
        button.setDisable(true);
        status.setText("");
        ServerConnection.get().sendAsync(request, response -> {
            button.setDisable(false);
            if (response.isSuccess()) {
                IWishApp.showMain((User) response.getData());
            } else {
                Ui.showError(status, response.getMessage());
            }
        });
    }

    private static ToggleButton segment(String text, String sideClass) {
        ToggleButton button = new ToggleButton(text);
        button.getStyleClass().addAll("segment", sideClass);
        button.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(button, Priority.ALWAYS);
        return button;
    }

    private static VBox form(String[] labels, Control[] fields, Button button, Label status) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(16, 4, 4, 4));
        for (int i = 0; i < labels.length; i++) {
            Label label = new Label(labels[i]);
            label.getStyleClass().add("field-label");
            box.getChildren().addAll(label, fields[i]);
        }
        button.getStyleClass().add("primary-button");
        button.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(button, new Insets(12, 0, 0, 0));
        box.getChildren().addAll(button, status);
        return box;
    }
}
