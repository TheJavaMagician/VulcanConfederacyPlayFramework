package controllers;

import model.FileUpload;
import model.Login;
import play.api.db.Database;
import play.data.*;
import play.mvc.*;
import services.Secured;
import views.html.*;
import play.data.Form;

import javax.inject.Inject;

import java.sql.*;


/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    @Inject FormFactory formFactory;

    public Result uploadImageRequest() {
        return ok(uploadImage.render(formFactory.form(FileUpload.class), ""));
    }

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(index.render());
    }


    public Result login() {
        return ok(login.render(formFactory.form(Login.class), ""));
    }

    public Result logout() {
        session().clear();
        return ok(index.render());
    }

    @Security.Authenticated(Secured.class)
    public Result membersLanding() {
        return ok(members.render(session().get("user_name"), session().get("title")));
    }

    private Database db;

    @Inject
    public HomeController(Database db) {
        this.db = db;
    }

    public Result userCredentials() {
        // Get the form posted from the login page
        Form<Login> loginForm = formFactory.form(Login.class).bindFromRequest();

        // Connect to the confederacy vulcan database
        Connection connection = db.getConnection();

        String errorMessage = "";

        try {
           // Find Users by Email
           String findUserByEmail = "SELECT * FROM vulcan.confederacy_users WHERE email=?;";

            // Use Java Prepared Statements to protect against SQL Injection
           PreparedStatement queryProtectedStatement = connection.prepareStatement(findUserByEmail);
           queryProtectedStatement.setString(1, loginForm.get().email);

           /* Locate email address in database */
           ResultSet result = queryProtectedStatement.executeQuery();

           /* Check the database result set to see if password matches the email account found in the database */
            if(result.next()) {
                if(loginForm.get().email.equals(result.getString("email")) &&
                        loginForm.get().password.equals(result.getString("password"))) {

                        // Set the users encrypted session email and username variables
                        session("email", loginForm.get().email);
                        session("user_name", result.getString("user_name"));
                        session("title", result.getString("title"));

                        // Send the user off to the members page
                        return redirect(routes.HomeController.membersLanding());

                }
            } else {
                errorMessage = "Username and/or Password incorrect";
            }
        } catch (SQLException e) {
            errorMessage = "Something went wrong.. Sorry about that.";
        }
        return ok(login.render(loginForm, errorMessage));
    }

}
