package controllers;

import akka.util.ByteString;
import com.google.common.io.Files;
import model.FileUpload;
import model.Login;
import org.h2.store.fs.FileUtils;
import play.api.Play;
import play.api.db.Database;
import play.data.*;
import play.mvc.*;
import services.Secured;
import views.html.*;
import play.data.Form;

import javax.inject.Inject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;


/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class FileController extends Controller {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */

    private Database db;

    @Inject
    public FileController(Database db) {
        this.db = db;
    }

    @Inject
    private FormFactory formFactory;

    @Security.Authenticated(Secured.class)
    public Result uploadImage() throws IOException {
        // Get the form posted from the login page
        Form<FileUpload> uploadForm = formFactory.form(FileUpload.class).bindFromRequest();
        Http.MultipartFormData<File> body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart<File> picture = body.getFile("picture");

        Http.RawBuffer bufferedImage = request().body().asRaw();

        ByteString bytes = bufferedImage.
        Files.toByteArray(picture.getFile());

        if (picture != null) {
            String fileName = picture.getFilename();
            String contentType = picture.getContentType();
            File file = picture.getFile();
            return ok("File uploaded");
        } else {
            flash("error", "Missing file");
            return badRequest();
        }
    }

    private void imageToDatabase(File file, String imageDescription) {

        // connect to the vulcan database
        Connection connection = db.getConnection();


        // Create the SQL for inserting the image into the database
        String insertImage = "INSERT INTO vulcan.image (image_file_name, image_description, image_user_name, image_user_ip) VALUES (?, ?, ?, ?);";

        // Use Java Prepared Statements to protect against SQL Injection
        PreparedStatement queryProtectedStatement = null;
        try {
            queryProtectedStatement = connection.prepareStatement(insertImage);
            queryProtectedStatement.setString(1, file.getName());
            queryProtectedStatement.setString(2, imageDescription);
            queryProtectedStatement.setString(3, session().get("user_name"));
            queryProtectedStatement.setString(4, request().remoteAddress());

           /* insert image into database */
            ResultSet result = queryProtectedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Security.Authenticated(Secured.class)
    public Result membersLanding() {
        return ok(members.render(session().get("user_name"), session().get("title")));
    }



}
