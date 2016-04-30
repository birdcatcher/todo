import java.io.*;
import java.sql.*; 
import java.util.*; 

import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.*;

import org.json.*;
 
public class AppServer { 
    private static boolean debug = true;

    public static void main(String[] args) throws Exception 
    {
        Server server = new Server(Integer.parseInt(args[0]));

        // Setup the basic application "context" for this application at "/"
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setResourceBase(".");
        context.setContextPath("/");
        server.setHandler(context);

        /********************* URL Mapping ************************/

        // add special pathspec of "/static/*" content
        ServletHolder holderStatic = new ServletHolder("static", DefaultServlet.class);
        holderStatic.setInitParameter("resourceBase", "./static");
        holderStatic.setInitParameter("dirAllowed", "false");
        holderStatic.setInitParameter("pathInfoOnly", "true");
        context.addServlet(holderStatic, "/static/*");
        context.addServlet(holderStatic, "/*");

        // add a simple Servlet at "/data/*"
        ServletHolder holderData = new ServletHolder("data", ToDo.class);
        context.addServlet(holderData, "/data/*");

        server.start();
        server.join();
    }

    /********************* Servlet Classes ************************/
    // IMPORTANT: inner class must be "public static class"

    // Servlet for COA
    public static class ToDo extends DefaultServlet {

        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
        {
            // get params, note how to get array
            // String[] id = request.getParameterValues("id[]");
            // debug("===> Params: "+String.join(",", id));

            // do db works
            JSONArray json1 = execute( "select * from task" );

            debug("===> Response: "+json1);

            // return response
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(""+json1);
        }

        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
        {
            try {
                StringBuffer jb = new StringBuffer();
                BufferedReader reader = request.getReader();
                String line = null;
                while ((line = reader.readLine()) != null)
                    jb.append(line);
                debug("===> Request: "+jb.toString());
    
                JSONObject json = new JSONObject(jb.toString());
                debug("===> Task: "+json.getString("name"));
    
                execute( "insert into task (name) values('"+json.getString("name")+"')" );
            } catch (Exception e) { 
                e.printStackTrace();
            }

            // return response
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("{}");
        }

    }

    /********************* Util Methods ************************/

    // Oracle DB config
    // private static String dbURL = "jdbc:oracle:thin:@host:port:db";
    // private static String dbUser= "*****";
    // private static String dbPass= "*****";

    // Embedded HSQLDB config
    // db.sh to start server in standalone mode and use dbtool.sh to open UI tool
    // UI tool choose server type and then use test script to create some test data
    // Make sure, in UI tool, use commit commnad to make data persistent
    private static String dbURL = "jdbc:hsqldb:file:test";
    private static String dbUser= "SA";
    private static String dbPass= "";

    // execute database statement
    public static JSONArray execute(String sql) {
        Connection conn = null;
        JSONArray json = null;
        try {
            conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
            Statement stmt = conn.createStatement();
            debug("===> SQL: "+sql);
            ResultSet rs = stmt.executeQuery(sql);
            json = convert(rs);
            debug("===> Result: "+json);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try { conn.close(); } catch(Throwable t) {}
        }
        return json;    
    }

    // convert ResultSet to JSON
    public static JSONArray convert(ResultSet rs)
        throws SQLException, JSONException 
    {
        JSONArray json = new JSONArray();
        ResultSetMetaData rsmd = rs.getMetaData();

        while(rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();

            for (int i=1; i<numColumns+1; i++) {
            String column_name = rsmd.getColumnName(i);

            if(rsmd.getColumnType(i)==java.sql.Types.ARRAY){
                obj.put(column_name, rs.getArray(column_name));
            } else if(rsmd.getColumnType(i)==java.sql.Types.BIGINT){
                obj.put(column_name, rs.getInt(column_name));
            } else if(rsmd.getColumnType(i)==java.sql.Types.BOOLEAN){
                obj.put(column_name, rs.getBoolean(column_name));
            } else if(rsmd.getColumnType(i)==java.sql.Types.BLOB){
                obj.put(column_name, rs.getBlob(column_name));
            } else if(rsmd.getColumnType(i)==java.sql.Types.DOUBLE){
                obj.put(column_name, rs.getDouble(column_name)); 
            } else if(rsmd.getColumnType(i)==java.sql.Types.FLOAT){
                obj.put(column_name, rs.getFloat(column_name));
            } else if(rsmd.getColumnType(i)==java.sql.Types.INTEGER){
                obj.put(column_name, rs.getInt(column_name));
            } else if(rsmd.getColumnType(i)==java.sql.Types.NVARCHAR){
                obj.put(column_name, rs.getNString(column_name));
            } else if(rsmd.getColumnType(i)==java.sql.Types.VARCHAR){
                obj.put(column_name, rs.getString(column_name));
            } else if(rsmd.getColumnType(i)==java.sql.Types.TINYINT){
                obj.put(column_name, rs.getInt(column_name));
            } else if(rsmd.getColumnType(i)==java.sql.Types.SMALLINT){
                obj.put(column_name, rs.getInt(column_name));
            } else if(rsmd.getColumnType(i)==java.sql.Types.DATE){
                obj.put(column_name, rs.getDate(column_name));
            } else if(rsmd.getColumnType(i)==java.sql.Types.TIMESTAMP){
                obj.put(column_name, rs.getTimestamp(column_name));   
            } else{
                obj.put(column_name, rs.getObject(column_name));
            }
          }
          json.put(obj);
        }
        return json;
    }

    public static void debug(Object o) {
        if (debug) System.out.println(o);
    }
}